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

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.lang.NumberFormatException
import kotlin.system.exitProcess
import org.calypsonet.keyple.demo.reload.remote.R
import org.calypsonet.keyple.demo.reload.remote.data.network.KeypleSyncEndPointClient
import org.calypsonet.keyple.demo.reload.remote.data.network.RestClient
import org.calypsonet.keyple.demo.reload.remote.databinding.ActivityServerSettingsBinding
import org.calypsonet.keyple.demo.reload.remote.di.scopes.ActivityScoped
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import timber.log.Timber

@ActivityScoped
class ServerSettingsActivity : AbstractDemoActivity() {

  private lateinit var disposables: CompositeDisposable
  private lateinit var activityServerSettingsBinding: ActivityServerSettingsBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityServerSettingsBinding = ActivityServerSettingsBinding.inflate(layoutInflater)
    toolbarBinding = activityServerSettingsBinding.appBarLayout
    setContentView(activityServerSettingsBinding.root)

    disposables = CompositeDisposable()

    activityServerSettingsBinding.serverIpEdit.text.append(prefData.loadServerIP() ?: "")
    activityServerSettingsBinding.serverPortEdit.text.append(prefData.loadServerPort().toString())
    activityServerSettingsBinding.serverProtocolEdit.text.append(prefData.loadServerProtocol())

    activityServerSettingsBinding.restart.setOnClickListener {
      if (validateEntries(true)) restartApp()
    }

    activityServerSettingsBinding.pingBtn.setOnClickListener {
      if (validateEntries(false)) {
        val serverUrl =
            activityServerSettingsBinding.serverProtocolEdit.text.toString() +
                activityServerSettingsBinding.serverIpEdit.text.toString() +
                ":" +
                activityServerSettingsBinding.serverPortEdit.text.toString()
        Timber.i("Loaded Rest client with URL: $serverUrl")
        val testKeypleEndpointClient =
            KeypleSyncEndPointClient(
                Retrofit.Builder()
                    .baseUrl(serverUrl)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build()
                    .create(RestClient::class.java))

        disposables.add(
            testKeypleEndpointClient
                .ping()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                  activityServerSettingsBinding.pingProgressBar.visibility = View.VISIBLE
                  activityServerSettingsBinding.pingResultText.visibility = View.INVISIBLE
                }
                .subscribe(
                    {
                      activityServerSettingsBinding.pingProgressBar.visibility = View.INVISIBLE
                      activityServerSettingsBinding.pingResultText.visibility = View.VISIBLE
                      activityServerSettingsBinding.pingResultText.text =
                          getString(R.string.ping_success)
                    },
                    {
                      Timber.e(it)
                      activityServerSettingsBinding.pingProgressBar.visibility = View.INVISIBLE
                      activityServerSettingsBinding.pingResultText.visibility = View.VISIBLE
                      activityServerSettingsBinding.pingResultText.text =
                          getString(R.string.ping_failed)
                    }))
      }
    }
  }

  override fun onDestroy() {
    disposables.clear()
    super.onDestroy()
  }

  private fun restartApp() {
    startActivity(Intent(applicationContext, MainActivity::class.java))
    exitProcess(0)
  }

  private fun validateEntries(saveOnSuccess: Boolean): Boolean {
    with(activityServerSettingsBinding.serverIpEdit) {
      if (this.text.isNotBlank() && Patterns.IP_ADDRESS.matcher(this.text.toString()).matches()) {
        if (saveOnSuccess) prefData.saveServerIP(this.text.toString())
      } else {
        this.error = "Please set a valid IP"
        return false
      }
    }
    with(activityServerSettingsBinding.serverPortEdit) {
      try {
        if (this.text.isNotBlank()) {
          if (saveOnSuccess) prefData.saveServerPort(Integer.valueOf(this.text.toString()))
        } else {
          throw NumberFormatException()
        }
      } catch (e: NumberFormatException) {
        this.error = "Please set a valid Port"
        return false
      }
    }
    with(activityServerSettingsBinding.serverProtocolEdit) {
      if (this.text.isNotBlank() && arrayOf("http://", "https://").contains(this.text.toString())) {
        if (saveOnSuccess) prefData.saveServerProtocol(this.text.toString())
      } else {
        this.error = "Please set a valid Protocol"
        return false
      }
    }
    return true
  }
}
