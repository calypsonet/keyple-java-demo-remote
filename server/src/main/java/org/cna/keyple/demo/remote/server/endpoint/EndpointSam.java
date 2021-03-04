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

import org.cna.keyple.demo.remote.server.SamResourceService;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Check if the sam is ready
 */
@Path("/sam")
public class EndpointSam {

  @Inject
  SamResourceService samResourceService;

  /**
   * Check if sam is present
   * @return status:200 if sam is found, status:404 else
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response isSamReady() {
    try{
      if(samResourceService.getSamReader().isCardPresent()){
        return Response.ok("{}").build();
      }
    }catch (KeypleReaderNotFoundException e){}
    return Response.status(Response.Status.NOT_FOUND).build();
  }
}
