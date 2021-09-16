/********************************************************************************
 * Copyright (c) 2021 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.calypsonet.keyple.demo.distribued.ui

import android.os.Bundle
import javax.inject.Inject
import kotlin.jvm.Throws
import org.calypsonet.terminal.reader.ObservableCardReader
import org.calypsonet.terminal.reader.spi.CardReaderObservationExceptionHandlerSpi
import org.calypsonet.terminal.reader.spi.CardReaderObserverSpi
import org.eclipse.keyple.core.service.KeyplePluginException
import org.eclipse.keyple.core.util.protocol.ContactCardCommonProtocol
import org.eclipse.keyple.core.util.protocol.ContactlessCardCommonProtocol
import org.calypsonet.keyple.demo.distribued.data.model.CardReaderResponse
import org.calypsonet.keyple.demo.distribued.data.model.DeviceEnum
import org.calypsonet.keyple.demo.distribued.data.model.Status
import org.calypsonet.keyple.demo.distribued.manager.KeypleManager
import org.eclipse.keyple.distributed.LocalServiceClient
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcPlugin
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcPluginFactoryProvider
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcReader
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPlugin
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPluginFactoryProvider
import timber.log.Timber

abstract class AbstractCardActivity : AbstractDemoActivity(), CardReaderObserverSpi,
    CardReaderObservationExceptionHandlerSpi {

    @Inject
    lateinit var localServiceClient: LocalServiceClient

    @Inject
    lateinit var keypleServices: KeypleManager

    lateinit var selectedDeviceReaderName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedDeviceReaderName = when (DeviceEnum.getDeviceEnum(prefData.loadDeviceType()!!)) {
            DeviceEnum.CONTACTLESS_CARD -> {
                keypleServices.aidEnum = KeypleManager.AidEnum.CDLIGHT_GTML
                AndroidNfcReader.READER_NAME
            }
            DeviceEnum.SIM -> {
                keypleServices.aidEnum = KeypleManager.AidEnum.NAVIGO2013
                KeypleManager.OMAPI_SIM_READER_NAME
            }
            DeviceEnum.WEARABLE -> {
                keypleServices.aidEnum = KeypleManager.AidEnum.CDLIGHT_GTML
                "WEARABLE"
            }
            DeviceEnum.EMBEDDED -> {
                keypleServices.aidEnum = KeypleManager.AidEnum.CDLIGHT_GTML
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

    /**
     * Android Nfc Reader is strongly dependent and Android Activity component.
     */
    @Throws(KeyplePluginException::class)
    fun initAndActivateAndroidKeypleNfcReader() {
        keypleServices.registerPlugin(
            AndroidNfcPluginFactoryProvider(this@AbstractCardActivity).getFactory()
        )

        val androidNfcReader = keypleServices.getObservableReader(selectedDeviceReaderName) as ObservableCardReader
        androidNfcReader.setReaderObservationExceptionHandler(this@AbstractCardActivity)
        androidNfcReader.addObserver(this@AbstractCardActivity)
        androidNfcReader.setReaderObservationExceptionHandler(this@AbstractCardActivity)

        keypleServices.getReader(selectedDeviceReaderName).activateProtocol(
            ContactlessCardCommonProtocol.ISO_14443_4.name,
            ContactlessCardCommonProtocol.ISO_14443_4.name
        )

        androidNfcReader.startCardDetection(ObservableCardReader.DetectionMode.REPEATING)
    }

    @Throws(KeyplePluginException::class)
    fun deactivateAndClearAndroidKeypleNfcReader() {
        (keypleServices.getReader(selectedDeviceReaderName) as ObservableCardReader).stopCardDetection()
        keypleServices.getReader(selectedDeviceReaderName)
            .deactivateProtocol(ContactlessCardCommonProtocol.ISO_14443_4.name)
        keypleServices.unregisterPlugin(AndroidNfcPlugin.PLUGIN_NAME)
    }

    /**
     * Ìnitialisation of AndroidOmapiPlugin is async and taked time
     * and cannot be observed.
     * So we'll trigger process only when the plugin is registered
     */
    @Throws(KeyplePluginException::class)
    fun initOmapiReader(callback: () -> Unit) {
        AndroidOmapiPluginFactoryProvider(this@AbstractCardActivity) {
            keypleServices.registerPlugin(it)
            keypleServices.getReader(KeypleManager.OMAPI_SIM_READER_NAME).activateProtocol(
                ContactCardCommonProtocol.ISO_7816_3.name,
                ContactCardCommonProtocol.ISO_7816_3.name
            )
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
                    Status.INVALID_CARD,
                    "",
                    0,
                    arrayListOf(),
                    arrayListOf(),
                    "",
                    "invalid card"
                )
            )
        }
    }

    fun launchServerErrorResponse() {
        runOnUiThread {
            changeDisplay(
                CardReaderResponse(
                    Status.ERROR,
                    "",
                    0,
                    arrayListOf(),
                    arrayListOf(),
                    ""
                )
            )
        }
    }

    fun launchExceptionResponse(e: Exception, finishActivity: Boolean? = false) {
        runOnUiThread {
            changeDisplay(
                CardReaderResponse(
                    Status.ERROR,
                    "",
                    0,
                    arrayListOf(),
                    arrayListOf(),
                    "",
                    e.message
                ),
                finishActivity = finishActivity
            )
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
