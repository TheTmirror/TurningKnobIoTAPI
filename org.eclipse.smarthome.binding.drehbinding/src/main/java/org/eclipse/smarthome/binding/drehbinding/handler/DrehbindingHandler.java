/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.drehbinding.handler;

import static org.eclipse.smarthome.binding.drehbinding.internal.DrehbindingBindingConstants.*;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.drehbinding.eventing.Subscriber;
import org.eclipse.smarthome.binding.drehbinding.eventing.SubscriptionService;
import org.eclipse.smarthome.binding.drehbinding.eventing.SubscriptionServiceImpl;
import org.eclipse.smarthome.binding.drehbinding.internal.DrehbindingConfiguration;
import org.eclipse.smarthome.binding.drehbinding.internal.REST.RESTIOService;
import org.eclipse.smarthome.binding.drehbinding.internal.REST.implementation.RESTIOServiceImpl;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.jupnp.UpnpService;
import org.jupnp.model.meta.LocalDevice;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.registry.Registry;
import org.jupnp.registry.RegistryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DrehbindingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tristan - Initial contribution
 */
@NonNullByDefault
public class DrehbindingHandler extends BaseThingHandler implements Subscriber, RegistryListener {

    private final Logger logger = LoggerFactory.getLogger(DrehbindingHandler.class);

    @Nullable
    private DrehbindingConfiguration config;

    private final RESTIOService restIOService = RESTIOServiceImpl.getInstance();

    private final SubscriptionService subscriptionService = SubscriptionServiceImpl.getInstance();

    /*
     * Missing BOOTID workaround -> informations will follow
     */
    private final long bootid;

    private final UpnpService upnpService;

    // Flag für RegistryListener Spezialfall 2)
    private boolean addedFlag = false;
    private final Lock addedFlagLock;

    public DrehbindingHandler(Thing thing, UpnpService upnpService) {
        super(thing);
        this.upnpService = upnpService;
        this.addedFlagLock = new ReentrantLock();

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC+1"));
        bootid = calendar.getTimeInMillis() / 1000L;

        /*
         * Not sure if the registry of the registrylistener should be done inside
         * or after the constructor. Is it safe to assume the upnpService and the
         * getThing are always != null?
         */
        upnpService.getRegistry().addListener(this);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        /*
         * The refresh command is maybe different and may needs to be
         * processed even if the device is offline.
         *
         * TODO: Think about this problem
         */

        if (getThing().getStatus() == ThingStatus.OFFLINE) {
            logger.debug("Command was not processed, because the device was offline");
            return;
        }

        /*
         * Is needed for ever channel, even if a device is readonly.
         * This is due to the fact, than one can send a command via
         * rules.
         *
         * If a command send by a rule should be ignored by a readonly
         * channel, just leave the case empty or remove it. Then the
         * channel is readonly even for a rule (Caution: the rule can
         * still use postUpdate to manipulate values)
         */
        switch (channelUID.getIdWithoutGroup()) {
            case CHANNEL_EVENT_TIME:
                break;

            case CHANNEL_LAST_MOTION:
                break;

            default:
                break;
        }

        // TODO: handle command

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information:
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");
        // }

    }

    /*
     * ###############################################################################################
     * #
     * #
     * # THING STUFF
     * #
     * #
     * ###############################################################################################
     */
    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly. Also, before leaving this method a thing
        // status from one of ONLINE, OFFLINE or UNKNOWN must be set. This might already be the real thing status in
        // case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.

