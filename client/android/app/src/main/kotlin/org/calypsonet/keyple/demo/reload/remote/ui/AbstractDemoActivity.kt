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
package org.calypsonet.keyple.demo.reload.remote.ui

import android.os.AsyncTask
import android.os.Bundle
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject
import kotlinx.android.synthetic.main.toolbar.*
import org.calypsonet.keyple.demo.reload.remote.R
import org.calypsonet.keyple.demo.reload.remote.data.SharedPrefDataRepository
import org.calypsonet.keyple.demo.reload.remote.data.model.SamStatus
import org.calypsonet.keyple.demo.reload.remote.data.model.ServerStatusEvent
import org.calypsonet.keyple.demo.reload.remote.data.network.RestClient
import org.eclipse.keyple.core.util.json.JsonUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

/** Each Activity of the app should show status connexion result */
abstract class AbstractDemoActivity : DaggerAppCompatActivity() {

  private lateinit var client: RestClient

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    client =
        Retrofit.Builder()
            .baseUrl(
                prefData.loadServerProtocol() +
                    prefData.loadServerIP() +
                    ":" +
                    prefData.loadServerPort())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(RestClient::class.java)
  }

  @Inject lateinit var prefData: SharedPrefDataRepository

  override fun onResume() {
    super.onResume()
    checkServerStatus()
  }

  override fun onStart() {
    super.onStart()
    EventBus.getDefault().register(this)
  }

  override fun onStop() {
    super.onStop()
    EventBus.getDefault().unregister(this)
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  fun onServerStatusEvent(serverStatusEvent: ServerStatusEvent) {
    prefData.saveLastStatus(serverStatusEvent.isUp)
    updateServerStatusIndicator()
    onSamCheckComplete()
  }

  private fun updateServerStatusIndicator() {
    if (prefData.loadLastStatus()) serverStatus.setImageResource(R.drawable.ic_connection_success)
    else serverStatus.setImageResource(R.drawable.ic_connection_wait)
  }

  open fun onSamCheckComplete() {}

  private fun checkServerStatus() {
    PingAsyncTask().execute(client)
  }

  class PingAsyncTask : AsyncTask<RestClient, Void, Long>() {
    override fun doInBackground(vararg client: RestClient): Long {
      try {
        val jsonRes = client[0].ping().blockingGet()
        val samStatus = JsonUtil.getParser().fromJson(jsonRes.toString(), SamStatus::class.java)
        EventBus.getDefault().post(ServerStatusEvent(samStatus.isSamReady))
      } catch (e: Exception) {
        EventBus.getDefault().post(ServerStatusEvent(false))
      }
      return 0
    }
  }
}
