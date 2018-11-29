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
package org.eclipse.smarthome.binding.drehbinding.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link DrehbindingBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Tristan - Initial contribution
 */
@NonNullByDefault
public class DrehbindingBindingConstants {

    private static final String BINDING_ID = "drehbinding";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_DREHKNOPF = new ThingTypeUID(BINDING_ID, "drehknopf");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_DREHKNOPF).collect(Collectors.toSet()));

    // List of all Channel ids
    public static final String CHANNEL_NUMBER = "numberOfGestures";

    // Thing Property Constants
    public static final String HOST = "host";
    public static final String SERIAL = "serialNumber";

    public static final String UDN = "udn";
}