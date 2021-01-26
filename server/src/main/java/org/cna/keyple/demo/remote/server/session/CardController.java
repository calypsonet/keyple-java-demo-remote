package org.cna.keyple.demo.remote.server.session;

import org.cna.keyple.demo.remote.server.util.CalypsoClassicInfo;
import org.cna.keyple.demo.remote.server.util.CalypsoUtils;
import org.cna.keyple.demo.sale.data.model.*;
import org.cna.keyple.demo.sale.data.model.type.DateCompact;
import org.cna.keyple.demo.sale.data.model.type.VersionNumber;
import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.card.selection.CardResource;
import org.eclipse.keyple.core.card.selection.CardSelectionsService;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.cna.keyple.demo.remote.server.util.CalypsoClassicInfo.*;

public class CardController {

    private static final Logger logger = LoggerFactory.getLogger(CardController.class);
    private final CalypsoPo calypsoPo;
    private final Reader poReader;
    private final CardResource<CalypsoSam> samResource;

    public CardController(CalypsoPo calypsoPo , Reader poReader, CardResource<CalypsoSam> samResource){
        this.calypsoPo = calypsoPo;
        this.poReader = poReader;
        this.samResource = samResource;
    }

    /**
     * Read card
     * @return
     */
    public CardContent readCard(){
        // prepare the PO Transaction
        PoTransaction poTransaction =
                new PoTransaction(
                        new CardResource<CalypsoPo>(this.poReader, this.calypsoPo),
                        CalypsoUtils.getSecuritySettings(this.samResource));

        /*
         * Open Calypso session
         */
        logger.info("Open Calypso Session...");
        poTransaction.processOpening(PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_LOAD);

        // Prepare reading of environment record
        poTransaction.prepareReadRecordFile(SFI_EnvironmentAndHolder, RECORD_NUMBER_1);

        // Prepare reading of last event record
        poTransaction.prepareReadRecordFile(
                CalypsoClassicInfo.SFI_EventLog, CalypsoClassicInfo.RECORD_NUMBER_1);

        // Prepare reading of contract records (Calypso Light)
        poTransaction.prepareReadRecordFile(
                SFI_Contracts, CalypsoClassicInfo.RECORD_NUMBER_1);
        poTransaction.prepareReadRecordFile(
                SFI_Contracts, CalypsoClassicInfo.RECORD_NUMBER_2);
        poTransaction.prepareReadRecordFile(
                SFI_Contracts, CalypsoClassicInfo.RECORD_NUMBER_3);
        poTransaction.prepareReadRecordFile(
                SFI_Contracts, CalypsoClassicInfo.RECORD_NUMBER_4);

        // Prepare reading of counter record
        poTransaction.prepareReadCounterFile(
                SFI_Counters, CalypsoClassicInfo.RECORD_NUMBER_1);

        logger.info("Read Card...");

        poTransaction.processPoCommands();

        /*
         * Close Calypso session
         */
        poTransaction.processClosing();

        logger.info("Calypso Session Closed.");
        return new CardContent().parse(calypsoPo);
    }

    /**
     * Update card based on a card session
     * @param cardContent
     * @return status code
     */
    public int writeCard(CardContent cardContent){
        // prepare the PO Transaction
        PoTransaction poTransaction =
                new PoTransaction(
                        new CardResource<>(this.poReader, this.calypsoPo),
                        CalypsoUtils.getSecuritySettings(this.samResource));

        /* Open Calypso session */
        logger.info("Open Calypso Session - SESSION_LVL_LOAD...");
        poTransaction.processOpening(PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_LOAD);

        /* Update contract records */
        if(!cardContent.getContractUpdated().isEmpty()){
            for(int i=0;i<4;i++){
                ContractStructureDto contract = cardContent.getContracts().get(i);
                if(cardContent.getContractUpdated().contains(contract)){
                   //update contract
                    poTransaction.prepareUpdateRecord(SFI_Contracts, i+1, ContractStructureParser.unparse(contract));
                }
            }
        }

        /* Update event */
        if(cardContent.isCounterUpdated()){
            poTransaction.prepareUpdateRecord(SFI_EventLog, 1,
                    EventStructureParser.unparse(buildEvent(cardContent.getEvent(), cardContent.getContracts())));
        }

        /* Update counter */
        if(cardContent.isCounterUpdated()){
            poTransaction.prepareUpdateRecord(SFI_Counters, 1,
                    CounterStructureParser.unparse(cardContent.getCounter()));
        }

        /* Close Session */
        poTransaction.processClosing();
        logger.info("Calypso Session Closed - SESSION_LVL_LOAD");

        return 0;
    }

    public void initCard(){
        // prepare the PO Transaction
        PoTransaction poTransaction =
                new PoTransaction(
                        new CardResource<CalypsoPo>(poReader, calypsoPo),
                        CalypsoUtils.getSecuritySettings(samResource));

        /*
         * Open Calypso session
         */
        logger.info("Open Calypso Session - SESSION_LVL_PERSO...");
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

        logger.info("Calypso Session Closed - SESSION_LVL_PERSO");
    }

    /**
     * Verify that the environment file of the card is valid
     * @return true if card is init
     */
    public Boolean verifyEnvironmentFile(){
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

        return environmentAndHolder.equals(CardController.getEnvironmentInit());
    }

    /**
     *
     * @return environment structure init
     */
    public static EnvironmentHolderStructureDto getEnvironmentInit() {
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
     * Fill the event structure to update:
     * - EventVersionNumber = 1
     * - EventDateStamp = value read from previous event
     * - EventTimeStamp = value read from previous event
     * - EventLocation = value read from previous event
     * - EventContractUsed = value read from previous event
     * - ContractPriority1 = Value of index 0 of ContractPriority persistent object
     * - ContractPriority2 = Value of index 1 of ContractPriority persistent object
     * - ContractPriority3 = Value of index 2 of ContractPriority persistent object
     * - ContractPriority4 = Value of index 3 of ContractPriority persistent object
     * - EventPadding = 0
     * @param oldEvent previous event
     * @param contracts list of updated contracts
     * @return a new event
     */
    private EventStructureDto buildEvent(EventStructureDto oldEvent, List<ContractStructureDto> contracts){
        return EventStructureDto.newBuilder()
                .setEventVersionNumber(VersionNumber.CURRENT_VERSION)
                .setEventDateStamp(oldEvent.getEventDateStamp())
                .setEventTimeStamp(oldEvent.getEventTimeStamp())
                .setEventLocation(oldEvent.getEventLocation())
                .setEventContractUsed(oldEvent.getEventContractUsed())
                .setContractPriority1(contracts.get(0).getContractTariff())
                .setContractPriority2(contracts.get(1).getContractTariff())
                .setContractPriority3(contracts.get(2).getContractTariff())
                .setContractPriority4(contracts.get(3).getContractTariff())
                .build();
    }
}