        // // Note: When initialization can NOT be done set the status with more details for further
        // // analysis. See also class ThingStatusDetail for all available status details.
        // // Add a description to give user information to understand why thing does not work as expected.
        // // E.g.
        // // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // // "Can not access device as username and/or password are invalid");
        // }
        // });
    }

    @Override
    public void dispose() {
        // Wird dies gebraucht?
        super.dispose();

        unsubscribe();
    }

    @Override
    public void thingUpdated(Thing thing) {
        // TODO Auto-generated method stub
        super.thingUpdated(thing);
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        // TODO Auto-generated method stub
        super.handleConfigurationUpdate(configurationParameters);
    }

    /*
     * ###############################################################################################
     * #
     * #
     * # SUBSCRIPTION STUFF
     * #
     * #
     * ###############################################################################################
     */
    private void subscribeForAllStaticTopics() {
        for (String topic : STATIC_TOPICS) {
            subscribe(topic);
        }
    }

    private void subscribe(String topic) {
        // Abgedeckt: 1, 2
        logger.trace("Subscribing for topic {} with bootid {}", TOPIC_NEW_MOTION, bootid);
        subscriptionService.subscribe(this, topic, bootid);
    }

    private void unsubscribe() {
        // Abgedeckt: 3
        logger.trace("Unsubscribing from topic {} with bootid {}", TOPIC_NEW_MOTION, bootid);
        subscriptionService.unsubscribe(this, TOPIC_NEW_MOTION, bootid);
    }

    @Override
    public String getIdentifier() {
        return getThing().getProperties().get(UDN);
    }

    /*
     * Should happen normaly
     *
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.binding.drehbinding.eventing.Subscriber#onFullSuccessfulSubscription(java.lang.String)
     */
    @Override
    public void onFullSuccessfulSubscription(String topic) {
        logger.debug("Subscribed successful for {}", topic);
    }

    /*
     * Should never happen, the handler will only subscribe if he received and online signal. Even so, this case should
     * be handled.
     *
     * In such case, all localy added subscriptions must be removed.
     *
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.binding.drehbinding.eventing.Subscriber#onPartialSucessfulSubscription(java.lang.String)
     */
    @Override
    public void onPartialSucessfulSubscription(String topic) {
        if (isTopicStatic(topic)) {
            // Static Subscriptions (topics)
            logger.debug("Subscribed partialy successful for {}", topic);
            logger.debug("This means full subscriptions failed. Removing all local subscriptions");

            subscriptionService.removeSubscription(this, topic);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        } else {
            // Dynamic Subscriptions (topics)
            // TODO: Siehe Word Dokument 1aii)
            // Simulation for to do: All retrys failed
            subscriptionService.removeSubscription(this, topic);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    /*
     * Should normaly happen if:
     * 1) The device goes offline after the binding
     * 2) The device goes offline before the binding but has a mechanism to wait a couple of seconds to get all expected
     * removing requests.
     * 3) The device and binding stay online but the binding want for whatever reason to unsubscribe for this topic
     *
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.binding.drehbinding.eventing.Subscriber#onFullSuccessfullUnsubscription(java.lang.String)
     */
    @Override
    public void onFullSuccessfullUnsubscription(String topic) {
        logger.debug("Unsubscribed successful for {}", topic);
    }

    /*
     * Should happen if:
     * 1) The device crashes (no possibility to send byebye message, therefor the registry entry expires) and in the
     * time before the device is identified as offline the binding wants to go offline
     * 2) The devices goes offline before the binding and has no mechanism to wait a couple of seconds to get all
     * expected removing requests
     * 3) The device crashes (no possibility to send byebye message, therefor the registry entry expires) and in the
     * time before the device is identified as offline the binding want to cancel it's subscription for whatever reason
     * but wants to stay online.
     *
     * Solutions:
     * 1 & 2) In any case the device is defently offline and the binding is about to go offline. This means both parts
     * will forget about all subscriptions. The local subscription got already removed. Even if multiple alive
     * notifications of the device were lost and therefor the device was detected falsly as offline, with the next boot
     * of the binding, the device will notice a new bootid for the subscriptions and remove all old and wrong
     * subscriptions, that couldn't be removed remotly by the binding. In the end: For these cases is no special
     * handling needed.
     *
     * 3) This case varies a bit. If the binding wants to cancel a subscription and stays online and the device is
     * online but not reachable, then their subscriptioninformations aren't synchronized anymore. The device will keep
     * sending informations refering the topic which the callbackListener of the subscription manager will still
     * receive. If I remember correctly the subscription manager should ignore such events, but he still has to accept
     * and check them. This costs time. Therefor it would be a good idea, to retry the remote unsubscription twice with
     * a bit of delay. If the device was just unreachable, there is a great chance it's reachable then. If it still
     * isn't one must assume the device is offline.
     * It can then happen, that the binding will reregister for a topic it already has with the same bootid. This
     * happens because there was no reboot and the device never received an information regarding deleting this
     * subscription. The device must be ready to handle such a case.
     *
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.binding.drehbinding.eventing.Subscriber#onPartialSucessfulUnsubscription(java.lang.String)
     */
    @Override
    public void onPartialSucessfulUnsubscription(String topic) {
        logger.debug("Snsubscribed partialy successful for {}", topic);
    }

    private boolean isTopicStatic(String topic) {
        // TODO: Implement logic instead of dummy
        return true;
    }

    /*
     * ###############################################################################################
     * #
     * #
     * # UPNP REGISTRY LISTENER STUFF
     * #
     * #
     * ###############################################################################################
     *
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.binding.drehbinding.eventing.Subscriber#onSubcriptionEvent(java.lang.String,
     * java.util.Map)
     */
    @Override
    public void onSubcriptionEvent(String topic, Map<String, String> values) {
        logger.trace("An Event happend for me!");

        switch (topic) {
            case TOPIC_NEW_MOTION:
                String name = values.get(NAME);
                updateState(CHANNEL_LAST_MOTION, new StringType(name));
                updateState(CHANNEL_EVENT_TIME, new DateTimeType());
                break;

            default:
                break;
        }
    }

    @Override
    public void remoteDeviceDiscoveryStarted(@Nullable Registry registry, @Nullable RemoteDevice device) {
        // TODO Auto-generated method stub

    }

    @Override
    public void remoteDeviceDiscoveryFailed(@Nullable Registry registry, @Nullable RemoteDevice device,
            @Nullable Exception ex) {
        // TODO Auto-generated method stub

    }

    @Override
    public void remoteDeviceAdded(@Nullable Registry registry, @Nullable RemoteDevice device) {
        if (device == null) {
            return;
        }

        if (!isDeviceRelevant(device)) {
            return;
        }

        addedFlagLock.lock();
        addedFlag = true;
        addedFlagLock.unlock();
        logger.debug("Set thing status: online!");
        updateStatus(ThingStatus.ONLINE);
        subscribeForAllStaticTopics();
    }

    private boolean isDeviceRelevant(RemoteDevice device) {
        String deviceUDN = device.getIdentity().getUdn().getIdentifierString();
        String thingUDN = getThing().getProperties().get(UDN);
        logger.trace(deviceUDN);
        logger.trace(thingUDN);

        return deviceUDN.equals(thingUDN);
    }

    /*
     * Infos zur Methode:
     *
     * 1) Diese Methode kann aktuell nicht dazu genutzt werden um ein Update der Bootid zu überprüfen da JUPNP die
     * Bootid momentan nicht unterstützt. Außerdem sollte das eh nicht nötig sein, da EIGENTLICH JUPNP dies automatisch
     * erkennen sollte
     *
     * 2) Es kann vorkommen, dass dieser Listener noch nicht registriert ist, wenn die M-SEARCH Message beim Systemstart
     * rausgeschickt wird und eine Antwort zurück kommt. Das Device wird dann zwar in der Registery aufgenommen, aber
     * die Callback Methode (Device added) wird nicht gecallt. Sie kann dann auch nicht durch weitere Alive Messages
     * aktiviert werden, da das Device sich ja bereits in der Registery befindet. Das Device wird also niemals in diesem
     * Listener als hinzugefügt bekannt, obwohl es das eigentlich aus Sicht der Registery ist. Die Update Methode wird
     * allerdings trotzdem gecallt und damit könnte man ein Workaround für den Spezialfall basteln, dass die Methode
     * remoteDeviceAdded(...) niemals gecallt wird. Es könnte ein boolsches Flag gesetzt werden, welches nachhält, ob
     * die Methode bereits gecallt wurde.Dieses Flag wird dann bei jedem Updatecall überprüft. Sollte erkannt werden,
     * dass das Flag noch nicht gecallt wurde (die logische Schlussfolgerung aus Sicht des Listener wäre es dann, dass
     * die Added Methode nie gecalled wurde), so kann die Added Methode simuliert und nachgeholt werden.
     *
     * (non-Javadoc)
     *
     * @see org.jupnp.registry.RegistryListener#remoteDeviceUpdated(org.jupnp.registry.Registry,
     * org.jupnp.model.meta.RemoteDevice)
     */
    @Override
    public void remoteDeviceUpdated(@Nullable Registry registry, @Nullable RemoteDevice device) {
        if (device == null) {
            return;
        }

        if (!isDeviceRelevant(device)) {
            return;
        }

        logger.trace("Received an update for my device!");

        // In 2) erklärten Spezialfall abfangen
        addedFlagLock.lock();
        if (!addedFlag) {
            addedFlagLock.unlock();
            logger.debug("Update instead of addition. Therefor simulating added");
            remoteDeviceAdded(registry, device);
            return;
        }
        addedFlagLock.unlock();

        // Hier beginnt die eigentliche Update Methode
        // Wenn das Device eine Alive Message geschickt hat muss es folglich online sein
        updateStatus(ThingStatus.ONLINE);

        /*
         * Falls ein Static Service noch nicht Subscribed ist muss ein Subscritpionversuch erfolgen
         */
        for (String topic : STATIC_TOPICS) {
            if (!subscriptionService.doesSubscriptionExists(this, topic)) {
                subscribe(topic);
            }
        }

    }

    @Override
    public void remoteDeviceRemoved(@Nullable Registry registry, @Nullable RemoteDevice device) {
        String deviceUDN = device.getIdentity().getUdn().getIdentifierString();
        String thingUDN = getThing().getProperties().get(UDN);
        logger.debug(deviceUDN);
        logger.debug(thingUDN);

        if (deviceUDN.equals(thingUDN)) {
            logger.debug("Set thing status: offline!");
            updateStatus(ThingStatus.OFFLINE);
            // Problem: Expiring und ByeBye können nicht unterschieden werden.
            unsubscribe();
        }
    }

    @Override
    public void localDeviceAdded(@Nullable Registry registry, @Nullable LocalDevice device) {
        // TODO Auto-generated method stub

    }

    @Override
    public void localDeviceRemoved(@Nullable Registry registry, @Nullable LocalDevice device) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeShutdown(@Nullable Registry registry) {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterShutdown() {
        // TODO Auto-generated method stub

    }

    /*
     * ###############################################################################################
     * #
     * #
     * # OTHER STUFF
     * #
     * #
     * ###############################################################################################
     */
    private void logChannelInformation() {
        List<Channel> channels = getThing().getChannels();
        logger.trace("Anzahl der channels: {}", channels.size());
        for (Channel channel : channels) {
            logger.trace("================== CHANNEL INFORMATION ==================");
            logger.trace(channel.getAcceptedItemType());
            logger.trace(channel.getDescription());
            logger.trace(channel.toString());
            logger.trace(channel.getChannelTypeUID().getAsString());
            logger.trace(channel.getConfiguration().toString());
            logger.trace(channel.getKind().name());
            logger.trace(channel.getUID().getAsString());
        }
    }

}
