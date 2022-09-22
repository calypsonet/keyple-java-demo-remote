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

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.calypsonet.keyple.demo.common.dto.*;
import org.calypsonet.keyple.demo.common.model.ContractStructure;
import org.calypsonet.terminal.calypso.card.CalypsoCard;
import org.cna.keyple.demo.distributed.server.log.TransactionLog;
import org.cna.keyple.demo.distributed.server.log.TransactionLogStore;
import org.cna.keyple.demo.distributed.server.util.CalypsoConstants;
import org.eclipse.keyple.core.service.ObservablePlugin;
import org.eclipse.keyple.core.service.PluginEvent;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.core.service.resource.CardResource;
import org.eclipse.keyple.core.service.resource.CardResourceServiceProvider;
import org.eclipse.keyple.core.service.spi.PluginObserverSpi;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.distributed.RemotePluginServer;
import org.eclipse.keyple.distributed.RemoteReaderServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class CalypsoCardRemotePluginObserver implements PluginObserverSpi {

  private static final Logger logger =
      LoggerFactory.getLogger(CalypsoCardRemotePluginObserver.class);

  @Inject TransactionLogStore transactionLogStore;

  public CalypsoCardRemotePluginObserver() {}

  /** {@inheritDoc} */
  @Override
  public void onPluginEvent(PluginEvent event) {

    // For a RemotePluginServer, the events can only be of type READER_CONNECTED.
    // So there is no need to analyze the event type.
    logger.info(
        "Event received {} {} {}",
        event.getType(),
        event.getPluginName(),
        event.getReaderNames().first());

    // Retrieves the remote plugin using the plugin name contains in the event.
    ObservablePlugin plugin =
        (ObservablePlugin) SmartCardServiceProvider.getService().getPlugin(event.getPluginName());
    RemotePluginServer pluginExtension = plugin.getExtension(RemotePluginServer.class);

    // Retrieves the name of the remote reader using the first reader name contains in the event.
    // Note that for a RemotePluginServer, there can be only one reader per event.
    String readerName = event.getReaderNames().first();

    // Retrieves the remote reader from the plugin using the reader name.
    Reader reader = plugin.getReader(readerName);
    RemoteReaderServer readerExtension = reader.getExtension(RemoteReaderServer.class);

    // Analyses the Service ID contains in the reader to find which business service to execute.
    // The Service ID was specified by the client when executing the remote service.
    Object userOutputData;
    if ("CONTRACT_ANALYSIS".equals(readerExtension.getServiceId())) {

      // Executes the business service using the remote reader.
      userOutputData = analyzeContracts(reader);

    } else if ("WRITE_CONTRACT".equals(readerExtension.getServiceId())) {

      // Executes the business service using the remote reader.
      userOutputData = writeContract(reader);

    } else if ("CARD_ISSUANCE".equals(readerExtension.getServiceId())) {

      // Executes the business service using the remote reader.
      userOutputData = initCard(reader);

    } else {
      throw new IllegalArgumentException("Service ID not recognized");
    }

    // Terminates the business service by providing the reader name and the optional output data.
    pluginExtension.endRemoteService(readerName, userOutputData);
  }

  /**
   * Analyze the contracts from the card inserted into the remote reader
   *
   * @param reader The remote reader on where to execute the business logic.
   * @return a nullable reference to the user output data to transmit to the client.
   */
  private AnalyzeContractsOutputDto analyzeContracts(Reader reader) {
    RemoteReaderServer readerExtension = reader.getExtension(RemoteReaderServer.class);
    /*
     * Retrieves the compatibleContractInput and initial  specified by the client when executing the remote service.
     */
    CalypsoCard calypsoCard = (CalypsoCard) readerExtension.getInitialCardContent();
    AnalyzeContractsInputDto input = readerExtension.getInputData(AnalyzeContractsInputDto.class);
    CardResource samResource =
        CardResourceServiceProvider.getService().getCardResource(CalypsoConstants.SAM_PROFILE_NAME);
    String pluginType = input.getPluginType();

    try {

      /*
       * Builds a CalypsoCardController to handle Read/Write operations
       */
      CalypsoCardController calypsoCardController =
          CalypsoCardController.newBuilder()
              .withCalypsoCard(calypsoCard)
              .withCardReader(reader)
              .withSamResource(samResource)
              .withPluginType(pluginType)
              .build();

      CalypsoCardRepresentation calypsoCardContent = calypsoCardController.readCard();

      logger.info(calypsoCardContent.toString());

      List<ContractStructure> validContracts = calypsoCardContent.listValidContracts();

      // Log a transaction to the dashboard store
      transactionLogStore.push(
          new TransactionLog()
              .setPlugin(input.getPluginType() == null ? "Android NFC" : input.getPluginType())
              .setStatus("SUCCESS")
              .setType("SECURED READ")
              .setPoSn(ByteArrayUtil.toHex(calypsoCard.getApplicationSerialNumber())));

      return new AnalyzeContractsOutputDto(validContracts, 0);

    } catch (RuntimeException e) {
      logger.error("An error occurred while analyzing the contracts : {}", e.getMessage());

      // Log a transaction to the dashboard store
      transactionLogStore.push(
          new TransactionLog()
              .setPlugin(input.getPluginType() == null ? "Android NFC" : input.getPluginType())
              .setStatus("FAIL")
              .setType("SECURED READ")
              .setPoSn(ByteArrayUtil.toHex(calypsoCard.getApplicationSerialNumber())));

      return new AnalyzeContractsOutputDto(null, 1);
    } finally {
      // deallocate samResource
      CardResourceServiceProvider.getService().releaseCardResource(samResource);
    }
  }

  /**
   * Write a contract into the card inserted into the remote reader
   *
   * @param reader The remote reader on where to execute the business logic.
   * @return a nullable reference to the user output data to transmit to the client.
   */
  private WriteContractOutputDto writeContract(Reader reader) {
    RemoteReaderServer readerExtension = reader.getExtension(RemoteReaderServer.class);

    /*
     * Retrieves the userInputData and initial calypso Card specified by the client when executing the remote service.
     */
    WriteContractInputDto writeContractInput =
        readerExtension.getInputData(WriteContractInputDto.class);
    CalypsoCard calypsoCard = (CalypsoCard) readerExtension.getInitialCardContent();
    CardResource samResource =
        CardResourceServiceProvider.getService().getCardResource(CalypsoConstants.SAM_PROFILE_NAME);
    String pluginType = writeContractInput.getPluginType();

    String asnHex = ByteArrayUtil.toHex(calypsoCard.getApplicationSerialNumber());
    String dfName = ByteArrayUtil.toHex(calypsoCard.getApplicationSerialNumber());

    logger.info("Inserted card application serial number : {}", asnHex);
    logger.info("Inserted card df name : {}", dfName);

    try {

      /*
       * Builds a CalypsoCardController to handle Read/Write operations
       */
      CalypsoCardController calypsoCardController =
          CalypsoCardController.newBuilder()
              .withCalypsoCard(calypsoCard)
              .withCardReader(reader)
              .withSamResource(samResource)
              .withPluginType(pluginType)
              .build();

      // read card
      CalypsoCardRepresentation calypsoCardContent = calypsoCardController.readCard();

      if (calypsoCardContent == null) {
        // is card has not been read previously, throw error
        return new WriteContractOutputDto(3);
      }

      logger.info(calypsoCardContent.toString());

      calypsoCardContent.insertNewContract(
          writeContractInput.getContractTariff(), writeContractInput.getTicketToLoad());

      /*
       * Write the updated content to the calypso card
       */
      int statusCode = calypsoCardController.writeCard(calypsoCardContent);

      // push a transaction log
      transactionLogStore.push(
          new TransactionLog()
              // TODO : change default name
              .setPlugin(
                  writeContractInput.getPluginType() == null
                      ? "Android NFC"
                      : writeContractInput.getPluginType())
              .setStatus("SUCCESS")
              .setType("RELOAD")
              .setPoSn(ByteArrayUtil.toHex(calypsoCard.getApplicationSerialNumber()))
              .setContractLoaded(
                  writeContractInput.getContractTariff().toString().replace("_", " ")
                      + ((writeContractInput.getTicketToLoad() != null
                              && writeContractInput.getTicketToLoad() != 0)
                          ? " : " + writeContractInput.getTicketToLoad()
                          : "")));

      return new WriteContractOutputDto(statusCode);

    } catch (RuntimeException e) {
      logger.error("An error occurred while writing the contract : {}", e.getMessage());

      // push a transaction log
      transactionLogStore.push(
          new TransactionLog()
              // TODO : change default name
              .setPlugin(
                  writeContractInput.getPluginType() == null
                      ? "Android NFC"
                      : writeContractInput.getPluginType())
              .setStatus("FAIL")
              .setType("RELOAD")
              .setPoSn(ByteArrayUtil.toHex(calypsoCard.getApplicationSerialNumber()))
              .setContractLoaded(""));
      return new WriteContractOutputDto(1);
    } finally {
      // deallocate samResource if needed
      CardResourceServiceProvider.getService().releaseCardResource(samResource);
    }
  }

  /**
   * Init the card inserted into the remote reader
   *
   * @param reader The remote reader on where to execute the business logic.
   * @return a nullable reference to the user output data to transmit to the client.
   */
  private CardIssuanceOutputDto initCard(Reader reader) {
    RemoteReaderServer readerExtension = reader.getExtension(RemoteReaderServer.class);

    CalypsoCard calypsoCard = (CalypsoCard) readerExtension.getInitialCardContent();

    CardResource samResource =
        CardResourceServiceProvider.getService().getCardResource(CalypsoConstants.SAM_PROFILE_NAME);

    try {

      // Create a Calypso Card controller
      CalypsoCardController calypsoCardController =
          CalypsoCardController.newBuilder()
              .withCalypsoCard(calypsoCard)
              .withCardReader(reader)
              .withSamResource(samResource)
              .withPluginType("Android NFC")
              .build();

      // init card
      calypsoCardController.initCard();

      // push a transaction log
      transactionLogStore.push(
          new TransactionLog()
              .setPlugin("Android NFC")
              .setStatus("SUCCESS")
              .setType("ISSUANCE")
              .setPoSn(ByteArrayUtil.toHex(calypsoCard.getApplicationSerialNumber())));

      return new CardIssuanceOutputDto(0);
    } catch (RuntimeException e) {

      transactionLogStore.push(
          new TransactionLog()
              .setPlugin("Android NFC")
              .setStatus("FAIL")
              .setType("ISSUANCE")
              .setPoSn(ByteArrayUtil.toHex(calypsoCard.getApplicationSerialNumber())));
      return new CardIssuanceOutputDto(1);
    } finally {
      // deallocate samResource if needed
      CardResourceServiceProvider.getService().releaseCardResource(samResource);
    }
  }
}
