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

import org.cna.keyple.demo.sale.data.model.ContractStructureDto;
import org.cna.keyple.demo.sale.data.model.CounterStructureDto;

import java.util.List;

/**
 * Output of Compatible Contract endpoint
 */
public class AnalyzeContractsOutput {

  //mandatory
  private List<ContractStructureDto> validContracts;
  //mandatory
  private List<CounterStructureDto> validContractsCounters;
  //mandatory
  private Integer statusCode;


  public AnalyzeContractsOutput setValidContracts(List<ContractStructureDto> validContracts) {
    this.validContracts = validContracts;
    return this;
  }

  public AnalyzeContractsOutput setValidContractsCounters(List<CounterStructureDto> validContractsCounters) {
    this.validContractsCounters = validContractsCounters;
    return this;
  }

  public AnalyzeContractsOutput setStatusCode(Integer statusCode) {
    this.statusCode = statusCode;
    return this;
  }

  /**
   * get status code
   * - 0 if successful
   * - 1 server is not ready
   * - 2 card rejected
   * @return not null status code
   */
  public int getStatusCode() {
    return statusCode;
  }

  /**
   * return the list of counters present in the card. Each counter is tied to a contract.
   * @return not null list of counters
   */
  public List<CounterStructureDto> getValidContractsCounters() {
    return validContractsCounters;
  }

  /**
   * return the list of contracts present in the card.
   * @return not null list of contracts
   */
  public List<ContractStructureDto> getValidContracts() {
    return validContracts;
  }


}
