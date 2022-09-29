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
package org.calypsonet.keyple.demo.reload.remote.server.card;

import java.util.ArrayList;
import java.util.List;
import org.calypsonet.keyple.demo.common.model.ContractStructure;
import org.calypsonet.keyple.demo.common.model.EnvironmentHolderStructure;
import org.calypsonet.keyple.demo.common.model.EventStructure;
import org.eclipse.keyple.core.util.json.JsonUtil;

class Card {

  private final EnvironmentHolderStructure environment;
  private final List<ContractStructure> contracts;
  private EventStructure event;

  private final List<ContractStructure> updatedContracts = new ArrayList<>();
  private boolean isEventUpdated;

  Card(
      EnvironmentHolderStructure environment,
      List<ContractStructure> contracts,
      EventStructure event) {
    this.environment = environment;
    this.contracts = contracts;
    this.event = event;
  }

  EnvironmentHolderStructure getEnvironment() {
    return environment;
  }

  List<ContractStructure> getContracts() {
    return contracts;
  }

  EventStructure getEvent() {
    return event;
  }

  List<ContractStructure> getUpdatedContracts() {
    return updatedContracts;
  }

  Boolean isEventUpdated() {
    return isEventUpdated;
  }

  void setContract(int index, ContractStructure contract) {
    contracts.set(index, contract);
    updatedContracts.add(contract);
  }

  void setEvent(EventStructure event) {
    this.event = event;
    this.isEventUpdated = true;
  }

  @Override
  public String toString() {
    return "CARD=" + JsonUtil.toJson(this);
  }
}
