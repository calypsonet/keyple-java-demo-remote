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
import java.lang.IllegalStateException
import kotlinx.android.synthetic.main.activity_card_reader.cardAnimation
import kotlinx.android.synthetic.main.activity_card_reader.loadingAnimation
import kotlinx.android.synthetic.main.activity_card_reader.presentTxt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cna.keyple.demo.sale.data.endpoint.WriteContractInput
import org.cna.keyple.demo.sale.data.endpoint.WriteContractOutput
import org.cna.keyple.demo.sale.data.model.type.PriorityCode
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
class ChargeActivity : AbstractCardActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charge)
    }

    override fun initReaders() {
        try {
            if (DeviceEnum.getDeviceEnum(prefData.loadDeviceType()!!) == DeviceEnum.CONTACTLESS_CARD) {
                showPresentNfcCardInstructions()
                initAndActivateAndroidKeypleNfcReader()
            } else {
                showNowLoadingInformation()
                initOmapiReader() {
                    GlobalScope.launch {
                        remoteServiceExecution(
                            selectedDeviceReaderName,
                            "Android OMAPI",
                            keypleServices.aidEnum.aid,
                            ContactCardCommonProtocols.ISO_7816_3.name
                        )
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
            }
        } catch (e: KeypleException) {
            Timber.e(e)
        }
        super.onPause()
    }

    override fun update(event: ReaderEvent?) {
        if (event?.eventType == ReaderEvent.EventType.CARD_INSERTED) {
            runOnUiThread {
                showNowLoadingInformation()
            }

            GlobalScope.launch {
                remoteServiceExecution(
                    selectedDeviceReaderName,
                    "Android NFC",
                    keypleServices.aidEnum.aid,
                    ContactlessCardCommonProtocols.ISO_14443_4.name
                )
            }
        }
    }

    private suspend fun remoteServiceExecution(
        selectedDeviceReaderName: String,
        pluginType: String,
        aid: String,
        protocol: String?
    ) {
        withContext(Dispatchers.IO) {
            try {
                val calypsoPo = keypleServices.getCalypsoPo(selectedDeviceReaderName, aid, protocol)
                val readCardSerialNumber = intent.getStringExtra(CARD_APPLICATION_NUMBER)
                if (calypsoPo.applicationSerialNumber != readCardSerialNumber) {
                    // Ticket would have been bought for the Card read at step one.
                    // To avoid swapping we check thant loading is done on the same card
                    throw IllegalStateException("Not the same card")
                }

                val writeContractInput = WriteContractInput()
                writeContractInput.pluginType = pluginType

                val ticketToBeLoaded = intent.getIntExtra(SelectTicketsActivity.TICKETS_NUMBER, 0)
                writeContractInput.contractTariff = PriorityCode.valueOf(
                    intent.getByteExtra(
                        SelectTicketsActivity.SELECTED_TICKET_PRIORITY_CODE,
                        0
                    )
                )
                writeContractInput.ticketToLoad = ticketToBeLoaded

                val writeTitleOutput = localServiceClient.executeRemoteService(
                    RemoteServiceParameters.builder(
                        "WRITE_CONTRACT",
                        keypleServices.getReader(selectedDeviceReaderName)
                    )
                        .withUserInputData(writeContractInput)
                        .withInitialCardContent(calypsoPo)
                        .build(),
                    WriteContractOutput::class.java
                )

                when (writeTitleOutput.statusCode) {
                    0 -> {
                        runOnUiThread {
                            changeDisplay(
                                CardReaderResponse(
                                    Status.SUCCESS,
                                    "",
                                    ticketToBeLoaded,
                                    arrayListOf(),
                                    arrayListOf(),
                                    ""
                                ),
                                finishActivity = true
                            )
                        }
                    }
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

    override fun changeDisplay(
        cardReaderResponse: CardReaderResponse,
        applicationSerialNumber: String?,
        finishActivity: Boolean?
    ) {
        loadingAnimation.cancelAnimation()
        cardAnimation.cancelAnimation()
        val intent = Intent(this, ChargeResultActivity::class.java)
        intent.putExtra(ChargeResultActivity.TICKETS_NUMBER, 0)
        intent.putExtra(ChargeResultActivity.STATUS, cardReaderResponse.status.toString())
        startActivity(intent)
        if(finishActivity == true){finish()}
    }

    private fun showPresentNfcCardInstructions() {
        presentTxt.text = getString(R.string.present_card)
        cardAnimation.visibility = View.VISIBLE
        cardAnimation.playAnimation()
        loadingAnimation.cancelAnimation()
        loadingAnimation.visibility = View.INVISIBLE
    }

    private fun showNowLoadingInformation() {
        presentTxt.text = getString(R.string.loading_in_progress)
        loadingAnimation.visibility = View.VISIBLE
        loadingAnimation.playAnimation()
        cardAnimation.cancelAnimation()
        cardAnimation.visibility = View.INVISIBLE
    }
}
