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
public final class CalypsoClassicInfo {
  /** AID: Keyple test kit profile 1, Application 2 */
  public static final String AID = "315449432E49434131";
  /// ** 1TIC.ICA AID */
  // public static final String AID = "315449432E494341";
  /** SAM C1 regular expression: platform, version and serial number values are ignored */
  public static final String SAM_C1_ATR_REGEX =
      "3B3F9600805A[0-9a-fA-F]{2}80C1[0-9a-fA-F]{14}829000";

  public static final String ATR_REV1_REGEX = "3B8F8001805A0A0103200311........829000..";

  public static final byte RECORD_NUMBER_1 = 1;
  public static final byte RECORD_NUMBER_2 = 2;
  public static final byte RECORD_NUMBER_3 = 3;
  public static final byte RECORD_NUMBER_4 = 4;

  public static final byte SFI_EnvironmentAndHolder = (byte) 0x07;
  public static final byte SFI_EventLog = (byte) 0x08;
  public static final byte SFI_Contracts = (byte) 0x09;
  public static final byte SFI_Counters = (byte) 0x19;

  public static final byte SFI_Counters_1 = (byte) 0x0A;
  public static final byte SFI_Counters_2 = (byte) 0x0B;
  public static final byte SFI_Counters_3 = (byte) 0x0C;
  public static final byte SFI_Counters_4 = (byte) 0x0D;

  public static final List<Byte> SFI_Counters_simulated = Arrays.asList(
          SFI_Counters_1,
          SFI_Counters_2,
          SFI_Counters_3,
          SFI_Counters_4);

  private CalypsoClassicInfo() {}
}
