package org.cna.keyple.demo.distributed.server.procedure;

import org.cna.keyple.demo.distributed.server.controller.SamResourceService;
import org.cna.keyple.demo.distributed.server.controller.CalypsoPoContent;
import org.cna.keyple.demo.distributed.server.controller.CalypsoPoController;
import org.cna.keyple.demo.distributed.server.util.PcscReaderUtils;
import org.cna.keyple.demo.sale.data.model.type.PriorityCode;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.CalypsoSam;
import org.eclipse.keyple.calypso.transaction.sammanager.SamIdentifier;
import org.eclipse.keyple.calypso.transaction.sammanager.SamResourceManager;
import org.eclipse.keyple.core.card.selection.CardResource;
import org.eclipse.keyple.core.service.Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.cna.keyple.demo.distributed.server.util.CalypsoUtils.selectPoWithEnvironment;

/**
 * Execute locally a write contract operation
 */
public class LocalCardLoadingMain {
    private static final Logger logger = LoggerFactory.getLogger(LocalCardLoadingMain.class);
    public static String poReaderFilter = ".*(ASK|ACS).*";

    public static void main(String[] args) {
        SamResourceManager samResourceManager = new SamResourceService().getSamResourceManager();

        /*
         * Init readers
         */
        //SmartCardService.getInstance().registerPlugin(new PcscPluginFactory(null, null));
        Reader poReader = PcscReaderUtils.initPoReader(poReaderFilter);

        /*
         * Select cards
         */
        CalypsoPo calypsoPo = selectPoWithEnvironment(poReader);

        //Reader samReader = PcscReaderUtils.initSamReader(PcscReaderUtils.samReaderFilter);
        //CalypsoSam calypsoSam = selectSam(samReader);
        //CardResource<CalypsoSam> samResource = new CardResource<>(samReader, calypsoSam);
        CardResource<CalypsoSam> samResource = samResourceManager.allocateSamResource(
                SamResourceManager.AllocationMode.BLOCKING,
                new SamIdentifier.SamIdentifierBuilder()
                        .serialNumber("")
                        .samRevision(SamRevision.AUTO)
                        .groupReference(".*").build());

        /*
         * Load a contract
         */
        CalypsoPoController calypsoPoController = CalypsoPoController.newBuilder()
                .withCalypsoPo(calypsoPo)
                .withReader(poReader)
                .withSamResource(samResource)
                .build();

        CalypsoPoContent calypsoPoContent = calypsoPoController.readCard();
        logger.info(calypsoPoContent.toString());
        calypsoPoContent.listValidContracts();

        //WriteContractInput writeContractInput = new WriteContractInput().setContractTariff(PriorityCode.MULTI_TRIP_TICKET).setTicketToLoad(2);
        //WriteContractInput writeContractInput = new WriteContractInput().setContractTariff(PriorityCode.STORED_VALUE).setTicketToLoad(13);

        calypsoPoContent.insertNewContract(PriorityCode.SEASON_PASS, 10);
        calypsoPoController.writeCard(calypsoPoContent);

        logger.info(calypsoPoContent.toString());
        samResourceManager.freeSamResource(samResource);
        System.exit(0);
    }









}
