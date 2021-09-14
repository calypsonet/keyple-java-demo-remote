package org.cna.keyple.demo.procedure;

import org.calypsonet.terminal.calypso.card.CalypsoCard;
import org.cna.keyple.demo.distributed.server.plugin.SamCardConfiguration;
import org.cna.keyple.demo.distributed.server.controller.CalypsoPoRepresentation;
import org.cna.keyple.demo.distributed.server.controller.CalypsoPoController;
import org.cna.keyple.demo.distributed.server.util.CalypsoConstants;
import org.cna.keyple.demo.distributed.server.util.PcscReaderUtils;
import org.cna.keyple.demo.sale.data.model.type.PriorityCode;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.resource.CardResource;
import org.eclipse.keyple.core.service.resource.CardResourceServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.cna.keyple.demo.distributed.server.util.CalypsoUtils.selectCardWithEnvironment;

/**
 * Execute locally a write contract operation
 */
public class LocalCardLoadingMain {
    private static final Logger logger = LoggerFactory.getLogger(LocalCardLoadingMain.class);
    public static String poReaderFilter = ".*(ASK|ACS).*";
    public static String samReaderFilter = ".*(Cherry TC|SCM Microsystems|Identive|HID|Generic).*";

    public static void main(String[] args) {
        SamCardConfiguration samCardConfiguration = new SamCardConfiguration(samReaderFilter);

        /*
         * Init readers
         */
        //SmartCardService.getInstance().registerPlugin(new PcscPluginFactory(null, null));
        Reader poReader = PcscReaderUtils.initPoReader(poReaderFilter);

        /*
         * Select cards
         */
        CalypsoCard calypsoCard = selectCardWithEnvironment(poReader);

        CardResource samResource = CardResourceServiceProvider.getService().getCardResource(CalypsoConstants.SAM_PROFILE_NAME);

        /*
         * Load a contract
         */
        CalypsoPoController calypsoPoController = CalypsoPoController.newBuilder()
                .withCalypsoCard(calypsoCard)
                .withCardReader(poReader)
                .withSamResource(samResource)
                .build();

        CalypsoPoRepresentation calypsoPoContent = calypsoPoController.readCard();
        logger.info(calypsoPoContent.toString());
        calypsoPoContent.listValidContracts();

        //WriteContractInput writeContractInput = new WriteContractInput().setContractTariff(PriorityCode.MULTI_TRIP_TICKET).setTicketToLoad(2);
        //WriteContractInput writeContractInput = new WriteContractInput().setContractTariff(PriorityCode.STORED_VALUE).setTicketToLoad(13);

        calypsoPoContent.insertNewContract(PriorityCode.SEASON_PASS, 10);
        calypsoPoController.writeCard(calypsoPoContent);

        logger.info(calypsoPoContent.toString());
        CardResourceServiceProvider.getService().releaseCardResource(samResource);
        System.exit(0);
    }









}
