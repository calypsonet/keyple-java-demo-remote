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
import org.calypsonet.terminal.calypso.WriteAccessLevel;
import org.calypsonet.terminal.calypso.card.CalypsoCard;
import org.calypsonet.terminal.calypso.card.FileData;
import org.calypsonet.terminal.calypso.sam.CalypsoSam;
import org.calypsonet.terminal.calypso.transaction.CardSecuritySetting;
import org.calypsonet.terminal.calypso.transaction.CardTransactionManager;
import org.eclipse.keyple.card.calypso.CalypsoExtensionService;
import org.eclipse.keyple.core.service.resource.CardResource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class CardRepository {

  private static final Logger logger = LoggerFactory.getLogger(CardRepository.class);

  private static final String PLUGIN_TYPE_ANDROID_OMAPI = "Android OMAPI";
  private static final String CALYPSO_SESSION_CLOSED = "Calypso Session Closed.";

  public Card readCard(CardResource cardResource, CardResource samResource, String pluginType) {

    CalypsoCard calypsoCard = (CalypsoCard) cardResource.getSmartCard();
    int contractCount = getContractCount(calypsoCard);

    CardTransactionManager cardTransactionManager =
        initCardTransactionManager(cardResource, samResource, pluginType, calypsoCard);

    logger.info("Open Calypso Session (LOAD)...");
    cardTransactionManager
        .processOpening(WriteAccessLevel.LOAD)
        .prepareReadRecord(CardConstant.SFI_ENVIRONMENT_AND_HOLDER, 1)
        .prepareReadRecord(CardConstant.SFI_EVENTS_LOG, 1)
        .prepareReadRecords(
            CardConstant.SFI_CONTRACTS, 1, contractCount, CardConstant.CONTRACT_RECORD_SIZE_BYTES)
        .prepareReadCounter(CardConstant.SFI_COUNTERS, 4)
        .processCommands()
        .processClosing();
    logger.info(CALYPSO_SESSION_CLOSED);

    return parse(calypsoCard);
  }

  public int writeCard(
      CardResource cardResource, CardResource samResource, String pluginType, Card card) {

    CalypsoCard calypsoCard = (CalypsoCard) cardResource.getSmartCard();
    int contractCount = card.getContracts().size();

    CardTransactionManager cardTransactionManager =
        initCardTransactionManager(cardResource, samResource, pluginType, calypsoCard);

    logger.info("Open Calypso Session (LOAD)...");
    cardTransactionManager.processOpening(WriteAccessLevel.LOAD);

    /* Update contract records */
    if (!card.getUpdatedContracts().isEmpty()) {
      for (int i = 0; i < contractCount; i++) {
        int contractIndex = i + 1;
        ContractStructure contract = card.getContracts().get(i);
        if (card.getUpdatedContracts().contains(contract)) {
          // update contract
          cardTransactionManager.prepareUpdateRecord(
              CardConstant.SFI_CONTRACTS,
              contractIndex,
              new ContractStructureParser().generate(contract));
          // update counter
          if (contract.getCounterValue() != null) {
            cardTransactionManager.prepareSetCounter(
                CardConstant.SFI_COUNTERS, contractIndex, contract.getCounterValue());
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

    cardTransactionManager.processClosing();
    logger.info(CALYPSO_SESSION_CLOSED);

    return 0;
  }

  public void initCard(CardResource cardResource, CardResource samResource, String pluginType) {

    CalypsoCard calypsoCard = (CalypsoCard) cardResource.getSmartCard();
    int contractCount = getContractCount(calypsoCard);

    CardTransactionManager cardTransactionManager =
        initCardTransactionManager(cardResource, samResource, pluginType, calypsoCard);

    logger.info("Open Calypso Session (PERSONALIZATION)...");
    cardTransactionManager.processOpening(WriteAccessLevel.PERSONALIZATION);

    // Fill the environment structure with predefined values
    cardTransactionManager.prepareUpdateRecord(
        CardConstant.SFI_ENVIRONMENT_AND_HOLDER,
        1,
        new EnvironmentHolderStructureParser().generate(buildEnvironmentHolderStructure()));

    // Clear the first event (update with a byte array filled with 0s).
    cardTransactionManager.prepareUpdateRecord(
        CardConstant.SFI_EVENTS_LOG,
        1,
        new byte[CardConstant.ENVIRONMENT_HOLDER_RECORD_SIZE_BYTES]);

    // Clear all contracts (update with a byte array filled with 0s).
    cardTransactionManager.prepareUpdateRecord(
        CardConstant.SFI_CONTRACTS, 1, new byte[CardConstant.CONTRACT_RECORD_SIZE_BYTES]);
    cardTransactionManager.prepareUpdateRecord(
        CardConstant.SFI_CONTRACTS, 2, new byte[CardConstant.CONTRACT_RECORD_SIZE_BYTES]);

    if (contractCount > 2) {
      cardTransactionManager.prepareUpdateRecord(
          CardConstant.SFI_CONTRACTS, 3, new byte[CardConstant.CONTRACT_RECORD_SIZE_BYTES]);
      cardTransactionManager.prepareUpdateRecord(
          CardConstant.SFI_CONTRACTS, 4, new byte[CardConstant.CONTRACT_RECORD_SIZE_BYTES]);
    }

    // Clear the counter file (update with a byte array filled with 0s).
    cardTransactionManager.prepareUpdateRecord(
        CardConstant.SFI_COUNTERS, 1, new byte[CardConstant.EVENT_RECORD_SIZE_BYTES]);

    cardTransactionManager.processClosing();
    logger.info(CALYPSO_SESSION_CLOSED);
  }

  @NotNull
  private CardTransactionManager initCardTransactionManager(
      CardResource cardResource,
      CardResource samResource,
      String pluginType,
      CalypsoCard calypsoCard) {

    CardSecuritySetting cardSecuritySetting =
        CalypsoExtensionService.getInstance()
            .createCardSecuritySetting()
            .assignDefaultKif(
                WriteAccessLevel.PERSONALIZATION, CardConstant.DEFAULT_KIF_PERSONALIZATION)
            .assignDefaultKif(WriteAccessLevel.LOAD, CardConstant.DEFAULT_KIF_LOAD)
            .assignDefaultKif(WriteAccessLevel.DEBIT, CardConstant.DEFAULT_KIF_DEBIT)
            .setControlSamResource(
                samResource.getReader(), (CalypsoSam) samResource.getSmartCard());

    CardTransactionManager cardTransactionManager =
        CalypsoExtensionService.getInstance()
            .createCardTransaction(cardResource.getReader(), calypsoCard, cardSecuritySetting);

    if (PLUGIN_TYPE_ANDROID_OMAPI.equals(pluginType)) {
      // For OMAPI Reader we release the channel after reading
      cardTransactionManager.prepareReleaseCardChannel();
    }
    return cardTransactionManager;
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
        contracts.get(1).getContractTariff(),
        contractCount == 4 ? contracts.get(2).getContractTariff() : PriorityCode.FORBIDDEN,
        contractCount == 4 ? contracts.get(3).getContractTariff() : PriorityCode.FORBIDDEN);
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
    return calypsoCard.getApplicationSubtype() == 50 ? 2 : 4;
  }
}
