/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.cna.keyple.demo.distributed.server.controller;

import io.quarkus.runtime.Startup;
import org.cna.keyple.demo.distributed.server.log.TransactionLog;
import org.cna.keyple.demo.distributed.server.log.TransactionLogStore;
import org.cna.keyple.demo.sale.data.endpoint.*;
import org.cna.keyple.demo.sale.data.model.ContractStructureDto;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.CalypsoSam;
import org.eclipse.keyple.calypso.transaction.sammanager.SamIdentifier;
import org.eclipse.keyple.calypso.transaction.sammanager.SamResourceManager;
import org.eclipse.keyple.core.card.selection.CardResource;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.ObservablePlugin;
import org.eclipse.keyple.core.service.event.PluginEvent;
import org.eclipse.keyple.core.service.exception.KeypleException;
import org.eclipse.keyple.distributed.RemotePluginServer;
import org.eclipse.keyple.distributed.RemoteReaderServer;
import org.eclipse.keyple.distributed.impl.RemotePluginServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.concurrent.Executors;

/**
 *{@link RemotePluginServer} observer.
 *
 * <p>It contains the business logic of the remote service execution.
 * <ul>
 *     <li>CONTRACT_ANALYSIS : returns the list of compatible title with the calypsoPo inserted</li>
 *      <li>WRITE_CONTRACT : write a new contract in the calypsoPo inserted</li>
 *      <li>CARD_ISSUANCE : Clean/Initialize Application of the calypsoPO inserted</li>
 * </ul>
 *
 */
@ApplicationScoped
@Startup
public class RemoteServerPluginObserver implements ObservablePlugin.PluginObserver {

  private static final Logger logger = LoggerFactory.getLogger(RemoteServerPluginObserver.class);

  TransactionLogStore transactionLogStore;
  SamResourceService samResourceService;

  public RemoteServerPluginObserver(SamResourceService samResourceService, TransactionLogStore transactionLogStore){
    logger.info("Init RemoteServerPluginObserver...");

    // Init the remote plugin factory with a sync node and a remote plugin observer.
    RemotePluginServerFactory factory =
            RemotePluginServerFactory.builder()
                    .withDefaultPluginName()
                    .withSyncNode()
                    .withPluginObserver(this)
                    .usingEventNotificationPool(
                            Executors.newCachedThreadPool(r -> new Thread(r, "server-pool")))
                    .build();

    // Register the remote plugin to the smart card service using the factory.
    SmartCardService.getInstance().registerPlugin(factory);

    this.samResourceService = samResourceService;
    this.transactionLogStore = transactionLogStore;
  }

  /** {@inheritDoc} */
  @Override
  public void update(PluginEvent event) {

    // For a RemotePluginServer, the events can only be of type READER_CONNECTED.
    // So there is no need to analyze the event type.
    logger.info(
            "Event received {} {} {}",
            event.getEventType(),
            event.getPluginName(),
            event.getReaderNames().first());

    // Retrieves the remote plugin using the plugin name contains in the event.
    RemotePluginServer plugin =
            (RemotePluginServer) SmartCardService.getInstance().getPlugin(event.getPluginName());

    // Retrieves the name of the remote reader using the first reader name contains in the event.
    // Note that for a RemotePluginServer, there can be only one reader per event.
    String readerName = event.getReaderNames().first();

    // Retrieves the remote reader from the plugin using the reader name.
    RemoteReaderServer reader = plugin.getReader(readerName);

    // Analyses the Service ID contains in the reader to find which business service to execute.
    // The Service ID was specified by the client when executing the remote service.
    Object userOutputData;
    if ("CONTRACT_ANALYSIS".equals(reader.getServiceId())) {

      // Executes the business service using the remote reader.
      userOutputData = analyzeContracts(reader);

    } else if ("WRITE_CONTRACT".equals(reader.getServiceId())) {

      // Executes the business service using the remote reader.
      userOutputData = writeContract(reader);

    } else if ("CARD_ISSUANCE".equals(reader.getServiceId())) {

      // Executes the business service using the remote reader.
      userOutputData = initCard(reader);

    } else{
      throw new IllegalArgumentException("Service ID not recognized");
    }

    // Terminates the business service by providing the reader name and the optional output data.
    plugin.terminateService(readerName, userOutputData);
  }

