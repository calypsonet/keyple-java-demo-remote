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

import java.util.List;

/**
 * Output of Compatible Contract endpoint
 */
public class AnalyzeContractsOutput {

  //mandatory
  private List<ContractStructureDto> validContracts;

  //mandatory
  private Integer statusCode;

  /**
   * Set valid contracts list
   * @param validContracts non nullable list of contracts, can be empty
   * @return this object
   */
  public AnalyzeContractsOutput setValidContracts(List<ContractStructureDto> validContracts) {
    this.validContracts = validContracts;
    return this;
  }

  /**
   * Set status code
   * @param statusCode non nullable integer
   * @return this object
   */
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
   * Return the list of contracts present in the card. Each contract is tied to a counter by its index.
   * @return not null list of contracts
   */
  public List<ContractStructureDto> getValidContracts() {
    return validContracts;
  }


}
