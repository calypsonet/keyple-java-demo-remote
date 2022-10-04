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
import android.view.View
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_home.contactlessCardBtn
import kotlinx.android.synthetic.main.activity_home.embeddedElemBtn
import kotlinx.android.synthetic.main.activity_home.simCardBtn
import kotlinx.android.synthetic.main.activity_home.wearableBtn
import kotlinx.android.synthetic.main.toolbar.*
import org.calypsonet.keyple.demo.reload.remote.R
import org.calypsonet.keyple.demo.reload.remote.data.SharedPrefDataRepository
import org.calypsonet.keyple.demo.reload.remote.data.model.DeviceEnum

class HomeActivity : AbstractDemoActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_home)
    menuBtn.visibility = View.VISIBLE
    menuBtn.setOnClickListener { startActivity(Intent(this, SettingsMenuActivity::class.java)) }
  }

  override fun onResume() {
    super.onResume()
    setupBtn(
        contactlessCardBtn,
        prefData.loadContactlessConfigurationVisibility(),
        DeviceEnum.CONTACTLESS_CARD)
    setupBtn(simCardBtn, prefData.loadSimConfigurationVisibility(), DeviceEnum.SIM)
    setupBtn(wearableBtn, prefData.loadWearableConfigurationVisibility(), DeviceEnum.WEARABLE)
    setupBtn(embeddedElemBtn, prefData.loadEmbeddedConfigurationVisibility(), DeviceEnum.EMBEDDED)
  }

  private fun setupBtn(
      btn: View,
      visibility: SharedPrefDataRepository.Companion.Visibility,
      type: DeviceEnum
  ) {
    btn.setOnClickListener {
      prefData.saveDeviceType(type.toString())
      if (intent.getBooleanExtra(CHOOSE_DEVICE_FOR_PERSO, false)) {
        intent.putExtras(intent)
        startActivity(Intent(this, PersonnalizationActivity::class.java))
        this.finish()
      } else startActivity(Intent(this, CardReaderActivity::class.java))
    }
    when (visibility) {
      SharedPrefDataRepository.Companion.Visibility.ENABLE -> {
        btn.visibility = View.VISIBLE
        btn.background = ContextCompat.getDrawable(this, R.drawable.white_card)
        btn.isEnabled = true
      }
      SharedPrefDataRepository.Companion.Visibility.DISABLE -> {
        btn.visibility = View.VISIBLE
        btn.background = ContextCompat.getDrawable(this, R.drawable.grey_card)
        btn.isEnabled = false
      }
      SharedPrefDataRepository.Companion.Visibility.HIDE -> {
        btn.visibility = View.GONE
      }
    }
  }

  companion object {
    const val CHOOSE_DEVICE_FOR_PERSO = "CHOOSE_DEVICE_FOR_PERSO"
  }
}
