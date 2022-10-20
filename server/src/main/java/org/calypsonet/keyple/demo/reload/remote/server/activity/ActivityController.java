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
package org.calypsonet.keyple.demo.reload.remote.server.activity;

import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/activity")
public class ActivityController {

  @Inject ActivityService activityService;

  /**
   * List all events
   *
   * @return not nullable set of events
   */
  @GET
  @Path("/events")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Activity> getEvents() {
    return activityService.list();
  }

  /**
   * Long Polling API to get a new event. http code:200: new event is available. HTTP code 204:
   * timeout, please renew request
   *
   * @return a {@link Activity} when a new log is push
   */
  @GET
  @Path("/events/wait")
  @Produces(MediaType.APPLICATION_JSON)
  public Response waitForEvent() {
    Activity t = activityService.waitForNew();
    if (t == null) {
      return Response.noContent().build();
    } else {
      return Response.ok(t).build();
    }
  }
}
