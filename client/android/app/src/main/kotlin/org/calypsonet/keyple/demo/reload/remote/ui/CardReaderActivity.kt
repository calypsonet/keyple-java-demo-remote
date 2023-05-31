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
import android.nfc.NfcManager
import android.os.Bundle
import android.view.View
import java.lang.IllegalStateException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.Exception
import kotlinx.android.synthetic.main.activity_card_reader.cardAnimation
import kotlinx.android.synthetic.main.activity_card_reader.loadingAnimation
import kotlinx.android.synthetic.main.activity_card_reader.presentTxt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.calypsonet.keyple.demo.common.constant.RemoteServiceId
import org.calypsonet.keyple.demo.common.dto.AnalyzeContractsInputDto
import org.calypsonet.keyple.demo.common.dto.AnalyzeContractsOutputDto
import org.calypsonet.keyple.demo.common.model.ContractStructure
import org.calypsonet.keyple.demo.common.model.type.PriorityCode
import org.calypsonet.keyple.demo.reload.remote.R
import org.calypsonet.keyple.demo.reload.remote.data.model.*
import org.calypsonet.keyple.demo.reload.remote.di.scopes.ActivityScoped
import org.calypsonet.keyple.demo.reload.remote.domain.TicketingService
import org.calypsonet.keyple.demo.reload.remote.ui.cardsummary.CardSummaryActivity
import org.calypsonet.terminal.reader.CardReaderEvent
import org.calypsonet.terminal.reader.ReaderCommunicationException
import org.eclipse.keyple.core.service.KeyplePluginException
import org.eclipse.keyple.core.util.HexUtil
import timber.log.Timber

@ActivityScoped
class CardReaderActivity : AbstractCardActivity() {

  @Inject lateinit var ticketingService: TicketingService

