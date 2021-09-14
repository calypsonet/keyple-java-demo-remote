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
package org.cna.keyple.demo.distributed.integration.client;

import org.cna.keyple.demo.distributed.server.endpoint.EndpointServer;
import org.eclipse.keyple.distributed.MessageDto;
import org.eclipse.keyple.distributed.spi.SyncEndpointClientSpi;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Example of a {@link SyncEndpointClientSpi} implementation using Web Services.
 *
 * <p>Sends requests to the {@link EndpointServer}.
 */
@RegisterRestClient(configKey = "remote-plugin-api")
public interface EndpointClient extends SyncEndpointClientSpi {

  @POST
  @Path("/remote-plugin")
  @Produces(MediaType.APPLICATION_JSON)
  @Override
  List<MessageDto> sendRequest(MessageDto messageDto);
}
