/* **************************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.cna.keyple.demo.distributed.server.util;

import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.service.util.ContactCardCommonProtocols;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.plugin.pcsc.PcscSupportedContactProtocols;
import org.eclipse.keyple.plugin.pcsc.PcscSupportedContactlessProtocols;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.regex.Pattern;

/** PCSC Reader Utilities to read properties file and differentiate SAM and PO reader */
public final class PcscReaderUtils {
  private static final Logger logger = LoggerFactory.getLogger(PcscReaderUtils.class);

  /*
   * Get the terminal which names match the expected pattern
   *
   * @param pattern Pattern
   * @return Reader
   * @throws KeypleReaderException the reader is not found or readers are not initialized
   */
  public static Reader getReaderByPattern(String pattern) {
    Pattern p = Pattern.compile(pattern);
    Collection<Plugin> plugins = SmartCardService.getInstance().getPlugins().values();
    for (Plugin plugin : plugins) {
      Collection<Reader> readers = plugin.getReaders().values();
      for (Reader reader : readers) {
        if (p.matcher(reader.getName()).matches()) {
          return reader;
        }
      }
    }
    throw new KeypleReaderNotFoundException("Reader name pattern: " + pattern);
  }

  static public Reader initPoReader(String poReaderFilter) {

    Reader reader = PcscReaderUtils.getReaderByPattern(poReaderFilter);

    // Get and configure the PO reader
    ((PcscReader) reader).setContactless(true).setIsoProtocol(PcscReader.IsoProtocol.T1);

    // activate protocols
    reader.activateProtocol(
            PcscSupportedContactlessProtocols.ISO_14443_4.name(),
            ContactlessCardCommonProtocols.ISO_14443_4.name());

    logger.info("PO Reader configured : {}", reader.getName());
    return reader;

  }

  public static Reader initSamReader(String samReaderFilter) {
    logger.info("Initialize card reader for SAM with filter :{}", samReaderFilter);

    Reader reader = PcscReaderUtils.getReaderByPattern(samReaderFilter);

    ((PcscReader) reader).setContactless(false).setIsoProtocol(PcscReader.IsoProtocol.T0);

    ((PcscReader) reader).setSharingMode(PcscReader.SharingMode.SHARED);

    reader.activateProtocol(
            PcscSupportedContactProtocols.ISO_7816_3.name(),
            ContactCardCommonProtocols.ISO_7816_3.name());


    logger.info("SAM Reader configured : {}", reader.getName());
    return reader;
  }
}
