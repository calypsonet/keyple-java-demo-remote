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
package org.cna.keyple.demo.distributed.server.plugin;

import io.quarkus.runtime.Startup;
import org.calypsonet.terminal.calypso.card.CalypsoCard;
import org.cna.keyple.demo.distributed.server.TicketingLogic;
import org.cna.keyple.demo.distributed.server.controller.CalypsoPoController;
import org.cna.keyple.demo.distributed.server.controller.CalypsoPoRepresentation;
import org.cna.keyple.demo.distributed.server.log.TransactionLog;
import org.cna.keyple.demo.distributed.server.log.TransactionLogStore;
import org.cna.keyple.demo.sale.data.endpoint.*;
import org.cna.keyple.demo.sale.data.model.ContractStructureDto;
import org.eclipse.keyple.core.service.ObservablePlugin;
import org.eclipse.keyple.core.service.PluginEvent;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.core.service.spi.PluginObservationExceptionHandlerSpi;
import org.eclipse.keyple.core.service.spi.PluginObserverSpi;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.distributed.RemotePluginServer;
import org.eclipse.keyple.distributed.RemotePluginServerFactory;
import org.eclipse.keyple.distributed.RemotePluginServerFactoryBuilder;
import org.eclipse.keyple.distributed.RemoteReaderServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

import static org.cna.keyple.demo.distributed.server.Main.KeypleDistributedServerDemo.REMOTE_PLUGIN_NAME;

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
public class CalypsoCardConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(CalypsoCardConfiguration.class);

    private final TransactionLogStore transactionLogStore;
  private final SamCardConfiguration samCardConfiguration;

  public CalypsoCardConfiguration(TransactionLogStore transactionLogStore, SamCardConfiguration samCardConfiguration){
    logger.info("Init CalypsoCardConfiguration...");
    this.samCardConfiguration = samCardConfiguration;
    
    // Init the remote plugin factory with a sync node and a remote plugin observer.
    RemotePluginServerFactory factory =
            RemotePluginServerFactoryBuilder.builder(REMOTE_PLUGIN_NAME).withSyncNode().build();

  /*  RemotePluginServerFactory factory =
            RemotePluginServerFactory.builder()
                    .withDefaultPluginName()
                    .withSyncNode()
                    .withPluginObserver(this)
                    .usingEventNotificationPool(
                            Executors.newCachedThreadPool(r -> new Thread(r, "server-pool")))
                    .build();*/

    // Register the remote plugin to the smart card service using the factory.
    org.eclipse.keyple.core.service.ObservablePlugin plugin =
            (org.eclipse.keyple.core.service.ObservablePlugin) SmartCardServiceProvider.getService().registerPlugin(factory);

  // Init the remote plugin observer.
    plugin.setPluginObservationExceptionHandler(
            new PluginObservationExceptionHandlerSpi() {
              @Override
              public void onPluginObservationError(String pluginName, Throwable e) {
                  logger.error( pluginName,  e);

              }
            });
    plugin.addObserver(new TicketingLogic(transactionLogStore));

    this.transactionLogStore = transactionLogStore;
  }





}
