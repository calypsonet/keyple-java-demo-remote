package org.cna.keyple.demo.remote.server.util;

import org.cna.keyple.demo.remote.server.session.CardContent;
import org.cna.keyple.demo.remote.server.session.CardController;
import org.cna.keyple.demo.sale.data.endpoint.WriteTitleInput;
import org.cna.keyple.demo.sale.data.model.type.PriorityCode;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.CalypsoSam;
import org.eclipse.keyple.core.card.selection.CardResource;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.cna.keyple.demo.remote.server.util.CalypsoUtils.selectPoWithEnvironment;
import static org.cna.keyple.demo.remote.server.util.CalypsoUtils.selectSam;

public class LoadingMain {
    private static String poReaderFilter = ".*(ASK|ACS).*";
    private static String samReaderFilter = ".*(Cherry TC|SCM Microsystems|Identive|HID|Generic).*";
    private static final Logger logger = LoggerFactory.getLogger(LoadingMain.class);

    public static void main(String[] args) {

        /*
         * Init readers
         */
        SmartCardService.getInstance().registerPlugin(new PcscPluginFactory(null, null));
        Reader poReader = PcscReaderUtils.initPoReader(poReaderFilter);
        Reader samReader = PcscReaderUtils.initSamReader(samReaderFilter);

        /*
         * Select cards
         */
        CalypsoSam calypsoSam = selectSam(samReader);
        CalypsoPo calypsoPo = selectPoWithEnvironment(poReader);
        CardResource<CalypsoSam> samResource = new CardResource<>(samReader, calypsoSam);

        /*
         * Load a contract
         */
        CardController cardController = new CardController(calypsoPo, poReader, samResource);

        CardContent cardContent = cardController.readCard();

        logger.info(cardContent.toString());

        cardContent.listValidContracts();

        //WriteTitleInput writeTitleInput = new WriteTitleInput().setContractTariff(PriorityCode.SEASON_PASS);

        WriteTitleInput writeTitleInput = new WriteTitleInput().setContractTariff(PriorityCode.MULTI_TRIP_TICKET).setTicketToLoad(2);

        //WriteTitleInput writeTitleInput = new WriteTitleInput().setContractTariff(PriorityCode.STORED_VALUE).setTicketToLoad(13);

        cardContent.insertNewContract(writeTitleInput.getContractTariff(), writeTitleInput.getTicketToLoad());

        cardController.writeCard(cardContent);

        logger.info(cardContent.toString());
    }









}
