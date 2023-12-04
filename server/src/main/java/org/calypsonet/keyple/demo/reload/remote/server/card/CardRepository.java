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
package org.calypsonet.keyple.demo.reload.remote.server.card;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import org.calypsonet.keyple.demo.common.constant.CardConstant;
import org.calypsonet.keyple.demo.common.model.ContractStructure;
import org.calypsonet.keyple.demo.common.model.EnvironmentHolderStructure;
import org.calypsonet.keyple.demo.common.model.EventStructure;
import org.calypsonet.keyple.demo.common.model.type.DateCompact;
import org.calypsonet.keyple.demo.common.model.type.PriorityCode;
import org.calypsonet.keyple.demo.common.model.type.VersionNumber;
import org.calypsonet.keyple.demo.common.parser.ContractStructureParser;
import org.calypsonet.keyple.demo.common.parser.EnvironmentHolderStructureParser;
import org.calypsonet.keyple.demo.common.parser.EventStructureParser;
import org.eclipse.keyple.card.calypso.CalypsoExtensionService;
import org.eclipse.keyple.card.calypso.crypto.legacysam.LegacySamExtensionService;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.core.service.resource.CardResource;
import org.eclipse.keypop.calypso.card.CalypsoCardApiFactory;
import org.eclipse.keypop.calypso.card.WriteAccessLevel;
import org.eclipse.keypop.calypso.card.card.CalypsoCard;
import org.eclipse.keypop.calypso.card.card.FileData;
import org.eclipse.keypop.calypso.card.transaction.ChannelControl;
import org.eclipse.keypop.calypso.card.transaction.SecureRegularModeTransactionManager;
import org.eclipse.keypop.calypso.card.transaction.SymmetricCryptoSecuritySetting;
import org.eclipse.keypop.calypso.crypto.legacysam.sam.LegacySam;
import org.eclipse.keypop.reader.CardReader;
import org.eclipse.keypop.reader.ReaderApiFactory;
import org.eclipse.keypop.reader.selection.CardSelectionManager;
import org.eclipse.keypop.reader.selection.CardSelectionResult;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class CardRepository {

  private static final Logger logger = LoggerFactory.getLogger(CardRepository.class);

  private static final String CALYPSO_SESSION_CLOSED = "Calypso Session Closed.";

  private static CardSelectionManager createCardSelectionManager() {
    ReaderApiFactory readerApiFactory = SmartCardServiceProvider.getService().getReaderApiFactory();
    CardSelectionManager cardSelectionManager = readerApiFactory.createCardSelectionManager();
    CalypsoCardApiFactory calypsoCardApiFactory =
        CalypsoExtensionService.getInstance().getCalypsoCardApiFactory();

    cardSelectionManager.prepareSelection(
        readerApiFactory
            .createIsoCardSelector()
            .filterByDfName(CardConstant.Companion.getAID_KEYPLE_GENERIC()),
        calypsoCardApiFactory.createCalypsoCardSelectionExtension().acceptInvalidatedCard());

    cardSelectionManager.prepareSelection(
        readerApiFactory
            .createIsoCardSelector()
            .filterByDfName(CardConstant.Companion.getAID_CALYPSO_LIGHT()),
        calypsoCardApiFactory.createCalypsoCardSelectionExtension().acceptInvalidatedCard());

    cardSelectionManager.prepareSelection(
        readerApiFactory
            .createIsoCardSelector()
            .filterByDfName(CardConstant.Companion.getAID_CD_LIGHT_GTML()),
        calypsoCardApiFactory.createCalypsoCardSelectionExtension().acceptInvalidatedCard());

    cardSelectionManager.prepareSelection(
        readerApiFactory
            .createIsoCardSelector()
            .filterByDfName(CardConstant.Companion.getAID_NORMALIZED_IDF()),
        calypsoCardApiFactory.createCalypsoCardSelectionExtension().acceptInvalidatedCard());
    return cardSelectionManager;
  }

  CalypsoCard selectCard(CardReader cardReader) {
    CardSelectionManager cardSelectionManager = createCardSelectionManager();
    // Actual card communication: run the selection scenario.
    CardSelectionResult selectionResult =
        cardSelectionManager.processCardSelectionScenario(cardReader);

    // Check the selection result.
    if (selectionResult.getActiveSmartCard() == null) {
      throw new IllegalStateException("The selection of the application failed.");
    }

    // Get the SmartCard resulting of the selection.
    return (CalypsoCard) selectionResult.getActiveSmartCard();
  }

  Card readCard(CardReader cardReader, CalypsoCard calypsoCard, CardResource samResource) {
    int contractCount = getContractCount(calypsoCard);

    SecureRegularModeTransactionManager cardTransactionManager =
        initCardTransactionManager(cardReader, calypsoCard, samResource);

    logger.info("Open Calypso Session (LOAD)...");
    cardTransactionManager
        .prepareOpenSecureSession(WriteAccessLevel.LOAD)
        .prepareReadRecord(CardConstant.SFI_ENVIRONMENT_AND_HOLDER, 1)
        .prepareReadRecord(CardConstant.SFI_EVENTS_LOG, 1)
        .prepareReadRecords(
            CardConstant.SFI_CONTRACTS, 1, contractCount, CardConstant.CONTRACT_RECORD_SIZE_BYTES)
        .prepareReadCounter(CardConstant.SFI_COUNTERS, contractCount)
        .prepareCloseSecureSession()
        .processCommands(ChannelControl.KEEP_OPEN);
    logger.info(CALYPSO_SESSION_CLOSED);

    return parse(calypsoCard);
  }

  int writeCard(
      CardReader cardReader, CalypsoCard calypsoCard, CardResource samResource, Card card) {

    SecureRegularModeTransactionManager cardTransactionManager =
        initCardTransactionManager(cardReader, calypsoCard, samResource);

    logger.info("Open Calypso Session (LOAD)...");
    cardTransactionManager.prepareOpenSecureSession(WriteAccessLevel.LOAD);

    /* Update contract records */
    if (!card.getUpdatedContracts().isEmpty()) {
      int contractCount = card.getContracts().size();
      for (int i = 0; i < contractCount; i++) {
        int contractNumber = i + 1;
        ContractStructure contract = card.getContracts().get(i);
        if (card.getUpdatedContracts().contains(contract)) {
          // update contract
          cardTransactionManager.prepareUpdateRecord(
              CardConstant.SFI_CONTRACTS,
              contractNumber,
              new ContractStructureParser().generate(contract));
          // update counter
          if (contract.getCounterValue() != null) {
            cardTransactionManager.prepareSetCounter(
                CardConstant.SFI_COUNTERS, contractNumber, contract.getCounterValue());
          }
        }
      }
    }
    /* Update event */
    if (Boolean.TRUE.equals(card.isEventUpdated())) {
      cardTransactionManager.prepareUpdateRecord(
          CardConstant.SFI_EVENTS_LOG,
          1,
          new EventStructureParser().generate(buildEvent(card.getEvent(), card.getContracts())));
    }

    cardTransactionManager.prepareCloseSecureSession().processCommands(ChannelControl.KEEP_OPEN);
    logger.info(CALYPSO_SESSION_CLOSED);

    return 0;
  }

  void initCard(CardReader cardReader, CalypsoCard calypsoCard, CardResource samResource) {

    SecureRegularModeTransactionManager cardTransactionManager =
        initCardTransactionManager(cardReader, calypsoCard, samResource);

    logger.info("Open Calypso Session (PERSONALIZATION)...");
    cardTransactionManager.prepareOpenSecureSession(WriteAccessLevel.PERSONALIZATION);

    // Fill the environment structure with predefined values
    cardTransactionManager.prepareUpdateRecord(
        CardConstant.SFI_ENVIRONMENT_AND_HOLDER,
        1,
        new EnvironmentHolderStructureParser().generate(buildEnvironmentHolderStructure()));

    // Clear the first event (update with a byte array filled with 0s).
    cardTransactionManager.prepareUpdateRecord(
        CardConstant.SFI_EVENTS_LOG, 1, new byte[CardConstant.EVENT_RECORD_SIZE_BYTES]);

    // Clear all contracts (update with a byte array filled with 0s).
    int contractCount = getContractCount(calypsoCard);
    for (int i = 1; i <= contractCount; i++) {
      cardTransactionManager.prepareUpdateRecord(
          CardConstant.SFI_CONTRACTS, i, new byte[CardConstant.CONTRACT_RECORD_SIZE_BYTES]);
    }

    // Clear the counter file (update with a byte array filled with 0s).
    cardTransactionManager.prepareUpdateRecord(
        CardConstant.SFI_COUNTERS, 1, new byte[contractCount * 3]);

    cardTransactionManager.prepareCloseSecureSession().processCommands(ChannelControl.KEEP_OPEN);
    logger.info(CALYPSO_SESSION_CLOSED);
  }

  @NotNull
  private SecureRegularModeTransactionManager initCardTransactionManager(
      CardReader cardReader, CalypsoCard calypsoCard, CardResource samResource) {
    CalypsoCardApiFactory calypsoCardApiFactory =
        CalypsoExtensionService.getInstance().getCalypsoCardApiFactory();
    SymmetricCryptoSecuritySetting cardSecuritySetting =
        calypsoCardApiFactory
            .createSymmetricCryptoSecuritySetting(
                LegacySamExtensionService.getInstance()
                    .getLegacySamApiFactory()
                    .createSymmetricCryptoCardTransactionManagerFactory(
                        samResource.getReader(), (LegacySam) samResource.getSmartCard()))
            .enableMultipleSession()
            .assignDefaultKif(
                WriteAccessLevel.PERSONALIZATION, CardConstant.DEFAULT_KIF_PERSONALIZATION)
            .assignDefaultKif(WriteAccessLevel.LOAD, CardConstant.DEFAULT_KIF_LOAD)
            .assignDefaultKif(WriteAccessLevel.DEBIT, CardConstant.DEFAULT_KIF_DEBIT);

    return calypsoCardApiFactory.createSecureRegularModeTransactionManager(
        cardReader, calypsoCard, cardSecuritySetting);
  }

  private EnvironmentHolderStructure buildEnvironmentHolderStructure() {
    // calculate issuing date
    Instant now = Instant.now();
    // calculate env end date
    LocalDate envEndDate =
        now.atZone(ZoneId.systemDefault()).toLocalDate().withDayOfMonth(1).plusYears(6);
    return new EnvironmentHolderStructure(
        VersionNumber.CURRENT_VERSION,
        1,
        new DateCompact(LocalDate.now()),
        new DateCompact(envEndDate),
        null,
        null);
  }

  private EventStructure buildEvent(EventStructure oldEvent, List<ContractStructure> contracts) {
    int contractCount = contracts.size();
    return new EventStructure(
        VersionNumber.CURRENT_VERSION,
        oldEvent.getEventDateStamp(),
        oldEvent.getEventTimeStamp(),
        oldEvent.getEventLocation(),
        oldEvent.getEventContractUsed(),
        contracts.get(0).getContractTariff(),
        contractCount >= 2 ? contracts.get(1).getContractTariff() : PriorityCode.FORBIDDEN,
        contractCount >= 3 ? contracts.get(2).getContractTariff() : PriorityCode.FORBIDDEN,
        contractCount >= 4 ? contracts.get(3).getContractTariff() : PriorityCode.FORBIDDEN);
  }

  private Card parse(CalypsoCard calypsoCard) {
    // Parse environment
    EnvironmentHolderStructure environment =
        new EnvironmentHolderStructureParser()
            .parse(
                calypsoCard
                    .getFileBySfi(CardConstant.SFI_ENVIRONMENT_AND_HOLDER)
                    .getData()
                    .getContent());
    // parse contracts
    List<ContractStructure> contracts = new ArrayList<>();
    FileData fileData = calypsoCard.getFileBySfi(CardConstant.SFI_CONTRACTS).getData();
    if (fileData != null) {
      int contractCount = getContractCount(calypsoCard);
      for (int i = 1; i < contractCount + 1; i++) {
        ContractStructure contract = new ContractStructureParser().parse(fileData.getContent(i));
        contracts.add(contract);
        // update counter tied to contract
        int counterValue =
            calypsoCard
                .getFileBySfi(CardConstant.SFI_COUNTERS)
                .getData()
                .getContentAsCounterValue(i);
        contract.setCounterValue(counterValue);
      }
    }
    // parse event
    EventStructure event =
        new EventStructureParser()
            .parse(calypsoCard.getFileBySfi(CardConstant.SFI_EVENTS_LOG).getData().getContent());
    return new Card(environment, contracts, event);
  }

  private int getContractCount(CalypsoCard calypsoCard) {
    if (calypsoCard.getProductType() == CalypsoCard.ProductType.BASIC) {
      return 1;
    } else if (calypsoCard.getProductType() == CalypsoCard.ProductType.LIGHT) {
      return 2;
    }
    return 4;
  }
}
