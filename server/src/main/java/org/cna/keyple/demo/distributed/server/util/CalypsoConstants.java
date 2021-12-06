/* **************************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://calypsonet.org/
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

import java.util.Arrays;
import java.util.List;

/**
 * Helper class to provide specific elements to handle Calypso cards.
 *
 * <ul>
 *   <li>AID application selection (default Calypso AID)
 *   <li>SAM_C1_ATR_REGEX regular expression matching the expected C1 SAM ATR
 *   <li>Files infos (SFI, rec number, etc) for
 *       <ul>
 *         <li>Environment and Holder
 *         <li>Event Log
 *         <li>Contract List
 *         <li>Contracts
 *       </ul>
 * </ul>
 */
public final class CalypsoConstants {
  // Application
  /** AID: Keyple test kit profile 1, Application 2 */
  public static final String AID_CALYPSO_PRIME = "315449432E49434131";

  public static final String AID_CALYPSO_LIGHT = "315449432E49434133";

  public static final String AID_NORMALIZED_IDF = "A0000004040125090101";

  public static final byte RECORD_NUMBER_1 = 1;
  public static final byte RECORD_NUMBER_2 = 2;
  public static final byte RECORD_NUMBER_3 = 3;
  public static final byte RECORD_NUMBER_4 = 4;

  // File identifiers
  public static final byte SFI_ENVIRONMENT_AND_HOLDER = (byte) 0x07;
  public static final byte SFI_EVENT_LOG = (byte) 0x08;
  public static final byte SFI_CONTRACTS = (byte) 0x09;

  public static final byte SFI_COUNTERS = (byte) 0x19;

  // Security settings
  public static final String SAM_PROFILE_NAME = "SAM C1";

  private CalypsoConstants() {}
}
