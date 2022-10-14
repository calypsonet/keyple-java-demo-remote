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
package org.calypsonet.keyple.demo.reload.remote

import android.content.Context
import androidx.multidex.MultiDex
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import dagger.android.DaggerApplication
import java.util.concurrent.TimeUnit
import org.calypsonet.keyple.demo.reload.remote.data.network.CheckServerStatusWorker
import org.calypsonet.keyple.demo.reload.remote.di.AppComponent
import org.calypsonet.keyple.demo.reload.remote.di.DaggerAppComponent
import timber.log.Timber
import timber.log.Timber.DebugTree

class Application : DaggerApplication() {

  override fun attachBaseContext(context: Context?) {
    super.attachBaseContext(context)
    MultiDex.install(this)
  }

  override fun onCreate() {
    super.onCreate()
    Timber.plant(DebugTree())
    initServerStatusCkecker()
  }

  private fun initServerStatusCkecker() {
    val pingWorkRequest: WorkRequest =
        PeriodicWorkRequestBuilder<CheckServerStatusWorker>(1, TimeUnit.SECONDS)
            .setConstraints(Constraints.NONE)
            .build()
    WorkManager.getInstance(applicationContext).enqueue(pingWorkRequest)
  }

  override fun applicationInjector(): AppComponent? {
    return DaggerAppComponent.builder().application(this).build()
  }
}
