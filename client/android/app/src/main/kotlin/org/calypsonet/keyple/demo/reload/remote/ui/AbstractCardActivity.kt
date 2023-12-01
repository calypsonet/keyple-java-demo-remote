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
import javax.inject.Inject
import kotlin.jvm.Throws
import org.calypsonet.keyple.demo.common.constant.CardConstant
import org.calypsonet.keyple.demo.reload.remote.data.ReaderRepository
import org.calypsonet.keyple.demo.reload.remote.data.model.AppSettings
import org.calypsonet.keyple.demo.reload.remote.data.model.CardReaderResponse
import org.calypsonet.keyple.demo.reload.remote.data.model.DeviceEnum
import org.calypsonet.keyple.demo.reload.remote.data.model.Status
import org.eclipse.keyple.core.service.KeyplePluginException
import org.eclipse.keyple.distributed.LocalServiceClient
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcPlugin
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcPluginFactoryProvider
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcReader
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcSupportedProtocols
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPlugin
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPluginFactoryProvider
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiReader
import org.eclipse.keypop.reader.ConfigurableCardReader
import org.eclipse.keypop.reader.ObservableCardReader
import org.eclipse.keypop.reader.spi.CardReaderObservationExceptionHandlerSpi
import org.eclipse.keypop.reader.spi.CardReaderObserverSpi
import timber.log.Timber

abstract class AbstractCardActivity :
    AbstractDemoActivity(), CardReaderObserverSpi, CardReaderObservationExceptionHandlerSpi {

  @Inject lateinit var localServiceClient: LocalServiceClient
  @Inject lateinit var readerRepository: ReaderRepository
  lateinit var selectedDeviceReaderName: String
  lateinit var device: DeviceEnum
  lateinit var pluginType: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    device = DeviceEnum.getDeviceEnum(prefData.loadDeviceType()!!)
    selectedDeviceReaderName =
        when (device) {
          DeviceEnum.CONTACTLESS_CARD -> {
            pluginType = "Android NFC"
            AppSettings.aidEnums.clear()
            AppSettings.aidEnums.add(CardConstant.AID_KEYPLE_GENERIC)
            AppSettings.aidEnums.add(CardConstant.AID_CD_LIGHT_GTML)
            AppSettings.aidEnums.add(CardConstant.AID_CALYPSO_LIGHT)
            AppSettings.aidEnums.add(CardConstant.AID_NORMALIZED_IDF)
            AndroidNfcReader.READER_NAME
          }
          DeviceEnum.SIM -> {
            pluginType = "Android OMAPI"
            AppSettings.aidEnums.clear()
            AppSettings.aidEnums.add(CardConstant.AID_CD_LIGHT_GTML)
            AppSettings.aidEnums.add(CardConstant.AID_NORMALIZED_IDF)
            AndroidOmapiReader.READER_NAME_SIM_1
          }
          DeviceEnum.WEARABLE -> {
            pluginType = "Android WEARABLE"
            AppSettings.aidEnums.clear()
            AppSettings.aidEnums.add(CardConstant.AID_CD_LIGHT_GTML)
            "WEARABLE"
          }
          DeviceEnum.EMBEDDED -> {
            pluginType = "Android EMBEDDED"
            AppSettings.aidEnums.clear()
            AppSettings.aidEnums.add(CardConstant.AID_CD_LIGHT_GTML)
            "EMBEDDED"
          }
        }
  }

  override fun onResume() {
    super.onResume()
    initReaders()
  }

  /** Android Nfc Reader is strongly dependent and Android Activity component. */
  @Throws(KeyplePluginException::class)
  fun initAndActivateAndroidKeypleNfcReader() {
    readerRepository.registerPlugin(
        AndroidNfcPluginFactoryProvider(this@AbstractCardActivity).getFactory())

    val androidNfcReader = readerRepository.getObservableReader(selectedDeviceReaderName)
    androidNfcReader.setReaderObservationExceptionHandler(this@AbstractCardActivity)
    androidNfcReader.addObserver(this@AbstractCardActivity)
    androidNfcReader.setReaderObservationExceptionHandler(this@AbstractCardActivity)

    (readerRepository.getReader(selectedDeviceReaderName) as ConfigurableCardReader)
        .activateProtocol(AndroidNfcSupportedProtocols.ISO_14443_4.name, "ISO_14443_4")

    androidNfcReader.startCardDetection(ObservableCardReader.DetectionMode.REPEATING)
  }

  @Throws(KeyplePluginException::class)
  fun deactivateAndClearAndroidKeypleNfcReader() {
    (readerRepository.getReader(selectedDeviceReaderName) as ObservableCardReader)
        .stopCardDetection()
    (readerRepository.getReader(selectedDeviceReaderName) as ConfigurableCardReader)
        .deactivateProtocol(AndroidNfcSupportedProtocols.ISO_14443_4.name)
    readerRepository.unregisterPlugin(AndroidNfcPlugin.PLUGIN_NAME)
  }

  /**
   * Initialisation of AndroidOmapiPlugin is async and take time and cannot be observed. So we'll
   * trigger process only when the plugin is registered
   */
  @Throws(KeyplePluginException::class)
  fun initOmapiReader(callback: () -> Unit) {
    AndroidOmapiPluginFactoryProvider(this@AbstractCardActivity) {
      readerRepository.registerPlugin(it)
      //            (readerRepository.getReader(AndroidOmapiReader.READER_NAME_SIM_1) as
      // ConfigurableCardReader).activateProtocol(
      //                ContactCardCommonProtocol.ISO_7816_3.name,
      //                ContactCardCommonProtocol.ISO_7816_3.name
      //            )
      callback()
    }
  }

  @Throws(KeyplePluginException::class)
  fun deactivateAndClearOmapiReader() {
    readerRepository.unregisterPlugin(AndroidOmapiPlugin.PLUGIN_NAME)
  }

  fun launchInvalidCardResponse(message: String) {
    runOnUiThread {
      changeDisplay(
          CardReaderResponse(Status.INVALID_CARD, "", 0, arrayListOf(), arrayListOf(), "", message),
          finishActivity =
              device !=
                  DeviceEnum.CONTACTLESS_CARD // /Only with NFC we can come back to 'wait for device
          // screen'
          )
    }
  }

  fun launchServerErrorResponse() {
    runOnUiThread {
      changeDisplay(
          CardReaderResponse(Status.ERROR, "", 0, arrayListOf(), arrayListOf(), ""),
          finishActivity =
              device !=
                  DeviceEnum.CONTACTLESS_CARD // /Only with NFC we can come back to 'wait for device
          // screen'
          )
    }
  }

  fun launchExceptionResponse(e: Exception, finishActivity: Boolean? = false) {
    runOnUiThread {
      changeDisplay(
          CardReaderResponse(Status.ERROR, "", 0, arrayListOf(), arrayListOf(), "", e.message),
          finishActivity = finishActivity)
    }
  }

  protected abstract fun changeDisplay(
      cardReaderResponse: CardReaderResponse,
      applicationSerialNumber: String? = null,
      finishActivity: Boolean? = false
  )

  override fun onReaderObservationError(contextInfo: String?, readerName: String?, e: Throwable?) {
    Timber.e(e)
    Timber.d("Error on $contextInfo, $readerName")
    this@AbstractCardActivity.finish()
  }

  protected abstract fun initReaders()

  companion object {
    const val CARD_APPLICATION_NUMBER = "cardApplicationNumber"
    const val CARD_CONTENT = "cardContent"
  }
}
