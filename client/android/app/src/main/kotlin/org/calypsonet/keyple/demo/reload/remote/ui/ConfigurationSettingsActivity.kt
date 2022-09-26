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

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import kotlinx.android.synthetic.main.activity_configuration_settings.backBtn
import kotlinx.android.synthetic.main.activity_configuration_settings.contactlessCardDisable
import kotlinx.android.synthetic.main.activity_configuration_settings.contactlessCardEnable
import kotlinx.android.synthetic.main.activity_configuration_settings.contactlessCardHide
import kotlinx.android.synthetic.main.activity_configuration_settings.embeddedCardDisable
import kotlinx.android.synthetic.main.activity_configuration_settings.embeddedCardEnable
import kotlinx.android.synthetic.main.activity_configuration_settings.embeddedCardHide
import kotlinx.android.synthetic.main.activity_configuration_settings.simCardDisable
import kotlinx.android.synthetic.main.activity_configuration_settings.simCardEnable
import kotlinx.android.synthetic.main.activity_configuration_settings.simCardHide
import kotlinx.android.synthetic.main.activity_configuration_settings.wearableCardDisable
import kotlinx.android.synthetic.main.activity_configuration_settings.wearableCardEnable
import kotlinx.android.synthetic.main.activity_configuration_settings.wearableCardHide
import org.calypsonet.keyple.demo.reload.remote.R
import org.calypsonet.keyple.demo.reload.remote.data.SharedPrefDataRepository

class ConfigurationSettingsActivity : AbstractDemoActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_configuration_settings)

    backBtn.setOnClickListener { onBackPressed() }

    setupRadioBtn(
        prefData.loadContactlessConfigurationVisibility(),
        contactlessCardEnable,
        contactlessCardDisable,
        contactlessCardHide)
    setupRadioBtn(
        prefData.loadSimConfigurationVisibility(), simCardEnable, simCardDisable, simCardHide)
    setupRadioBtn(
        prefData.loadWearableConfigurationVisibility(),
        wearableCardEnable,
        wearableCardDisable,
        wearableCardHide)
    setupRadioBtn(
        prefData.loadEmbeddedConfigurationVisibility(),
        embeddedCardEnable,
        embeddedCardDisable,
        embeddedCardHide)
  }

  private fun setupRadioBtn(
      visibility: SharedPrefDataRepository.Companion.Visibility,
      enableBtn: RadioButton,
      disableBtn: RadioButton,
      hideBtn: RadioButton
  ) {
    when (visibility) {
      SharedPrefDataRepository.Companion.Visibility.ENABLE -> {
        enableBtn.isChecked = true
        enableBtn.setTextColor(resources.getColor(R.color.dark_blue))
        disableBtn.isChecked = false
        disableBtn.setTextColor(resources.getColor(R.color.light_grey))
        hideBtn.isChecked = false
        hideBtn.setTextColor(resources.getColor(R.color.light_grey))
      }
      SharedPrefDataRepository.Companion.Visibility.DISABLE -> {
        enableBtn.isChecked = false
        enableBtn.setTextColor(resources.getColor(R.color.light_grey))
        disableBtn.isChecked = true
        disableBtn.setTextColor(resources.getColor(R.color.dark_blue))
        hideBtn.isChecked = false
        hideBtn.setTextColor(resources.getColor(R.color.light_grey))
      }
      SharedPrefDataRepository.Companion.Visibility.HIDE -> {
        enableBtn.isChecked = false
        enableBtn.setTextColor(resources.getColor(R.color.light_grey))
        disableBtn.isChecked = false
        disableBtn.setTextColor(resources.getColor(R.color.light_grey))
        hideBtn.isChecked = true
        hideBtn.setTextColor(resources.getColor(R.color.dark_blue))
      }
    }
  }

  fun onContactlessRadioButtonClicked(view: View) {
    if (view is RadioButton) {
      // Is the button now checked?
      val checked = view.isChecked

      // Check which radio button was clicked
      when (view.getId()) {
        R.id.contactlessCardEnable ->
            if (checked) {
              prefData.saveContactlessConfigurationVisibility(
                  SharedPrefDataRepository.Companion.Visibility.ENABLE)
            }
        R.id.contactlessCardDisable ->
            if (checked) {
              prefData.saveContactlessConfigurationVisibility(
                  SharedPrefDataRepository.Companion.Visibility.DISABLE)
            }
        R.id.contactlessCardHide ->
            if (checked) {
              prefData.saveContactlessConfigurationVisibility(
                  SharedPrefDataRepository.Companion.Visibility.HIDE)
            }
      }

      // Update layout
      setupRadioBtn(
          prefData.loadContactlessConfigurationVisibility(),
          contactlessCardEnable,
          contactlessCardDisable,
          contactlessCardHide)
    }
  }

  fun onSimRadioButtonClicked(view: View) {
    if (view is RadioButton) {
      // Is the button now checked?
      val checked = view.isChecked

      // Check which radio button was clicked
      when (view.getId()) {
        R.id.simCardEnable ->
            if (checked) {
              prefData.saveSimConfigurationVisibility(
                  SharedPrefDataRepository.Companion.Visibility.ENABLE)
            }
        R.id.simCardDisable ->
            if (checked) {
              prefData.saveSimConfigurationVisibility(
                  SharedPrefDataRepository.Companion.Visibility.DISABLE)
            }
        R.id.simCardHide ->
            if (checked) {
              prefData.saveSimConfigurationVisibility(
                  SharedPrefDataRepository.Companion.Visibility.HIDE)
            }
      }
      setupRadioBtn(
          prefData.loadSimConfigurationVisibility(), simCardEnable, simCardDisable, simCardHide)
    }
  }

  fun onWearableRadioButtonClicked(view: View) {
    if (view is RadioButton) {
      // Is the button now checked?
      val checked = view.isChecked

      // Check which radio button was clicked
      when (view.getId()) {
        R.id.wearableCardEnable ->
            if (checked) {
              prefData.saveWearableConfigurationVisibility(
                  SharedPrefDataRepository.Companion.Visibility.ENABLE)
            }
        R.id.wearableCardDisable ->
            if (checked) {
              prefData.saveWearableConfigurationVisibility(
                  SharedPrefDataRepository.Companion.Visibility.DISABLE)
            }
        R.id.wearableCardHide ->
            if (checked) {
              prefData.saveWearableConfigurationVisibility(
                  SharedPrefDataRepository.Companion.Visibility.HIDE)
            }
      }
      setupRadioBtn(
          prefData.loadWearableConfigurationVisibility(),
          wearableCardEnable,
          wearableCardDisable,
          wearableCardHide)
    }
  }

  fun onEmbeddedRadioButtonClicked(view: View) {
    if (view is RadioButton) {
      // Is the button now checked?
      val checked = view.isChecked

      // Check which radio button was clicked
      when (view.getId()) {
        R.id.embeddedCardEnable ->
            if (checked) {
              prefData.saveEmbeddedConfigurationVisibility(
                  SharedPrefDataRepository.Companion.Visibility.ENABLE)
            }
        R.id.embeddedCardDisable ->
            if (checked) {
              prefData.saveEmbeddedConfigurationVisibility(
                  SharedPrefDataRepository.Companion.Visibility.DISABLE)
            }
        R.id.embeddedCardHide ->
            if (checked) {
              prefData.saveEmbeddedConfigurationVisibility(
                  SharedPrefDataRepository.Companion.Visibility.HIDE)
            }
      }
      setupRadioBtn(
          prefData.loadEmbeddedConfigurationVisibility(),
          embeddedCardEnable,
          embeddedCardDisable,
          embeddedCardHide)
    }
  }
}
