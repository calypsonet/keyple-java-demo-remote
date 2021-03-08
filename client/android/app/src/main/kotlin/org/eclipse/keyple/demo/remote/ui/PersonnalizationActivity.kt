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
package org.eclipse.keyple.demo.remote.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import java.lang.Exception
import kotlinx.android.synthetic.main.activity_card_reader.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cna.keyple.demo.sale.data.endpoint.CardIssuanceOutput
import org.eclipse.keyple.core.service.event.ReaderEvent
import org.eclipse.keyple.core.service.exception.KeypleException
import org.eclipse.keyple.core.service.util.ContactCardCommonProtocols
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols
import org.eclipse.keyple.demo.remote.R
import org.eclipse.keyple.demo.remote.data.model.CardReaderResponse
import org.eclipse.keyple.demo.remote.data.model.DeviceEnum
import org.eclipse.keyple.demo.remote.data.model.Status
import org.eclipse.keyple.demo.remote.di.scopes.ActivityScoped
import org.eclipse.keyple.distributed.RemoteServiceParameters
import timber.log.Timber

@ActivityScoped
class PersonnalizationActivity : AbstractCardActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personnalization)
    }

    override fun onResume() {
        super.onResume()
        try {
            if (DeviceEnum.getDeviceEnum(prefData.loadDeviceType()!!) == DeviceEnum.CONTACTLESS_CARD) {
                showPresentNfcCardInstructions()
                initAndActivateAndroidKeypleNfcReader()
            } else {
                showNowPersonnalizingInformation()
                initOmapiReader() {
                    GlobalScope.launch {
                        remoteServiceExecution(selectedDeviceReaderName, "Android OMAPI", keypleServices.aidEnum.aid, ContactCardCommonProtocols.ISO_7816_3.name)
                    }
                }
            }
        } catch (e: KeypleException) {
            Timber.e(e)
        }
    }

    override fun onPause() {
        cardAnimation.cancelAnimation()
        loadingAnimation.cancelAnimation()
        try {
            if (DeviceEnum.getDeviceEnum(prefData.loadDeviceType()!!) == DeviceEnum.CONTACTLESS_CARD) {
                deactivateAndClearAndroidKeypleNfcReader()
            } else {
                deactivateAndClearOmapiReader()
            }
        } catch (e: KeypleException) {
            Timber.e(e)
        }
        super.onPause()
    }

    private fun showPresentNfcCardInstructions() {
        presentTxt.text = getString(R.string.present_card_personnalisation)
        cardAnimation.visibility = View.VISIBLE
        cardAnimation.playAnimation()
        loadingAnimation.cancelAnimation()
        loadingAnimation.visibility = View.INVISIBLE
    }

    private fun showNowPersonnalizingInformation() {
        presentTxt.text = getString(R.string.read_in_progress)
        loadingAnimation.visibility = View.VISIBLE
        loadingAnimation.playAnimation()
        cardAnimation.cancelAnimation()
        cardAnimation.visibility = View.INVISIBLE
    }

    override fun changeDisplay(cardReaderResponse: CardReaderResponse, applicationSerialNumber: String?) {
        val intent = Intent(this, ChargeResultActivity::class.java)
        intent.putExtra(ChargeResultActivity.IS_PERSONNALIZATION_RESULT, true)
        intent.putExtra(ChargeResultActivity.STATUS, cardReaderResponse.status.name)
        startActivity(intent)
        this.finish()
    }

    override fun update(event: ReaderEvent?) {
        if (event?.eventType == ReaderEvent.EventType.CARD_INSERTED) {
            runOnUiThread {
                showNowPersonnalizingInformation()
            }
            GlobalScope.launch {
                remoteServiceExecution(selectedDeviceReaderName, "Android NFC", keypleServices.aidEnum.aid, ContactlessCardCommonProtocols.ISO_14443_4.name)
            }
        }
    }

    private suspend fun remoteServiceExecution(selectedDeviceReaderName: String, pluginType: String, aid: String, protocol: String?) {
        withContext(Dispatchers.IO) {
            try {
                val calypsoPo = keypleServices.getCalypsoPo(selectedDeviceReaderName, aid, protocol)
                val cardIssuanceOutput = localServiceClient.executeRemoteService(
                    RemoteServiceParameters
                        .builder("CARD_ISSUANCE", keypleServices.getReader(selectedDeviceReaderName))
                        .withInitialCardContent(calypsoPo)
                        .build(),
                    CardIssuanceOutput::class.java
                )

                when (cardIssuanceOutput.statusCode) {
                    0 -> {
                        runOnUiThread {

                            changeDisplay(
                                CardReaderResponse(
                                    Status.SUCCESS,
                                    "",
                                    0,
                                    arrayListOf(),
                                    arrayListOf(),
                                    ""
                                ),
                                calypsoPo.applicationSerialNumber
                            )
                        }
                    } // success,
                    1 -> {
                        launchServerErrorResponse()
                    } // server not ready,
                    2 -> {
                        launchInvalidCardResponse()
                    } // card rejected
                }
            } catch (e: Exception) {
                Timber.e(e)
                launchExceptionResponse(e)
            }
        }
    }
}
