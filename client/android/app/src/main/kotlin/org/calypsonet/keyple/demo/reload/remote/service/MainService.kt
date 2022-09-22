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
package org.calypsonet.keyple.demo.reload.remote.service

import java.lang.IllegalStateException
import kotlin.jvm.Throws
import org.calypsonet.terminal.calypso.card.CalypsoCard
import org.calypsonet.terminal.calypso.transaction.CardTransactionManager
import org.calypsonet.terminal.reader.CardReader
import org.calypsonet.terminal.reader.ReaderCommunicationException
import org.eclipse.keyple.card.calypso.CalypsoExtensionService
import org.eclipse.keyple.core.common.KeyplePluginExtensionFactory
import org.eclipse.keyple.core.service.ObservableReader
import org.eclipse.keyple.core.service.SmartCardServiceProvider
import timber.log.Timber

/**
 * Manager provided to encapsulate slight differences between readers provide methods to improve
 * code readability.
 */
object MainService {

  // Aid to select
  var aidEnums = arrayListOf<String>()

  /** Register any keyple plugin */
  fun registerPlugin(factory: KeyplePluginExtensionFactory) {
    try {
      SmartCardServiceProvider.getService().registerPlugin(factory)
    } catch (e: Exception) {
      Timber.e(e)
    }
  }

  /** Un register any keyple plugin */
  fun unregisterPlugin(pluginName: String) {
    try {
      SmartCardServiceProvider.getService().unregisterPlugin(pluginName)
    } catch (e: Exception) {
      Timber.e(e)
    }
  }

  /** Retrieve a registered reader */
  @Throws(ReaderCommunicationException::class)
  fun getReader(readerName: String): CardReader {
    var reader: CardReader? = null
    SmartCardServiceProvider.getService().plugins.forEach { reader = it.getReader(readerName) }
    return reader ?: throw ReaderCommunicationException("$readerName not found")
  }

  /** Retrieve a registered observable reader. */
  @Throws(Exception::class)
  fun getObservableReader(readerName: String): ObservableReader {
    val reader = getReader(readerName)
    return if (reader is ObservableReader) reader else throw Exception("$readerName not found")
  }

  /** Select card and retrieve CalypsoPO */
  @Throws(IllegalStateException::class, Exception::class)
  fun getTransactionManager(
      readerName: String,
      aidEnums: ArrayList<String>,
      protocol: String?
  ): CardTransactionManager {
    with(getReader(readerName)) {
      if (isCardPresent) {
        val smartCardService = SmartCardServiceProvider.getService()

        val reader = getReader(readerName)

        /** Get the generic card extension service */
        val calypsoExtension = CalypsoExtensionService.getInstance()

        /** Verify that the extension's API level is consistent with the current service. */
        smartCardService.checkCardExtension(calypsoExtension)

        val cardSelectionManager = smartCardService.createCardSelectionManager()

        aidEnums.forEach {
          /**
           * Generic selection: configures a CardSelector with all the desired attributes to make
           * the selection and read additional information afterwards
           */
          val cardSelection =
              if (protocol != null) {
                calypsoExtension
                    .createCardSelection()
                    .filterByDfName(it)
                    .filterByCardProtocol(protocol)
              } else {
                calypsoExtension.createCardSelection().filterByDfName(it)
              }

          cardSelectionManager.prepareSelection(cardSelection)
        }

        val selectionResult = cardSelectionManager.processCardSelectionScenario(reader)
        if (selectionResult.activeSmartCard != null) {
          return calypsoExtension.createCardTransactionWithoutSecurity(
              reader, selectionResult.activeSmartCard as CalypsoCard)
        } else {
          throw ReaderCommunicationException("Card app not found")
        }
      } else {
        throw Exception("Card is not present")
      }
    }
  }
}
