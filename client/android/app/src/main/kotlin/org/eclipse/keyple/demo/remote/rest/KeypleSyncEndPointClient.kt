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
package org.eclipse.keyple.demo.remote.rest

import io.reactivex.Single
import org.eclipse.keyple.distributed.MessageDto
import org.eclipse.keyple.distributed.spi.SyncEndpointClient

/**
 * We have to wrap the retrofit client
 */
class KeypleSyncEndPointClient(private val restClient: RestClient) : SyncEndpointClient {

    override fun sendRequest(msg: MessageDto?): MutableList<MessageDto> {
        return restClient.sendRequest(msg).blockingGet()
    }

    public fun ping(): Single<String> {
        return restClient.ping()
    }
}
