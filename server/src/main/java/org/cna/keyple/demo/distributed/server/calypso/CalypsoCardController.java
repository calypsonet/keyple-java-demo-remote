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
package org.cna.keyple.demo.distributed.server.calypso;

import static org.cna.keyple.demo.distributed.server.util.CalypsoConstants.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import org.calypsonet.keyple.demo.common.constant.CardConstant;
import org.calypsonet.keyple.demo.common.model.ContractStructure;
import org.calypsonet.keyple.demo.common.model.EnvironmentHolderStructure;
import org.calypsonet.keyple.demo.common.model.EventStructure;
import org.calypsonet.keyple.demo.common.model.type.DateCompact;
import org.calypsonet.keyple.demo.common.model.type.VersionNumber;
import org.calypsonet.keyple.demo.common.parser.ContractStructureParser;
import org.calypsonet.keyple.demo.common.parser.EnvironmentHolderStructureParser;
import org.calypsonet.keyple.demo.common.parser.EventStructureParser;
import org.calypsonet.terminal.calypso.WriteAccessLevel;
import org.calypsonet.terminal.calypso.card.CalypsoCard;
import org.calypsonet.terminal.calypso.sam.CalypsoSam;
import org.calypsonet.terminal.calypso.transaction.CardSecuritySetting;
import org.calypsonet.terminal.calypso.transaction.CardTransactionManager;
import org.cna.keyple.demo.distributed.server.util.CalypsoConstants;
import org.cna.keyple.demo.distributed.server.util.CalypsoUtils;
import org.eclipse.keyple.card.calypso.CalypsoExtensionService;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.resource.CardResource;
import org.eclipse.keyple.core.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Perform operations with a Calypso Card inserted in a Reader. It requires a {@link CalypsoSam} to
 * perform secured operations
 */
public class CalypsoCardController {

  private static final Logger logger = LoggerFactory.getLogger(CalypsoCardController.class);
  private final CalypsoCard calypsoCard;
  private final Reader cardReader;
  private final CardResource samResource;
  private final String pluginType;

  private static final String PLUGIN_TYPE_ANDROID_OMAPI = "Android OMAPI";

  public static Builder newBuilder() {
    return new Builder();
  }

  /** static Builder class */
  public static final class Builder {
    private CalypsoCard calypsoCard;
    private Reader calypsoCardReader;
    private CardResource samResource;
    private String pluginType;

    /**
     * Specify the calypso Card to which the operation will be executed
     *
     * @param calypsoCard non null instance of a calypso smart card object
     * @return next step of configuration
     */
    public Builder withCalypsoCard(CalypsoCard calypsoCard) {
      Assert.getInstance().notNull(calypsoCard, "calypsoCard");
      this.calypsoCard = calypsoCard;
      return this;
    }

    /**
     * Specify the reader where the calypso card is inserted
     *
     * @param calypsoCardReader non null instance of a reader
     * @return next step of configuration
     */
    public Builder withCardReader(Reader calypsoCardReader) {
      Assert.getInstance().notNull(calypsoCardReader, "calypsoCardReader");
      this.calypsoCardReader = calypsoCardReader;
      return this;
    }
    /**
     * Specify the calypso sam resource
     *
     * @param samResource non null instance of a reader
     * @return next step of configuration
     */
    public Builder withSamResource(CardResource samResource) {
      Assert.getInstance().notNull(samResource, "samResource");
      this.samResource = samResource;
      return this;
    }

    /**
     * Specify the plugin type
     *
     * @param pluginType non null instance of a reader
     * @return next step of configuration
     */
    public Builder withPluginType(String pluginType) {
      Assert.getInstance().notNull(samResource, "pluginType");
      this.pluginType = pluginType;
      return this;
    }

    public CalypsoCardController build() {
      return new CalypsoCardController(calypsoCard, calypsoCardReader, samResource, pluginType);
    }
  }

  /**
   * (private) Build this controller with the calypso resource you aim to read.
   *
   * @param calypsoCard selected smart card
   * @param cardReader reader the smart card is inserted into
   */
  private CalypsoCardController(
      CalypsoCard calypsoCard, Reader cardReader, CardResource samResource, String pluginType) {
    this.calypsoCard = calypsoCard;
    this.cardReader = cardReader;
    this.samResource = samResource;
    this.pluginType = pluginType;
  }

