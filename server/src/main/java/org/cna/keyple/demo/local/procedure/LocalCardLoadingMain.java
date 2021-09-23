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
package org.cna.keyple.demo.local.procedure;

import static org.cna.keyple.demo.distributed.server.util.CalypsoUtils.selectCardWithEnvironment;

import org.calypsonet.terminal.calypso.card.CalypsoCard;
import org.cna.keyple.demo.distributed.server.calypso.CalypsoCardController;
import org.cna.keyple.demo.distributed.server.calypso.CalypsoCardRepresentation;
import org.cna.keyple.demo.distributed.server.plugin.SamResourceConfiguration;
import org.cna.keyple.demo.distributed.server.util.CalypsoConstants;
import org.cna.keyple.demo.sale.data.model.type.PriorityCode;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.resource.CardResource;
import org.eclipse.keyple.core.service.resource.CardResourceServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Execute locally a write contract operation */
public class LocalCardLoadingMain {
  private static final Logger logger = LoggerFactory.getLogger(LocalCardLoadingMain.class);
  public static String poReaderFilter = ".*(ASK|ACS).*";
  public static String samReaderFilter = ".*(Cherry TC|SCM Microsystems|Identive|HID|Generic).*";

  public static void main(String[] args) {
    SamResourceConfiguration samResourceConfiguration =
        new SamResourceConfiguration(samReaderFilter);
    samResourceConfiguration.init();

    /*
     * Init readers
     */
    Reader poReader = LocalConfigurationUtil.initReader(poReaderFilter);

    /*
     * Select cards
     */
    CalypsoCard calypsoCard = selectCardWithEnvironment(poReader);
    CardResource samResource =
        CardResourceServiceProvider.getService().getCardResource(CalypsoConstants.SAM_PROFILE_NAME);

    /*
     * Load a contract
     */
    CalypsoCardController calypsoCardController =
        CalypsoCardController.newBuilder()
            .withCalypsoCard(calypsoCard)
            .withCardReader(poReader)
            .withSamResource(samResource)
            .build();

    CalypsoCardRepresentation calypsoCardContent = calypsoCardController.readCard();
    logger.info(calypsoCardContent.toString());
    calypsoCardContent.listValidContracts();

    // WriteContractInput writeContractInput = new
    // WriteContractInput().setContractTariff(PriorityCode.MULTI_TRIP_TICKET).setTicketToLoad(2);
    // WriteContractInput writeContractInput = new
    // WriteContractInput().setContractTariff(PriorityCode.STORED_VALUE).setTicketToLoad(13);

    calypsoCardContent.insertNewContract(PriorityCode.SEASON_PASS, 10);
    calypsoCardController.writeCard(calypsoCardContent);

    logger.info(calypsoCardContent.toString());
    CardResourceServiceProvider.getService().releaseCardResource(samResource);
    System.exit(0);
  }
}
