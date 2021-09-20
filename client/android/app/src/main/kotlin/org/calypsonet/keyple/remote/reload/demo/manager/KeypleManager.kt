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
package org.calypsonet.keyple.remote.reload.demo.manager

import java.lang.IllegalStateException
import kotlin.jvm.Throws
import org.calypsonet.terminal.calypso.card.CalypsoCard
import org.calypsonet.terminal.calypso.transaction.CardTransactionManager
import org.calypsonet.terminal.reader.CardReader
import org.calypsonet.terminal.reader.ConfigurableCardReader
import org.calypsonet.terminal.reader.ReaderCommunicationException
import org.eclipse.keyple.card.calypso.CalypsoExtensionService
import org.eclipse.keyple.core.common.KeyplePluginExtensionFactory
import org.eclipse.keyple.core.plugin.ReaderIOException
import org.eclipse.keyple.core.service.ObservableReader
import org.eclipse.keyple.core.service.SmartCardServiceProvider
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiReader
import timber.log.Timber

/**
 * Manager provided to encapsulate slight differences
 * between readers provide methods to improve code readability.
 */
object KeypleManager {

    var aidEnum = AidEnum.CALYPSO

    /**
     * Register any keyple plugin
     */
    public fun registerPlugin(factory: KeyplePluginExtensionFactory) {
        try {
            SmartCardServiceProvider.getService().registerPlugin(factory)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    /**
     * Un register any keyple plugin
     */
    public fun unregisterPlugin(pluginName: String) {
        try {
            SmartCardServiceProvider.getService().unregisterPlugin(pluginName)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    /**
     * Retrieve a registered reader
     */
    @Throws(ReaderIOException::class)
    public fun getReader(readerName: String): CardReader {
        var reader: CardReader? = null
        SmartCardServiceProvider.getService().plugins.forEach {
            try {
                reader = it.getReader(readerName)
            } catch (e: ReaderIOException) { }
        }
        return reader ?: throw ReaderIOException("$readerName not found")
    }

    /**
     * Retrieve a registered observable reader.
     */
    @Throws(ReaderIOException::class)
    public fun getObservableReader(readerName: String): ObservableReader {
        val reader = getReader(readerName)
        return if (reader is ObservableReader) reader else throw Exception("$readerName not found")
    }

    /**
     * Select card and retrieve CalypsoPO
     */
    @Throws(IllegalStateException::class, ReaderIOException::class)
    public fun getTransactionManager(readerName: String, aid: String, protocol: String?): CardTransactionManager {
        with(getReader(readerName)) {
            if (isCardPresent) {
                val smartCardService = SmartCardServiceProvider.getService()

                val reader = getReader(readerName)

                /**
                 * Get the generic card extension service
                 */
                val calypsoExtension = CalypsoExtensionService.getInstance()

                /**
                 * Verify that the extension's API level is consistent with the current service.
                 */
                smartCardService.checkCardExtension(calypsoExtension)

                /**
                 * Generic selection: configures a CardSelector with all the desired attributes to make
                 * the selection and read additional information afterwards
                 */
                val cardSelection =
                    if(protocol != null){
                        calypsoExtension
                            .createCardSelection()
                            .filterByDfName(aid)
                            .filterByCardProtocol(protocol)
                    }else{
                        calypsoExtension
                            .createCardSelection()
                            .filterByDfName(aid)
                    }

                val cardSelectionManager = smartCardService.createCardSelectionManager()
                cardSelectionManager.prepareSelection(cardSelection)

                val selectionResult = cardSelectionManager.processCardSelectionScenario(reader)
                if (selectionResult.activeSmartCard != null) {
                    return calypsoExtension.createCardTransactionWithoutSecurity(reader, selectionResult.activeSmartCard as CalypsoCard)
                } else {
                    throw ReaderCommunicationException("Card app not found")
                }
            } else {
                throw ReaderIOException("Card is not present")
            }
        }
    }

    enum class AidEnum(val aid: String) {
        CALYPSO("315449432E494341"),
        CDLIGHT_GTML("315449432E49434131"),
        INTERCODE_22("315449432E49434132"),
        CALYPSO_LIGHT_CL("315449432E49434133"),
        HOPLINK("A000000291A000000191"),
        NAVIGO2013("A00000040401250901")
    }
}
