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
package org.calypsonet.keyple.demo.reload.remote.server.card;

import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.calypsonet.keyple.demo.common.constant.RemoteServiceId;
import org.calypsonet.keyple.demo.common.dto.AnalyzeContractsInputDto;
import org.calypsonet.keyple.demo.common.dto.CardIssuanceInputDto;
import org.calypsonet.keyple.demo.common.dto.WriteContractInputDto;
import org.calypsonet.terminal.calypso.card.CalypsoCard;
import org.calypsonet.terminal.calypso.sam.CalypsoSam;
import org.calypsonet.terminal.reader.CardReader;
import org.eclipse.keyple.card.calypso.CalypsoExtensionService;
import org.eclipse.keyple.core.service.*;
import org.eclipse.keyple.core.service.resource.*;
import org.eclipse.keyple.core.service.resource.spi.CardResourceProfileExtension;
import org.eclipse.keyple.core.service.resource.spi.ReaderConfiguratorSpi;
import org.eclipse.keyple.core.service.spi.PluginObservationExceptionHandlerSpi;
import org.eclipse.keyple.core.service.spi.PluginObserverSpi;
import org.eclipse.keyple.distributed.RemotePluginServer;
import org.eclipse.keyple.distributed.RemotePluginServerFactoryBuilder;
import org.eclipse.keyple.distributed.RemoteReaderServer;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactoryBuilder;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class CardConfigurator {

  private static final Logger logger = LoggerFactory.getLogger(CardConfigurator.class);

  static final String REMOTE_PLUGIN_NAME = "REMOTE_PLUGIN_#1";
  static final String SAM_RESOURCE_PROFILE_NAME = "SAM C1";

  @ConfigProperty(name = "sam.pcsc.reader.filter")
  String samReaderFilter;

  @Inject CardService cardService;

  public CardReader getSamReader() {
    Pattern p = Pattern.compile(samReaderFilter);
    for (Plugin plugin : SmartCardServiceProvider.getService().getPlugins()) {
      for (CardReader reader : plugin.getReaders()) {
        if (p.matcher(reader.getName()).matches()) {
          return reader;
        }
      }
    }
    return null;
  }

  public void init() {
    initSamPlugin();
    initCardPlugin();
  }

  private void initSamPlugin() {
    // Register the SAM plugin
    SmartCardService smartCardService = SmartCardServiceProvider.getService();
    Plugin plugin = smartCardService.registerPlugin(PcscPluginFactoryBuilder.builder().build());
    if (plugin.getReaders().isEmpty()) {
      throw new IllegalStateException(
          "For the matter of this demo, we expect at least one PCSC reader to be connected");
    }
    // Set up the associated card resource service
    setupCardResourceService(plugin);
  }

  private void setupCardResourceService(Plugin plugin) {
    logger.info(
        "Set up CardResourceService for plugin '{}' with readerNameRegex '{}' and samProfileName '{}'",
        plugin.getName(),
        samReaderFilter,
        SAM_RESOURCE_PROFILE_NAME);
    // Create a card resource extension expecting a SAM "C1".
    CardResourceProfileExtension samResourceProfileExtension =
        CalypsoExtensionService.getInstance()
            .createSamResourceProfileExtension(
                CalypsoExtensionService.getInstance()
                    .createSamSelection()
                    .filterByProductType(CalypsoSam.ProductType.SAM_C1));
    // Create a minimalist configuration (no plugin/reader observation)
    CardResourceService cardResourceService = CardResourceServiceProvider.getService();
    cardResourceService
        .getConfigurator()
        .withPlugins(
            PluginsConfigurator.builder().addPlugin(plugin, new SamReaderConfigurator()).build())
        .withCardResourceProfiles(
            CardResourceProfileConfigurator.builder(
                    SAM_RESOURCE_PROFILE_NAME, samResourceProfileExtension)
                .withReaderNameRegex(samReaderFilter)
                .build())
        .configure();
    cardResourceService.start();
    // verify the resource availability
    CardResource cardResource = cardResourceService.getCardResource(SAM_RESOURCE_PROFILE_NAME);
    if (cardResource == null) {
      throw new IllegalStateException(
          String.format(
              "Unable to retrieve a SAM card resource for profile '%s' from reader '%s' in plugin '%s'",
              SAM_RESOURCE_PROFILE_NAME, samReaderFilter, plugin.getName()));
    }
    cardResourceService.releaseCardResource(cardResource);
  }

  private static class SamReaderConfigurator implements ReaderConfiguratorSpi {
    @Override
    public void setupReader(CardReader cardReader) {
      try {
        PcscReader readerExtension =
            SmartCardServiceProvider.getService()
                .getPlugin(cardReader)
                .getReaderExtension(PcscReader.class, cardReader.getName());
        readerExtension
            .setContactless(false)
            .setIsoProtocol(PcscReader.IsoProtocol.ANY)
            .setSharingMode(PcscReader.SharingMode.SHARED);
      } catch (Exception e) {
        logger.error(
            "An error occurred while setting up the SAM reader {}", cardReader.getName(), e);
      }
    }
  }

  private void initCardPlugin() {
    // Register the remote plugin to the smart card service using the factory.
    ObservablePlugin plugin =
        (ObservablePlugin)
            SmartCardServiceProvider.getService()
                .registerPlugin(
                    RemotePluginServerFactoryBuilder.builder(REMOTE_PLUGIN_NAME)
                        .withSyncNode()
                        .build());
    // Init the remote plugin observer.
    plugin.setPluginObservationExceptionHandler(new CardRemotePluginObservationExceptionHandler());
    // Add the main observer
    plugin.addObserver(new CardRemotePluginObserver());
  }

  private static class CardRemotePluginObservationExceptionHandler
      implements PluginObservationExceptionHandlerSpi {
    @Override
    public void onPluginObservationError(String pluginName, Throwable e) {
      logger.error(
          "An error occurred while observing the card reader remote plugin {}", pluginName, e);
    }
  }

  private class CardRemotePluginObserver implements PluginObserverSpi {

    @Override
    public void onPluginEvent(PluginEvent pluginEvent) {

      // For a RemotePluginServer, the events can only be of type READER_CONNECTED.
      // So there is no need to analyze the event type.
      logger.info(
          "Event received {} {} {}",
          pluginEvent.getType(),
          pluginEvent.getPluginName(),
          pluginEvent.getReaderNames().first());

      // Retrieves the remote plugin using the plugin name contains in the event.
      ObservablePlugin plugin =
          (ObservablePlugin)
              SmartCardServiceProvider.getService().getPlugin(pluginEvent.getPluginName());
      RemotePluginServer pluginExtension = plugin.getExtension(RemotePluginServer.class);

      // Retrieves the name of the remote reader using the first reader name contains in the event.
      // Note that for a RemotePluginServer, there can be only one reader per event.
      String readerName = pluginEvent.getReaderNames().first();

      // Retrieves the remote reader from the plugin using the reader name.
      CardReader reader = plugin.getReader(readerName);
      RemoteReaderServer readerExtension =
          plugin.getReaderExtension(RemoteReaderServer.class, reader.getName());
      // Analyses the Service ID contains in the reader to find which business service to execute.
      // The Service ID was specified by the client when executing the remote service.
      Object outputData;
      if (RemoteServiceId.CONTRACT_ANALYSIS.name().equals(readerExtension.getServiceId())) {

        // Get input data
        CardResource cardResource =
            new CardResource(reader, (CalypsoCard) readerExtension.getInitialCardContent());
        AnalyzeContractsInputDto inputData =
            readerExtension.getInputData(AnalyzeContractsInputDto.class);

        // Execute service
        outputData = cardService.analyzeContracts(cardResource, inputData);

      } else if (RemoteServiceId.WRITE_CONTRACT.name().equals(readerExtension.getServiceId())) {

        // Get input data
        CardResource cardResource =
            new CardResource(reader, (CalypsoCard) readerExtension.getInitialCardContent());
        WriteContractInputDto inputData = readerExtension.getInputData(WriteContractInputDto.class);

        // Execute service
        outputData = cardService.writeContract(cardResource, inputData);

      } else if (RemoteServiceId.CARD_ISSUANCE.name().equals(readerExtension.getServiceId())) {

        // Get input data
        CardResource cardResource =
            new CardResource(reader, (CalypsoCard) readerExtension.getInitialCardContent());
        CardIssuanceInputDto inputData = readerExtension.getInputData(CardIssuanceInputDto.class);

        // Execute service
        outputData = cardService.initCard(cardResource, inputData);

      } else {
        throw new IllegalArgumentException("Service ID not recognized");
      }

      // Terminates the business service by providing the reader name and the optional output data.
      pluginExtension.endRemoteService(readerName, outputData);
    }
  }
}
