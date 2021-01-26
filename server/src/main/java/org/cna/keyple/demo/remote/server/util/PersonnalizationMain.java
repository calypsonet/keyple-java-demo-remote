package org.cna.keyple.demo.remote.server.util;

import org.cna.keyple.demo.remote.server.session.CardController;
import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.card.selection.CardResource;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersonnalizationMain {
    private static String poReaderFilter = ".*(ASK|ACS).*";
    private static String samReaderFilter = ".*(Cherry TC|SCM Microsystems|Identive|HID|Generic).*";
    private static final Logger logger = LoggerFactory.getLogger(PersonnalizationMain.class);

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
        CalypsoSam calypsoSam = CalypsoUtils.selectSam(samReader);
        CalypsoPo calypsoPo = CalypsoUtils.selectPo(poReader);
        CardResource<CalypsoSam> samResource = new CardResource<CalypsoSam>(samReader, calypsoSam);

        /*
         * Reset environment file
         */
        CardController cardController = new CardController(calypsoPo,poReader,samResource);
        logger.info(cardController.readCard().toString());
        cardController.initCard();
        logger.info(cardController.readCard().toString());
        cardController.verifyEnvironmentFile();
    }




}
