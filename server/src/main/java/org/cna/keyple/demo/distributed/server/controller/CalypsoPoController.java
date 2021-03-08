package org.cna.keyple.demo.distributed.server.controller;


import org.cna.keyple.demo.distributed.server.util.CalypsoClassicInfo;
import org.cna.keyple.demo.distributed.server.util.CalypsoUtils;
import org.cna.keyple.demo.sale.data.model.ContractStructureDto;
import org.cna.keyple.demo.sale.data.model.EnvironmentHolderStructureDto;
import org.cna.keyple.demo.sale.data.model.EventStructureDto;
import org.cna.keyple.demo.sale.data.model.parser.ContractStructureParser;
import org.cna.keyple.demo.sale.data.model.parser.CounterStructureParser;
import org.cna.keyple.demo.sale.data.model.parser.EnvironmentHolderStructureParser;
import org.cna.keyple.demo.sale.data.model.parser.EventStructureParser;
import org.cna.keyple.demo.sale.data.model.type.DateCompact;
import org.cna.keyple.demo.sale.data.model.type.VersionNumber;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.CalypsoSam;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.core.card.selection.CardResource;
import org.eclipse.keyple.core.service.Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.cna.keyple.demo.distributed.server.util.CalypsoClassicInfo.*;

/**
 * Perform operations with a calypso PO inserted in a Reader. It requires a {@link CalypsoSam} to perform secured operations
 */
public class CalypsoPoController {

    private static final Logger logger = LoggerFactory.getLogger(CalypsoPoController.class);
    private final CalypsoPo calypsoPo;
    private final Reader poReader;
    private final CardResource<CalypsoSam> samResource;

    public static Builder newBuilder(){
        return new Builder();
    }

    /**
     * static Builder class
     */
    public static final class Builder {
        private  CalypsoPo calypsoPo;
        private  Reader poReader;
        private  CardResource<CalypsoSam> samResource;

        /**
         * Specify the calypso PO to which the operation will be executed
         * @param calypsoPo non null instance of a calypso smart card object
         * @return next step of configuration
         */
        public Builder withCalypsoPo(CalypsoPo calypsoPo){
            this.calypsoPo = calypsoPo;
            return this;
        }

        /**
         * Specify the reader where the calypso PO is inserted
         * @param poReader non null instance of a reader
         * @return next step of configuration
         */
        public Builder withReader(Reader poReader){
            this.poReader = poReader;
            return this;
        }
        /**
         * Specify the sam resource required to perform secured operations
         * @param samResource non null instance of a card resource
         * @return next step of configuration
         */
        public Builder withSamResource(CardResource<CalypsoSam> samResource){
            this.samResource = samResource;
            return this;
        }

        public CalypsoPoController build(){
            return new CalypsoPoController(calypsoPo, poReader,samResource);
        }

    }

    /**
     * (private)
     * Build this controller with the calypso resource you aim to read.
     * @param calypsoPo selected smart card
     * @param poReader reader the smart card is inserted into
     * @param samResource sam resource needed to perform secure operations
     */
    private CalypsoPoController(CalypsoPo calypsoPo , Reader poReader, CardResource<CalypsoSam> samResource){
        this.calypsoPo = calypsoPo;
        this.poReader = poReader;
        this.samResource = samResource;
    }

    /**
     * (public)
     * Read all files on the calypso PO inserted in the Reader
     * @return card content
     */
    public CalypsoPoContent readCard(){
        // prepare the PO TransactionLog
        PoTransaction poTransaction =
                new PoTransaction(
                        new CardResource<>(this.poReader, this.calypsoPo),
                        CalypsoUtils.getSecuritySettings(this.samResource));

        //We release the channel after reading
        poTransaction.prepareReleasePoChannel();

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

        // Prepare reading of simulated counter record
        poTransaction.prepareReadCounterFile(
                SFI_Counters_1, CalypsoClassicInfo.RECORD_NUMBER_1);
        poTransaction.prepareReadCounterFile(
                SFI_Counters_2, CalypsoClassicInfo.RECORD_NUMBER_1);
        poTransaction.prepareReadCounterFile(
                SFI_Counters_3, CalypsoClassicInfo.RECORD_NUMBER_1);
        poTransaction.prepareReadCounterFile(
                SFI_Counters_4, CalypsoClassicInfo.RECORD_NUMBER_1);


        logger.info("Read Card...");

        poTransaction.processPoCommands();

        /*
         * Close Calypso session
         */
        poTransaction.processClosing();

        logger.info("Calypso Session Closed.");
        return CalypsoPoContent.parse(calypsoPo);
    }

    /**
     * Write the card content into the inserted card. Only updated-marked files will be physically updated.
     * @param calypsoPoContent updated content to be written
     * @return status code
     */
    public int writeCard(CalypsoPoContent calypsoPoContent){
        // prepare the PO TransactionLog
        PoTransaction poTransaction =
                new PoTransaction(
                        new CardResource<>(this.poReader, this.calypsoPo),
                        CalypsoUtils.getSecuritySettings(this.samResource));

        /* Open Calypso session */
        logger.info("Open Calypso Session - SESSION_LVL_LOAD...");
        poTransaction.processOpening(PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_LOAD);

        /* Update contract records */
        if(!calypsoPoContent.getContractUpdated().isEmpty()){
            for(int i=0;i<4;i++){

                ContractStructureDto contract = calypsoPoContent.getContracts().get(i);

                if(calypsoPoContent.getContractUpdated().contains(contract)){
                   //update contract
                    poTransaction.prepareUpdateRecord(
                            SFI_Contracts,
                            i+1,
                            ContractStructureParser.unparse(contract));

                    //update counter
                    if(contract.getCounter() != null){
                        poTransaction.prepareUpdateRecord(
                                SFI_Counters_simulated.get(i),
                                1,
                                CounterStructureParser.unparse(contract.getCounter()));
                    }
                }
            }
        }

        /* Update event */
        if(calypsoPoContent.isEventUpdated()){
            poTransaction.prepareUpdateRecord(SFI_EventLog, 1,
                    EventStructureParser.unparse(buildEvent(calypsoPoContent.getEvent(), calypsoPoContent.getContracts())));
        }

        /* Close Session */
        poTransaction.processClosing();
        logger.info("Calypso Session Closed - SESSION_LVL_LOAD");

        return 0;
    }

    /**
     * Empty the inserted card with empty files for event, contracts, counters. Init the environment file.
     */
    public void initCard(){
        // prepare the PO TransactionLog
        PoTransaction poTransaction =
                new PoTransaction(
                        new CardResource<>(poReader, calypsoPo),
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
