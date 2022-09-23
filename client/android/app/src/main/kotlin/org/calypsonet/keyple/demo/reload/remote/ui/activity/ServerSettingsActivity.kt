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
package org.calypsonet.keyple.demo.reload.remote.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.lang.NumberFormatException
import kotlin.system.exitProcess
import kotlinx.android.synthetic.main.activity_server_settings.*
import org.calypsonet.keyple.demo.reload.remote.R
import org.calypsonet.keyple.demo.reload.remote.ui.di.scopes.ActivityScoped
import org.calypsonet.keyple.demo.reload.remote.ui.network.KeypleSyncEndPointClient
import org.calypsonet.keyple.demo.reload.remote.ui.network.RestClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import timber.log.Timber

@ActivityScoped
class ServerSettingsActivity : AbstractDemoActivity() {

  private lateinit var disposables: CompositeDisposable

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_server_settings)

    disposables = CompositeDisposable()

    server_ip_edit.text.append(prefData.loadServerIP() ?: "")
    server_port_edit.text.append(prefData.loadServerPort().toString())
    server_protocol_edit.text.append(prefData.loadServerProtocol())

    restart.setOnClickListener { if (validateEntries(true)) restartApp() }

    ping_btn.setOnClickListener {
      if (validateEntries(false)) {
        val serverUrl =
            server_protocol_edit.text.toString() +
                server_ip_edit.text.toString() +
                ":" +
                server_port_edit.text.toString()
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
                  ping_progress_bar.visibility = View.VISIBLE
                  ping_result_text.visibility = View.INVISIBLE
                }
                .subscribe(
                    {
                      ping_progress_bar.visibility = View.INVISIBLE
                      ping_result_text.visibility = View.VISIBLE
                      ping_result_text.text = getString(R.string.ping_success)
                    },
                    {
                      Timber.e(it)
                      ping_progress_bar.visibility = View.INVISIBLE
                      ping_result_text.visibility = View.VISIBLE
                      ping_result_text.text = getString(R.string.ping_failed)
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
    with(server_ip_edit) {
      if (this.text.isNotBlank() && Patterns.IP_ADDRESS.matcher(this.text.toString()).matches()) {
        if (saveOnSuccess) prefData.saveServerIP(this.text.toString())
      } else {
        this.error = "Please set a valid IP"
        return false
      }
    }
    with(server_port_edit) {
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
    with(server_protocol_edit) {
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
