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
package org.calypsonet.keyple.demo.reload.remote.di

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.calypsonet.keyple.demo.reload.remote.data.SharedPrefDataRepository
import org.calypsonet.keyple.demo.reload.remote.data.network.KeypleSyncEndPointClient
import org.calypsonet.keyple.demo.reload.remote.data.network.RestClient
import org.calypsonet.keyple.demo.reload.remote.di.scopes.AppScoped
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

@Suppress("unused")
@Module
class RestModule {

  @Provides
  @AppScoped
  fun provideKeypleSyncEndpointClient(
      prefData: SharedPrefDataRepository
  ): KeypleSyncEndPointClient {
    val serverUrl =
        prefData.loadServerProtocol() + prefData.loadServerIP() + ":" + prefData.loadServerPort()
    Timber.i("Loaded Rest client with URL: $serverUrl")
    return KeypleSyncEndPointClient(
        Retrofit.Builder()
            .baseUrl(serverUrl)
            .client(OkHttpClient.Builder().addNetworkInterceptor(HttpLoggingInterceptor()).build())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(RestClient::class.java))
  }
}
