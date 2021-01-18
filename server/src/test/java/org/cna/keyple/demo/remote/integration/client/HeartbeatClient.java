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
package org.cna.keyple.demo.remote.integration.client;

import org.cna.keyple.demo.remote.server.endpoint.EndpointServer;
import org.eclipse.keyple.distributed.MessageDto;
import org.eclipse.keyple.distributed.spi.SyncEndpointClient;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;

/**
 * Heartbeat tests client
 */
@RegisterRestClient(configKey = "heartbeat")
public interface HeartbeatClient {

  @GET
  @Path("/heartbeat")
  String ping();
}
