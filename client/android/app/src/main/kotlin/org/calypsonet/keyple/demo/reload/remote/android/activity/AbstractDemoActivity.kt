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
package org.calypsonet.keyple.demo.reload.remote.android.activity

import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject
import kotlinx.android.synthetic.main.toolbar.serverStatus
import org.calypsonet.keyple.demo.reload.remote.R
import org.calypsonet.keyple.demo.reload.remote.SharedPrefData
import org.calypsonet.keyple.demo.reload.remote.android.network.ServerStatusEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/** Each Activity of the app should show status connexion result */
abstract class AbstractDemoActivity : DaggerAppCompatActivity() {

  @Inject lateinit var prefData: SharedPrefData

  override fun onResume() {
    super.onResume()
    updateServerStatusIndicator(prefData.loadLastStatus())
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
    updateServerStatusIndicator(serverStatusEvent.isUp)
  }

  private fun updateServerStatusIndicator(isServerUp: Boolean) {
    if (isServerUp) serverStatus.setImageResource(R.drawable.ic_connection_success)
    else serverStatus.setImageResource(R.drawable.ic_connection_wait)
  }
}