  /**
   * Analyze the contracts from the card inserted into the remote reader
   * @param reader The remote reader on where to execute the business logic.
   * @return a nullable reference to the user output data to transmit to the client.
   */
  private AnalyzeContractsOutput analyzeContracts(RemoteReaderServer reader) {
    /*
     * Retrieves the compatibleContractInput and initial calypsoPO specified by the client when executing the remote service.
     */
    CalypsoPo calypsoPo = reader.getInitialCardContent(CalypsoPo.class);
    AnalyzeContractsInput input = reader.getUserInputData(AnalyzeContractsInput.class);

    CardResource<CalypsoSam> samResource = null;

    try{
      //allocate a sam resource using the Sam Resource Manager
      samResource = samResourceService.getSamResourceManager().allocateSamResource(
              SamResourceManager.AllocationMode.BLOCKING,
              new SamIdentifier.SamIdentifierBuilder().serialNumber("").samRevision(SamRevision.AUTO).groupReference(".*").build());

      CalypsoPoController calypsoPoController = CalypsoPoController.newBuilder()
              .withCalypsoPo(calypsoPo)
              .withReader(reader)
              .withSamResource(samResource)
              .build();


      CalypsoPoContent calypsoPoContent = calypsoPoController.readCard();

      logger.info(calypsoPoContent.toString());

      List<ContractStructureDto> validContracts = calypsoPoContent.listValidContracts();

      //push a transaction log
    transactionLogStore.push(new TransactionLog()
            .setPlugin(input.getPluginType()==null?"Android NFC":input.getPluginType())
            .setStatus("SUCCESS")
            .setType("SECURED READ")
            .setPoSn(calypsoPo.getApplicationSerialNumber()));

    return new AnalyzeContractsOutput()
            .setValidContracts(validContracts)
            .setStatusCode(0);

    }catch(KeypleException e){
      logger.error("An error occurred while analyzing the contracts : {}", e.getMessage());
      //push a transaction log
      transactionLogStore.push(new TransactionLog()
              .setPlugin(input.getPluginType()==null?"Android NFC":input.getPluginType())
              .setStatus("FAIL")
              .setType("SECURED READ")
              .setPoSn(calypsoPo.getApplicationSerialNumber()));

      return new AnalyzeContractsOutput()
              .setStatusCode(1);
    }finally {
      //deallocate samResource
      if(samResource!=null){
        samResourceService.getSamResourceManager().freeSamResource(samResource);
      }
    }
  }

