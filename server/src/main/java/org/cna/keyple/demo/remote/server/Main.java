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
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusMain
public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String... args) {
    Quarkus.run(DemoApp.class, args);
  }

  /** Main class of the server application. */
  public static class DemoApp implements QuarkusApplication {

    /** {@inheritDoc} */
    @Override
    public int run(String... args) {
      logger.info("Main app init...");
      Quarkus.waitForExit();
      return 0;
    }
  }
}
