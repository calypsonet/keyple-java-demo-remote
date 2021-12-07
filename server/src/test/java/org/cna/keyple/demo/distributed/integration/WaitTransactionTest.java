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

import static org.calypsonet.terminal.reader.ObservableCardReader.DetectionMode.REPEATING;

import io.quarkus.test.junit.QuarkusTest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import javax.inject.Inject;
import org.calypsonet.terminal.reader.CardReaderEvent;
import org.calypsonet.terminal.reader.spi.CardReaderObservationExceptionHandlerSpi;
import org.calypsonet.terminal.reader.spi.CardReaderObserverSpi;
import org.cna.keyple.demo.distributed.integration.client.EndpointClient;
import org.cna.keyple.demo.distributed.server.plugin.CalypsoCardResourceConfiguration;
import org.cna.keyple.demo.distributed.server.plugin.SamResourceConfiguration;
import org.cna.keyple.demo.local.procedure.LocalConfigurationUtil;
import org.eclipse.keyple.core.service.ObservableReader;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.distributed.LocalServiceClientFactory;
import org.eclipse.keyple.distributed.LocalServiceClientFactoryBuilder;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusTest
public class WaitTransactionTest {

  private static String LOCAL_SERVICE_NAME = "TransactionTest";
  private static String CALYPSO_CARD_READER_FILTER = ".*(ASK|ACS).*";

  private static final Logger logger = LoggerFactory.getLogger(WaitTransactionTest.class);

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
    cardReader = LocalConfigurationUtil.initReader(CALYPSO_CARD_READER_FILTER);
  }

  @Test
  public void execute_wait_for_calypso_card() throws InterruptedException {

    ((ObservableReader) cardReader)
        .setReaderObservationExceptionHandler(
            new CardReaderObservationExceptionHandlerSpi() {
              @Override
              public void onReaderObservationError(
                  String contextInfo, String readerName, Throwable e) {
                logger.error("onReaderObservationError", e);
              }
            });

    ((ObservableReader) cardReader)
        .addObserver(
            new CardReaderObserverSpi() {
              @Override
              public void onReaderEvent(CardReaderEvent event) {
                switch (event.getType()) {
                  case CARD_INSERTED:
                    // Randomly load tickets or season pass
                    if (new Random().nextInt() % 2 == 0) {
                      TransactionTest.issue_card_then_load_tickets(cardReader);
                    } else {
                      TransactionTest.load_season_pass(cardReader);
                    }
                    break;
                }
              }
            });

    ((ObservableReader) cardReader).startCardDetection(REPEATING);

    // Wait indefinitely. CTRL-C to exit.
    synchronized (waitForEnd) {
      waitForEnd.wait();
    }
  }

  private static final Object waitForEnd = new Object();
}
