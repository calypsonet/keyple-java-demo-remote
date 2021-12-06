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

import static org.cna.keyple.demo.distributed.server.util.CalypsoConstants.*;

import org.calypsonet.terminal.calypso.card.CalypsoCard;
import org.calypsonet.terminal.reader.selection.CardSelectionManager;
import org.calypsonet.terminal.reader.selection.CardSelectionResult;
import org.eclipse.keyple.card.calypso.CalypsoExtensionService;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.core.util.Assert;

public final class CalypsoUtils {

  /**
   * Operate the Calypso Card selection
   *
   * @param cardReader the reader where to operate the Calypso Card selection
   * @return a CalypsoCard object if the selection succeed
   * @throws IllegalStateException if the selection fails
   */
  public static CalypsoCard selectCard(Reader cardReader) {
    // Get the instance of the SmartCardService (singleton pattern)
    SmartCardService smartCardService = SmartCardServiceProvider.getService();

    // Get the Calypso card extension service
    CalypsoExtensionService cardExtension = CalypsoExtensionService.getInstance();

    // Get the core card selection manager.
    CardSelectionManager cardSelectionManager = smartCardService.createCardSelectionManager();

    // Create a card selection using the Calypso card extension.
    // Prepare the selection by adding the created Calypso card selection to the card selection
    // scenario.
    cardSelectionManager.prepareSelection(
        cardExtension
            .createCardSelection()
            .acceptInvalidatedCard()
            .filterByDfName(AID_CALYPSO_PRIME));

    cardSelectionManager.prepareSelection(
        cardExtension
            .createCardSelection()
            .acceptInvalidatedCard()
            .filterByDfName(AID_CALYPSO_LIGHT));

    cardSelectionManager.prepareSelection(
        cardExtension
            .createCardSelection()
            .acceptInvalidatedCard()
            .filterByDfName(AID_NORMALIZED_IDF));

    // Actual card communication: run the selection scenario.
    CardSelectionResult selectionResult =
        cardSelectionManager.processCardSelectionScenario(cardReader);

    // Check the selection result.
    if (selectionResult.getActiveSmartCard() == null) {
      throw new IllegalStateException(
          "The selection of the application " + AID_CALYPSO_PRIME + " failed.");
    }

    // Get the SmartCard resulting of the selection.
    return (CalypsoCard) selectionResult.getActiveSmartCard();
  }

  /**
   * Operate the Calypso Card selection with the read of the environment file
   *
   * @param cardReader the reader where to operate the Calypso Card selection
   * @return a CalypsoCard object if the selection succeed
   * @throws IllegalStateException if the selection fails
   */
  public static CalypsoCard selectCardWithEnvironment(Reader cardReader) {
    // Get the instance of the SmartCardService (singleton pattern)
    SmartCardService smartCardService = SmartCardServiceProvider.getService();

    // Get the Calypso card extension service
    CalypsoExtensionService cardExtension = CalypsoExtensionService.getInstance();

    // Get the core card selection manager.
    CardSelectionManager cardSelectionManager = smartCardService.createCardSelectionManager();

    // Create a card selection using the Calypso card extension.
    // Prepare the selection by adding the created Calypso card selection to the card selection
    // scenario.
    cardSelectionManager.prepareSelection(
        cardExtension
            .createCardSelection()
            .filterByDfName(AID_CALYPSO_PRIME)
            .acceptInvalidatedCard()
            .prepareReadRecordFile(SFI_ENVIRONMENT_AND_HOLDER, RECORD_NUMBER_1));

    cardSelectionManager.prepareSelection(
        cardExtension
            .createCardSelection()
            .filterByDfName(AID_CALYPSO_LIGHT)
            .acceptInvalidatedCard()
            .prepareReadRecordFile(SFI_ENVIRONMENT_AND_HOLDER, RECORD_NUMBER_1));

     cardSelectionManager.prepareSelection(
        cardExtension
            .createCardSelection()
            .filterByDfName(AID_NORMALIZED_IDF)
            .acceptInvalidatedCard()
            .prepareReadRecordFile(SFI_ENVIRONMENT_AND_HOLDER, RECORD_NUMBER_1));

    // Actual card communication: run the selection scenario.
    CardSelectionResult selectionResult =
        cardSelectionManager.processCardSelectionScenario(cardReader);

    // Check the selection result.
    if (selectionResult.getActiveSmartCard() == null) {
      throw new IllegalStateException(
          "The selection of the application " + AID_CALYPSO_PRIME + " failed.");
    }

    // Get the SmartCard resulting of the selection.
    return (CalypsoCard) selectionResult.getActiveSmartCard();
  }

  /**
   * Determine how much contracts contains the card based on the application subtype. It is used to differentiate calypso prime from calypso light.
   * @param calypsoCard not nullable instance of a calypso card
   * @return 2 or 4 contracts based on the application subtype
   */
  public static int getContractCount(CalypsoCard calypsoCard) {
    Assert.getInstance().notNull(calypsoCard, "calypsoCard");
    return calypsoCard.getApplicationSubtype() == 50 ? 2 : 4;
  }
}
