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
package org.cna.keyple.demo.sale.data.endpoint;

import org.cna.keyple.demo.sale.data.model.type.PriorityCode;

import java.util.List;

/**
 * Output of Compatible Contract endpoint
 */
public class CompatibleContractOutput {

  //mandatory
  private List<PriorityCode> validContracts;
  //optional
  private Integer ticketLoaded;

  /*
   * mandatory
   * - 0 if successful
   * - 1 server is not ready
   * - 2 card rejected
   */
  private Integer statusCode;

  public List<PriorityCode> getValidContracts() {
    return validContracts;
  }

  public CompatibleContractOutput setValidContracts(List<PriorityCode> validContracts) {
    this.validContracts = validContracts;
    return this;
  }

  public Integer getStatusCode() {
    return statusCode;
  }

  public CompatibleContractOutput setStatusCode(Integer statusCode) {
    this.statusCode = statusCode;
    return this;
  }

  public CompatibleContractOutput setTicketLoaded(Integer ticketLoaded) {
    this.ticketLoaded = ticketLoaded;
    return this;
  }

  public Integer getTicketLoaded() {
    return ticketLoaded;
  }
}
