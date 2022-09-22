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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.calypsonet.keyple.demo.common.model.ContractStructure;
import org.calypsonet.keyple.demo.common.model.EnvironmentHolderStructure;
import org.calypsonet.keyple.demo.common.model.EventStructure;
import org.calypsonet.keyple.demo.common.model.type.DateCompact;
import org.calypsonet.keyple.demo.common.model.type.PriorityCode;
import org.calypsonet.keyple.demo.common.model.type.VersionNumber;
import org.calypsonet.keyple.demo.common.parser.ContractStructureParser;
import org.calypsonet.keyple.demo.common.parser.EnvironmentHolderStructureParser;
import org.calypsonet.keyple.demo.common.parser.EventStructureParser;
import org.calypsonet.terminal.calypso.card.CalypsoCard;
import org.calypsonet.terminal.calypso.card.FileData;
import org.cna.keyple.demo.distributed.server.util.CalypsoConstants;
import org.cna.keyple.demo.distributed.server.util.CalypsoUtils;
import org.eclipse.keyple.core.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds a Calypso Card Content and provides method to prepare an update of the card. Use the {@link
 * #parse(CalypsoCard)} method to build this object.
 */
public class CalypsoCardRepresentation {

  private static final Logger logger = LoggerFactory.getLogger(CalypsoCardRepresentation.class);

  private EventStructure event;
  private final List<ContractStructure> contracts;
  private EnvironmentHolderStructure environment;

  private final List<ContractStructure> updatedContracts; // Updated contracts in this object
  private Boolean isEventUpdated;

  private CalypsoCardRepresentation() {
    contracts = new ArrayList<>();
    updatedContracts = new ArrayList<>();
    isEventUpdated = false;
  }

  /**
   * Parse a Calypso Card object to a card content object
   *
   * @param calypsoCard not null calypsoCard object
   * @return cardSession not null object
   */
  public static CalypsoCardRepresentation parse(CalypsoCard calypsoCard) {
    CalypsoCardRepresentation card = new CalypsoCardRepresentation();
    int contractCount = CalypsoUtils.getContractCount(calypsoCard);

    // parse event
    card.event =
        new EventStructureParser()
            .parse(calypsoCard.getFileBySfi(SFI_EVENT_LOG).getData().getContent());

    // parse contracts
    FileData fileData = calypsoCard.getFileBySfi(SFI_CONTRACTS).getData();
    if (fileData != null) {
      for (int i = 1; i < contractCount + 1; i++) {
        ContractStructure contract = new ContractStructureParser().parse(fileData.getContent(i));
        card.contracts.add(contract);

        // update counter tied to contract
        int counterValue =
            calypsoCard.getFileBySfi(SFI_COUNTERS).getData().getContentAsCounterValue(i);

        contract.setCounterValue(counterValue);
      }
    }

    // parse environment
    card.environment =
        new EnvironmentHolderStructureParser()
            .parse(
                calypsoCard
                    .getFileBySfi(CalypsoConstants.SFI_ENVIRONMENT_AND_HOLDER)
                    .getData()
                    .getContent());

    return card;
  }

  /**
   * Insert a new contract in the card content
   *
   * @param contractTariff priority code of the contract to write (mandatory)
   * @param ticketToLoad number of ticket to load (optional)
   */
  public void insertNewContract(PriorityCode contractTariff, Integer ticketToLoad) {
    if (contractTariff != PriorityCode.SEASON_PASS && contractTariff != PriorityCode.MULTI_TRIP) {
      throw new IllegalArgumentException("Only Season Pass or Multi Trip ticket can be loaded");
    }

    // find contract in card
    int existingContractIndex = isReload(contractTariff);
    int newContractIndex;

    EventStructure currentEvent = getEvent();
    ContractStructure newContract = null;

    // if is a renew
    if (existingContractIndex > 0) {
      newContractIndex = existingContractIndex;
      ContractStructure currentContract = getContractByCalypsoIndex(existingContractIndex);

      // build new contract
      if (PriorityCode.MULTI_TRIP == contractTariff) {
        newContract =
            buildMultiTripContract(
                environment.getEnvEndDate(), currentContract.getCounterValue() + ticketToLoad);
      } else if (PriorityCode.SEASON_PASS == contractTariff) {
        newContract = buildSeasonContract(environment.getEnvEndDate());
      }

    } else {
      // is a new contract
      int newPosition = findAvailablePosition();

      if (newPosition == 0) {
        // no available position, reject card
        return;
      }
      newContractIndex = newPosition;

      // build new contract
      if (PriorityCode.MULTI_TRIP == contractTariff) {
        newContract = buildMultiTripContract(environment.getEnvEndDate(), ticketToLoad);
      } else if (PriorityCode.SEASON_PASS == contractTariff) {
        newContract = buildSeasonContract(environment.getEnvEndDate());
      }
    }

    switch (newContractIndex) {
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
    }
    updateContract(newContractIndex, newContract);
    updateEvent(currentEvent);
  }

  /**
   * List valid contracts and counters
   *
   * @return list of valid contracts, null if error
   */
  public List<ContractStructure> listValidContracts() {
    // output
    List<ContractStructure> validContracts = new ArrayList<>();

    // read environment
    EnvironmentHolderStructure environment = getEnvironment();

    if (environment.getEnvVersionNumber() != VersionNumber.CURRENT_VERSION) {
      // reject card
      logger.warn("Version Number of card is invalid, reject card");
      return null;
    }

    if (environment.getEnvEndDate().getValue() < new DateCompact(new Date()).getValue()) {
      // reject card
      logger.warn("EnvEndDate of card is invalid, reject card");
      return null;
    }

    EventStructure lastEvent = getEvent();

    if (lastEvent.getEventVersionNumber().getValue() != VersionNumber.CURRENT_VERSION.getValue()
        && lastEvent.getEventVersionNumber().getValue() != VersionNumber.UNDEFINED.getValue()) {
      // reject card
      logger.warn("EventVersionNumber of card is invalid, reject card");
      return null;
    }

    int calypsoIndex = 1;
    /* Iterate through the contracts in the card session */
    for (ContractStructure contract : getContracts()) {
      logger.info(
          "Contract at index {} : {} {}",
          calypsoIndex,
          contract.getContractTariff(),
          contract.getContractSaleDate().getValue());

      if (contract.getContractVersionNumber() == VersionNumber.UNDEFINED) {
        //  If ContractVersionNumber is 0 ensure that the associated ContractPriority field value is
        // 0 and
        //  move on to the next contract.
        if (contract.getContractTariff() != PriorityCode.FORBIDDEN) {
          logger.warn("Contract tariff is not valid for this contract");
          // todo what to do here?
        }

      } else {
        if (contract.getContractAuthenticator() != null) {
          // PSO Verify Signature command of the SAM.
        }
        // If ContractValidityEndDate points to a date in the past
        if (contract.getContractValidityEndDate().getValue()
            < new DateCompact(new Date()).getValue()) {
          //  Update the associated ContractPriorty field present
          //  in the persistent object to 31 and set the change flag to true.
          contract.setContractTariff(PriorityCode.EXPIRED);
          updateContract(calypsoIndex, contract);
        }
        validContracts.add(contract);
      }
      calypsoIndex++;
    }

    // results
    logger.info("Contracts {}", Arrays.deepToString(validContracts.toArray()));
    return validContracts;
  }

  public EventStructure getEvent() {
    return event;
  }

  public List<ContractStructure> getContracts() {
    return contracts;
  }

  public EnvironmentHolderStructure getEnvironment() {
    return environment;
  }

  public ContractStructure getContractByCalypsoIndex(int i) {
    return contracts.get(i - 1);
  }

  public List<ContractStructure> getUpdatedContracts() {
    return updatedContracts;
  }

  public Boolean isEventUpdated() {
    return isEventUpdated;
  }

  @Override
  public String toString() {
    return "CalypsoCardContent{"
        + "event="
        + event
        + ", contracts="
        + Arrays.deepToString(contracts.toArray())
        + ", environment="
        + environment
        + ", contractUpdated="
        + updatedContracts
        + ", eventUpdated="
        + isEventUpdated
        + '}';
  }

  /**
   * (private) Update event in this object
   *
   * @param event new event
   */
  private void updateEvent(EventStructure event) {
    this.event = event;
    this.isEventUpdated = true;
  }

  /**
   * (private) Update contract at a specific index
   *
   * @param contract not nullable contract object
   * @param calypsoIndex calypso index where to update the contract
   */
  private void updateContract(int calypsoIndex, ContractStructure contract) {
    Assert.getInstance().notNull(contract, "contract should not be null");
    contracts.set(calypsoIndex - 1, contract);
    updatedContracts.add(contract);
  }

  /**
   * (private) Return the calypso index of the contractTariff if present in the card
   *
   * @param contractTariff
   * @return calypso index (1-4), 0 if none
   */
  private int isReload(PriorityCode contractTariff) {
    int contractCount = this.contracts.size();
    for (int i = 0; i < contractCount; i++) {
      if (contractTariff.equals(contracts.get(i).getContractTariff())) {
        return i + 1;
      }
    }
    return 0;
  }

  /**
   * (private) Find the position for a new contract
   *
   * @return calypso index (1-4), 0 if none
   */
  private int findAvailablePosition() {
    int contractCount = this.contracts.size();
    for (int i = 0; i < contractCount; i++) {
      if (PriorityCode.FORBIDDEN.equals(contracts.get(i).getContractTariff())) {
        return i + 1;
      }
    }

    for (int i = 0; i < contractCount; i++) {
      if (PriorityCode.EXPIRED.equals(contracts.get(i).getContractTariff())) {
        return i + 1;
      }
    }

    return 0;
  }

  /**
   * (private) Fill the contract structure to update: - ContractVersionNumber = 1 - ContractTariff =
   * Value provided by upper layer - ContractSaleDate = Current Date converted to DateCompact
   *
   * @param envEndDate
   * @param counterValue
   * @return a new contract
   */
  private ContractStructure buildMultiTripContract(DateCompact envEndDate, Integer counterValue) {
    DateCompact contractSaleDate = new DateCompact(new Date());
    DateCompact contractValidityEndDate;

    // calculate ContractValidityEndDate
    contractValidityEndDate = envEndDate;

    ContractStructure contract =
        new ContractStructure(
            VersionNumber.CURRENT_VERSION,
            PriorityCode.MULTI_TRIP,
            contractSaleDate,
            contractValidityEndDate,
            null,
            null,
            null,
            null);

    contract.setCounterValue(counterValue);

    return contract;
  }

  /**
   * (private) Fill the contract structure to update: - ContractVersionNumber = 1 - ContractTariff =
   * Value provided by upper layer - ContractSaleDate = Current Date converted to DateCompact
   *
   * @param envEndDate
   * @return a new contract
   */
  private ContractStructure buildSeasonContract(DateCompact envEndDate) {
    DateCompact contractSaleDate = new DateCompact(new Date());
    DateCompact contractValidityEndDate;

    contractValidityEndDate = new DateCompact((short) (contractSaleDate.getValue() + 30));

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
}
