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

/**
 * Input of Compatible Contract Endpoint
 */
public class CompatibleContractInput {

  //mandatory
  private String pluginType;

  public String getPluginType() {
    return pluginType;
  }

  public CompatibleContractInput setPluginType(String pluginType) {
    this.pluginType = pluginType;
    return this;
  }
}
