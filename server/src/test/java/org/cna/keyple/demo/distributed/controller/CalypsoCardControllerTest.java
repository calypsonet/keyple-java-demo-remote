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
package org.cna.keyple.demo.distributed.controller;

import io.quarkus.test.junit.QuarkusTest;
import java.util.Date;
import org.calypsonet.keyple.demo.common.model.ContractStructure;
import org.calypsonet.keyple.demo.common.model.EventStructure;
import org.calypsonet.keyple.demo.common.model.type.DateCompact;
import org.calypsonet.keyple.demo.common.model.type.PriorityCode;
import org.calypsonet.keyple.demo.common.model.type.VersionNumber;
import org.calypsonet.terminal.calypso.card.CalypsoCard;
import org.cna.keyple.demo.distributed.server.calypso.CalypsoCardController;
import org.cna.keyple.demo.distributed.server.calypso.CalypsoCardRepresentation;
import org.cna.keyple.demo.distributed.server.plugin.SamResourceConfiguration;
import org.cna.keyple.demo.distributed.server.util.CalypsoConstants;
import org.cna.keyple.demo.distributed.server.util.CalypsoUtils;
import org.cna.keyple.demo.local.procedure.LocalConfigurationUtil;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.resource.CardResource;
import org.eclipse.keyple.core.service.resource.CardResourceServiceProvider;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Test the read and write operations atomically */
@QuarkusTest
public class CalypsoCardControllerTest {
  private static final Logger logger = LoggerFactory.getLogger(CalypsoCardControllerTest.class);

  private static Reader poReader;
  private CalypsoCardController calypsoCardController;
  private static CalypsoCard calypsoPo;
  private CardResource samResource;
  private static final String poReaderFilter = ".*(ASK|ACS).*";
  public static String samReaderFilter = ".*(Cherry TC|SCM Microsystems|Identive|HID|Generic).*";
  public int contractCount;
  static SamResourceConfiguration samResourceConfiguration;

  @BeforeAll
  public static void staticSetUp() {
    samResourceConfiguration = new SamResourceConfiguration(samReaderFilter);
    samResourceConfiguration.init();

    /* Get PO Reader */
    poReader = LocalConfigurationUtil.initReader(poReaderFilter);

    /* select PO */
    calypsoPo = CalypsoUtils.selectCard(poReader);
  }

  @BeforeEach
  public void setUp() {

    /* Calculate how many contracts are expected on the card;
     * 4 contracts for a Calypso PRIME;
     * 2 contracts for a Calypso LIGHT;
     * */
    contractCount = CalypsoUtils.getContractCount(calypsoPo);

    /* Request SAM resource before each test */
    samResource =
        CardResourceServiceProvider.getService().getCardResource(CalypsoConstants.SAM_PROFILE_NAME);

    /* Create a calypso card controller */
    calypsoCardController =
        CalypsoCardController.newBuilder()
            .withCalypsoCard(calypsoPo)
            .withCardReader(poReader)
            .withSamResource(samResource)
            .build();
  }

  @AfterEach
  public void tearDown() {
    /* Release SAM resource after each test */
    CardResourceServiceProvider.getService().releaseCardResource(samResource);
  }

  @Test
  public void init_card() {
    // init card
    calypsoCardController.initCard();

    // read card
    CalypsoCardRepresentation card = calypsoCardController.readCard();
    Assertions.assertEquals(contractCount, card.getContracts().size());
    Assertions.assertEquals(0, card.listValidContracts().size());
    Assertions.assertEquals(
        PriorityCode.FORBIDDEN, card.getContractByCalypsoIndex(1).getContractTariff());
    Assertions.assertEquals(
        PriorityCode.FORBIDDEN, card.getContractByCalypsoIndex(2).getContractTariff());
    Assertions.assertEquals(PriorityCode.FORBIDDEN, card.getEvent().getContractPriority1());
    Assertions.assertEquals(PriorityCode.FORBIDDEN, card.getEvent().getContractPriority2());

    // if card contains more than 2 contracts, check them too
    if (contractCount > 2) {
      Assertions.assertEquals(
          PriorityCode.FORBIDDEN, card.getContractByCalypsoIndex(3).getContractTariff());
      Assertions.assertEquals(
          PriorityCode.FORBIDDEN, card.getContractByCalypsoIndex(4).getContractTariff());
      Assertions.assertEquals(PriorityCode.FORBIDDEN, card.getEvent().getContractPriority3());
      Assertions.assertEquals(PriorityCode.FORBIDDEN, card.getEvent().getContractPriority4());
    }
  }