  private val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_card_reader)
  }

  override fun initReaders() {
    try {
      when (device) {
        DeviceEnum.CONTACTLESS_CARD -> {
          val nfcManager = getSystemService(NFC_SERVICE) as NfcManager
          if (nfcManager.defaultAdapter?.isEnabled == true) {
            showPresentNfcCardInstructions()
            initAndActivateAndroidKeypleNfcReader()
          } else {
            launchExceptionResponse(
                IllegalStateException("NFC not activated"), finishActivity = true)
          }
        }
        DeviceEnum.SIM -> {
          showNowLoadingInformation()
          initOmapiReader {
            GlobalScope.launch {
              remoteServiceExecution(
                  selectedDeviceReaderName, pluginType, AppSettings.aidEnums, null)
            }
          }
        }
        DeviceEnum.WEARABLE -> {
          throw KeyplePluginException("Wearable")
        }
        DeviceEnum.EMBEDDED -> {
          throw KeyplePluginException("Embedded")
        }
      }
    } catch (e: ReaderCommunicationException) {
      Timber.e(e)
      launchExceptionResponse(e, true)
    } catch (e: Exception) {
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
    } catch (e: Exception) {
      Timber.e(e)
    }
    super.onPause()
  }

  override fun onReaderEvent(event: CardReaderEvent?) {
    if (event?.type == CardReaderEvent.Type.CARD_INSERTED) {
      // We'll select Card when SmartCard is presented in field
      // Method handlePo is described below
      runOnUiThread { showNowLoadingInformation() }
      GlobalScope.launch {
        remoteServiceExecution(
            selectedDeviceReaderName, pluginType, AppSettings.aidEnums, "ISO_14443_4")
      }
    }
  }

  private suspend fun remoteServiceExecution(
      selectedDeviceReaderName: String,
      pluginType: String,
      aidEnums: ArrayList<ByteArray>,
      protocol: String?
  ) {
    withContext(Dispatchers.IO) {
      try {
        val transactionManager =
            ticketingService.getTransactionManager(selectedDeviceReaderName, aidEnums, protocol)
        val analyseContractsInput = AnalyzeContractsInputDto(pluginType)
        // un-mock for run
        val compatibleContractOutput =
            localServiceClient.executeRemoteService(
                RemoteServiceId.READ_CARD_AND_ANALYZE_CONTRACTS.name,
                selectedDeviceReaderName,
                transactionManager.calypsoCard,
                analyseContractsInput,
                AnalyzeContractsOutputDto::class.java)

        when (compatibleContractOutput.statusCode) {
          0 -> {
            runOnUiThread {
              val contracts = compatibleContractOutput.validContracts
              val status = if (contracts.isNotEmpty()) Status.TICKETS_FOUND else Status.EMPTY_CARD
              val finishActivity =
                  device !=
                      DeviceEnum
                          .CONTACTLESS_CARD // Only with NFC we can come back to 'wait for device
              // screen'

              changeDisplay(
                  CardReaderResponse(
                      status, "", contracts.size, buildCardTitles(contracts), arrayListOf(), ""),
                  HexUtil.toHex(transactionManager.calypsoCard.applicationSerialNumber),
                  finishActivity)
            }
          } // success,
          1 -> {
            launchServerErrorResponse()
          } // server not ready,
          2 -> {
            launchInvalidCardResponse(
                String.format(
                    getString(R.string.card_invalid_structure),
                    HexUtil.toHex(transactionManager.calypsoCard.applicationSubtype)))
          } // card rejected
          3 -> {
            launchInvalidCardResponse(getString(R.string.card_not_personalized))
          } // card not personalized
          4 -> {
            launchInvalidCardResponse(getString(R.string.expired_environment))
          } // expired environment
        }
      } catch (e: IllegalStateException) {
        Timber.e(e)
        launchInvalidCardResponse(e.message!!)
      } catch (e: Exception) {
        Timber.e(e)
        val finishActivity =
            device !=
                DeviceEnum
                    .CONTACTLESS_CARD // Only with NFC we can come back to 'wait for device screen'
        launchExceptionResponse(
            IllegalStateException("Server error:\n" + e.message), finishActivity)
      }
    }
  }

  private fun buildCardTitle(contractStructure: ContractStructure): CardTitle {
    return when (contractStructure.contractTariff) {
      PriorityCode.MULTI_TRIP -> {
        var isValid = false
        val description =
            contractStructure.counterValue?.let {
              isValid = (it >= 1)
              if (it > 1) "$it trips left" else "$it trip left"
            }
        CardTitle("Multi trip", description ?: "No counter", isValid)
      }
      PriorityCode.SEASON_PASS -> {
        val now = LocalDate.now()
        val isValid =
            (contractStructure.contractSaleDate.getDate().isBefore(now) ||
                contractStructure.contractSaleDate.getDate().isEqual(now)) &&
                (contractStructure.contractValidityEndDate.getDate().isAfter(now) ||
                    contractStructure.contractValidityEndDate.getDate().isEqual(now))
        CardTitle(
            "Season pass",
            "From ${contractStructure.contractSaleDate.getDate().format(dateTimeFormatter)} to ${contractStructure.contractValidityEndDate.getDate().format(dateTimeFormatter)}",
            isValid)
      }
      PriorityCode.EXPIRED -> {
        CardTitle(
            "Season pass - Expired",
            "From ${contractStructure.contractSaleDate.getDate().format(dateTimeFormatter)} to ${contractStructure.contractValidityEndDate.getDate().format(dateTimeFormatter)}",
            false)
      }
      PriorityCode.FORBIDDEN -> {
        CardTitle("FORBIDDEN", "", false)
      }
      PriorityCode.STORED_VALUE -> {
        CardTitle("STORED_VALUE", "", false)
      }
      else -> CardTitle("UNKNOWN", "", false)
    }
  }

  private fun buildCardTitles(contractStructures: List<ContractStructure>?): List<CardTitle> {
    val cardTitles = contractStructures?.map { buildCardTitle(it) }
    return cardTitles ?: arrayListOf()
  }

  override fun changeDisplay(
      cardReaderResponse: CardReaderResponse,
      applicationSerialNumber: String?,
      finishActivity: Boolean?
  ) {
    loadingAnimation?.cancelAnimation()
    cardAnimation?.cancelAnimation()
    val intent = Intent(this, CardSummaryActivity::class.java)
    intent.putExtra(CARD_CONTENT, cardReaderResponse)
    intent.putExtra(CARD_APPLICATION_NUMBER, applicationSerialNumber)
    startActivity(intent)
    if (finishActivity == true) {
      finish()
    }
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
