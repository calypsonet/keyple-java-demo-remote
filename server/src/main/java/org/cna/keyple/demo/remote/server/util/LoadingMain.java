package org.cna.keyple.demo.remote.server.util;

import org.cna.keyple.demo.remote.server.SamResourceManagerConfig;
import org.cna.keyple.demo.remote.server.session.CardContent;
import org.cna.keyple.demo.remote.server.session.CardController;
import org.cna.keyple.demo.sale.data.endpoint.WriteTitleInput;
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

import static org.cna.keyple.demo.remote.server.util.CalypsoUtils.selectPoWithEnvironment;

public class LoadingMain {
    private static final Logger logger = LoggerFactory.getLogger(LoadingMain.class);

    public static void main(String[] args) {
        SamResourceManager samResourceManager = new SamResourceManagerConfig().samResourceManager();

        /*
         * Init readers
         */
        //SmartCardService.getInstance().registerPlugin(new PcscPluginFactory(null, null));
        Reader poReader = PcscReaderUtils.initPoReader(PcscReaderUtils.poReaderFilter);

        /*
         * Select cards
         */
        CalypsoPo calypsoPo = selectPoWithEnvironment(poReader);

        //Reader samReader = PcscReaderUtils.initSamReader(PcscReaderUtils.samReaderFilter);
        //CalypsoSam calypsoSam = selectSam(samReader);
        //CardResource<CalypsoSam> samResource = new CardResource<>(samReader, calypsoSam);
        CardResource<CalypsoSam> samResource = samResourceManager.allocateSamResource(
                SamResourceManager.AllocationMode.BLOCKING,
                new SamIdentifier.SamIdentifierBuilder().serialNumber("").samRevision(SamRevision.AUTO).groupReference(".*").build());

        /*
         * Load a contract
         */
        CardController cardController = CardController.newBuilder()
                .withCalypsoPo(calypsoPo)
                .withReader(poReader)
                .withSamResource(samResource)
                .build();

        CardContent cardContent = cardController.readCard();
        logger.info(cardContent.toString());
        cardContent.listValidContracts();

        WriteTitleInput writeTitleInput = new WriteTitleInput().setContractTariff(PriorityCode.SEASON_PASS);
        //WriteTitleInput writeTitleInput = new WriteTitleInput().setContractTariff(PriorityCode.MULTI_TRIP_TICKET).setTicketToLoad(2);
        //WriteTitleInput writeTitleInput = new WriteTitleInput().setContractTariff(PriorityCode.STORED_VALUE).setTicketToLoad(13);

        cardContent.insertNewContract(writeTitleInput.getContractTariff(), writeTitleInput.getTicketToLoad());
        cardController.writeCard(cardContent);

        logger.info(cardContent.toString());
        samResourceManager.freeSamResource(samResource);
        System.exit(0);
    }









}
