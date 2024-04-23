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
package org.calypsonet.keyple.demo.reload.remote.data

import android.annotation.SuppressLint
import android.content.SharedPreferences
import java.util.*
import javax.inject.Inject
import org.calypsonet.keyple.demo.reload.remote.di.scopes.AppScoped

@AppScoped
class SharedPrefDataRepository @Inject constructor(private var prefs: SharedPreferences) {

  @SuppressLint("ApplySharedPref")
  fun saveServerIP(serverIp: String?) {
    val editor = prefs.edit()
    editor.putString(SERVER_IP_KEY, serverIp)
    editor.commit() // We need to use commit instead of apply because the app is restart just after
    // the change of this pref
  }

  fun loadServerIP(): String? {
    return prefs.getString(SERVER_IP_KEY, DEFAULT_SERVER_IP_KEY)
  }

  @SuppressLint("ApplySharedPref")
  fun saveServerPort(serverPort: Int?) {
    val editor = prefs.edit()
    editor.putInt(SERVER_PORT_KEY, serverPort!!)
    editor.commit() // We need to use commit instead of apply because the app is restart just after
    // the change of this pref
  }

  fun loadServerPort(): Int {
    return prefs.getInt(SERVER_PORT_KEY, DEFAULT_PORT)
  }

  @SuppressLint("ApplySharedPref")
  fun saveServerProtocol(serverProtocol: String?) {
    val editor = prefs.edit()
    editor.putString(SERVER_PROTOCOL_KEY, serverProtocol)
    editor.commit() // We need to use commit instead of apply because the app is restart just after
    // the change of this pref
  }

  fun loadServerProtocol(): String? {
    return prefs.getString(SERVER_PROTOCOL_KEY, DEFAULT_PROTOCOL)
  }

  fun saveDeviceType(deviceType: String?) {
    val editor = prefs.edit()
    editor.putString(DEVICE_TYPE, deviceType)
    editor.apply()
  }

  fun loadDeviceType(): String? {
    return prefs.getString(DEVICE_TYPE, "")
  }

  fun saveContactlessConfigurationVisibility(visibility: Visibility) {
    val editor = prefs.edit()
    editor.putString(SETTING_CONTACTLESS_VISIBILITY, visibility.text)
    editor.apply()
  }

  fun loadContactlessConfigurationVisibility(): Visibility {
    return Visibility.valueOf(
        prefs
            .getString(SETTING_CONTACTLESS_VISIBILITY, Visibility.ENABLE.text)!!
            .uppercase(Locale.ROOT))
  }

  fun saveSimConfigurationVisibility(visibility: Visibility) {
    val editor = prefs.edit()
    editor.putString(SETTING_SIM_VISIBILITY, visibility.text)
    editor.apply()
  }

  fun loadSimConfigurationVisibility(): Visibility {
    return Visibility.valueOf(
        prefs.getString(SETTING_SIM_VISIBILITY, Visibility.DISABLE.text)!!.uppercase(Locale.ROOT))
  }

  fun saveWearableConfigurationVisibility(visibility: Visibility) {
    val editor = prefs.edit()
    editor.putString(SETTING_WEARABLE_VISIBILITY, visibility.text)
    editor.apply()
  }

  fun loadWearableConfigurationVisibility(): Visibility {
    return Visibility.valueOf(
        prefs
            .getString(SETTING_WEARABLE_VISIBILITY, Visibility.DISABLE.text)!!
            .uppercase(Locale.ROOT))
  }

  fun saveEmbeddedConfigurationVisibility(visibility: Visibility) {
    val editor = prefs.edit()
    editor.putString(SETTING_EMBEDDED_VISIBILITY, visibility.text)
    editor.apply()
  }

  fun loadEmbeddedConfigurationVisibility(): Visibility {
    return Visibility.valueOf(
        prefs
            .getString(SETTING_EMBEDDED_VISIBILITY, Visibility.DISABLE.text)!!
            .uppercase(Locale.ROOT))
  }

  fun saveLastStatus(up: Boolean) {
    val editor = prefs.edit()
    editor.putBoolean(SETTING_SERVER_LAST_STATUS_UP, up)
    editor.apply()
  }

  fun loadLastStatus(): Boolean {
    return prefs.getBoolean(SETTING_SERVER_LAST_STATUS_UP, false)
  }

  companion object {
    private const val SERVER_IP_KEY = "server_ip_key"
    private const val SERVER_PORT_KEY = "server_port_key"
    private const val SERVER_PROTOCOL_KEY = "server_protocol_key"
    private const val DEVICE_TYPE = "device_type"
    private const val DEFAULT_SERVER_IP_KEY = "192.168.0.1"
    private const val DEFAULT_PORT = 8080
    private const val DEFAULT_PROTOCOL = "http://"
    private const val SETTING_CONTACTLESS_VISIBILITY = "setting_contactless_visibility"
    private const val SETTING_SIM_VISIBILITY = "setting_sim_visibility"
    private const val SETTING_WEARABLE_VISIBILITY = "setting_wearable_visibility"
    private const val SETTING_EMBEDDED_VISIBILITY = "setting_embedded_visibility"
    private const val SETTING_SERVER_LAST_STATUS_UP = "setting_server_last_status_up"

    enum class Visibility constructor(val text: String) {
      ENABLE("enable"),
      DISABLE("disable"),
      HIDE("hide")
    }
  }
}
