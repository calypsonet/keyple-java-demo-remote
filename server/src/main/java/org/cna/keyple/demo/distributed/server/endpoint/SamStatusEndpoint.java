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
package org.cna.keyple.demo.distributed.server.endpoint;

import com.google.gson.JsonObject;
import org.calypsonet.terminal.reader.ReaderCommunicationException;
import org.cna.keyple.demo.distributed.server.plugin.SamResourceConfiguration;

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
public class SamStatusEndpoint {

  @Inject
  SamResourceConfiguration samResourceService;

  /**
   * Check if sam is present
   * @return {isSamReady:true} is sam is ready
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response isSamReady() {
    Boolean isSamReady;
    try{
      isSamReady  =  samResourceService.getSamReader().isCardPresent(); //ping sam
    }catch (ReaderCommunicationException e){
      //reader is disconnected
      isSamReady = false;
    }

    JsonObject object = new JsonObject();
    object.addProperty("isSamReady", isSamReady);
    return Response.ok(object.toString()).build();
  }
}
