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
package org.cna.keyple.demo.remote.server.util;

import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;

import java.util.Collection;
import java.util.regex.Pattern;

/** PCSC Reader Utilities to read properties file and differentiate SAM and PO reader */
public final class PcscReaderUtils {
  /*
   * Get the terminal which names match the expected pattern
   *
   * @param pattern Pattern
   * @return Reader
   * @throws KeypleReaderException the reader is not found or readers are not initialized
   */
  static Reader getReaderByPattern(String pattern) {
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
}
