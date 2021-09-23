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
import java.util.List;
import org.calypsonet.terminal.calypso.WriteAccessLevel;
import org.calypsonet.terminal.calypso.card.CalypsoCard;
import org.calypsonet.terminal.calypso.sam.CalypsoSam;
import org.calypsonet.terminal.calypso.transaction.CardSecuritySetting;
import org.calypsonet.terminal.calypso.transaction.CardTransactionManager;
import org.cna.keyple.demo.distributed.server.util.CalypsoConstants;
import org.cna.keyple.demo.sale.data.model.ContractStructureDto;
import org.cna.keyple.demo.sale.data.model.EnvironmentHolderStructureDto;
import org.cna.keyple.demo.sale.data.model.EventStructureDto;
import org.cna.keyple.demo.sale.data.model.parser.ContractStructureParser;
import org.cna.keyple.demo.sale.data.model.parser.CounterStructureParser;
import org.cna.keyple.demo.sale.data.model.parser.EnvironmentHolderStructureParser;
import org.cna.keyple.demo.sale.data.model.parser.EventStructureParser;
import org.cna.keyple.demo.sale.data.model.type.DateCompact;
import org.cna.keyple.demo.sale.data.model.type.VersionNumber;
import org.eclipse.keyple.card.calypso.CalypsoExtensionService;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.resource.CardResource;
import org.eclipse.keyple.core.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Perform operations with a calypso PO inserted in a Reader. It requires a {@link CalypsoSam} to
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
    private Reader poReader;
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
     * Specify the reader where the calypso PO is inserted
     *
     * @param poReader non null instance of a reader
     * @return next step of configuration
     */
    public Builder withCardReader(Reader poReader) {
      Assert.getInstance().notNull(poReader, "poReader");
      this.poReader = poReader;
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
      return new CalypsoCardController(calypsoCard, poReader, samResource, pluginType);
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
   * (public) Read all files on the calypso PO inserted in the Reader
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
            .setSamResource(samResource.getReader(), (CalypsoSam) samResource.getSmartCard())
            .setPinVerificationCipheringKey(
                CalypsoConstants.PIN_VERIFICATION_CIPHERING_KEY_KIF,
                CalypsoConstants.PIN_VERIFICATION_CIPHERING_KEY_KVC)
            .setPinModificationCipheringKey(
                CalypsoConstants.PIN_MODIFICATION_CIPHERING_KEY_KIF,
                CalypsoConstants.PIN_MODIFICATION_CIPHERING_KEY_KVC);

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
    logger.info("Open Calypso Session...");
    cardTransaction.processOpening(WriteAccessLevel.LOAD);

    // Prepare reading of environment record
    cardTransaction.prepareReadRecordFile(SFI_ENVIRONMENT_AND_HOLDER, RECORD_NUMBER_1);

    // Prepare reading of last event record
    cardTransaction.prepareReadRecordFile(
        CalypsoConstants.SFI_EVENT_LOG, CalypsoConstants.RECORD_NUMBER_1);

    // Prepare reading of contract records (Calypso Light)
    cardTransaction.prepareReadRecordFile(SFI_CONTRACTS, CalypsoConstants.RECORD_NUMBER_1);
    cardTransaction.prepareReadRecordFile(SFI_CONTRACTS, CalypsoConstants.RECORD_NUMBER_2);
    cardTransaction.prepareReadRecordFile(SFI_CONTRACTS, CalypsoConstants.RECORD_NUMBER_3);
    cardTransaction.prepareReadRecordFile(SFI_CONTRACTS, CalypsoConstants.RECORD_NUMBER_4);

    // Prepare reading of simulated counter record
    cardTransaction.prepareReadCounterFile(SFI_Counters_1, CalypsoConstants.RECORD_NUMBER_1);
    cardTransaction.prepareReadCounterFile(SFI_Counters_2, CalypsoConstants.RECORD_NUMBER_1);
    cardTransaction.prepareReadCounterFile(SFI_Counters_3, CalypsoConstants.RECORD_NUMBER_1);
    cardTransaction.prepareReadCounterFile(SFI_Counters_4, CalypsoConstants.RECORD_NUMBER_1);

    logger.info("Read Card...");

    cardTransaction.processCardCommands();

    /*
     * Close Calypso session
     */
    cardTransaction.processClosing();

    logger.info("Calypso Session Closed.");
    return CalypsoCardRepresentation.parse(calypsoCard);
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
            .setSamResource(samResource.getReader(), (CalypsoSam) samResource.getSmartCard())
            .setPinVerificationCipheringKey(
                CalypsoConstants.PIN_VERIFICATION_CIPHERING_KEY_KIF,
                CalypsoConstants.PIN_VERIFICATION_CIPHERING_KEY_KVC)
            .setPinModificationCipheringKey(
                CalypsoConstants.PIN_MODIFICATION_CIPHERING_KEY_KIF,
                CalypsoConstants.PIN_MODIFICATION_CIPHERING_KEY_KVC);
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

    /* Update contract records */
    if (!calypsoCardContent.getUpdatedContracts().isEmpty()) {
      for (int i = 0; i < 4; i++) {

        ContractStructureDto contract = calypsoCardContent.getContracts().get(i);

        if (calypsoCardContent.getUpdatedContracts().contains(contract)) {
          // update contract
          cardTransaction.prepareUpdateRecord(
              SFI_CONTRACTS, i + 1, ContractStructureParser.unparse(contract));

          // update counter
          if (contract.getCounter() != null) {
            cardTransaction.prepareUpdateRecord(
                SFI_Counters_simulated.get(i),
                1,
                CounterStructureParser.unparse(contract.getCounter()));
          }
        }
      }
    }

    /* Update event */
    if (calypsoCardContent.isEventUpdated()) {
      cardTransaction.prepareUpdateRecord(
          SFI_EVENT_LOG,
          1,
          EventStructureParser.unparse(
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
            .setSamResource(samResource.getReader(), (CalypsoSam) samResource.getSmartCard())
            .setPinVerificationCipheringKey(
                CalypsoConstants.PIN_VERIFICATION_CIPHERING_KEY_KIF,
                CalypsoConstants.PIN_VERIFICATION_CIPHERING_KEY_KVC)
            .setPinModificationCipheringKey(
                CalypsoConstants.PIN_MODIFICATION_CIPHERING_KEY_KIF,
                CalypsoConstants.PIN_MODIFICATION_CIPHERING_KEY_KVC);
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
        EnvironmentHolderStructureParser.unparse(getEnvironmentInit()));

    // Clear the first event (update with a byte array filled with 0s).
    cardTransaction.prepareUpdateRecord(
        SFI_EVENT_LOG, 1, EnvironmentHolderStructureParser.getEmpty());

    // Clear all contracts (update with a byte array filled with 0s).
    // TODO do not support CLAP
    cardTransaction.prepareUpdateRecord(SFI_CONTRACTS, 1, ContractStructureParser.getEmpty());
    cardTransaction.prepareUpdateRecord(SFI_CONTRACTS, 2, ContractStructureParser.getEmpty());
    cardTransaction.prepareUpdateRecord(SFI_CONTRACTS, 3, ContractStructureParser.getEmpty());
    cardTransaction.prepareUpdateRecord(SFI_CONTRACTS, 4, ContractStructureParser.getEmpty());

    // Clear the counter file (update with a byte array filled with 0s).
    cardTransaction.prepareUpdateRecord(SFI_COUNTERS, 1, EventStructureParser.getEmpty());

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
  public static EnvironmentHolderStructureDto getEnvironmentInit() {
    // calculate issuing date
    Instant now = Instant.now();

    // calculate env end date
    LocalDate envEndDate =
        now.atZone(ZoneId.systemDefault()).toLocalDate().withDayOfMonth(1).plusYears(6);

    return EnvironmentHolderStructureDto.newBuilder()
        .setEnvVersionNumber(VersionNumber.CURRENT_VERSION)
        .setEnvApplicationNumber(1)
        .setEnvIssuingDate(new DateCompact(now))
        .setEnvEndDate(new DateCompact(envEndDate.atStartOfDay().toInstant(ZoneOffset.UTC)))
        .build();
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
  private EventStructureDto buildEvent(
      EventStructureDto oldEvent, List<ContractStructureDto> contracts) {
    return EventStructureDto.newBuilder()
        .setEventVersionNumber(VersionNumber.CURRENT_VERSION)
        .setEventDateStamp(oldEvent.getEventDateStamp())
        .setEventTimeStamp(oldEvent.getEventTimeStamp())
        .setEventLocation(oldEvent.getEventLocation())
        .setEventContractUsed(oldEvent.getEventContractUsed())
        .setContractPriority1(contracts.get(0).getContractTariff())
        .setContractPriority2(contracts.get(1).getContractTariff())
        .setContractPriority3(contracts.get(2).getContractTariff())
        .setContractPriority4(contracts.get(3).getContractTariff())
        .build();
  }
}
