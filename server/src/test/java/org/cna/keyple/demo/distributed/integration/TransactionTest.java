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
package org.cna.keyple.demo.distributed.integration;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import java.net.MalformedURLException;
import java.net.URL;
import javax.inject.Inject;
import org.calypsonet.keyple.demo.common.dto.*;
import org.calypsonet.keyple.demo.common.model.ContractStructure;
import org.calypsonet.keyple.demo.common.model.type.PriorityCode;
import org.calypsonet.terminal.calypso.card.CalypsoCard;
import org.calypsonet.terminal.reader.selection.spi.SmartCard;
import org.cna.keyple.demo.distributed.integration.client.EndpointClient;
import org.cna.keyple.demo.distributed.integration.client.SamClient;
import org.cna.keyple.demo.distributed.server.plugin.CalypsoCardResourceConfiguration;
import org.cna.keyple.demo.distributed.server.plugin.SamResourceConfiguration;
import org.cna.keyple.demo.distributed.server.util.CalypsoUtils;
import org.cna.keyple.demo.local.procedure.LocalConfigurationUtil;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.distributed.LocalServiceClient;
import org.eclipse.keyple.distributed.LocalServiceClientFactory;
import org.eclipse.keyple.distributed.LocalServiceClientFactoryBuilder;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test real transactions. Requires a Sam and Po pcsc reader along with a SAM and PO smartcard. */
@QuarkusTest
public class TransactionTest {

  private static final String LOCAL_SERVICE_NAME = "TransactionTest";
  private static final String PO_READER_FILTER = ".*(ASK|ACS).*";
  private static final Integer TICKETS_TO_LOAD = 10;

  @Inject @RestClient SamClient samClient;

  static EndpointClient endpointClient;

  @Inject CalypsoCardResourceConfiguration calypsoCardResourceConfiguration;

  @Inject SamResourceConfiguration samResourceConfiguration;

