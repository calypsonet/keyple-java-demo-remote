package org.cna.keyple.demo.distributed.server.procedure;

import org.cna.keyple.demo.distributed.server.controller.CalypsoPoController;
import org.cna.keyple.demo.distributed.server.controller.SamResourceService;
import org.cna.keyple.demo.distributed.server.util.CalypsoClassicInfo;
import org.cna.keyple.demo.distributed.server.util.CalypsoUtils;
import org.cna.keyple.demo.distributed.server.util.PcscReaderUtils;
import org.cna.keyple.demo.sale.data.model.EnvironmentHolderStructureDto;
import org.cna.keyple.demo.sale.data.model.parser.EnvironmentHolderStructureParser;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.calypso.transaction.sammanager.SamIdentifier;
import org.eclipse.keyple.calypso.transaction.sammanager.SamResourceManager;
import org.eclipse.keyple.core.card.selection.CardResource;
import org.eclipse.keyple.core.card.selection.CardSelectionsService;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execute locally a card issuance
 */
public class LocalCardIssuanceMain {

    private static final Logger logger = LoggerFactory.getLogger(LocalCardIssuanceMain.class);
    public static String poReaderFilter = ".*(ASK|ACS).*";

    public static void main(String[] args) {
        SamResourceManager samResourceManager = new SamResourceService().getSamResourceManager();

        /*
         * Init readers
         */
         Reader poReader = PcscReaderUtils.initPoReader(poReaderFilter);

        CardResource<CalypsoSam> samResource = samResourceManager.allocateSamResource(
                SamResourceManager.AllocationMode.BLOCKING,
                new SamIdentifier.SamIdentifierBuilder()
                        .serialNumber("")
                        .samRevision(SamRevision.AUTO)
                        .groupReference(".*").build());

        /*
         * Select cards
         */
        CalypsoPo calypsoPo = CalypsoUtils.selectPo(poReader);

        /*
         * Reset environment file
         */
        CalypsoPoController calypsoPoController = CalypsoPoController.newBuilder()
                .withCalypsoPo(calypsoPo)
                .withReader(poReader)
                .withSamResource(samResource)
                .build();
        logger.info(calypsoPoController.readCard().toString());
        calypsoPoController.initCard();
        logger.info(calypsoPoController.readCard().toString());
        logger.info("Is card init : {}", verifyEnvironmentFile(poReader));
    }


    /**
     * Verify that the environment file of the card is valid
     * @return true if card is init
     */
    static public Boolean verifyEnvironmentFile(Reader poReader){
        // Prepare a Calypso PO selection
        CardSelectionsService cardSelectionsService = new CardSelectionsService();

        // Setting of an AID based selection of a Calypso REV3 PO
        PoSelection poSelection =
                new PoSelection(
                        PoSelector.builder()
                                .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                                .aidSelector(
                                        CardSelector.AidSelector.builder().aidToSelect(CalypsoClassicInfo.AID).build())
                                .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                                .build());

        // Prepare the reading order.
        poSelection.prepareReadRecordFile(
                CalypsoClassicInfo.SFI_EnvironmentAndHolder, 1);
        cardSelectionsService.prepareSelection(poSelection);

        CalypsoPo calypsoPo =
                (CalypsoPo) cardSelectionsService.processExplicitSelections(poReader).getActiveSmartCard();

        logger.info("The selection of the PO has succeeded.");

        // Retrieve the data read from the CalyspoPo updated during the transaction process
        ElementaryFile efEnvironmentAndHolder =
                calypsoPo.getFileBySfi(CalypsoClassicInfo.SFI_EnvironmentAndHolder);

        EnvironmentHolderStructureDto environmentAndHolder =
                EnvironmentHolderStructureParser.parse(efEnvironmentAndHolder.getData().getContent());

        // Log the result
        logger.info("EnvironmentAndHolder file data: {}", environmentAndHolder);

        return environmentAndHolder.equals(CalypsoPoController.getEnvironmentInit());
    }

}
