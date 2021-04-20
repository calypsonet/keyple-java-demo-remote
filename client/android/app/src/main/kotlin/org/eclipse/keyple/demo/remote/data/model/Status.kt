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

enum class Status
/**
 * @param status
 */(private val status: String) {
    LOADING("loading"), ERROR("error"), TICKETS_FOUND("tickets_found"), INVALID_CARD(
        "invalid_card"
    ),
    EMPTY_CARD("empty_card"), WRONG_CARD(
        "wrong_card"
    ),
    DEVICE_CONNECTED("device_connected"), SUCCESS("success");

    override fun toString(): String {
        return status
    }

    companion object {
        fun getStatus(name: String?): Status {
            return try {
                valueOf(name!!.toUpperCase())
            } catch (e: Exception) {
                // If the given state does not exist, return the default value.
                ERROR
            }
        }
    }
}
