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
import org.calypsonet.keyple.demo.common.constant.RemoteServiceId
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
import org.eclipse.keyple.core.util.HexUtil
import org.eclipse.keypop.reader.CardReaderEvent
import timber.log.Timber

@ActivityScoped
class ReloadActivity : AbstractCardActivity() {

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
            RemoteServiceId.READ_CARD_AND_ANALYZE_CONTRACTS.name,
            selectedDeviceReaderName,
            transactionManager.calypsoCard,
            analyseContractsInput,
            AnalyzeContractsOutputDto::class.java)

        val contractTariff =
            PriorityCode.findEnumByKey(
                intent.getIntExtra(SelectTicketsActivity.SELECTED_TICKET_PRIORITY_CODE, 0))
        val ticketToBeLoaded = intent.getIntExtra(SelectTicketsActivity.TICKETS_NUMBER, 0)

        val writeContractInputDto =
            WriteContractInputDto(contractTariff, ticketToBeLoaded, pluginType)

        val writeTitleOutput =
            localServiceClient.executeRemoteService(
                RemoteServiceId.READ_CARD_AND_WRITE_CONTRACT.name,
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
            launchInvalidCardResponse(
                String.format(
                    getString(R.string.card_invalid_structure),
                    HexUtil.toHex(transactionManager.calypsoCard.applicationSubtype)))
          } // card rejected
        }
      } catch (e: IllegalStateException) {
        Timber.e(e)
        launchInvalidCardResponse(e.message!!)
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
    val intent = Intent(this, ReloadResultActivity::class.java)
    intent.putExtra(ReloadResultActivity.TICKETS_NUMBER, 0)
    intent.putExtra(ReloadResultActivity.STATUS, cardReaderResponse.status.toString())
    intent.putExtra(ReloadResultActivity.MESSAGE, cardReaderResponse.errorMessage)
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