  /**
   * (public) Read all files on the Calypso Card inserted in the Reader
   *
   * @return card content
   */
  public CalypsoCardRepresentation readCard() {
    // Create the card transaction manager
    CardTransactionManager cardTransaction;

    // Get the Calypso card extension service
    CalypsoExtensionService cardExtension = CalypsoExtensionService.getInstance();

    CardSecuritySetting cardSecuritySetting =
        CalypsoExtensionService.getInstance()
            .createCardSecuritySetting()
            .setSamResource(samResource.getReader(), (CalypsoSam) samResource.getSmartCard());

    // create a secured card transaction
    cardTransaction =
        cardExtension.createCardTransaction(cardReader, calypsoCard, cardSecuritySetting);

    if (PLUGIN_TYPE_ANDROID_OMAPI.equals(pluginType)) {
      // For OMAPI Reader we release the channel after reading
      cardTransaction.prepareReleaseCardChannel();
    }

    int contractCount = CalypsoUtils.getContractCount(calypsoCard);

    /*
     * Open Calypso session
     */
    logger.info("Open Calypso Session...");
    cardTransaction.processOpening(WriteAccessLevel.LOAD);

    // Prepare reading of environment record
    cardTransaction.prepareReadRecordFile(SFI_ENVIRONMENT_AND_HOLDER, RECORD_NUMBER_1);
    // Prepare reading of last event record
    cardTransaction.prepareReadRecordFile(
        CalypsoConstants.SFI_EVENT_LOG, CalypsoConstants.RECORD_NUMBER_1);

    // Prepare reading of contract records (Calypso Light)
    cardTransaction.prepareReadRecordFile(
        SFI_CONTRACTS,
        CalypsoConstants.RECORD_NUMBER_1,
        contractCount,
        CardConstant.CONTRACT_RECORD_SIZE_BYTES);

    // Prepare reading of counter record
    cardTransaction.prepareReadCounterFile(SFI_COUNTERS, CalypsoConstants.RECORD_NUMBER_4);
    /*
     */
    logger.info("Read Card...");

    cardTransaction.processCardCommands();

    /*
     * Close Calypso session
     */
    cardTransaction.processClosing();

    logger.info("Calypso Session Closed.");
    return CalypsoCardRepresentation.parse(calypsoCard);
    // return null;
  }

  /**
   * Write the card content into the inserted card. Only updated-marked files will be physically
   * updated.
   *
   * @param calypsoCardContent updated content to be written
   * @return status code
   */
  public int writeCard(CalypsoCardRepresentation calypsoCardContent) {
    // Create the card transaction manager
    CardTransactionManager cardTransaction;

    // Get the Calypso card extension service
    CalypsoExtensionService cardExtension = CalypsoExtensionService.getInstance();

    // Create security settings that reference the same SAM profile requested from the card resource
    // service, specifying the key ciphering key parameters.
    CardSecuritySetting cardSecuritySetting =
        CalypsoExtensionService.getInstance()
            .createCardSecuritySetting()
            .setSamResource(samResource.getReader(), (CalypsoSam) samResource.getSmartCard());

    // create a secured card transaction
    cardTransaction =
        cardExtension.createCardTransaction(cardReader, calypsoCard, cardSecuritySetting);

    if (PLUGIN_TYPE_ANDROID_OMAPI.equals(pluginType)) {
      // For OMAPI Reader we release the channel after reading
      cardTransaction.prepareReleaseCardChannel();
    }

    /* Open Calypso session */
    logger.info("Open Calypso Session - SESSION_LVL_LOAD...");
    cardTransaction.processOpening(WriteAccessLevel.LOAD);

    int contractCount = calypsoCardContent.getContracts().size();

    /* Update contract records */
    if (!calypsoCardContent.getUpdatedContracts().isEmpty()) {
      for (int i = 0; i < contractCount; i++) {
        int contractIndex = i + 1;
        ContractStructure contract = calypsoCardContent.getContracts().get(i);

        if (calypsoCardContent.getUpdatedContracts().contains(contract)) {
          // update contract
          cardTransaction.prepareUpdateRecord(
              SFI_CONTRACTS, contractIndex, new ContractStructureParser().generate(contract));

          // update counter
          if (contract.getCounterValue() != null) {
            cardTransaction.prepareSetCounter(
                SFI_COUNTERS, contractIndex, contract.getCounterValue());
          }
        }
      }
    }

    /* Update event */
    if (calypsoCardContent.isEventUpdated()) {
      cardTransaction.prepareUpdateRecord(
          SFI_EVENT_LOG,
          1,
          new EventStructureParser()
              .generate(
                  buildEvent(calypsoCardContent.getEvent(), calypsoCardContent.getContracts())));
    }

    /* Close Session */
    cardTransaction.processClosing();
    logger.info("Calypso Session Closed - SESSION_LVL_LOAD");

    return 0;
  }

