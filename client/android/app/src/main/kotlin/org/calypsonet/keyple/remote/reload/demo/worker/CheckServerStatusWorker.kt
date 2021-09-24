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
package org.calypsonet.keyple.remote.reload.demo.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.calypsonet.keyple.remote.reload.demo.KEYPLE_PREFS
import org.calypsonet.keyple.remote.reload.demo.data.SharedPrefData
import org.calypsonet.keyple.remote.reload.demo.event.ServerStatusEvent
import org.calypsonet.keyple.remote.reload.demo.rest.RestClient
import org.greenrobot.eventbus.EventBus
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

/**
 * Checks Server status every Minutes.
 */
class CheckServerStatusWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    val client: RestClient
    val prefData: SharedPrefData =
        SharedPrefData(appContext.getSharedPreferences(KEYPLE_PREFS, Context.MODE_PRIVATE))

    init {
        client = Retrofit.Builder()
            .baseUrl(prefData.loadServerProtocol() + prefData.loadServerIP() + ":" + prefData.loadServerPort())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(RestClient::class.java)
    }

    override fun doWork(): Result {
        return try {
            client.ping().blockingGet()
            prefData.saveLastStatus(true)
            with(EventBus.getDefault()) {
                if (this.isRegistered(ServerStatusEvent::class.java)) this.post(
                    ServerStatusEvent(
                        true
                    )
                )
            }
            Result.success()
        } catch (e: Exception) {
            prefData.saveLastStatus(false)
            with(EventBus.getDefault()) {
                if (this.isRegistered(ServerStatusEvent::class.java)) this.post(
                    ServerStatusEvent(
                        false
                    )
                )
            }
            Result.retry()
        }
    }
}