  /**
   * Write a contract into the card inserted into the remote reader
   * @param reader The remote reader on where to execute the business logic.
   * @return a nullable reference to the user output data to transmit to the client.
   */
  private WriteContractOutput writeContract(RemoteReaderServer reader) {

    /*
     * Retrieves the userInputData and initial calypsoPO specified by the client when executing the remote service.
     */
    WriteContractInput writeContractInput = reader.getUserInputData(WriteContractInput.class);
    CalypsoPo calypsoPo = reader.getInitialCardContent(CalypsoPo.class);
    CardResource<CalypsoSam> samResource = null;

    try{
      //allocate a sam resource using the Sam Resource Manager
       samResource = samResourceService.getSamResourceManager().allocateSamResource(
              SamResourceManager.AllocationMode.BLOCKING,
              new SamIdentifier.SamIdentifierBuilder().serialNumber("").samRevision(SamRevision.AUTO).groupReference(".*").build());

      CalypsoPoController calypsoPoController = CalypsoPoController.newBuilder()
              .withCalypsoPo(calypsoPo)
              .withReader(reader)
              .withSamResource(samResource)
              .build();

      //re-read card
      CalypsoPoContent calypsoPoContent = calypsoPoController.readCard();

      if(calypsoPoContent ==null){
        //is card has not been read previously, throw error
        return new WriteContractOutput().setStatusCode(3);
      }

      logger.info(calypsoPoContent.toString());

      calypsoPoContent.insertNewContract(
              writeContractInput.getContractTariff(),
              writeContractInput.getTicketToLoad());

      int statusCode = calypsoPoController.writeCard(calypsoPoContent);

      //push a transaction log
      transactionLogStore.push(new TransactionLog()
              //TODO : change default name
              .setPlugin(writeContractInput.getPluginType()==null?"Android NFC":writeContractInput.getPluginType())
              .setStatus("SUCCESS")
              .setType("RELOAD")
              .setPoSn(calypsoPo.getApplicationSerialNumber())
              .setContractLoaded(writeContractInput.getContractTariff().toString().replace("_", " ")+
                      ((writeContractInput.getTicketToLoad()!=null && writeContractInput.getTicketToLoad()!=0)? " : " +writeContractInput.getTicketToLoad():""))
      );

      return new WriteContractOutput().setStatusCode(statusCode);

    }catch(KeypleException e){
      logger.error("An error occurred while writing the contract : {}", e.getMessage());

      //push a transaction log
      transactionLogStore.push(new TransactionLog()
              //TODO : change default name
              .setPlugin(writeContractInput.getPluginType()==null?"Android NFC":writeContractInput.getPluginType())
              .setStatus("FAIL")
              .setType("RELOAD")
              .setPoSn(calypsoPo.getApplicationSerialNumber())
              .setContractLoaded("")
      );
      return new WriteContractOutput().setStatusCode(1);
    }finally {
      //deallocate samResource if needed
      if(samResource!=null){
        //release the sam resource using the Sam Resource Manager
        samResourceService.getSamResourceManager().freeSamResource(samResource);
      }
    }
  }

  /**
   * Init the card inserted into the remote reader
   * @param reader The remote reader on where to execute the business logic.
   * @return a nullable reference to the user output data to transmit to the client.
   */
  private CardIssuanceOutput initCard(RemoteReaderServer reader) {

    CalypsoPo calypsoPo = reader.getInitialCardContent(CalypsoPo.class);

    CardResource<CalypsoSam> samResource = null;

    try{
      //allocate a sam resource using the Sam Resource Manager
      samResource = samResourceService.getSamResourceManager().allocateSamResource(
              SamResourceManager.AllocationMode.BLOCKING,
              new SamIdentifier.SamIdentifierBuilder().serialNumber("").samRevision(SamRevision.AUTO).groupReference(".*").build());

      //Create a Calypso PO controller
      CalypsoPoController calypsoPoController = CalypsoPoController.newBuilder()
              .withCalypsoPo(calypsoPo)
              .withReader(reader)
              .withSamResource(samResource)
              .build();

      //init card
      calypsoPoController.initCard();

      //push a transaction log
      transactionLogStore.push(new TransactionLog()
              .setPlugin("Android NFC")
              .setStatus("SUCCESS")
              .setType("ISSUANCE")
              .setPoSn(calypsoPo.getApplicationSerialNumber()));

      return new CardIssuanceOutput().setStatusCode(0);
    }catch (KeypleException e){

      transactionLogStore.push(new TransactionLog()
              .setPlugin("Android NFC")
              .setStatus("FAIL")
              .setType("ISSUANCE")
              .setPoSn(calypsoPo.getApplicationSerialNumber()));
      return new CardIssuanceOutput().setStatusCode(1);
    }finally {
      //deallocate samResource if needed
      if(samResource!=null){
        //release the sam resource using the Sam Resource Manager
        samResourceService.getSamResourceManager().freeSamResource(samResource);
      }
    }
  }

}
