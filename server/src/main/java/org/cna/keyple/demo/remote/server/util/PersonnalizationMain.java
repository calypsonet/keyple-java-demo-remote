package org.cna.keyple.demo.remote.server.util;

import org.cna.keyple.demo.sale.data.model.ContractStructureParser;
import org.cna.keyple.demo.sale.data.model.EnvironmentHolderStructureDto;
import org.cna.keyple.demo.sale.data.model.EnvironmentHolderStructureParser;
import org.cna.keyple.demo.sale.data.model.EventStructureParser;
import org.cna.keyple.demo.sale.data.model.type.DateCompact;
import org.cna.keyple.demo.sale.data.model.type.VersionNumber;
import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.card.selection.CardResource;
import org.eclipse.keyple.core.card.selection.CardSelectionsResult;
import org.eclipse.keyple.core.card.selection.CardSelectionsService;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.util.ContactCardCommonProtocols;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.plugin.pcsc.PcscSupportedContactProtocols;
import org.eclipse.keyple.plugin.pcsc.PcscSupportedContactlessProtocols;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.cna.keyple.demo.remote.server.util.CalypsoClassicInfo.*;
import static org.eclipse.keyple.calypso.command.sam.SamRevision.C1;

public class PersonnalizationMain {
    private static String poReaderFilter = ".*(ASK|ACS).*";
    private static String samReaderFilter = ".*(Cherry TC|SCM Microsystems|Identive|HID|Generic).*";
    private static final Logger logger = LoggerFactory.getLogger(PersonnalizationMain.class);

    public static void main(String[] args) {
        Plugin pcscPlugin = SmartCardService.getInstance().registerPlugin(new PcscPluginFactory(null, null));

        Reader poReader = initPoReader();

        Reader samReader = initSamReader();

        CalypsoSam calypsoSam = selectSam(samReader);

        CalypsoPo calypsoPo = selectPo(poReader);

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


    /**
     * Operate the PO selection
     *
     * @param poReader the reader where to operate the PO selection
     * @return a CalypsoPo object if the selection succeed
     * @throws IllegalStateException if the selection fails
     */
    static CalypsoPo selectPo(Reader poReader) {

        // Check if a PO is present in the reader
        if (!poReader.isCardPresent()) {
            throw new IllegalStateException("No PO is present in the reader " + poReader.getName());
        }

        // Prepare a Calypso PO selection
        CardSelectionsService cardSelectionsService = new CardSelectionsService();

        // make the selection and read additional information afterwards
        PoSelection poSelection =
                new PoSelection(
                        PoSelector.builder()
                                .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                                .aidSelector(
                                        CardSelector.AidSelector.builder().aidToSelect(AID).build())
                                .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                                .build());

        // Add the selection case to the current selection
        cardSelectionsService.prepareSelection(poSelection);


        // Actual PO communication: operate through a single request the Calypso PO selection
        // and the file read
        CalypsoPo calypsoPo =
                (CalypsoPo) cardSelectionsService.processExplicitSelections(poReader).getActiveSmartCard();

        return calypsoPo;
    }

    /**
     * Operate the SAM selection
     *
     * @param samReader the reader where to operate the SAM selection
     * @return a CalypsoSam object if the selection succeed
     * @throws IllegalStateException if the selection fails
     */
    static CalypsoSam selectSam(Reader samReader) {
        // Create a SAM resource after selecting the SAM
        CardSelectionsService samSelection = new CardSelectionsService();

        // Prepare selector
        samSelection.prepareSelection(
                new SamSelection(SamSelector.builder().samRevision(C1).serialNumber(".*").build()));

        if (!samReader.isCardPresent()) {
            throw new IllegalStateException("No SAM is present in the reader " + samReader.getName());
        }

        CardSelectionsResult cardSelectionsResult = samSelection.processExplicitSelections(samReader);

        if (!cardSelectionsResult.hasActiveSelection()) {
            throw new IllegalStateException("Unable to open a logical channel for SAM!");
        }

        CalypsoSam calypsoSam = (CalypsoSam) cardSelectionsResult.getActiveSmartCard();

        return calypsoSam;
    }



    private static Reader initPoReader() {

    Reader reader = PcscReaderUtils.getReaderByPattern(poReaderFilter);

    // Get and configure the PO reader
    ((PcscReader) reader).setContactless(true).setIsoProtocol(PcscReader.IsoProtocol.T1);

    // activate protocols
    reader.activateProtocol(
            PcscSupportedContactlessProtocols.ISO_14443_4.name(),
            ContactlessCardCommonProtocols.ISO_14443_4.name());

    logger.info("PO Reader configured : {}", reader.getName());
    return reader;

    }
    private static Reader initSamReader() {

        Reader reader = PcscReaderUtils.getReaderByPattern(samReaderFilter);

        ((PcscReader) reader).setContactless(false).setIsoProtocol(PcscReader.IsoProtocol.T0);

        reader.activateProtocol(
                PcscSupportedContactProtocols.ISO_7816_3.name(),
                ContactCardCommonProtocols.ISO_7816_3.name());
        logger.info("SAM Reader configured : {}", reader.getName());
        return reader;
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
