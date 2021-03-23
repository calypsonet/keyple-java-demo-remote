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

import java.lang.IllegalStateException
import kotlin.jvm.Throws
import org.eclipse.keyple.calypso.transaction.CalypsoPo
import org.eclipse.keyple.calypso.transaction.PoSelection
import org.eclipse.keyple.calypso.transaction.PoSelector
import org.eclipse.keyple.core.card.selection.CardSelectionsService
import org.eclipse.keyple.core.card.selection.CardSelector
import org.eclipse.keyple.core.service.PluginFactory
import org.eclipse.keyple.core.service.Reader
import org.eclipse.keyple.core.service.SmartCardService
import org.eclipse.keyple.core.service.event.ObservableReader
import org.eclipse.keyple.core.service.exception.KeypleAllocationNoReaderException
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException

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
    public fun registerPlugin(factory: PluginFactory) {
        SmartCardService.getInstance().registerPlugin(factory)
    }

    /**
     * Un register any keyple plugin
     */
    public fun unregisterPlugin(pluginName: String) {
        SmartCardService.getInstance().unregisterPlugin(pluginName)
    }

    /**
     * Retrieve a registered reader
     */
    @Throws(KeypleAllocationNoReaderException::class)
    public fun getReader(readerName: String): Reader {
        var reader: Reader? = null
        SmartCardService.getInstance().plugins.forEach {
            try {
                reader = it.value.getReader(readerName)
            } catch (e: KeypleReaderNotFoundException) {
                if (readerName == OMAPI_SIM_READER_NAME) {
                    try {
                        reader = it.value.getReader(OMAPI_SIM_1_READER_NAME)
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
    public fun getCalypsoPo(readerName: String, aid: String, protocol: String?): CalypsoPo {
        with(getReader(readerName)) {
            if (isCardPresent) {
                val cardSelectionService = CardSelectionsService()
                // cardSelectionService.prepareReleaseChannel()
                val poSelection = PoSelection(
                    PoSelector
                        .builder()
                        .cardProtocol(protocol)
                        .aidSelector(
                            CardSelector.AidSelector.builder()
                                .aidToSelect(aid) // Set the AID of your Calypso PO
                                .build()
                        )
                        .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                        .build())
                cardSelectionService.prepareSelection(poSelection)
                val selectionResult = cardSelectionService.processExplicitSelections(this)
                if (selectionResult.hasActiveSelection()) {
                    return selectionResult.activeSmartCard as CalypsoPo
                } else {
                    throw KeypleReaderIOException("Card is not present")
                }
            } else {
                throw IllegalStateException()
            }
        }
    }

    enum class AidEnum(val aid: String) {
        CDLIGHT_GTML("315449432E49434131"),
        HOPLINK("A000000291A000000191"),
        NAVIGO2013("A00000040401250901"),
        STORED_VALUE("304554502E494341"),
        INTERCODE_22("315449432E49434132"),
    }
}
