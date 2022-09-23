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
package org.calypsonet.keyple.demo.reload.remote.data

import kotlin.jvm.Throws
import org.calypsonet.terminal.reader.CardReader
import org.calypsonet.terminal.reader.ReaderCommunicationException
import org.eclipse.keyple.core.common.KeyplePluginExtensionFactory
import org.eclipse.keyple.core.service.ObservableReader
import org.eclipse.keyple.core.service.SmartCardServiceProvider
import timber.log.Timber

/**
 * Manager provided to encapsulate slight differences between readers provide methods to improve
 * code readability.
 */
object ReaderRepository {

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
}
