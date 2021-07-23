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
package org.eclipse.keyple.demo.remote.manager

import org.calypsonet.terminal.calypso.card.CalypsoCard
import org.calypsonet.terminal.calypso.transaction.CardTransactionManager
import org.eclipse.keyple.card.calypso.CalypsoExtensionService
import java.lang.IllegalStateException
import kotlin.jvm.Throws
import org.eclipse.keyple.core.common.KeyplePluginExtensionFactory
import org.eclipse.keyple.core.service.Reader
import org.eclipse.keyple.core.service.SmartCardServiceProvider
import org.eclipse.keyple.core.service.event.ObservableReader
import org.eclipse.keyple.core.service.exception.KeypleAllocationNoReaderException
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException
import timber.log.Timber

/**
 * Manager provided to encapsulate slight differences
 * between readers provide methods to improve code readability.
 */
object KeypleManager {

    var aidEnum = AidEnum.CDLIGHT_GTML
    const val OMAPI_SIM_READER_NAME = "SIM"

    // On devices with multi sim, native SIM reader can be identified by SIM1 on the device.
    private const val OMAPI_SIM_1_READER_NAME = "SIM1"

    /**
     * Register any keyple plugin
     */
    public fun registerPlugin(factory: KeyplePluginExtensionFactory) {
        try {
            SmartCardServiceProvider.getService().registerPlugin(factory)
        }catch (e: Exception){
            Timber.e(e)
        }
    }

    /**
     * Un register any keyple plugin
     */
    public fun unregisterPlugin(pluginName: String) {
        try {
            SmartCardServiceProvider.getService().unregisterPlugin(pluginName)
        }catch (e: Exception){
            Timber.e(e)
        }
    }

    /**
     * Retrieve a registered reader
     */
    @Throws(KeypleAllocationNoReaderException::class)
    public fun getReader(readerName: String): Reader {
        var reader: Reader? = null
        SmartCardServiceProvider.getService().plugins.forEach {
            try {
                reader = it.getReader(readerName)
            } catch (e: KeypleReaderNotFoundException) {
                if (readerName == OMAPI_SIM_READER_NAME) {
                    try {
                        reader = it.getReader(OMAPI_SIM_1_READER_NAME)
                    } catch (e: KeypleReaderNotFoundException) { }
                }
            }
        }
        return reader ?: throw KeypleReaderNotFoundException("$readerName not found")
    }

    /**
     * Retrieve a registered observable reader.
     */
    @Throws(KeypleAllocationNoReaderException::class)
    public fun getObservableReader(readerName: String): ObservableReader {
        val reader = getReader(readerName)
        return if (reader is ObservableReader) reader else throw KeypleReaderNotFoundException("$readerName not found")
    }

    /**
     * Select card and retrieve CalypsoPO
     */
    @Throws(IllegalStateException::class, KeypleReaderIOException::class, KeypleAllocationNoReaderException::class)
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
                val cardSelection = calypsoExtension
                    .createCardSelection()
                    .filterByDfName(aid)
                    .filterByCardProtocol(protocol)

                val cardSelectionManager = smartCardService.createCardSelectionManager()
                cardSelectionManager.prepareSelection(cardSelection)

                val selectionResult = cardSelectionManager.processCardSelectionScenario(reader)
                if (selectionResult.activeSmartCard != null) {
                    return calypsoExtension.createCardTransactionWithoutSecurity(reader, selectionResult.activeSmartCard as CalypsoCard)
                } else {
                    throw KeypleReaderIOException("Card app not found")
                }
            } else {
                throw KeypleReaderIOException("Card is not present")
            }
        }
    }

    enum class AidEnum(val aid: String) {
        CDLIGHT_GTML("315449432E49434131"),
        INTERCODE_22("315449432E49434132"),
        CALYPSO_LIGHT_CL("315449432E49434133"),
        HOPLINK("A000000291A000000191"),
        NAVIGO2013("A00000040401250901"),
        STORED_VALUE("304554502E494341"),
    }
}
