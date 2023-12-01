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
package org.calypsonet.keyple.demo.reload.remote.domain

import java.lang.IllegalStateException
import java.util.*
import javax.inject.Inject
import kotlin.jvm.Throws
import org.calypsonet.keyple.demo.common.constant.CardConstant
import org.calypsonet.keyple.demo.reload.remote.data.ReaderRepository
import org.calypsonet.keyple.demo.reload.remote.di.scopes.AppScoped
import org.eclipse.keyple.card.calypso.CalypsoExtensionService
import org.eclipse.keyple.core.service.SmartCardServiceProvider
import org.eclipse.keypop.calypso.card.card.CalypsoCard
import org.eclipse.keypop.calypso.card.transaction.FreeTransactionManager

@AppScoped
class TicketingService @Inject constructor(private var readerRepository: ReaderRepository) {

  /** Select card and retrieve CalypsoPO */
  @Throws(IllegalStateException::class, Exception::class)
  fun getTransactionManager(
      readerName: String,
      aidEnums: ArrayList<ByteArray>,
      protocol: String?
  ): FreeTransactionManager? {
    with(ReaderRepository.getReader(readerName)) {
      if (isCardPresent) {
        val smartCardService = SmartCardServiceProvider.getService()

        val readerApiFactory = smartCardService.readerApiFactory

        val reader = ReaderRepository.getReader(readerName)

        /** Get the generic card extension service */
        val calypsoExtension = CalypsoExtensionService.getInstance()

        /** Verify that the extension's API level is consistent with the current service. */
        smartCardService.checkCardExtension(calypsoExtension)

        val cardSelectionManager =
            smartCardService.getReaderApiFactory().createCardSelectionManager()

        aidEnums.forEach {
          /**
           * Generic selection: configures a CardSelector with all the desired attributes to make
           * the selection and read additional information afterwards
           */
          val cardSelector =
              if (protocol != null) {
                readerApiFactory
                    .createIsoCardSelector()
                    .filterByDfName(it)
                    .filterByCardProtocol(protocol)
              } else {
                readerApiFactory.createIsoCardSelector().filterByDfName(it)
              }
          cardSelectionManager.prepareSelection(
              cardSelector,
              calypsoExtension.calypsoCardApiFactory.createCalypsoCardSelectionExtension())
        }

        val selectionResult = cardSelectionManager.processCardSelectionScenario(reader)
        if (selectionResult.activeSmartCard != null) {
          val calypsoCard = selectionResult.activeSmartCard as CalypsoCard
          // check is the DF name is the expected one (Req. TL-SEL-AIDMATCH.1)
          if (!CardConstant.aidMatch(
              aidEnums[selectionResult.activeSelectionIndex], calypsoCard.dfName)) {
            throw IllegalStateException("Unexpected DF name")
          }
          return calypsoExtension.calypsoCardApiFactory.createFreeTransactionManager(
              reader, selectionResult.activeSmartCard as CalypsoCard)
        } else {
          throw IllegalStateException("Selection error: AID not found")
        }
      } else {
        throw Exception("Card is not present")
      }
    }
  }
}
