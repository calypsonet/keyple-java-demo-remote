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
import java.util.Locale
import kotlinx.android.synthetic.main.activity_card_reader.cardAnimation
import kotlinx.android.synthetic.main.activity_card_reader.loadingAnimation
import kotlinx.android.synthetic.main.activity_card_reader.presentTxt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cna.keyple.demo.sale.data.endpoint.AnalyzeContractsInput
import org.cna.keyple.demo.sale.data.endpoint.AnalyzeContractsOutput
import org.cna.keyple.demo.sale.data.model.ContractStructureDto
import org.cna.keyple.demo.sale.data.model.type.PriorityCode
import org.eclipse.keyple.core.service.event.ReaderEvent
import org.eclipse.keyple.core.service.exception.KeypleException
import org.eclipse.keyple.core.service.util.ContactCardCommonProtocols
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols
import org.eclipse.keyple.demo.remote.R
import org.eclipse.keyple.demo.remote.data.model.CardReaderResponse
import org.eclipse.keyple.demo.remote.data.model.CardTitle
import org.eclipse.keyple.demo.remote.data.model.DeviceEnum
import org.eclipse.keyple.demo.remote.data.model.Status
import org.eclipse.keyple.demo.remote.di.scopes.ActivityScoped
import org.eclipse.keyple.distributed.RemoteServiceParameters
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import timber.log.Timber

@ActivityScoped
class CardReaderActivity : AbstractCardActivity() {

    private val dateTimeFormatter = DateTimeFormat.forPattern("d MMMM yyyy").withLocale(Locale.ENGLISH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_reader)
    }

    override fun onResume() {
        super.onResume()
        try {
            if (DeviceEnum.getDeviceEnum(prefData.loadDeviceType()!!) == DeviceEnum.CONTACTLESS_CARD) {
                showPresentNfcCardInstructions()
                initAndActivateAndroidKeypleNfcReader()
            } else {
                showNowLoadingInformation()
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

    override fun update(event: ReaderEvent?) {
        if (event?.eventType == ReaderEvent.EventType.CARD_INSERTED) {
            // We'll select PO when SmartCard is presented in field
            // Method handlePo is described below
            runOnUiThread {
                showNowLoadingInformation()
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
                val analyseContractsInput = AnalyzeContractsInput().setPluginType(pluginType)
                // unmock for run
                val compatibleContractOutput = localServiceClient.executeRemoteService(
                    RemoteServiceParameters
                        .builder("CONTRACT_ANALYSIS", keypleServices.getReader(selectedDeviceReaderName))
                        .withUserInputData(analyseContractsInput)
                        .withInitialCardContent(calypsoPo)
                        .build(),
                    AnalyzeContractsOutput::class.java)

                when (compatibleContractOutput.statusCode) {
                    0 -> {
                        runOnUiThread {

                            val contracts = compatibleContractOutput.validContracts
                            val status = if (contracts?.size != null && contracts.size> 0) Status.TICKETS_FOUND else Status.EMPTY_CARD

                            changeDisplay(
                                CardReaderResponse(
                                    status,
                                    "",
                                    contracts?.size ?: 0,
                                    buildCardTitles(contracts),
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

    private fun buildCardTitle(contractStructureDto: ContractStructureDto): CardTitle {
        return when (contractStructureDto.contractTariff) {
            PriorityCode.MULTI_TRIP_TICKET -> {
                var valid: Boolean
                val description = contractStructureDto.counter.let {
                    valid = (it.counterValue >= 1)
                    if (it.counterValue > 1)
                        "${it.counterValue} trips left"
                    else
                        "${it.counterValue} trip left"
                }

                CardTitle("Multi trip", description ?: "No counter", valid ?: false)
            }
            PriorityCode.SEASON_PASS -> {
                val saleDate = DateTime.parse("2010-01-01T00:00").plusDays(contractStructureDto.contactSaleDate.daysSinceReference.toInt())
                val validityEndDate = DateTime.parse("2010-01-01T00:00").plusDays(contractStructureDto.contractValidityEndDate.daysSinceReference.toInt())
                val validity = DateTime.now() in saleDate..validityEndDate
                CardTitle("Season pass", "From ${saleDate.toString(dateTimeFormatter)} to ${validityEndDate.toString(dateTimeFormatter)}", validity)
            }
            else -> CardTitle("Else", "", false)
        }
    }

    private fun buildCardTitles(contractStructureDtos: List<ContractStructureDto>?): List<CardTitle> {
        val cardTitles = contractStructureDtos?.map { buildCardTitle(it) }
        return cardTitles ?: arrayListOf()
    }

    override fun changeDisplay(cardReaderResponse: CardReaderResponse, applicationSerialNumber: String?) {
        loadingAnimation.cancelAnimation()
        cardAnimation.cancelAnimation()
        val intent = Intent(this, CardSummaryActivity::class.java)
        intent.putExtra(CARD_CONTENT, cardReaderResponse)
        intent.putExtra(CARD_APPLICATION_NUMBER, applicationSerialNumber)
        startActivity(intent)
    }

    private fun showPresentNfcCardInstructions() {
        presentTxt.text = getString(R.string.present_travel_card_label)
        cardAnimation.visibility = View.VISIBLE
        cardAnimation.playAnimation()
        loadingAnimation.cancelAnimation()
        loadingAnimation.visibility = View.INVISIBLE
    }

    private fun showNowLoadingInformation() {
        presentTxt.text = getString(R.string.read_in_progress)
        loadingAnimation.visibility = View.VISIBLE
        loadingAnimation.playAnimation()
        cardAnimation.cancelAnimation()
        cardAnimation.visibility = View.INVISIBLE
    }
}
