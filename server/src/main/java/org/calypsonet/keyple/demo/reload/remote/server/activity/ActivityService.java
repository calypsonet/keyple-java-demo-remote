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
package org.calypsonet.keyple.demo.reload.remote.server.activity;

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
public class ActivityService {

  private static final Logger logger = LoggerFactory.getLogger(ActivityService.class);

  List<Activity> activities; // list of all transactions
  BlockingQueue<Activity> activityQueue; // queue for the subscriber

  ActivityService() {
    activities = new ArrayList<>();
    activityQueue = new ArrayBlockingQueue<>(1);
  }

  public List<Activity> list() {
    return (List<Activity>) ((ArrayList<Activity>) activities).clone();
  }

  /**
   * Pushes a new transaction to a subscriber.
   *
   * @param t Transaction object to push.
   */
  public void push(@NotNull Activity t) {
    // store the new transaction
    activities.add(t);
    // make it available in the queue
    if (!activityQueue.isEmpty()) {
      activityQueue.clear();
    }
    if (activityQueue.offer(t)) {
      logger.trace("A new transaction is available in the queue");
    }
  }

  /**
   * Blocking call, waits for a new transaction to be published. Timeout of 10 seconds.
   *
   * @return Transaction when published, or null if no transaction were published.
   */
  public Activity waitForNew() {
    try {
      return activityQueue.poll(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      // Restore interrupted state...
      Thread.currentThread().interrupt();
      return null;
    }
  }
}