  static {
    try {
      endpointClient =
          RestClientBuilder.newBuilder()
              .baseUrl(new URL("http://0.0.0.0:8080/"))
              .build(EndpointClient.class);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
  }

  Reader cardReader;

  @BeforeAll
  public static void setUpAll() {
    // Init the local service factory.
    LocalServiceClientFactory factory =
        LocalServiceClientFactoryBuilder.builder(LOCAL_SERVICE_NAME)
            .withSyncNode(endpointClient)
            .build();

    // Init the local service using the associated factory.
    SmartCardServiceProvider.getService().registerDistributedLocalService(factory);
  }

  @BeforeEach
  public void setUp() {

    samResourceConfiguration.init();

    calypsoCardResourceConfiguration.init();

    /* Get PO Reader */
    cardReader = LocalConfigurationUtil.initReader(PO_READER_FILTER);
  }

  @Test
  public void is_sam_ready() {
    assertEquals("{\"isSamReady\":true}", samClient.ping());
  }

  @Test
  public void execute_successful_load_tickets() {
    issue_card_then_load_tickets(cardReader);
  }

  @Test
  public void execute_successful_load_season_pass() {
    issue_card_then_load_season_pass(cardReader);
  }

  @Test
  public void execute_successful_load_tickets_N_times() {
    final int N = 3;
    for (int i = 0; i < N; i++) {
      execute_successful_load_tickets();
    }
  }

  @Test
  public void execute_successful_load_pass_N_times() {
    final int N = 3;
    for (int i = 0; i < N; i++) {
      execute_successful_load_season_pass();
    }
  }

  /**
   * Reset card, read valid contract and write a MULTI TRIP CONTRACT with X titles
   *
   * @param cardReader
   */
  static void issue_card_then_load_tickets(Reader cardReader) {
    /* Select PO */
    SmartCard calypsoCard = CalypsoUtils.selectCard(cardReader);

    // Retrieves the local service.
    LocalServiceClient localService =
        SmartCardServiceProvider.getService()
            .getDistributedLocalService(LOCAL_SERVICE_NAME)
            .getExtension(LocalServiceClient.class);

    /* Execute Remote Service : Reset card */
    CardIssuanceOutputDto cardIssuanceOutput =
        localService.executeRemoteService(
            "CARD_ISSUANCE", cardReader.getName(), calypsoCard, null, CardIssuanceOutputDto.class);

    assertEquals(0, cardIssuanceOutput.getStatusCode());

    AnalyzeContractsInputDto compatibleContractInput = new AnalyzeContractsInputDto("Android NFC");

    /* Execute Remote Service : Get Valid Contracts */
    AnalyzeContractsOutputDto contractAnalysisOutput =
        localService.executeRemoteService(
            "CONTRACT_ANALYSIS",
            cardReader.getName(),
            calypsoCard,
            compatibleContractInput,
            AnalyzeContractsOutputDto.class);

    assertNotNull(contractAnalysisOutput);
    assertEquals(0, contractAnalysisOutput.getStatusCode());
    assertEquals(0, contractAnalysisOutput.getValidContracts().size());

    /*
     * User select the title....
     */

    load_tickets(cardReader);

    /* Execute Remote Service : Check that MULTI-TRIP is written in the card */
    AnalyzeContractsOutputDto passExpected =
        localService.executeRemoteService(
            "CONTRACT_ANALYSIS",
            cardReader.getName(),
            calypsoCard,
            compatibleContractInput,
            AnalyzeContractsOutputDto.class);

    assertNotNull(passExpected);
    assertEquals(0, passExpected.getStatusCode());
    assertEquals(1, passExpected.getValidContracts().size());
    ContractStructure writtenContract = passExpected.getValidContracts().get(0);
    assertEquals(PriorityCode.MULTI_TRIP, writtenContract.getContractTariff());
    assertEquals(TICKETS_TO_LOAD, writtenContract.getCounterValue());
  }

  /**
   * Reset card, read valid contract and write a MULTI TRIP CONTRACT with X titles
   *
   * @param cardReader
   */
  static void issue_card_then_load_season_pass(Reader cardReader) {
    /* Select PO */
    SmartCard calypsoCard = CalypsoUtils.selectCard(cardReader);

    // Retrieves the local service.
    LocalServiceClient localService =
        SmartCardServiceProvider.getService()
            .getDistributedLocalService(LOCAL_SERVICE_NAME)
            .getExtension(LocalServiceClient.class);

    /* Execute Remote Service : Reset card */
    CardIssuanceOutputDto cardIssuanceOutput =
        localService.executeRemoteService(
            "CARD_ISSUANCE", cardReader.getName(), calypsoCard, null, CardIssuanceOutputDto.class);

    assertEquals(0, cardIssuanceOutput.getStatusCode());

    AnalyzeContractsInputDto compatibleContractInput = new AnalyzeContractsInputDto("Android NFC");

    /* Execute Remote Service : Get Valid Contracts */
    AnalyzeContractsOutputDto contractAnalysisOutput =
        localService.executeRemoteService(
            "CONTRACT_ANALYSIS",
            cardReader.getName(),
            calypsoCard,
            compatibleContractInput,
            AnalyzeContractsOutputDto.class);

    assertNotNull(contractAnalysisOutput);
    assertEquals(0, contractAnalysisOutput.getStatusCode());
    assertEquals(0, contractAnalysisOutput.getValidContracts().size());

    /*
     * User select the title....
     */

    load_season_pass(cardReader);

    /* Execute Remote Service : Check that SEASON PASS is written in the card */
    AnalyzeContractsOutputDto passExpected =
        localService.executeRemoteService(
            "CONTRACT_ANALYSIS",
            cardReader.getName(),
            calypsoCard,
            compatibleContractInput,
            AnalyzeContractsOutputDto.class);

    assertNotNull(passExpected);
    assertEquals(0, passExpected.getStatusCode());
    assertEquals(1, passExpected.getValidContracts().size());
    ContractStructure writtenContract = passExpected.getValidContracts().get(0);
    assertEquals(PriorityCode.SEASON_PASS, writtenContract.getContractTariff());
  }

  /**
   * write a MULTI TRIP CONTRACT with X titles
   *
   * @param poReader
   */
  static void load_tickets(Reader poReader) {
    /* Select PO */
    CalypsoCard calypsoCard = CalypsoUtils.selectCard(poReader);

    // Retrieves the local service.
    LocalServiceClient localService =
        SmartCardServiceProvider.getService()
            .getDistributedLocalService(LOCAL_SERVICE_NAME)
            .getExtension(LocalServiceClient.class);

    AnalyzeContractsInputDto compatibleContractInput = new AnalyzeContractsInputDto("Android NFC");

    /*
     * User select the title....
     */

    WriteContractInputDto writeContractInput =
        new WriteContractInputDto(PriorityCode.MULTI_TRIP, TICKETS_TO_LOAD, "Android NFC");

    /* Execute Remote Service : Write Contract */
    WriteContractOutputDto writeTitleOutput =
        localService.executeRemoteService(
            "WRITE_CONTRACT",
            poReader.getName(),
            calypsoCard,
            writeContractInput,
            WriteContractOutputDto.class);

    assertNotNull(writeTitleOutput);
    assertEquals(0, writeTitleOutput.getStatusCode());
  }

  /**
   * write a SEASON PASS
   *
   * @param poReader
   */
  static void load_season_pass(Reader poReader) {
    /* Select PO */
    CalypsoCard calypsoCard = CalypsoUtils.selectCard(poReader);

    // Retrieves the local service.
    LocalServiceClient localService =
        SmartCardServiceProvider.getService()
            .getDistributedLocalService(LOCAL_SERVICE_NAME)
            .getExtension(LocalServiceClient.class);

    AnalyzeContractsInputDto compatibleContractInput = new AnalyzeContractsInputDto("Android NFC");

    /*
     * User select the title....
     */

    WriteContractInputDto writeContractInput =
        new WriteContractInputDto(PriorityCode.SEASON_PASS, null, "Android NFC");

    /* Execute Remote Service : Write Contract */
    WriteContractOutputDto writeTitleOutput =
        localService.executeRemoteService(
            "WRITE_CONTRACT",
            poReader.getName(),
            calypsoCard,
            writeContractInput,
            WriteContractOutputDto.class);

    assertNotNull(writeTitleOutput);
    assertEquals(0, writeTitleOutput.getStatusCode());
  }
}
