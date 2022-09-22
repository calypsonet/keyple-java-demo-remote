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

import android.os.Bundle
import javax.inject.Inject
import kotlin.jvm.Throws
import org.calypsonet.keyple.demo.common.constant.CardConstant
import org.calypsonet.keyple.demo.reload.remote.service.MainService
import org.calypsonet.keyple.demo.reload.remote.service.ticketing.model.CardReaderResponse
import org.calypsonet.keyple.demo.reload.remote.service.ticketing.model.DeviceEnum
import org.calypsonet.keyple.demo.reload.remote.service.ticketing.model.Status
import org.calypsonet.terminal.reader.ConfigurableCardReader
import org.calypsonet.terminal.reader.ObservableCardReader
import org.calypsonet.terminal.reader.spi.CardReaderObservationExceptionHandlerSpi
import org.calypsonet.terminal.reader.spi.CardReaderObserverSpi
import org.eclipse.keyple.core.service.KeyplePluginException
import org.eclipse.keyple.core.util.protocol.ContactlessCardCommonProtocol
import org.eclipse.keyple.distributed.LocalServiceClient
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcPlugin
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcPluginFactoryProvider
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcReader
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPlugin
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPluginFactoryProvider
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiReader
import timber.log.Timber

abstract class AbstractCardActivity :
    AbstractDemoActivity(), CardReaderObserverSpi, CardReaderObservationExceptionHandlerSpi {

  @Inject lateinit var localServiceClient: LocalServiceClient
  @Inject lateinit var keypleServices: MainService
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
            keypleServices.aidEnums.clear()
            keypleServices.aidEnums.add(CardConstant.AID_CD_LIGHT_GTML)
            keypleServices.aidEnums.add(CardConstant.AID_CALYPSO_LIGHT_CL)
            keypleServices.aidEnums.add(CardConstant.AID_NAVIGO_2013)
            AndroidNfcReader.READER_NAME
          }
          DeviceEnum.SIM -> {
            pluginType = "Android OMAPI"
            keypleServices.aidEnums.clear()
            keypleServices.aidEnums.add(CardConstant.AID_CD_LIGHT_GTML)
            keypleServices.aidEnums.add(CardConstant.AID_NAVIGO_2013)
            AndroidOmapiReader.READER_NAME_SIM_1
          }
          DeviceEnum.WEARABLE -> {
            pluginType = "Android WEARABLE"
            keypleServices.aidEnums.clear()
            keypleServices.aidEnums.add(CardConstant.AID_CD_LIGHT_GTML)
            "WEARABLE"
          }
          DeviceEnum.EMBEDDED -> {
            pluginType = "Android EMBEDDED"
            keypleServices.aidEnums.clear()
            keypleServices.aidEnums.add(CardConstant.AID_CD_LIGHT_GTML)
            "EMBEDDED"
          }
        }
  }

  override fun onResume() {
    super.onResume()
    if (!prefData.loadLastStatus()) {
      launchExceptionResponse(IllegalStateException("Server not available"), true)
      return
    } else {
      initReaders()
    }
  }

  /** Android Nfc Reader is strongly dependent and Android Activity component. */
  @Throws(KeyplePluginException::class)
  fun initAndActivateAndroidKeypleNfcReader() {
    keypleServices.registerPlugin(
        AndroidNfcPluginFactoryProvider(this@AbstractCardActivity).getFactory())

    val androidNfcReader =
        keypleServices.getObservableReader(selectedDeviceReaderName) as ObservableCardReader
    androidNfcReader.setReaderObservationExceptionHandler(this@AbstractCardActivity)
    androidNfcReader.addObserver(this@AbstractCardActivity)
    androidNfcReader.setReaderObservationExceptionHandler(this@AbstractCardActivity)

    (keypleServices.getReader(selectedDeviceReaderName) as ConfigurableCardReader).activateProtocol(
        ContactlessCardCommonProtocol.ISO_14443_4.name,
        ContactlessCardCommonProtocol.ISO_14443_4.name)

    androidNfcReader.startCardDetection(ObservableCardReader.DetectionMode.REPEATING)
  }

  @Throws(KeyplePluginException::class)
  fun deactivateAndClearAndroidKeypleNfcReader() {
    (keypleServices.getReader(selectedDeviceReaderName) as ObservableCardReader).stopCardDetection()
    (keypleServices.getReader(selectedDeviceReaderName) as ConfigurableCardReader)
        .deactivateProtocol(ContactlessCardCommonProtocol.ISO_14443_4.name)
    keypleServices.unregisterPlugin(AndroidNfcPlugin.PLUGIN_NAME)
  }

  /**
   * Initialisation of AndroidOmapiPlugin is async and take time and cannot be observed. So we'll
   * trigger process only when the plugin is registered
   */
  @Throws(KeyplePluginException::class)
  fun initOmapiReader(callback: () -> Unit) {
    AndroidOmapiPluginFactoryProvider(this@AbstractCardActivity) {
      keypleServices.registerPlugin(it)
      //            (keypleServices.getReader(AndroidOmapiReader.READER_NAME_SIM_1) as
      // ConfigurableCardReader).activateProtocol(
      //                ContactCardCommonProtocol.ISO_7816_3.name,
      //                ContactCardCommonProtocol.ISO_7816_3.name
      //            )
      callback()
    }
  }

  @Throws(KeyplePluginException::class)
  fun deactivateAndClearOmapiReader() {
    keypleServices.unregisterPlugin(AndroidOmapiPlugin.PLUGIN_NAME)
  }

  fun launchInvalidCardResponse() {
    runOnUiThread {
      changeDisplay(
          CardReaderResponse(
              Status.INVALID_CARD, "", 0, arrayListOf(), arrayListOf(), "", "invalid card"),
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
