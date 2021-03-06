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
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Cannot extends directly SyncEndpointClient because retrofit allo API interfaces to extend interfaces.
 */
public interface RestClient {
    @GET("/sam")
    fun ping(): Single<String>

    @Headers("Accept: application/json", "Content-Type: application/json; charset=UTF-8")
    @POST("/remote-plugin")
    fun sendRequest(@Body msg: MessageDto?): Single<MutableList<MessageDto>>
}
