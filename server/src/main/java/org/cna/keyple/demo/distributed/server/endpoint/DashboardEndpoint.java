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
package org.cna.keyple.demo.distributed.server.endpoint;

import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.cna.keyple.demo.distributed.server.log.TransactionLog;
import org.cna.keyple.demo.distributed.server.log.TransactionLogStore;

@Path("/dashboard")
public class DashboardEndpoint {

  @Inject TransactionLogStore transactionLogStore;

  /**
   * List all transactions
   *
   * @return not nullable set of transactions
   */
  @GET
  @Path("/transaction")
  @Produces(MediaType.APPLICATION_JSON)
  public List<TransactionLog> getTransactions() {
    return transactionLogStore.list();
  }

  /**
   * Long Polling API to get a new transaction. http code:200 : new transaction is available. http
   * code:204 : timeout, please renew request
   *
   * @return a {@link TransactionLog} when a new log is push
   */
  @GET
  @Path("/transaction/wait")
  @Produces(MediaType.APPLICATION_JSON)
  public Response waitForTransaction() throws InterruptedException {
    TransactionLog t = transactionLogStore.waitForNew();
    if (t == null) {
      return Response.noContent().build();
    } else {
      return Response.ok(t).build();
    }
  }
}
