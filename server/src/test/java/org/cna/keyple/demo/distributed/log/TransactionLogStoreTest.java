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
package org.cna.keyple.demo.distributed.log;

import io.quarkus.test.junit.QuarkusTest;
import javax.inject.Inject;
import org.cna.keyple.demo.distributed.server.log.TransactionLog;
import org.cna.keyple.demo.distributed.server.log.TransactionLogStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Test the transaction logs store */
@QuarkusTest
public class TransactionLogStoreTest {

  @Inject TransactionLogStore transactionLogStore;

  final TransactionLog t = new TransactionLog();

  @Test
  public void wait_for_new_transaction() {
    pushTransactionAsync();
    Assertions.assertEquals(t, transactionLogStore.waitForNew());
    Assertions.assertEquals(1, transactionLogStore.list().size());
  }

  /** Push a transaction into the store asynchronously */
  private void pushTransactionAsync() {
    new Thread(
            () -> {
              try {
                Thread.sleep(1000);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              transactionLogStore.push(t);
            })
        .start();
  }
}
