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
import java.lang.Exception
import java.lang.IllegalStateException
import javax.inject.Inject
import kotlinx.android.synthetic.main.activity_card_reader.cardAnimation
import kotlinx.android.synthetic.main.activity_card_reader.loadingAnimation
import kotlinx.android.synthetic.main.activity_card_reader.presentTxt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.calypsonet.keyple.demo.common.dto.AnalyzeContractsInputDto
import org.calypsonet.keyple.demo.common.dto.AnalyzeContractsOutputDto
import org.calypsonet.keyple.demo.common.dto.WriteContractInputDto
import org.calypsonet.keyple.demo.common.dto.WriteContractOutputDto
import org.calypsonet.keyple.demo.common.model.type.PriorityCode
import org.calypsonet.keyple.demo.reload.remote.R
import org.calypsonet.keyple.demo.reload.remote.data.model.AppSettings
import org.calypsonet.keyple.demo.reload.remote.data.model.CardReaderResponse
import org.calypsonet.keyple.demo.reload.remote.data.model.DeviceEnum
import org.calypsonet.keyple.demo.reload.remote.data.model.Status
import org.calypsonet.keyple.demo.reload.remote.di.scopes.ActivityScoped
import org.calypsonet.keyple.demo.reload.remote.domain.TicketingService
import org.calypsonet.terminal.reader.CardReaderEvent
import org.eclipse.keyple.core.util.HexUtil
import org.eclipse.keyple.core.util.protocol.ContactlessCardCommonProtocol
import timber.log.Timber

@ActivityScoped
class ChargeActivity : AbstractCardActivity() {

  @Inject lateinit var ticketingService: TicketingService

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
        initOmapiReader {
          GlobalScope.launch {
            remoteServiceExecution(
                selectedDeviceReaderName, pluginType, AppSettings.aidEnums, protocol = null)
          }
        }
      }
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
      }
    } catch (e: Exception) {
      Timber.e(e)
    }
    super.onPause()
  }

  override fun onReaderEvent(event: CardReaderEvent?) {
    if (event?.type == CardReaderEvent.Type.CARD_INSERTED) {
      runOnUiThread { showNowLoadingInformation() }
      GlobalScope.launch {
        remoteServiceExecution(
            selectedDeviceReaderName,
            pluginType,
            AppSettings.aidEnums,
            ContactlessCardCommonProtocol.ISO_14443_4.name)
      }
    }
  }

  private suspend fun remoteServiceExecution(
      selectedDeviceReaderName: String,
      pluginType: String,
      aidEnums: ArrayList<String>,
      protocol: String?
  ) {
    withContext(Dispatchers.IO) {
      try {
        val readCardSerialNumber = intent.getStringExtra(CARD_APPLICATION_NUMBER)

        val transactionManager =
            ticketingService.getTransactionManager(selectedDeviceReaderName, aidEnums, protocol)
        if (HexUtil.toHex(transactionManager.calypsoCard.applicationSerialNumber) !=
            readCardSerialNumber) {
          // Ticket would have been bought for the Card read at step one.
          // To avoid swapping we check thant loading is done on the same card
          throw IllegalStateException("Not the same card")
        }

        val analyseContractsInput = AnalyzeContractsInputDto(pluginType)
        // un-mock for run
        localServiceClient.executeRemoteService(
            "CONTRACT_ANALYSIS",
            selectedDeviceReaderName,
            transactionManager.calypsoCard,
            analyseContractsInput,
            AnalyzeContractsOutputDto::class.java)

        val writeContractInputDto = WriteContractInputDto(null, null, pluginType)

        val ticketToBeLoaded = intent.getIntExtra(SelectTicketsActivity.TICKETS_NUMBER, 0)
        writeContractInputDto.contractTariff =
            PriorityCode.findEnumByKey(
                intent.getByteExtra(SelectTicketsActivity.SELECTED_TICKET_PRIORITY_CODE, 0).toInt())
        writeContractInputDto.ticketToLoad = ticketToBeLoaded

        val writeTitleOutput =
            localServiceClient.executeRemoteService(
                "WRITE_CONTRACT",
                selectedDeviceReaderName,
                transactionManager.calypsoCard,
                writeContractInputDto,
                WriteContractOutputDto::class.java)

        when (writeTitleOutput.statusCode) {
          0 -> {
            runOnUiThread {
              changeDisplay(
                  CardReaderResponse(
                      Status.SUCCESS, "", ticketToBeLoaded, arrayListOf(), arrayListOf(), ""),
                  finishActivity = true)
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
    if (finishActivity == true) {
      finish()
    }
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
