/* **************************************************************************************
 * Copyright (c) 2022 Calypso Networks Association https://calypsonet.org/
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

import java.time.LocalDate;
import java.util.*;
import java.util.Collections;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.calypsonet.keyple.demo.common.constant.CardConstant;
import org.calypsonet.keyple.demo.common.dto.*;
import org.calypsonet.keyple.demo.common.model.ContractStructure;
import org.calypsonet.keyple.demo.common.model.EnvironmentHolderStructure;
import org.calypsonet.keyple.demo.common.model.EventStructure;
import org.calypsonet.keyple.demo.common.model.type.DateCompact;
import org.calypsonet.keyple.demo.common.model.type.PriorityCode;
import org.calypsonet.keyple.demo.common.model.type.VersionNumber;
import org.calypsonet.keyple.demo.reload.remote.server.activity.Activity;
import org.calypsonet.keyple.demo.reload.remote.server.activity.ActivityService;
import org.calypsonet.terminal.calypso.card.CalypsoCard;
import org.eclipse.keyple.core.service.resource.CardResource;
import org.eclipse.keyple.core.service.resource.CardResourceServiceProvider;
import org.eclipse.keyple.core.util.HexUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class CardService {

  private static final Logger logger = LoggerFactory.getLogger(CardService.class);
  private static final String SUCCESS = "SUCCESS";
  private static final String FAIL = "FAIL";
  private static final String SECURED_READ = "SECURED READ";
  private static final String RELOAD = "RELOAD";
  private static final String ISSUANCE = "ISSUANCE";

  @Inject CardRepository cardRepository;
  @Inject ActivityService activityService;

  AnalyzeContractsOutputDto analyzeContracts(
      CardResource cardResource, AnalyzeContractsInputDto inputData) {

    String pluginType = inputData.getPluginType();
    CalypsoCard calypsoCard = (CalypsoCard) cardResource.getSmartCard();
    String appSerialNumber = HexUtil.toHex(calypsoCard.getApplicationSerialNumber());

    if (!CardConstant.Companion.getALLOWED_FILE_STRUCTURES()
        .contains(calypsoCard.getApplicationSubtype())) {
      return new AnalyzeContractsOutputDto(Collections.emptyList(), 2);
    }

    CardResource samResource =
        CardResourceServiceProvider.getService()
            .getCardResource(CardConfigurator.SAM_RESOURCE_PROFILE_NAME);
    try {
      Card card = cardRepository.readCard(cardResource, samResource, pluginType);
      logger.info("{}", card);
      List<ContractStructure> validContracts = findValidContracts(card);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(SUCCESS)
              .setType(SECURED_READ)
              .setCardSerialNumber(appSerialNumber));
      return new AnalyzeContractsOutputDto(validContracts, 0);
    } catch (RuntimeException e) {
      logger.error("An error occurred while analyzing the contracts: {}", e.getMessage());
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(SECURED_READ)
              .setCardSerialNumber(appSerialNumber));
      return new AnalyzeContractsOutputDto(Collections.emptyList(), 1);
    } finally {
      CardResourceServiceProvider.getService().releaseCardResource(samResource);
    }
  }

  WriteContractOutputDto writeContract(CardResource cardResource, WriteContractInputDto inputData) {

    String pluginType = inputData.getPluginType();
    CalypsoCard calypsoCard = (CalypsoCard) cardResource.getSmartCard();
    String appSerialNumber = HexUtil.toHex(calypsoCard.getApplicationSerialNumber());

    if (!CardConstant.Companion.getALLOWED_FILE_STRUCTURES()
        .contains(calypsoCard.getApplicationSubtype())) {
      return new WriteContractOutputDto(2);
    }

    logger.info("Inserted card application serial number: {}", appSerialNumber);

    CardResource samResource =
        CardResourceServiceProvider.getService()
            .getCardResource(CardConfigurator.SAM_RESOURCE_PROFILE_NAME);
    try {
      Card card = cardRepository.readCard(cardResource, samResource, pluginType);
      if (card == null) {
        // If card has not been read previously, throw error
        return new WriteContractOutputDto(3);
      }
      logger.info("{}", card);
      insertNewContract(inputData.getContractTariff(), inputData.getTicketToLoad(), card);
      int statusCode = cardRepository.writeCard(cardResource, samResource, pluginType, card);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(SUCCESS)
              .setType(RELOAD)
              .setCardSerialNumber(appSerialNumber)
              .setContractLoaded(
                  inputData.getContractTariff().toString().replace("_", " ")
                      + ((inputData.getTicketToLoad() != 0)
                          ? ": " + inputData.getTicketToLoad()
                          : "")));
      return new WriteContractOutputDto(statusCode);
    } catch (RuntimeException e) {
      logger.error("An error occurred while writing the contract: {}", e.getMessage());
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(RELOAD)
              .setCardSerialNumber(appSerialNumber)
              .setContractLoaded(""));
      return new WriteContractOutputDto(1);
    } finally {
      CardResourceServiceProvider.getService().releaseCardResource(samResource);
    }
  }

  CardIssuanceOutputDto initCard(CardResource cardResource, CardIssuanceInputDto inputData) {

    String pluginType = inputData.getPluginType();
    CalypsoCard calypsoCard = (CalypsoCard) cardResource.getSmartCard();
    String appSerialNumber = HexUtil.toHex(calypsoCard.getApplicationSerialNumber());

    if (!CardConstant.Companion.getALLOWED_FILE_STRUCTURES()
        .contains(calypsoCard.getApplicationSubtype())) {
      return new CardIssuanceOutputDto(2);
    }

    CardResource samResource =
        CardResourceServiceProvider.getService()
            .getCardResource(CardConfigurator.SAM_RESOURCE_PROFILE_NAME);
    try {
      cardRepository.initCard(cardResource, samResource, pluginType);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(SUCCESS)
              .setType(ISSUANCE)
              .setCardSerialNumber(appSerialNumber));
      return new CardIssuanceOutputDto(0);
    } catch (RuntimeException e) {
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(ISSUANCE)
              .setCardSerialNumber(appSerialNumber));
      return new CardIssuanceOutputDto(1);
    } finally {
      CardResourceServiceProvider.getService().releaseCardResource(samResource);
    }
  }

  private List<ContractStructure> findValidContracts(Card card) {
    // Check environment
    EnvironmentHolderStructure environment = card.getEnvironment();
    if (environment.getEnvVersionNumber() != VersionNumber.CURRENT_VERSION) {
      logger.warn("Version Number of card is invalid, reject card");
      return Collections.emptyList();
    }
    if (environment.getEnvEndDate().getDate().isBefore(LocalDate.now())) {
      logger.warn("EnvEndDate of card is invalid, reject card");
      return Collections.emptyList();
    }
    // Check last event
    EventStructure lastEvent = card.getEvent();
    if (lastEvent.getEventVersionNumber() != VersionNumber.CURRENT_VERSION
        && lastEvent.getEventVersionNumber() != VersionNumber.UNDEFINED) {
      logger.warn("EventVersionNumber of card is invalid, reject card");
      return Collections.emptyList();
    }
    // Iterate through the contracts in the card session
    List<ContractStructure> contracts = card.getContracts();
    List<ContractStructure> validContracts = new ArrayList<>();
    int calypsoIndex = 1;
    for (ContractStructure contract : contracts) {
      logger.info(
          "Contract at index {}: {} {}",
          calypsoIndex,
          contract.getContractTariff(),
          contract.getContractSaleDate().getValue());
      if (contract.getContractVersionNumber() == VersionNumber.UNDEFINED) {
        // If ContractVersionNumber is 0 ensure that the associated ContractPriority field value is
        // 0 and move on to the next contract.
        if (contract.getContractTariff() != PriorityCode.FORBIDDEN) {
          logger.warn("Contract tariff is not valid for this contract");
        }
      } else {
        // If ContractValidityEndDate points to a date in the past
        if (contract.getContractValidityEndDate().getDate().isBefore(LocalDate.now())) {
          // Update the associated ContractPriority field present in the persistent object to 31 and
          // set the change flag to true.
          contract.setContractTariff(PriorityCode.EXPIRED);
          // Update contract
          card.setContract(calypsoIndex - 1, contract);
        }
        validContracts.add(contract);
      }
      calypsoIndex++;
    }
    logger.info("Contracts {}", Arrays.deepToString(validContracts.toArray()));
    return validContracts;
  }

  private void insertNewContract(PriorityCode contractTariff, Integer ticketToLoad, Card card) {

    if (contractTariff != PriorityCode.SEASON_PASS && contractTariff != PriorityCode.MULTI_TRIP) {
      throw new IllegalArgumentException("Only Season Pass or Multi Trip ticket can be loaded");
    }

    EnvironmentHolderStructure environment = card.getEnvironment();
    List<ContractStructure> contracts = card.getContracts();
    EventStructure currentEvent = card.getEvent();
    ContractStructure newContract;
    int newContractNumber;

    int existingContractNumber = getContractNumber(contractTariff, contracts);
    if (existingContractNumber > 0) {
      // Reloading
      newContractNumber = existingContractNumber;
      ContractStructure currentContract = contracts.get(existingContractNumber - 1);
      // build new contract
      if (PriorityCode.MULTI_TRIP == contractTariff) {
        newContract =
            buildMultiTripContract(
                environment.getEnvEndDate(), currentContract.getCounterValue() + ticketToLoad);
      } else {
        newContract = buildSeasonContract();
      }
    } else {
      // Issuing
      newContractNumber = findAvailablePosition(contracts);
      if (newContractNumber == 0) {
        // no available position, reject card
        return;
      }
      // build new contract
      if (PriorityCode.MULTI_TRIP == contractTariff) {
        newContract = buildMultiTripContract(environment.getEnvEndDate(), ticketToLoad);
      } else {
        newContract = buildSeasonContract();
      }
    }
    switch (newContractNumber) {
      case 1:
        currentEvent.setContractPriority1(newContract.getContractTariff());
        break;
      case 2:
        currentEvent.setContractPriority2(newContract.getContractTariff());
        break;
      case 3:
        currentEvent.setContractPriority3(newContract.getContractTariff());
        break;
      case 4:
        currentEvent.setContractPriority4(newContract.getContractTariff());
        break;
      default:
        throw new IllegalStateException("Unexpected contract number: " + newContractNumber);
    }
    // Update contract & Event
    card.setContract(newContractNumber - 1, newContract);
    card.setEvent(currentEvent);
  }

  private int getContractNumber(PriorityCode contractTariff, List<ContractStructure> contracts) {
    int contractCount = contracts.size();
    for (int i = 0; i < contractCount; i++) {
      if (contractTariff.equals(contracts.get(i).getContractTariff())) {
        return i + 1;
      }
    }
    return 0;
  }

  private ContractStructure buildMultiTripContract(DateCompact envEndDate, Integer counterValue) {
    DateCompact contractSaleDate = new DateCompact(LocalDate.now());
    ContractStructure contract =
        new ContractStructure(
            VersionNumber.CURRENT_VERSION,
            PriorityCode.MULTI_TRIP,
            contractSaleDate,
            envEndDate,
            null,
            null,
            null,
            null);
    contract.setCounterValue(counterValue);
    return contract;
  }

  private ContractStructure buildSeasonContract() {
    DateCompact contractSaleDate = new DateCompact(LocalDate.now());
    DateCompact contractValidityEndDate = new DateCompact(contractSaleDate.getValue() + 30);
    return new ContractStructure(
        VersionNumber.CURRENT_VERSION,
        PriorityCode.SEASON_PASS,
        contractSaleDate,
        contractValidityEndDate,
        null,
        null,
        null,
        null);
  }

  private int findAvailablePosition(List<ContractStructure> contracts) {
    int contractCount = contracts.size();
    for (int i = 0; i < contractCount; i++) {
      if (PriorityCode.FORBIDDEN == contracts.get(i).getContractTariff()) {
        return i + 1;
      }
    }
    for (int i = 0; i < contractCount; i++) {
      if (PriorityCode.EXPIRED == contracts.get(i).getContractTariff()) {
        return i + 1;
      }
    }
    return 0;
  }
}
