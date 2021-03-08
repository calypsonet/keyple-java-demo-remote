/********************************************************************************
 * Copyright (c) 2021 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.demo.remote.data.model

import org.eclipse.keyple.demo.remote.R

enum class DeviceEnum
/**
 * @param stringId
 */(val textId: Int) {
    CONTACTLESS_CARD(R.string.contactless_card), SIM(R.string.sim_card), WEARABLE(
        R.string.wearable
    ),
    EMBEDDED(R.string.embedded_secure_elem);

    companion object {
        @JvmStatic
        fun getDeviceEnum(name: String): DeviceEnum {
            return try {
                valueOf(name.toUpperCase())
            } catch (e: Exception) {
                // If the given state does not exist, return the default value.
                CONTACTLESS_CARD
            }
        }
    }
}
