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
package org.cna.keyple.demo.distributed.server.log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.enterprise.context.ApplicationScoped;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Store transaction logs and allow a (unique) subscriber to receive push notification */
@ApplicationScoped
public class TransactionLogStore {

  private static final Logger logger = LoggerFactory.getLogger(TransactionLogStore.class);

  List<TransactionLog> transactionLogs; // list of all transactions
  BlockingQueue<TransactionLog> transactionLogQueue; // queue for the subscriber

  /** (package private) Constructor */
  TransactionLogStore() {
    transactionLogs = new ArrayList<>();
    transactionLogQueue = new ArrayBlockingQueue<>(1);
  }

  /**
   * Return all transactionLogs
   *
   * @return not nullable list of transactionLogs
   */
  public List<TransactionLog> list() {
    return (List<TransactionLog>) ((ArrayList<TransactionLog>) transactionLogs).clone();
  }

  /**
   * Push a new transaction to a subscriber
   *
   * @param t transaction object to push
   */
  public void push(@NotNull TransactionLog t) {
    // store the new transaction
    transactionLogs.add(t);

    // make it available in the queue
    if (!transactionLogQueue.isEmpty()) {
      transactionLogQueue.clear();
    }
    if (transactionLogQueue.offer(t)) {
      logger.trace("A new transaction is available in the queue");
    }
    ;
  }

  /**
   * Blocking call, wait for a new transaction to be published. Timeout of 10 seconds
   *
   * @return transaction when published, or null if no transaction were published
   */
  public TransactionLog waitForNew() {
    try {
      return transactionLogQueue.poll(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      return null;
    }
  }
}
