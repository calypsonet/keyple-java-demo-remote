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
package org.calypsonet.keyple.remote.reload.demo.di

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.calypsonet.keyple.remote.reload.demo.data.SharedPrefData
import org.calypsonet.keyple.remote.reload.demo.di.scopes.AppScoped
import org.calypsonet.keyple.remote.reload.demo.rest.KeypleSyncEndPointClient
import org.calypsonet.keyple.remote.reload.demo.rest.RestClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

@Module
class RestModule {

    @Provides
    @AppScoped
    public fun provideKeypleSyncEndpointClient(prefData: SharedPrefData): KeypleSyncEndPointClient {
        val serverUrl = prefData.loadServerProtocol() + prefData.loadServerIP() + ":" + prefData.loadServerPort()
        Timber.i("Loaded Rest client with URL: $serverUrl")
        return KeypleSyncEndPointClient(Retrofit.Builder()
                .baseUrl(serverUrl)
                .client(OkHttpClient
                        .Builder()
                        .addNetworkInterceptor(HttpLoggingInterceptor())
                        .build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(RestClient::class.java))
    }
}
