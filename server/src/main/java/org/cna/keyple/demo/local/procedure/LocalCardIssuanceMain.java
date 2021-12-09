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
package org.cna.keyple.demo.local.procedure;

import org.calypsonet.terminal.calypso.card.CalypsoCard;
import org.calypsonet.terminal.calypso.card.ElementaryFile;
import org.calypsonet.terminal.reader.selection.CardSelectionManager;
import org.calypsonet.terminal.reader.selection.CardSelectionResult;
import org.cna.keyple.demo.distributed.server.calypso.CalypsoCardController;
import org.cna.keyple.demo.distributed.server.plugin.SamResourceConfiguration;
import org.cna.keyple.demo.distributed.server.util.CalypsoConstants;
import org.cna.keyple.demo.distributed.server.util.CalypsoUtils;
import org.cna.keyple.demo.sale.data.model.EnvironmentHolderStructureDto;
import org.cna.keyple.demo.sale.data.model.parser.EnvironmentHolderStructureParser;
import org.eclipse.keyple.card.calypso.CalypsoExtensionService;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.core.service.resource.CardResource;
import org.eclipse.keyple.core.service.resource.CardResourceServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Execute locally a card issuance */
public class LocalCardIssuanceMain {

  private static final Logger logger = LoggerFactory.getLogger(LocalCardIssuanceMain.class);
  public static String calypsoCardReaderFilter = ".*(ASK|ACS).*";
  public static String samReaderFilter = ".*(Cherry TC|SCM Microsystems|Identive|HID|Generic).*";

  public static void main(String[] args) {
    /*
     * Init the sam reader
     */
    SamResourceConfiguration samResourceConfiguration =
        new SamResourceConfiguration(samReaderFilter);
    samResourceConfiguration.init();

    /*
     * Init calypso card reader
     */
    Reader calypsoCardReader = LocalConfigurationUtil.initReader(calypsoCardReaderFilter);

    /*
     * Select cards
     */
    CalypsoCard calypsoCard = CalypsoUtils.selectCardWithEnvironment(calypsoCardReader);

    CardResource samResource =
        CardResourceServiceProvider.getService().getCardResource(CalypsoConstants.SAM_PROFILE_NAME);

    /*
     * Reset environment file
     */
    CalypsoCardController calypsoCardController =
        CalypsoCardController.newBuilder()
            .withCalypsoCard(calypsoCard)
            .withCardReader(calypsoCardReader)
            .withSamResource(samResource)
            .build();
    calypsoCardController.initCard();
    logger.info(calypsoCardController.readCard().toString());
    logger.info("Is card init : {}", verifyEnvironmentFile(calypsoCardReader));
    CardResourceServiceProvider.getService().releaseCardResource(samResource);
  }

  /**
   * Verify that the environment file of the card is valid
   *
   * @return true if card is init
   */
  public static Boolean verifyEnvironmentFile(Reader cardReader) {

    // Get the instance of the SmartCardService (singleton pattern)
    SmartCardService smartCardService = SmartCardServiceProvider.getService();

    // Get the Calypso card extension service
    CalypsoExtensionService cardExtension = CalypsoExtensionService.getInstance();

    logger.info("= #### Select application with AID = '{}'.", CalypsoConstants.AID_CALYPSO_PRIME);

    // Get the core card selection manager.
    CardSelectionManager cardSelectionManager = smartCardService.createCardSelectionManager();

    // Create a card selection using the Calypso card extension.
    // Prepare the selection by adding the created Calypso card selection to the card selection
    // scenario.
    cardSelectionManager.prepareSelection(
        cardExtension
            .createCardSelection()
            .filterByDfName(CalypsoConstants.AID_CALYPSO_PRIME)
            .acceptInvalidatedCard()
            .prepareReadRecordFile(
                CalypsoConstants.SFI_ENVIRONMENT_AND_HOLDER, CalypsoConstants.RECORD_NUMBER_1));

    // Actual card communication: run the selection scenario.
    CardSelectionResult selectionResult =
        cardSelectionManager.processCardSelectionScenario(cardReader);

    // Check the selection result.
    if (selectionResult.getActiveSmartCard() == null) {
      throw new IllegalStateException(
          "The selection of the application '" + CalypsoConstants.AID_CALYPSO_PRIME + "' failed.");
    }

    // Get the SmartCard resulting of the selection.
    CalypsoCard calypsoCard = (CalypsoCard) selectionResult.getActiveSmartCard();

    ElementaryFile sfiEnv = calypsoCard.getFileBySfi(CalypsoConstants.SFI_ENVIRONMENT_AND_HOLDER);

    logger.info(
        "File SFI {}h, rec 1: FILE_CONTENT = {}",
        String.format("%02X", CalypsoConstants.SFI_ENVIRONMENT_AND_HOLDER),
        sfiEnv);

    EnvironmentHolderStructureDto environmentAndHolder =
        EnvironmentHolderStructureParser.parse(sfiEnv.getData().getContent());

    // Log the result
    logger.info("EnvironmentAndHolder file data: {}", environmentAndHolder);

    return environmentAndHolder.equals(CalypsoCardController.getEnvironmentInit());
  }
}
