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

import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.card.selection.CardResource;
import org.eclipse.keyple.core.card.selection.CardSelectionsResult;
import org.eclipse.keyple.core.card.selection.CardSelectionsService;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;

import static org.cna.keyple.demo.remote.server.util.CalypsoClassicInfo.AID;
import static org.eclipse.keyple.calypso.command.sam.SamRevision.C1;
import static org.eclipse.keyple.calypso.transaction.PoTransaction.SessionSetting.AccessLevel;

public final class CalypsoUtils {


  /**
   * Define the security parameters to provide when creating {@link
   * org.eclipse.keyple.calypso.transaction.PoTransaction}
   *
   * @param samResource sam resource to build Po Security from
   * @return PoSecuritySettings settings the set the security on the PO
   */
  public static PoSecuritySettings getSecuritySettings(CardResource<CalypsoSam> samResource) {

    // The default KIF values for personalization, loading and debiting
    final byte DEFAULT_KIF_PERSO = (byte) 0x21;
    final byte DEFAULT_KIF_LOAD = (byte) 0x27;
    final byte DEFAULT_KIF_DEBIT = (byte) 0x30;
    // The default key record number values for personalization, loading and debiting
    // The actual value should be adjusted.
    final byte DEFAULT_KEY_RECORD_NUMBER_PERSO = (byte) 0x01;
    final byte DEFAULT_KEY_RECORD_NUMBER_LOAD = (byte) 0x02;
    final byte DEFAULT_KEY_RECORD_NUMBER_DEBIT = (byte) 0x03;
    /* define the security parameters to provide when creating PoTransaction */
    return new PoSecuritySettings.PoSecuritySettingsBuilder(samResource)
        .sessionDefaultKif(AccessLevel.SESSION_LVL_PERSO, DEFAULT_KIF_PERSO)
        .sessionDefaultKif(AccessLevel.SESSION_LVL_LOAD, DEFAULT_KIF_LOAD)
        .sessionDefaultKif(AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KIF_DEBIT)
        .sessionDefaultKeyRecordNumber(
            AccessLevel.SESSION_LVL_PERSO, DEFAULT_KEY_RECORD_NUMBER_PERSO)
        .sessionDefaultKeyRecordNumber(AccessLevel.SESSION_LVL_LOAD, DEFAULT_KEY_RECORD_NUMBER_LOAD)
        .sessionDefaultKeyRecordNumber(
            AccessLevel.SESSION_LVL_DEBIT, DEFAULT_KEY_RECORD_NUMBER_DEBIT)
        .build();
  }
  /**
   * Operate the SAM selection
   *
   * @param samReader the reader where to operate the SAM selection
   * @return a CalypsoSam object if the selection succeed
   * @throws IllegalStateException if the selection fails
   */
  public static CalypsoSam selectSam(Reader samReader) {
    // Create a SAM resource after selecting the SAM
    CardSelectionsService samSelection = new CardSelectionsService();

    // Prepare selector
    samSelection.prepareSelection(
            new SamSelection(SamSelector.builder().samRevision(C1).serialNumber(".*").build()));

    if (!samReader.isCardPresent()) {
      throw new IllegalStateException("No SAM is present in the reader " + samReader.getName());
    }

    CardSelectionsResult cardSelectionsResult = samSelection.processExplicitSelections(samReader);

    if (!cardSelectionsResult.hasActiveSelection()) {
      throw new IllegalStateException("Unable to open a logical channel for SAM!");
    }

    return (CalypsoSam) cardSelectionsResult.getActiveSmartCard();
  }

  /**
   * Operate the PO selection
   *
   * @param poReader the reader where to operate the PO selection
   * @return a CalypsoPo object if the selection succeed
   * @throws IllegalStateException if the selection fails
   */
  public static CalypsoPo selectPo(Reader poReader) {

    // Check if a PO is present in the reader
    if (!poReader.isCardPresent()) {
      throw new IllegalStateException("No PO is present in the reader " + poReader.getName());
    }

    // Prepare a Calypso PO selection
    CardSelectionsService cardSelectionsService = new CardSelectionsService();

    // make the selection and read additional information afterwards
    PoSelection poSelection =
            new PoSelection(
                    PoSelector.builder()
                            .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                            .aidSelector(
                                    CardSelector.AidSelector.builder().aidToSelect(AID).build())
                            .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                            .build());

    // Add the selection case to the current selection
    cardSelectionsService.prepareSelection(poSelection);


    // Actual PO communication: operate through a single request the Calypso PO selection
    // and the file read

    return (CalypsoPo) cardSelectionsService.processExplicitSelections(poReader).getActiveSmartCard();
  }

  /**
   * Operate the PO selection
   *
   * @param poReader the reader where to operate the PO selection
   * @return a CalypsoPo object if the selection succeed
   * @throws IllegalStateException if the selection fails
   */
  static CalypsoPo selectPoWithEnvironment(Reader poReader) {

    // Check if a PO is present in the reader
    if (!poReader.isCardPresent()) {
      throw new IllegalStateException("No PO is present in the reader " + poReader.getName());
    }

    // Prepare a Calypso PO selection
    CardSelectionsService cardSelectionsService = new CardSelectionsService();

    // make the selection and read additional information afterwards
    PoSelection poSelection =
            new PoSelection(
                    PoSelector.builder()
                            .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                            .aidSelector(
                                    CardSelector.AidSelector.builder().aidToSelect(AID).build())
                            .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                            .build());


    // Prepare the reading of the environment file
    poSelection.prepareReadRecordFile(
            CalypsoClassicInfo.SFI_EnvironmentAndHolder, CalypsoClassicInfo.RECORD_NUMBER_1);

    // Add the selection case to the current selection
    cardSelectionsService.prepareSelection(poSelection);


    // Actual PO communication: operate through a single request the Calypso PO selection
    // and the file read

    return (CalypsoPo) cardSelectionsService.processExplicitSelections(poReader).getActiveSmartCard();
  }

}
