package org.cna.keyple.demo.remote.server.util;

import org.cna.keyple.demo.sale.data.model.ContractStructureParser;
import org.cna.keyple.demo.sale.data.model.EnvironmentHolderStructureDto;
import org.cna.keyple.demo.sale.data.model.EnvironmentHolderStructureParser;
import org.cna.keyple.demo.sale.data.model.EventStructureParser;
import org.cna.keyple.demo.sale.data.model.type.DateCompact;
import org.cna.keyple.demo.sale.data.model.type.VersionNumber;
import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.card.selection.CardResource;
import org.eclipse.keyple.core.card.selection.CardSelectionsService;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.cna.keyple.demo.remote.server.util.CalypsoClassicInfo.*;

public class PersonnalizationMain {
    private static String poReaderFilter = ".*(ASK|ACS).*";
    private static String samReaderFilter = ".*(Cherry TC|SCM Microsystems|Identive|HID|Generic).*";
    private static final Logger logger = LoggerFactory.getLogger(PersonnalizationMain.class);

    public static void main(String[] args) {
        Plugin pcscPlugin = SmartCardService.getInstance().registerPlugin(new PcscPluginFactory(null, null));

        Reader poReader = PcscReaderUtils.initPoReader(poReaderFilter);

        Reader samReader = PcscReaderUtils.initSamReader(samReaderFilter);

        CalypsoSam calypsoSam = CalypsoUtils.selectSam(samReader);

        CalypsoPo calypsoPo = CalypsoUtils.selectPo(poReader);

        CardResource<CalypsoSam> samResource = new CardResource<CalypsoSam>(samReader, calypsoSam);

        // prepare the PO Transaction
        PoTransaction poTransaction =
                new PoTransaction(
                        new CardResource<CalypsoPo>(poReader, calypsoPo),
                        CalypsoUtils.getSecuritySettings(samResource));

        /*
         * Open Calypso session
         */
        logger.info("Open Calypso Session...");

        poTransaction.processOpening(PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_PERSO);

        /*
         * Prepare file update
         */


        //Fill the environment structure with predefined values
        poTransaction.prepareUpdateRecord(SFI_EnvironmentAndHolder, 1,
                EnvironmentHolderStructureParser.unparse(getEnvironmentInit()));

        //Clear the first event (update with a byte array filled with 0s).
        poTransaction.prepareUpdateRecord(SFI_EventLog, 1,
                EnvironmentHolderStructureParser.getEmpty());

        //Clear all contracts (update with a byte array filled with 0s).
        //TODO do not support CLAP
        poTransaction.prepareUpdateRecord(SFI_Contracts, 1,
                ContractStructureParser.getEmpty());
        poTransaction.prepareUpdateRecord(SFI_Contracts, 2,
                ContractStructureParser.getEmpty());
        poTransaction.prepareUpdateRecord(SFI_Contracts, 3,
                ContractStructureParser.getEmpty());
        poTransaction.prepareUpdateRecord(SFI_Contracts, 4,
                ContractStructureParser.getEmpty());

        //Clear the counter file (update with a byte array filled with 0s).
        poTransaction.prepareUpdateRecord(SFI_Counters, 1, EventStructureParser.getEmpty());

        /*
         * Close Calypso session
         */
        poTransaction.processClosing();

        logger.info("Calypso Session Closed.");

        verifyEnvironmentFile(poReader);
    }


    private static EnvironmentHolderStructureDto getEnvironmentInit() {
        //calculate issuing date
        Instant now = Instant.now();

        //calculate env end date
        LocalDate envEndDate = now.atZone(ZoneId.systemDefault()).toLocalDate()
                .withDayOfMonth(1).plusYears(6);

        return EnvironmentHolderStructureDto.newBuilder()
                .setEnvVersionNumber(VersionNumber.CURRENT_VERSION)
                .setEnvApplicationNumber(1)
                .setEnvIssuingDate(new DateCompact(now))
                .setEnvEndDate(new DateCompact(envEndDate.atStartOfDay().toInstant(ZoneOffset.UTC)))
                .build();
    }

    private static void verifyEnvironmentFile(Reader poReader){
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

        assert  environmentAndHolder.equals(getEnvironmentInit());

    }
}
