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
import java.time.Instant;
import org.calypsonet.terminal.calypso.card.CalypsoCard;
import org.cna.keyple.demo.distributed.server.calypso.CalypsoCardController;
import org.cna.keyple.demo.distributed.server.calypso.CalypsoCardRepresentation;
import org.cna.keyple.demo.distributed.server.plugin.SamResourceConfiguration;
import org.cna.keyple.demo.distributed.server.util.CalypsoConstants;
import org.cna.keyple.demo.distributed.server.util.CalypsoUtils;
import org.cna.keyple.demo.local.procedure.LocalConfigurationUtil;
import org.cna.keyple.demo.sale.data.model.ContractStructureDto;
import org.cna.keyple.demo.sale.data.model.EventStructureDto;
import org.cna.keyple.demo.sale.data.model.type.DateCompact;
import org.cna.keyple.demo.sale.data.model.type.PriorityCode;
import org.cna.keyple.demo.sale.data.model.type.VersionNumber;
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
    Assertions.assertEquals(PriorityCode.FORBIDDEN, card.getEvent().getContractPriorityAt(1));
    Assertions.assertEquals(PriorityCode.FORBIDDEN, card.getEvent().getContractPriorityAt(2));
    if (contractCount > 2) {
      Assertions.assertEquals(
          PriorityCode.FORBIDDEN, card.getContractByCalypsoIndex(3).getContractTariff());
      Assertions.assertEquals(
          PriorityCode.FORBIDDEN, card.getContractByCalypsoIndex(4).getContractTariff());
      Assertions.assertEquals(PriorityCode.FORBIDDEN, card.getEvent().getContractPriorityAt(3));
      Assertions.assertEquals(PriorityCode.FORBIDDEN, card.getEvent().getContractPriorityAt(4));
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
    ContractStructureDto updatedContract = updatedCard.getContractByCalypsoIndex(1);
    logger.trace("updatedCard : {}", updatedCard);
    Assertions.assertEquals(1, updatedCard.listValidContracts().size());
    Assertions.assertEquals(PriorityCode.SEASON_PASS, updatedContract.getContractTariff());
    Assertions.assertEquals(0, updatedContract.getCounter().getCounterValue());
    Assertions.assertEquals(
        VersionNumber.CURRENT_VERSION, updatedContract.getContractVersionNumber());
    DateCompact today = new DateCompact(Instant.now());
    Assertions.assertEquals(today, updatedContract.getContactSaleDate());
    Assertions.assertEquals(
        today.getDaysSinceReference() + 30,
        updatedContract.getContractValidityEndDate().getDaysSinceReference());
    Assertions.assertEquals(
        PriorityCode.SEASON_PASS, updatedCard.getEvent().getContractPriorityAt(1));
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
    ContractStructureDto updatedContract = updatedCard.getContractByCalypsoIndex(1);
    DateCompact today = new DateCompact(Instant.now());

    Assertions.assertEquals(1, updatedCard.listValidContracts().size());
    Assertions.assertEquals(PriorityCode.SEASON_PASS, updatedContract.getContractTariff());
    Assertions.assertEquals(0, updatedContract.getCounter().getCounterValue());
    Assertions.assertEquals(
        VersionNumber.CURRENT_VERSION, updatedContract.getContractVersionNumber());
    Assertions.assertEquals(today, updatedContract.getContactSaleDate());
    Assertions.assertEquals(
        today.getDaysSinceReference() + 30,
        updatedContract.getContractValidityEndDate().getDaysSinceReference());
    Assertions.assertEquals(
        PriorityCode.FORBIDDEN, updatedCard.getContractByCalypsoIndex(2).getContractTariff());
    Assertions.assertEquals(
        PriorityCode.SEASON_PASS, updatedCard.getEvent().getContractPriorityAt(1));
    Assertions.assertEquals(
        PriorityCode.FORBIDDEN, updatedCard.getEvent().getContractPriorityAt(2));
    if (contractCount > 2) {
      Assertions.assertEquals(
          PriorityCode.FORBIDDEN, updatedCard.getContractByCalypsoIndex(3).getContractTariff());
      Assertions.assertEquals(
          PriorityCode.FORBIDDEN, updatedCard.getContractByCalypsoIndex(4).getContractTariff());
      Assertions.assertEquals(
          PriorityCode.FORBIDDEN, updatedCard.getEvent().getContractPriorityAt(3));
      Assertions.assertEquals(
          PriorityCode.FORBIDDEN, updatedCard.getEvent().getContractPriorityAt(4));
    }
  }

  @Test
  public void load_ticket_on_empty_card() {
    // prepare card
    init_card();

    // test
    CalypsoCardRepresentation card = calypsoCardController.readCard();
    card.insertNewContract(PriorityCode.MULTI_TRIP_TICKET, 1);
    calypsoCardController.writeCard(card);

    // check
    CalypsoCardRepresentation updatedCard = calypsoCardController.readCard();
    ContractStructureDto updatedContract = updatedCard.getContractByCalypsoIndex(1);

    // logger.trace("updatedCard : {}", updatedCard);
    Assertions.assertEquals(1, updatedCard.listValidContracts().size());
    Assertions.assertEquals(PriorityCode.MULTI_TRIP_TICKET, updatedContract.getContractTariff());
    Assertions.assertEquals(1, updatedContract.getCounter().getCounterValue());
    Assertions.assertEquals(
        PriorityCode.MULTI_TRIP_TICKET, updatedCard.getEvent().getContractPriorityAt(1));
  }

  @Test
  public void renew_load_ticket() {
    // prepare card
    load_ticket_on_empty_card();

    // test
    CalypsoCardRepresentation card = calypsoCardController.readCard();
    card.insertNewContract(PriorityCode.MULTI_TRIP_TICKET, 1);
    calypsoCardController.writeCard(card);

    // check
    CalypsoCardRepresentation updatedCard = calypsoCardController.readCard();
    ContractStructureDto updatedContract = updatedCard.getContractByCalypsoIndex(1);
    EventStructureDto event = updatedCard.getEvent();

    Assertions.assertEquals(1, updatedCard.listValidContracts().size());
    Assertions.assertEquals(PriorityCode.MULTI_TRIP_TICKET, updatedContract.getContractTariff());
    Assertions.assertEquals(2, updatedContract.getCounter().getCounterValue());
    Assertions.assertEquals(
        PriorityCode.FORBIDDEN, updatedCard.getContractByCalypsoIndex(2).getContractTariff());
    Assertions.assertEquals(PriorityCode.MULTI_TRIP_TICKET, event.getContractPriorityAt(1));
    Assertions.assertEquals(PriorityCode.FORBIDDEN, event.getContractPriorityAt(2));
    if (contractCount > 2) {

      Assertions.assertEquals(
          PriorityCode.FORBIDDEN, updatedCard.getContractByCalypsoIndex(3).getContractTariff());
      Assertions.assertEquals(
          PriorityCode.FORBIDDEN, updatedCard.getContractByCalypsoIndex(4).getContractTariff());
      Assertions.assertEquals(PriorityCode.FORBIDDEN, event.getContractPriorityAt(3));
      Assertions.assertEquals(PriorityCode.FORBIDDEN, event.getContractPriorityAt(4));
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
    ContractStructureDto updatedContract = updatedCard.getContractByCalypsoIndex(2);
    EventStructureDto event = updatedCard.getEvent();

    Assertions.assertEquals(2, updatedCard.listValidContracts().size());
    Assertions.assertEquals(
        PriorityCode.MULTI_TRIP_TICKET,
        updatedCard.getContractByCalypsoIndex(1).getContractTariff());
    Assertions.assertEquals(PriorityCode.SEASON_PASS, updatedContract.getContractTariff());
    Assertions.assertEquals(PriorityCode.MULTI_TRIP_TICKET, event.getContractPriorityAt(1));
    Assertions.assertEquals(PriorityCode.SEASON_PASS, event.getContractPriorityAt(2));
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
    card.insertNewContract(PriorityCode.MULTI_TRIP_TICKET, 1);
    calypsoCardController.writeCard(card);

    // check
    CalypsoCardRepresentation updatedCard = calypsoCardController.readCard();
    ContractStructureDto updatedContract = updatedCard.getContractByCalypsoIndex(2);
    EventStructureDto event = updatedCard.getEvent();

    Assertions.assertEquals(2, updatedCard.listValidContracts().size());
    Assertions.assertEquals(
        PriorityCode.SEASON_PASS, updatedCard.getContractByCalypsoIndex(1).getContractTariff());
    Assertions.assertEquals(
        0, updatedCard.getContractByCalypsoIndex(1).getCounter().getCounterValue());
    Assertions.assertEquals(PriorityCode.MULTI_TRIP_TICKET, updatedContract.getContractTariff());
    Assertions.assertEquals(1, updatedContract.getCounter().getCounterValue());
    Assertions.assertEquals(PriorityCode.SEASON_PASS, event.getContractPriorityAt(1));
    Assertions.assertEquals(PriorityCode.MULTI_TRIP_TICKET, event.getContractPriorityAt(2));
  }
}
