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

    public DrehbindingHandler(Thing thing, UpnpService upnpService) {
        super(thing);
        this.upnpService = upnpService;

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC+1"));
        bootid = calendar.getTimeInMillis() / 1000L;
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
        upnpService.getRegistry().addListener(this);

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
    private void subscribe() {
        // Abgedeckt: 1, 2
        subscriptionService.subscribe(this, TOPIC_NEW_MOTION, bootid);
    }

    private void unsubscribe() {
        // Abgedeckt: 3
        subscriptionService.unsubscribe(this, TOPIC_NEW_MOTION, bootid);
    }

    @Override
    public String getIdentifier() {
        return getThing().getProperties().get(UDN);
    }

    @Override
    public void onFullSuccessfulSubscription(String topic) {
        logger.debug("Subscribed successful for {}", topic);
    }

    @Override
    public void onPartialSucessfulSubscription(String topic) {
        logger.debug("Subscribed partialy successful for {}", topic);
    }

    @Override
    public void onFullSuccessfullUnsubscription(String topic) {
        logger.debug("Unsubscribed successful for {}", topic);
    }

    @Override
    public void onPartialSucessfulUnsubscription(String topic) {
        logger.debug("Snsubscribed partialy successful for {}", topic);
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
        logger.debug("Something was added!");
        String deviceUDN = device.getIdentity().getUdn().getIdentifierString();
        String thingUDN = getThing().getProperties().get(UDN);
        logger.debug(deviceUDN);
        logger.debug(thingUDN);

        if (deviceUDN.equals(thingUDN)) {
            logger.debug("Set thing status: online!");
            updateStatus(ThingStatus.ONLINE);
            subscribe();
        }
    }

    @Override
    public void remoteDeviceUpdated(@Nullable Registry registry, @Nullable RemoteDevice device) {
        logger.trace("Update for device: {}", device.getDetails().getFriendlyName());
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
