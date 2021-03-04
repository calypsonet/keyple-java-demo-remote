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

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;

@QuarkusMain
public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String... args) {
    Quarkus.run(DemoApp.class, args);
  }

  /** Main class of the server application. */
  public static class DemoApp implements QuarkusApplication {

    @ConfigProperty(name = "quarkus.http.port")
    Integer assignedPort;


    /** {@inheritDoc} */
    @Override
    public int run(String... args)  throws Exception  {
      URI webappUri = new URI("http://localhost:" + assignedPort + "/");

      Desktop.getDesktop().browse(webappUri);
      logger.info("Keyple Demo Remote Server Started at port : {}", assignedPort);
      Quarkus.waitForExit();
      return 0;
    }
  }
}
