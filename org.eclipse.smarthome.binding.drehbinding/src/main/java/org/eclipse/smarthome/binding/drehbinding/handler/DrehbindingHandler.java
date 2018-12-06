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

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.binding.drehbinding.internal.DrehbindingConfiguration;
import org.eclipse.smarthome.binding.drehbinding.internal.REST.RESTIOParticipant;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DrehbindingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tristan - Initial contribution
 */
@NonNullByDefault
public class DrehbindingHandler extends BaseThingHandler implements RESTIOParticipant {

    private final Logger logger = LoggerFactory.getLogger(DrehbindingHandler.class);

    @Nullable
    private DrehbindingConfiguration config;

    private final RESTIOService service = RESTIOServiceImpl.getInstance();

    /*
     * ATTENTION!!!!!
     * This task is running 24/7 in the background
     * to keep track if the device is online or not.
     * This is a backup check to keep the thing
     * status updated.
     */
    Runnable onlineCheckTask = new Runnable() {

        @Override
        public void run() {
            logger.debug("CHECKING");
            if (service.isDeviceOnlineAndReachable()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        }
    };

    public DrehbindingHandler(Thing thing) {
        super(thing);
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
        logger.debug("REACHED");
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

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        config = getConfigAs(DrehbindingConfiguration.class);

        scheduler.execute(() -> {

            if (service.isDeviceOnlineAndReachable()) {
                updateStatus(ThingStatus.ONLINE);
                service.addSubscription(this, TOPIC_NEW_MOTION);
                service.addSubscription(this, "onoff");
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }

        });

        scheduler.scheduleWithFixedDelay(onlineCheckTask, 0, 5, TimeUnit.SECONDS);

        // scheduler.execute(() -> {
        // logger.debug("{}", getThing().getChannel(CHANNEL_LAST_MOTION));
        // TimeUnit()
        // });

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

        // Example for background initialization:
        // scheduler.execute(() -> {
        // // Sende Anfrage an das Device
        // // Zu Testen: Ist es online, sind die Services erreichbar?
        // if (!isDeviceOnline() && !areDeviceServicesOnline()) {
        // updateStatus(ThingStatus.ONLINE);
        // } else {
        // updateStatus(ThingStatus.OFFLINE);
        // // Note: When initialization can NOT be done set the status with more details for further
        // // analysis. See also class ThingStatusDetail for all available status details.
        // // Add a description to give user information to understand why thing does not work as expected.
        // // E.g.
        // // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // // "Can not access device as username and/or password are invalid");
        // }
        // });

        // logger.debug("Finished initializing!");
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        super.dispose();
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

    @Override
    public void onSuccessfulSubscription() {
        logger.debug("Subscription was successfull");
    }

    @Override
    public void onFailedSubscription() {
        logger.debug("Subscription was not successfull");
    }

    @Override
    public void onSuccessfulUnsubscription() {
        logger.debug("Unsubscription was successfull");
    }

    @Override
    public void onFailedUnsubscription() {
        logger.debug("Unsubscription was not successfull");
    }

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

    @Override
    public String getIdentifier() {
        return getThing().getProperties().get(UDN);
    }
}