  /**
   * Empty the inserted card with empty files for event, contracts, counters. Init the environment
   * file.
   */
  public void initCard() {
    // Create the card transaction manager
    CardTransactionManager cardTransaction;

    // Get the Calypso card extension service
    CalypsoExtensionService cardExtension = CalypsoExtensionService.getInstance();

    // Create security settings that reference the same SAM profile requested from the card resource
    // service, specifying the key ciphering key parameters.
    CardSecuritySetting cardSecuritySetting =
        CalypsoExtensionService.getInstance()
            .createCardSecuritySetting()
            .setSamResource(samResource.getReader(), (CalypsoSam) samResource.getSmartCard());

    // create a secured card transaction
    cardTransaction =
        cardExtension.createCardTransaction(cardReader, calypsoCard, cardSecuritySetting);

    if (PLUGIN_TYPE_ANDROID_OMAPI.equals(pluginType)) {
      // For OMAPI Reader we release the channel after reading
      cardTransaction.prepareReleaseCardChannel();
    }

    /*
     * Open Calypso session
     */
    logger.info("Open Calypso Session - SESSION_LVL_PERSO...");
    cardTransaction.processOpening(WriteAccessLevel.PERSONALIZATION);

    /*
     * Prepare file update
     */
    // Fill the environment structure with predefined values
    cardTransaction.prepareUpdateRecord(
        SFI_ENVIRONMENT_AND_HOLDER,
        1,
        new EnvironmentHolderStructureParser().generate(getEnvironmentInit()));

    // Clear the first event (update with a byte array filled with 0s).
    cardTransaction.prepareUpdateRecord(
        SFI_EVENT_LOG, 1, new byte[CardConstant.ENVIRONMENT_HOLDER_RECORD_SIZE_BYTES]);

    int contractCount = CalypsoUtils.getContractCount(calypsoCard);

    // Clear all contracts (update with a byte array filled with 0s).
    cardTransaction.prepareUpdateRecord(
        SFI_CONTRACTS, 1, new byte[CardConstant.CONTRACT_RECORD_SIZE_BYTES]);
    cardTransaction.prepareUpdateRecord(
        SFI_CONTRACTS, 2, new byte[CardConstant.CONTRACT_RECORD_SIZE_BYTES]);

    if (contractCount > 2) {
      cardTransaction.prepareUpdateRecord(
          SFI_CONTRACTS, 3, new byte[CardConstant.CONTRACT_RECORD_SIZE_BYTES]);
      cardTransaction.prepareUpdateRecord(
          SFI_CONTRACTS, 4, new byte[CardConstant.CONTRACT_RECORD_SIZE_BYTES]);
    }

    // Clear the counter file (update with a byte array filled with 0s).
    cardTransaction.prepareUpdateRecord(
        SFI_COUNTERS, 1, new byte[CardConstant.EVENT_RECORD_SIZE_BYTES]);

    /*
     * Close Calypso session
     */
    cardTransaction.processClosing();

    logger.info("Calypso Session Closed - SESSION_LVL_PERSO");
  }

  /**
   * Return a init environment structure
   *
   * @return environment structure init
   */
  public static EnvironmentHolderStructure getEnvironmentInit() {
    // calculate issuing date
    Instant now = Instant.now();

    // calculate env end date
    LocalDate envEndDate =
        now.atZone(ZoneId.systemDefault()).toLocalDate().withDayOfMonth(1).plusYears(6);

    return new EnvironmentHolderStructure(
        VersionNumber.CURRENT_VERSION,
        1,
        new DateCompact(new Date(now.toEpochMilli())),
        new DateCompact(
            new Date(envEndDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())),
        null,
        null);
  }

  /**
   * Fill the event structure to update: - EventVersionNumber = 1 - EventDateStamp = value read from
   * previous event - EventTimeStamp = value read from previous event - EventLocation = value read
   * from previous event - EventContractUsed = value read from previous event - ContractPriority1 =
   * Value of index 0 of ContractPriority persistent object - ContractPriority2 = Value of index 1
   * of ContractPriority persistent object - ContractPriority3 = Value of index 2 of
   * ContractPriority persistent object - ContractPriority4 = Value of index 3 of ContractPriority
   * persistent object - EventPadding = 0
   *
   * @param oldEvent previous event
   * @param contracts list of updated contracts
   * @return a new event
   */
  private EventStructure buildEvent(EventStructure oldEvent, List<ContractStructure> contracts) {
    int contractCount = contracts.size();

    return new EventStructure(
        VersionNumber.CURRENT_VERSION,
        oldEvent.getEventDateStamp(),
        oldEvent.getEventTimeStamp(),
        oldEvent.getEventLocation(),
        oldEvent.getEventContractUsed(),
        contracts.get(0).getContractTariff(),
        contracts.get(1).getContractTariff(),
        contractCount == 4 ? contracts.get(2).getContractTariff() : null,
        contractCount == 4 ? contracts.get(3).getContractTariff() : null);
  }
}
