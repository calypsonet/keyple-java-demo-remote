/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://calypsonet.org/
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

import static org.cna.keyple.demo.distributed.server.Main.KeypleDistributedServerDemo.REMOTE_PLUGIN_NAME;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.cna.keyple.demo.distributed.server.calypso.CalypsoCardRemotePluginObserver;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.core.service.spi.PluginObservationExceptionHandlerSpi;
import org.eclipse.keyple.distributed.RemotePluginServer;
import org.eclipse.keyple.distributed.RemotePluginServerFactory;
import org.eclipse.keyple.distributed.RemotePluginServerFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RemotePluginServer} observer.
 *
 * <p>It contains the business logic of the remote service execution.
 *
 * <ul>
 *   <li>CONTRACT_ANALYSIS : returns the list of compatible title with the calypsoCard inserted
 *   <li>WRITE_CONTRACT : write a new contract in the calypsoCard inserted
 *   <li>CARD_ISSUANCE : Clean/Initialize Application of the calypsoPO inserted
 * </ul>
 */
@ApplicationScoped
public class CalypsoCardResourceConfiguration {

  private static final Logger logger =
      LoggerFactory.getLogger(CalypsoCardResourceConfiguration.class);

  @Inject CalypsoCardRemotePluginObserver calypsoCardRemotePluginObserver;

  public CalypsoCardResourceConfiguration() {
    logger.info("Init CalypsoCardConfiguration...");
  }

  public void init() {
    // Init the remote plugin factory with a sync node and a remote plugin observer.
    RemotePluginServerFactory factory =
        RemotePluginServerFactoryBuilder.builder(REMOTE_PLUGIN_NAME).withSyncNode().build();

    // Register the remote plugin to the smart card service using the factory.
    org.eclipse.keyple.core.service.ObservablePlugin plugin =
        (org.eclipse.keyple.core.service.ObservablePlugin)
            SmartCardServiceProvider.getService().registerPlugin(factory);

    // Init the remote plugin observer.
    plugin.setPluginObservationExceptionHandler(
        new PluginObservationExceptionHandlerSpi() {
          @Override
          public void onPluginObservationError(String pluginName, Throwable e) {
            logger.error(pluginName, e);
          }
        });

    // Attach the business logic
    plugin.addObserver(calypsoCardRemotePluginObserver);
  }
}