  @Test
  public void load_season_pass_on_empty_card() {
    // prepare card
    init_card();

    // test
    CalypsoCardRepresentation card = calypsoCardController.readCard();
    card.insertNewContract(PriorityCode.SEASON_PASS, null);
    calypsoCardController.writeCard(card);

    // check
    CalypsoCardRepresentation updatedCard = calypsoCardController.readCard();
    ContractStructure updatedContract = updatedCard.getContractByCalypsoIndex(1);
    logger.trace("updatedCard : {}", updatedCard);
    Assertions.assertEquals(1, updatedCard.listValidContracts().size());
    Assertions.assertEquals(PriorityCode.SEASON_PASS, updatedContract.getContractTariff());
    Assertions.assertEquals(0, updatedContract.getCounterValue());
    Assertions.assertEquals(
        VersionNumber.CURRENT_VERSION, updatedContract.getContractVersionNumber());
    DateCompact today = new DateCompact(new Date());
    Assertions.assertEquals(today, updatedContract.getContractSaleDate());
    Assertions.assertEquals(
        today.getValue() + 30, updatedContract.getContractValidityEndDate().getValue());
    Assertions.assertEquals(
        PriorityCode.SEASON_PASS, updatedCard.getEvent().getContractPriority1());
  }

  @Test
  public void renew_season_pass() {
    // prepare card
    load_season_pass_on_empty_card();

    // test
    CalypsoCardRepresentation card = calypsoCardController.readCard();
    card.insertNewContract(PriorityCode.SEASON_PASS, null);
    calypsoCardController.writeCard(card);

    // check
    CalypsoCardRepresentation updatedCard = calypsoCardController.readCard();
    ContractStructure updatedContract = updatedCard.getContractByCalypsoIndex(1);
    DateCompact today = new DateCompact(new Date());

    Assertions.assertEquals(1, updatedCard.listValidContracts().size());
    Assertions.assertEquals(PriorityCode.SEASON_PASS, updatedContract.getContractTariff());
    Assertions.assertEquals(0, updatedContract.getCounterValue());
    Assertions.assertEquals(
        VersionNumber.CURRENT_VERSION, updatedContract.getContractVersionNumber());
    Assertions.assertEquals(today, updatedContract.getContractSaleDate());
    Assertions.assertEquals(
        today.getValue() + 30, updatedContract.getContractValidityEndDate().getValue());
    Assertions.assertEquals(
        PriorityCode.FORBIDDEN, updatedCard.getContractByCalypsoIndex(2).getContractTariff());
    Assertions.assertEquals(
        PriorityCode.SEASON_PASS, updatedCard.getEvent().getContractPriority1());
    Assertions.assertEquals(PriorityCode.FORBIDDEN, updatedCard.getEvent().getContractPriority2());

    // if card contains more than 2 contracts, check them too
    if (contractCount > 2) {
      Assertions.assertEquals(
          PriorityCode.FORBIDDEN, updatedCard.getContractByCalypsoIndex(3).getContractTariff());
      Assertions.assertEquals(
          PriorityCode.FORBIDDEN, updatedCard.getContractByCalypsoIndex(4).getContractTariff());
      Assertions.assertEquals(
          PriorityCode.FORBIDDEN, updatedCard.getEvent().getContractPriority3());
      Assertions.assertEquals(
          PriorityCode.FORBIDDEN, updatedCard.getEvent().getContractPriority4());
    }
  }

  @Test
  public void load_ticket_on_empty_card() {
    // prepare card
    init_card();

    // test
    CalypsoCardRepresentation card = calypsoCardController.readCard();
    card.insertNewContract(PriorityCode.MULTI_TRIP, 1);
    calypsoCardController.writeCard(card);

    // check
    CalypsoCardRepresentation updatedCard = calypsoCardController.readCard();
    ContractStructure updatedContract = updatedCard.getContractByCalypsoIndex(1);

    // logger.trace("updatedCard : {}", updatedCard);
    Assertions.assertEquals(1, updatedCard.listValidContracts().size());
    Assertions.assertEquals(PriorityCode.MULTI_TRIP, updatedContract.getContractTariff());
    Assertions.assertEquals(1, updatedContract.getCounterValue());
    Assertions.assertEquals(PriorityCode.MULTI_TRIP, updatedCard.getEvent().getContractPriority1());
  }

