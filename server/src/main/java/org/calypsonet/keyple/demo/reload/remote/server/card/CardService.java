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
import org.calypsonet.terminal.reader.CardReader;
import org.calypsonet.terminal.reader.selection.CardSelectionManager;
import org.calypsonet.terminal.reader.selection.CardSelectionResult;
import org.eclipse.keyple.card.calypso.CalypsoExtensionService;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.core.service.resource.CardResource;
import org.eclipse.keyple.core.service.resource.CardResourceServiceProvider;
import org.eclipse.keyple.core.util.HexUtil;
import org.jetbrains.annotations.NotNull;
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
  private static final String AID = "315449432E49434131";
  private static final String AN_ERROR_OCCURRED_WHILE_READING_THE_CARD_CONTENT =
      "An error occurred while reading the card content: {}";
  private static final String AN_ERROR_OCCURRED_WHILE_INCREASING_THE_CONTRACT_COUNTER =
      "An error occurred while increasing the contract counter: {}";
  private static final String AN_ERROR_OCCURRED_WHILE_ANALYZING_THE_CONTRACTS =
      "An error occurred while analyzing the contracts: {}";
  private static final String AN_ERROR_OCCURRED_WHILE_WRITING_THE_CONTRACT =
      "An error occurred while writing the contract: {}";
  private static final String AN_ERROR_OCCURRED_WHILE_INITIALIZING_THE_CARD =
      "An error occurred while initializing the card: {}";
  private static final String VERSION_NUMBER_OF_CARD_IS_INVALID_REJECT_CARD =
      "Version Number of card is invalid, reject card";
  private static final String ENV_END_DATE_OF_CARD_IS_INVALID_REJECT_CARD =
      "EnvEndDate of card is invalid, reject card";
  private static final String VERSION_NUMBER_OF_CARD_IS_INVALID_REJECT_CARD1 =
      "EventVersionNumber of card is invalid, reject card";
  private static final String CONTRACT_TARIFF_IS_NOT_VALID_FOR_THIS_CONTRACT =
      "Contract tariff is not valid for this contract";
  private static final String ONLY_SEASON_PASS_OR_MULTI_TRIP_TICKET_CAN_BE_LOADED =
      "Only Season Pass or Multi Trip ticket can be loaded";
  private static final String UNEXPECTED_CONTRACT_NUMBER = "Unexpected contract number: ";
  private static final String THE_CARD_IS_NOT_PERSONALIZED = "The card is not personalized";
  private static final String THE_ENVIRONMENT_HAS_EXPIRED = "The environment has expired";
  private static final String CONTRACT_AT_INDEX = "Contract at index {}: {} {}";
  private static final String CONTRACTS = "Contracts {}";
  private static final String CUSTOM_PLUGIN = "Non Keyple plugin";
  private static final String CARD_NOT_PERSONALIZED = "Card not personalized.";
  private static final String ENVIRONMENT_EXPIRED = "Environment expired.";
  private static final String RUNTIME_EXCEPTION = "Runtime exception: ";

  @Inject CardRepository cardRepository;
  @Inject ActivityService activityService;

  private String formatContractStructure(ContractStructure contractStructure) {
    StringBuilder builder = new StringBuilder();

    builder
        .append("Contract Version Number: ")
        .append(contractStructure.getContractVersionNumber())
        .append("\n");

    builder.append("Contract Tariff: ").append(contractStructure.getContractTariff()).append("\n");

    builder
        .append("Contract Sale Date: ")
        .append(contractStructure.getContractSaleDate().getDate())
        .append("\n");

    builder
        .append("Contract Validity End Date: ")
        .append(contractStructure.getContractValidityEndDate().getDate())
        .append("\n");

    if (contractStructure.getContractSaleSam() != null) {
      builder
          .append("Contract Sale Sam: ")
          .append(contractStructure.getContractSaleSam())
          .append("\n");
    }

    if (contractStructure.getContractSaleCounter() != null) {
      builder
          .append("Contract Sale Counter: ")
          .append(contractStructure.getContractSaleCounter())
          .append("\n");
    }

    if (contractStructure.getContractAuthKvc() != null) {
      builder
          .append("Contract Auth Kvc: ")
          .append(contractStructure.getContractAuthKvc())
          .append("\n");
    }

    if (contractStructure.getContractAuthenticator() != null) {
      builder
          .append("Contract Authenticator: ")
          .append(contractStructure.getContractAuthenticator())
          .append("\n");
    }

    if (contractStructure.getCounterValue() != null) {
      builder.append("Counter Value: ").append(contractStructure.getCounterValue()).append("\n");
    }

    return builder.toString();
  }

  @NotNull
  private static CardSelectionManager createCardSelectionManager() {
    SmartCardService smartCardService = SmartCardServiceProvider.getService();

    CalypsoExtensionService calypsoCardService = CalypsoExtensionService.getInstance();

    CardSelectionManager cardSelectionManager = smartCardService.createCardSelectionManager();

    cardSelectionManager.prepareSelection(
        calypsoCardService
            .createCardSelection()
            .acceptInvalidatedCard()
            .filterByDfName(CardConstant.Companion.getAID_KEYPLE_GENERIC()));

    cardSelectionManager.prepareSelection(
        calypsoCardService
            .createCardSelection()
            .acceptInvalidatedCard()
            .filterByDfName(CardConstant.Companion.getAID_CALYPSO_LIGHT()));

    cardSelectionManager.prepareSelection(
        calypsoCardService
            .createCardSelection()
            .acceptInvalidatedCard()
            .filterByDfName(CardConstant.Companion.getAID_CD_LIGHT_GTML()));

    cardSelectionManager.prepareSelection(
        calypsoCardService
            .createCardSelection()
            .acceptInvalidatedCard()
            .filterByDfName(CardConstant.Companion.getAID_NORMALIZED_IDF()));
    return cardSelectionManager;
  }

  SelectAppAndReadContractsOutputDto selectAppAndReadContracts(CardReader cardReader) {

    CardSelectionManager cardSelectionManager = createCardSelectionManager();

    CalypsoCard calypsoCard = null;
    CardResource samResource = null;
    List<String> output = new ArrayList<>();
    int statusCode = 0;
    String message = "Success.";
    try {
      // Actual card communication: run the selection scenario.
      CardSelectionResult selectionResult =
          cardSelectionManager.processCardSelectionScenario(cardReader);

      // Check the selection result.
      if (selectionResult.getActiveSmartCard() == null) {
        throw new IllegalStateException("The selection of the application " + AID + " failed.");
      }

      // Get the SmartCard resulting of the selection.
      calypsoCard = (CalypsoCard) selectionResult.getActiveSmartCard();

      samResource =
          CardResourceServiceProvider.getService()
              .getCardResource(CardConfigurator.SAM_RESOURCE_PROFILE_NAME);

      Card card = cardRepository.readCard(cardReader, calypsoCard, samResource);
      logger.info("{}", card);
      activityService.push(
          new Activity()
              .setPlugin(CUSTOM_PLUGIN)
              .setStatus(SUCCESS)
              .setType(SECURED_READ)
              .setCardSerialNumber(HexUtil.toHex(calypsoCard.getApplicationSerialNumber())));
      List<ContractStructure> validContracts = findValidContracts(card);

      for (ContractStructure contractStructure : validContracts) {
        output.add(formatContractStructure(contractStructure));
      }
    } catch (CardNotPersonalizedException e) {
      statusCode = 3;
      logger.error(AN_ERROR_OCCURRED_WHILE_INCREASING_THE_CONTRACT_COUNTER, e.getMessage());
      message = CARD_NOT_PERSONALIZED;
    } catch (ExpiredEnvironmentException e) {
      statusCode = 4;
      logger.error(AN_ERROR_OCCURRED_WHILE_INCREASING_THE_CONTRACT_COUNTER, e.getMessage());
      message = ENVIRONMENT_EXPIRED;
    } catch (RuntimeException e) {
      statusCode = 1;
      logger.error(AN_ERROR_OCCURRED_WHILE_INCREASING_THE_CONTRACT_COUNTER, e.getMessage(), e);
      message = RUNTIME_EXCEPTION + e.getMessage();
    } finally {
      activityService.push(
          new Activity()
              .setPlugin(CUSTOM_PLUGIN)
              .setStatus(statusCode == 0 ? SUCCESS : FAIL)
              .setType(RELOAD)
              .setCardSerialNumber(
                  calypsoCard == null
                      ? "-"
                      : HexUtil.toHex(calypsoCard.getApplicationSerialNumber())));
      if (samResource != null) {
        CardResourceServiceProvider.getService().releaseCardResource(samResource);
      }
    }
    return new SelectAppAndReadContractsOutputDto(output, statusCode, message);
  }

  SelectAppAndIncreaseContractCounterOutputDto selectAppAndIncreaseContractCounter(
      CardReader cardReader, SelectAppAndIncreaseContractCounterInputDto inputData) {

    CardSelectionManager cardSelectionManager = createCardSelectionManager();

    CalypsoCard calypsoCard = null;
    CardResource samResource = null;
    int statusCode = 0;
    String message = "Success.";
    try {
      // Actual card communication: run the selection scenario.
      CardSelectionResult selectionResult =
          cardSelectionManager.processCardSelectionScenario(cardReader);

      // Check the selection result.
      if (selectionResult.getActiveSmartCard() == null) {
        throw new IllegalStateException("The selection of the application " + AID + " failed.");
      }

      // Get the SmartCard resulting of the selection.
      calypsoCard = (CalypsoCard) selectionResult.getActiveSmartCard();

      samResource =
          CardResourceServiceProvider.getService()
              .getCardResource(CardConfigurator.SAM_RESOURCE_PROFILE_NAME);

      Card card = cardRepository.readCard(cardReader, calypsoCard, samResource);
      logger.info("{}", card);
      activityService.push(
          new Activity()
              .setPlugin(CUSTOM_PLUGIN)
              .setStatus(SUCCESS)
              .setType(RELOAD)
              .setCardSerialNumber(HexUtil.toHex(calypsoCard.getApplicationSerialNumber()))
              .setContractLoaded("MULTI TRIP: " + inputData.getCounterIncrement()));
      insertNewContract(PriorityCode.MULTI_TRIP, inputData.getCounterIncrement(), card);
      statusCode = cardRepository.writeCard(cardReader, calypsoCard, samResource, card);
    } catch (CardNotPersonalizedException e) {
      statusCode = 3;
      message = CARD_NOT_PERSONALIZED;
      logger.error(AN_ERROR_OCCURRED_WHILE_INCREASING_THE_CONTRACT_COUNTER, e.getMessage());
    } catch (ExpiredEnvironmentException e) {
      statusCode = 4;
      message = ENVIRONMENT_EXPIRED;
      logger.error(AN_ERROR_OCCURRED_WHILE_INCREASING_THE_CONTRACT_COUNTER, e.getMessage());
    } catch (RuntimeException e) {
      statusCode = 1;
      message = RUNTIME_EXCEPTION + e.getMessage();
      logger.error(AN_ERROR_OCCURRED_WHILE_INCREASING_THE_CONTRACT_COUNTER, e.getMessage(), e);
    } finally {
      activityService.push(
          new Activity()
              .setPlugin(CUSTOM_PLUGIN)
              .setStatus(statusCode == 0 ? SUCCESS : FAIL)
              .setType(RELOAD)
              .setCardSerialNumber(
                  calypsoCard == null
                      ? "-"
                      : HexUtil.toHex(calypsoCard.getApplicationSerialNumber())));
      if (samResource != null) {
        CardResourceServiceProvider.getService().releaseCardResource(samResource);
      }
    }
    return new SelectAppAndIncreaseContractCounterOutputDto(statusCode, message);
  }

  AnalyzeContractsOutputDto analyzeContracts(
      CardReader cardReader, CalypsoCard calypsoCard, AnalyzeContractsInputDto inputData) {

    String pluginType = inputData.getPluginType();
    String appSerialNumber = HexUtil.toHex(calypsoCard.getApplicationSerialNumber());

    if (!CardConstant.Companion.getALLOWED_FILE_STRUCTURES()
        .contains(calypsoCard.getApplicationSubtype())) {
      return new AnalyzeContractsOutputDto(Collections.emptyList(), 2);
    }

    CardResource samResource =
        CardResourceServiceProvider.getService()
            .getCardResource(CardConfigurator.SAM_RESOURCE_PROFILE_NAME);
    try {
      Card card = cardRepository.readCard(cardReader, calypsoCard, samResource);
      logger.info("{}", card);
      List<ContractStructure> validContracts = findValidContracts(card);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(SUCCESS)
              .setType(SECURED_READ)
              .setCardSerialNumber(appSerialNumber));
      return new AnalyzeContractsOutputDto(validContracts, 0);
    } catch (CardNotPersonalizedException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_ANALYZING_THE_CONTRACTS, e.getMessage());
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(SECURED_READ)
              .setCardSerialNumber(appSerialNumber));
      return new AnalyzeContractsOutputDto(Collections.emptyList(), 3);
    } catch (ExpiredEnvironmentException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_ANALYZING_THE_CONTRACTS, e.getMessage());
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(FAIL)
              .setType(SECURED_READ)
              .setCardSerialNumber(appSerialNumber));
      return new AnalyzeContractsOutputDto(Collections.emptyList(), 4);
    } catch (RuntimeException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_ANALYZING_THE_CONTRACTS, e.getMessage(), e);
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

  WriteContractOutputDto writeContract(
      CardReader cardReader, CalypsoCard calypsoCard, WriteContractInputDto inputData) {

    String pluginType = inputData.getPluginType();
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
      Card card = cardRepository.readCard(cardReader, calypsoCard, samResource);
      if (card == null) {
        // If card has not been read previously, throw error
        return new WriteContractOutputDto(3);
      }
      logger.info("{}", card);
      insertNewContract(inputData.getContractTariff(), inputData.getTicketToLoad(), card);
      int statusCode = cardRepository.writeCard(cardReader, calypsoCard, samResource, card);
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
      logger.error(AN_ERROR_OCCURRED_WHILE_WRITING_THE_CONTRACT, e.getMessage(), e);
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

  CardIssuanceOutputDto initCard(
      CardReader cardReader, CalypsoCard calypsoCard, CardIssuanceInputDto inputData) {

    String pluginType = inputData.getPluginType();
    String appSerialNumber = HexUtil.toHex(calypsoCard.getApplicationSerialNumber());

    if (!CardConstant.Companion.getALLOWED_FILE_STRUCTURES()
        .contains(calypsoCard.getApplicationSubtype())) {
      return new CardIssuanceOutputDto(2);
    }

    CardResource samResource =
        CardResourceServiceProvider.getService()
            .getCardResource(CardConfigurator.SAM_RESOURCE_PROFILE_NAME);
    try {
      cardRepository.initCard(cardReader, calypsoCard, samResource);
      activityService.push(
          new Activity()
              .setPlugin(pluginType)
              .setStatus(SUCCESS)
              .setType(ISSUANCE)
              .setCardSerialNumber(appSerialNumber));
      return new CardIssuanceOutputDto(0);
    } catch (RuntimeException e) {
      logger.error(AN_ERROR_OCCURRED_WHILE_INITIALIZING_THE_CARD, e.getMessage(), e);
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
      logger.warn(VERSION_NUMBER_OF_CARD_IS_INVALID_REJECT_CARD);
      throw new CardNotPersonalizedException();
    }
    if (environment.getEnvEndDate().getDate().isBefore(LocalDate.now())) {
      logger.warn(ENV_END_DATE_OF_CARD_IS_INVALID_REJECT_CARD);
      throw new ExpiredEnvironmentException();
    }
    // Check last event
    EventStructure lastEvent = card.getEvent();
    if (lastEvent.getEventVersionNumber() != VersionNumber.CURRENT_VERSION
        && lastEvent.getEventVersionNumber() != VersionNumber.UNDEFINED) {
      logger.warn(VERSION_NUMBER_OF_CARD_IS_INVALID_REJECT_CARD1);
      return Collections.emptyList();
    }
    // Iterate through the contracts in the card session
    List<ContractStructure> contracts = card.getContracts();
    List<ContractStructure> validContracts = new ArrayList<>();
    int calypsoIndex = 1;
    for (ContractStructure contract : contracts) {
      logger.info(
          CONTRACT_AT_INDEX,
          calypsoIndex,
          contract.getContractTariff(),
          contract.getContractSaleDate().getValue());
      if (contract.getContractVersionNumber() == VersionNumber.UNDEFINED) {
        // If ContractVersionNumber is 0 ensure that the associated ContractPriority field value is
        // 0 and move on to the next contract.
        if (contract.getContractTariff() != PriorityCode.FORBIDDEN) {
          logger.warn(CONTRACT_TARIFF_IS_NOT_VALID_FOR_THIS_CONTRACT);
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
    logger.info(CONTRACTS, Arrays.deepToString(validContracts.toArray()));
    return validContracts;
  }

  private void insertNewContract(PriorityCode contractTariff, Integer ticketToLoad, Card card) {

    if (contractTariff != PriorityCode.SEASON_PASS && contractTariff != PriorityCode.MULTI_TRIP) {
      throw new IllegalArgumentException(ONLY_SEASON_PASS_OR_MULTI_TRIP_TICKET_CAN_BE_LOADED);
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
        throw new IllegalStateException(UNEXPECTED_CONTRACT_NUMBER + newContractNumber);
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

  private static class CardNotPersonalizedException extends RuntimeException {
    CardNotPersonalizedException() {
      super(THE_CARD_IS_NOT_PERSONALIZED);
    }
  }

  private static class ExpiredEnvironmentException extends RuntimeException {
    ExpiredEnvironmentException() {
      super(THE_ENVIRONMENT_HAS_EXPIRED);
    }
  }
}
