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
package org.cna.keyple.demo.remote.server.endpoint;

import org.eclipse.keyple.distributed.MessageDto;
import org.eclipse.keyple.distributed.SyncNodeServer;
import org.eclipse.keyple.distributed.impl.RemotePluginServerUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * heartbeat endpoint
 */
@Path("/heartbeat")
public class EndpointHeartbeat {

  @GET
  public String ping() {
    return "ok";
  }
}
