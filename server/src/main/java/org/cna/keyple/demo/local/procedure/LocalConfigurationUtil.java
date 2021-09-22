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
package org.cna.keyple.demo.local.procedure;

import org.cna.keyple.demo.distributed.server.util.ConfigurationUtil;
import org.eclipse.keyple.core.service.ConfigurableReader;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.util.protocol.ContactlessCardCommonProtocol;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.plugin.pcsc.PcscSupportedContactlessProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** PCSC Reader Utilities to read properties file and differentiate SAM and PO reader */
public final class LocalConfigurationUtil {

  private static final Logger logger = LoggerFactory.getLogger(LocalConfigurationUtil.class);

  static public Reader initReader(String poReaderFilter) {

    Reader reader = ConfigurationUtil.getReaderByPattern(poReaderFilter);

    if(reader==null){
      return null;
    }

    // Get and configure the PO reader
    reader.getExtension(PcscReader.class).setContactless(true).setIsoProtocol(PcscReader.IsoProtocol.T1);

    ((ConfigurableReader) reader)
            .activateProtocol(
                    PcscSupportedContactlessProtocol.ISO_14443_4.name(),
                    ContactlessCardCommonProtocol.ISO_14443_4.name());

    logger.info("PO Reader configured : {}", reader.getName());
    return reader;

  }

}
