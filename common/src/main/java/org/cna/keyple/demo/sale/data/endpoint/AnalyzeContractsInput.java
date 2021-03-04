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
public class AnalyzeContractsInput {

  private String pluginType;

  /**
   * Return the type of plugin used for the po reader
   * @return nullable plugin type
   */
  public String getPluginType() {
    return pluginType;
  }

  /**
   * Set the type of plugin used for the po reader
   * @param pluginType name of the plugin to show on the dashboard
   * @return this object
   */
  public AnalyzeContractsInput setPluginType(String pluginType) {
    this.pluginType = pluginType;
    return this;
  }
}
