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
import javax.inject.Inject
import kotlinx.android.synthetic.main.activity_card_reader.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.calypsonet.keyple.demo.common.dto.CardIssuanceOutputDto
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
class PersonnalizationActivity : AbstractCardActivity() {

  @Inject lateinit var ticketingService: TicketingService

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_personnalization)
  }

  override fun initReaders() {
    try {
      if (DeviceEnum.getDeviceEnum(prefData.loadDeviceType()!!) == DeviceEnum.CONTACTLESS_CARD) {
        showPresentNfcCardInstructions()
        initAndActivateAndroidKeypleNfcReader()
      } else {
        showNowPersonnalizingInformation()
        initOmapiReader {
          GlobalScope.launch {
            remoteServiceExecution(selectedDeviceReaderName, AppSettings.aidEnums, null)
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
      } else {
        deactivateAndClearOmapiReader()
      }
    } catch (e: Exception) {
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
    presentTxt.text = getString(R.string.personalization_in_progress)
    loadingAnimation.visibility = View.VISIBLE
    loadingAnimation.playAnimation()
    cardAnimation.cancelAnimation()
    cardAnimation.visibility = View.INVISIBLE
  }

  override fun changeDisplay(
      cardReaderResponse: CardReaderResponse,
      applicationSerialNumber: String?,
      finishActivity: Boolean?
  ) {
    val intent = Intent(this, ChargeResultActivity::class.java)
    intent.putExtra(ChargeResultActivity.IS_PERSONNALIZATION_RESULT, true)
    intent.putExtra(ChargeResultActivity.STATUS, cardReaderResponse.status.name)
    startActivity(intent)
    if (finishActivity == true) {
      finish()
    }
  }

  override fun onReaderEvent(event: CardReaderEvent?) {
    if (event?.type == CardReaderEvent.Type.CARD_INSERTED) {
      runOnUiThread { showNowPersonnalizingInformation() }
      GlobalScope.launch {
        remoteServiceExecution(
            selectedDeviceReaderName,
            AppSettings.aidEnums,
            ContactlessCardCommonProtocol.ISO_14443_4.name)
      }
    }
  }

  private suspend fun remoteServiceExecution(
      selectedDeviceReaderName: String,
      aidEnums: ArrayList<String>,
      protocol: String?
  ) {
    withContext(Dispatchers.IO) {
      try {
        val transactionManager =
            ticketingService.getTransactionManager(selectedDeviceReaderName, aidEnums, protocol)
        val cardIssuanceOutput =
            localServiceClient.executeRemoteService(
                "CARD_ISSUANCE",
                selectedDeviceReaderName,
                transactionManager.calypsoCard,
                null,
                CardIssuanceOutputDto::class.java)
        when (cardIssuanceOutput.statusCode) {
          0 -> {
            runOnUiThread {
              changeDisplay(
                  CardReaderResponse(Status.SUCCESS, "", 0, arrayListOf(), arrayListOf(), ""),
                  applicationSerialNumber =
                      HexUtil.toHex(transactionManager.calypsoCard.applicationSerialNumber),
                  finishActivity = true)
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
