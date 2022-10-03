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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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
  public static final String SAM_C1 = "SAM C1";
  public static final String ANDROID_NFC_PLUGIN_NAME = "Android NFC";
  public static final String SUCCESS = "SUCCESS";

  @Inject CardRepository cardRepository;
  @Inject ActivityService activityService;

  AnalyzeContractsOutputDto analyzeContracts(
      CardResource cardResource, AnalyzeContractsInputDto inputData) {

    String pluginType = inputData.getPluginType();
    String asnHex =
        HexUtil.toHex(((CalypsoCard) cardResource.getSmartCard()).getApplicationSerialNumber());

    CardResource samResource = CardResourceServiceProvider.getService().getCardResource(SAM_C1);
    try {
      Card card = cardRepository.readCard(cardResource, samResource, pluginType);
      logger.info("{}", card);
      List<ContractStructure> validContracts = findValidContracts(card);
      activityService.push(
          new Activity()
              .setPlugin(pluginType == null ? ANDROID_NFC_PLUGIN_NAME : pluginType)
              .setStatus(SUCCESS)
              .setType("SECURED READ")
              .setCardSerialNumber(asnHex));
      return new AnalyzeContractsOutputDto(validContracts, 0);
    } catch (RuntimeException e) {
      logger.error("An error occurred while analyzing the contracts: {}", e.getMessage());
      activityService.push(
          new Activity()
              .setPlugin(pluginType == null ? ANDROID_NFC_PLUGIN_NAME : pluginType)
              .setStatus("FAIL")
              .setType("SECURED READ")
              .setCardSerialNumber(asnHex));
      return new AnalyzeContractsOutputDto(null, 1);
    } finally {
      CardResourceServiceProvider.getService().releaseCardResource(samResource);
    }
  }

  WriteContractOutputDto writeContract(CardResource cardResource, WriteContractInputDto inputData) {

    String pluginType = inputData.getPluginType();
    String asnHex =
        HexUtil.toHex(((CalypsoCard) cardResource.getSmartCard()).getApplicationSerialNumber());

    logger.info("Inserted card application serial number: {}", asnHex);

    CardResource samResource = CardResourceServiceProvider.getService().getCardResource(SAM_C1);
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
              // TODO change default name
              .setPlugin(pluginType == null ? ANDROID_NFC_PLUGIN_NAME : pluginType)
              .setStatus(SUCCESS)
              .setType("RELOAD")
              .setCardSerialNumber(asnHex)
              .setContractLoaded(
                  inputData.getContractTariff().toString().replace("_", " ")
                      + ((inputData.getTicketToLoad() != null && inputData.getTicketToLoad() != 0)
                          ? ": " + inputData.getTicketToLoad()
                          : "")));
      return new WriteContractOutputDto(statusCode);
    } catch (RuntimeException e) {
      logger.error("An error occurred while writing the contract: {}", e.getMessage());
      activityService.push(
          new Activity()
              // TODO change default name
              .setPlugin(pluginType == null ? ANDROID_NFC_PLUGIN_NAME : pluginType)
              .setStatus("FAIL")
              .setType("RELOAD")
              .setCardSerialNumber(asnHex)
              .setContractLoaded(""));
      return new WriteContractOutputDto(1);
    } finally {
      CardResourceServiceProvider.getService().releaseCardResource(samResource);
    }
  }

  CardIssuanceOutputDto initCard(CardResource cardResource) {

    String asnHex =
        HexUtil.toHex(((CalypsoCard) cardResource.getSmartCard()).getApplicationSerialNumber());

    CardResource samResource = CardResourceServiceProvider.getService().getCardResource(SAM_C1);
    try {
      cardRepository.initCard(cardResource, samResource, ANDROID_NFC_PLUGIN_NAME);
      activityService.push(
          new Activity()
              .setPlugin(ANDROID_NFC_PLUGIN_NAME)
              .setStatus(SUCCESS)
              .setType("ISSUANCE")
              .setCardSerialNumber(asnHex));
      return new CardIssuanceOutputDto(0);
    } catch (RuntimeException e) {
      activityService.push(
          new Activity()
              .setPlugin(ANDROID_NFC_PLUGIN_NAME)
              .setStatus("FAIL")
              .setType("ISSUANCE")
              .setCardSerialNumber(asnHex));
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
      return null;
    }
    if (environment.getEnvEndDate().getValue() < new DateCompact(new Date()).getValue()) {
      logger.warn("EnvEndDate of card is invalid, reject card");
      return null;
    }
    // Check last event
    EventStructure lastEvent = card.getEvent();
    if (lastEvent.getEventVersionNumber() != VersionNumber.CURRENT_VERSION
        && lastEvent.getEventVersionNumber() != VersionNumber.UNDEFINED) {
      logger.warn("EventVersionNumber of card is invalid, reject card");
      return null;
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
          // todo what to do here?
        }
      } else {
        // If ContractValidityEndDate points to a date in the past
        if (contract.getContractValidityEndDate().getValue()
            < new DateCompact(new Date()).getValue()) {
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
    ContractStructure newContract = null;
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
    DateCompact contractSaleDate = new DateCompact(new Date());
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
    DateCompact contractSaleDate = new DateCompact(new Date());
    DateCompact contractValidityEndDate =
        new DateCompact((short) (contractSaleDate.getValue() + 30));
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