  @Test
  public void renew_load_ticket() {
    // prepare card
    load_ticket_on_empty_card();

    // test
    CalypsoCardRepresentation card = calypsoCardController.readCard();
    card.insertNewContract(PriorityCode.MULTI_TRIP, 1);
    calypsoCardController.writeCard(card);

    // check
    CalypsoCardRepresentation updatedCard = calypsoCardController.readCard();
    ContractStructure updatedContract = updatedCard.getContractByCalypsoIndex(1);
    EventStructure event = updatedCard.getEvent();

    Assertions.assertEquals(1, updatedCard.listValidContracts().size());
    Assertions.assertEquals(PriorityCode.MULTI_TRIP, updatedContract.getContractTariff());
    Assertions.assertEquals(2, updatedContract.getCounterValue());
    Assertions.assertEquals(
        PriorityCode.FORBIDDEN, updatedCard.getContractByCalypsoIndex(2).getContractTariff());
    Assertions.assertEquals(PriorityCode.MULTI_TRIP, event.getContractPriority1());
    Assertions.assertEquals(PriorityCode.FORBIDDEN, event.getContractPriority2());

    // if card contains more than 2 contracts, check them too
    if (contractCount > 2) {
      Assertions.assertEquals(
          PriorityCode.FORBIDDEN, updatedCard.getContractByCalypsoIndex(3).getContractTariff());
      Assertions.assertEquals(
          PriorityCode.FORBIDDEN, updatedCard.getContractByCalypsoIndex(4).getContractTariff());
      Assertions.assertEquals(PriorityCode.FORBIDDEN, event.getContractPriority3());
      Assertions.assertEquals(PriorityCode.FORBIDDEN, event.getContractPriority4());
    }
  }

  @Test
  public void load_season_pass_on_card_with_tickets() {
    load_ticket_on_empty_card();

    // test
    CalypsoCardRepresentation card = calypsoCardController.readCard();
    card.insertNewContract(PriorityCode.SEASON_PASS, null);
    calypsoCardController.writeCard(card);

    // check
    CalypsoCardRepresentation updatedCard = calypsoCardController.readCard();
    ContractStructure updatedContract = updatedCard.getContractByCalypsoIndex(2);
    EventStructure event = updatedCard.getEvent();

    Assertions.assertEquals(2, updatedCard.listValidContracts().size());
    Assertions.assertEquals(
        PriorityCode.MULTI_TRIP, updatedCard.getContractByCalypsoIndex(1).getContractTariff());
    Assertions.assertEquals(PriorityCode.SEASON_PASS, updatedContract.getContractTariff());
    Assertions.assertEquals(PriorityCode.MULTI_TRIP, event.getContractPriority1());
    Assertions.assertEquals(PriorityCode.SEASON_PASS, event.getContractPriority2());
  }

  @Test
  public void load_ticket_on_card_with_season_pass() {
    // prepare card
    init_card();
    CalypsoCardRepresentation initCard = calypsoCardController.readCard();
    initCard.insertNewContract(PriorityCode.SEASON_PASS, null);
    calypsoCardController.writeCard(initCard);

    // test
    CalypsoCardRepresentation card = calypsoCardController.readCard();
    card.insertNewContract(PriorityCode.MULTI_TRIP, 1);
    calypsoCardController.writeCard(card);

    // check
    CalypsoCardRepresentation updatedCard = calypsoCardController.readCard();
    ContractStructure updatedContract = updatedCard.getContractByCalypsoIndex(2);
    EventStructure event = updatedCard.getEvent();

    Assertions.assertEquals(2, updatedCard.listValidContracts().size());
    Assertions.assertEquals(
        PriorityCode.SEASON_PASS, updatedCard.getContractByCalypsoIndex(1).getContractTariff());
    Assertions.assertEquals(0, updatedCard.getContractByCalypsoIndex(1).getCounterValue());
    Assertions.assertEquals(PriorityCode.MULTI_TRIP, updatedContract.getContractTariff());
    Assertions.assertEquals(1, updatedContract.getCounterValue());
    Assertions.assertEquals(PriorityCode.SEASON_PASS, event.getContractPriority1());
    Assertions.assertEquals(PriorityCode.MULTI_TRIP, event.getContractPriority2());
  }
}
