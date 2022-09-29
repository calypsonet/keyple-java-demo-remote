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
package org.calypsonet.keyple.demo.reload.remote.server;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import java.awt.*;
import java.net.URI;
import javax.inject.Inject;
import org.calypsonet.keyple.demo.reload.remote.server.card.CardConfigurator;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Main class of quarkus */
@QuarkusMain
public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String... args) {
    Quarkus.run(AppServer.class, args);
  }

  /** Main class of the Demo Application. */
  public static class AppServer implements QuarkusApplication {

    @ConfigProperty(name = "quarkus.http.port")
    Integer assignedPort;

    @Inject CardConfigurator cardConfigurator;

    @Override
    public int run(String... args) throws Exception {
      // Start the SAM & Calypso Card configuration
      cardConfigurator.init();
      // Open the dashboard on the default browser
      URI webappUri = new URI("http://localhost:" + assignedPort + "/");
      Desktop.getDesktop().browse(webappUri);
      logger.info("Keyple Demo Reload Remote Server started at port {}", assignedPort);
      Quarkus.waitForExit();
      return 0;
    }
  }
}
