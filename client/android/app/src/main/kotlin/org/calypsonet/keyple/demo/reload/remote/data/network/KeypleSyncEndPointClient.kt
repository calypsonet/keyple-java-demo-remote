/* **************************************************************************************
 * Copyright (c) 2021 Calypso Networks Association https://calypsonet.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.calypsonet.keyple.demo.reload.remote.data.network

import io.reactivex.Single
import org.eclipse.keyple.distributed.MessageDto
import org.eclipse.keyple.distributed.spi.SyncEndpointClientSpi

/** We have to wrap the retrofit client */
class KeypleSyncEndPointClient(private val restClient: RestClient) : SyncEndpointClientSpi {

  override fun sendRequest(msg: MessageDto?): MutableList<MessageDto> {
    return restClient.sendRequest(msg).blockingGet()
  }

  fun ping(): Single<String> {
    return restClient.ping()
  }
}
