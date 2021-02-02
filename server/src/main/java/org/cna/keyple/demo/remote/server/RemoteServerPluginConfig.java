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
package org.cna.keyple.demo.remote.server;

import io.quarkus.runtime.Startup;
import org.cna.keyple.demo.remote.server.card.CardContent;
import org.cna.keyple.demo.remote.server.card.CardController;
import org.cna.keyple.demo.sale.data.endpoint.AnalyzeContractsOutput;
import org.cna.keyple.demo.sale.data.endpoint.WriteContractInput;
import org.cna.keyple.demo.sale.data.endpoint.WriteContractOutput;
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
import org.eclipse.keyple.distributed.RemotePluginServer;
import org.eclipse.keyple.distributed.RemoteReaderServer;
import org.eclipse.keyple.distributed.impl.RemotePluginServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

/**
 *{@link RemotePluginServer} observer.
 *
 * <p>It contains the business logic of the remote service execution.
 * <ul>
 *     <li>CONTRACT_ANALYSIS : returns the list of compatible title with the calypsoPo inserted</li>
 *      <li>WRITE_CONTRACT : returns the list of compatible title with the calypsoPo inserted</li>
 * </ul>
 *
 */
@ApplicationScoped
@Startup
public class RemoteServerPluginConfig implements ObservablePlugin.PluginObserver {

  private static final Logger logger = LoggerFactory.getLogger(RemoteServerPluginConfig.class);

  SamResourceManager samResourceManager;

  public RemoteServerPluginConfig(SamResourceManager samResourceManager){
    logger.info("Init RemoteServerPluginConfig...");

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

    this.samResourceManager = samResourceManager;
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
      userOutputData = writeTitle(reader);

    } else{
      throw new IllegalArgumentException("Service ID not recognized");
    }

    // Terminates the business service by providing the reader name and the optional output data.
    plugin.terminateService(readerName, userOutputData);
  }

  /**
   *
   * @param reader The remote reader on where to execute the business logic.
   * @return a nullable reference to the user output data to transmit to the client.
   */
  private AnalyzeContractsOutput analyzeContracts(RemoteReaderServer reader) {

    /*
     * Retrieves the compatibleContractInput and initial calypsoPO specified by the client when executing the remote service.
     */
    CalypsoPo calypsoPo = reader.getInitialCardContent(CalypsoPo.class);

    CardResource<CalypsoSam> samResource = samResourceManager.allocateSamResource(
            SamResourceManager.AllocationMode.BLOCKING,
            new SamIdentifier.SamIdentifierBuilder().serialNumber("").samRevision(SamRevision.AUTO).groupReference(".*").build());

    CardController cardController = CardController.newBuilder()
            .withCalypsoPo(calypsoPo)
            .withReader(reader)
            .withSamResource(samResource)
            .build();

    CardContent cardContent = cardController.readCard();

    logger.info(cardContent.toString());

    List<ContractStructureDto> validContracts = cardContent.listValidContracts();

    samResourceManager.freeSamResource(samResource);

    return new AnalyzeContractsOutput()
            .setValidContracts(validContracts)
            .setValidContractsCounters(Arrays.asList(cardContent.getCounter()))
            .setStatusCode(0);
  }

  /**
   *
   * @param reader The remote reader on where to execute the business logic.
   * @return a nullable reference to the user output data to transmit to the client.
   */
  private WriteContractOutput writeTitle(RemoteReaderServer reader) {

    /*
     * Retrieves the userInputData and initial calypsoPO specified by the client when executing the remote service.
     */
    WriteContractInput writeContractInput = reader.getUserInputData(WriteContractInput.class);
    CalypsoPo calypsoPo = reader.getInitialCardContent(CalypsoPo.class);

    CardResource<CalypsoSam> samResource = samResourceManager.allocateSamResource(
            SamResourceManager.AllocationMode.BLOCKING,
            new SamIdentifier.SamIdentifierBuilder().serialNumber("").samRevision(SamRevision.AUTO).groupReference(".*").build());

    CardController cardController = CardController.newBuilder()
            .withCalypsoPo(calypsoPo)
            .withReader(reader)
            .withSamResource(samResource)
            .build();

    //re-read card
    CardContent cardContent = cardController.readCard();

    if(cardContent==null){
      //is card has not been read previously, throw error
      return new WriteContractOutput().setStatusCode(3);
    }

    logger.info(cardContent.toString());

    cardContent.insertNewContract(
            writeContractInput.getContractTariff(),
            writeContractInput.getTicketToLoad());

    int statusCode = cardController.writeCard(cardContent);

    samResourceManager.freeSamResource(samResource);

    return new WriteContractOutput().setStatusCode(statusCode);
  }
}
