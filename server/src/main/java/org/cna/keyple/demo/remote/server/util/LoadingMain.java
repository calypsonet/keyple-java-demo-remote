package org.cna.keyple.demo.remote.server.util;

import org.cna.keyple.demo.sale.data.model.*;
import org.cna.keyple.demo.sale.data.model.type.DateCompact;
import org.cna.keyple.demo.sale.data.model.type.PriorityCode;
import org.cna.keyple.demo.sale.data.model.type.VersionNumber;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.CalypsoSam;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.core.card.selection.CardResource;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.cna.keyple.demo.remote.server.util.CalypsoClassicInfo.*;
import static org.cna.keyple.demo.remote.server.util.CalypsoUtils.selectPoWithEnvironment;
import static org.cna.keyple.demo.remote.server.util.CalypsoUtils.selectSam;

public class LoadingMain {
    private static String poReaderFilter = ".*(ASK|ACS).*";
    private static String samReaderFilter = ".*(Cherry TC|SCM Microsystems|Identive|HID|Generic).*";
    private static final Logger logger = LoggerFactory.getLogger(LoadingMain.class);

    public static void main(String[] args) {
        //output
        List<ContractStructureDto> validContracts = new ArrayList<ContractStructureDto>();
        CounterStructureDto counter;

        SmartCardService.getInstance().registerPlugin(new PcscPluginFactory(null, null));

        Reader poReader = PcscReaderUtils.initPoReader(poReaderFilter);
        Reader samReader = PcscReaderUtils.initSamReader(samReaderFilter);

        CalypsoSam calypsoSam = selectSam(samReader);
        CalypsoPo calypsoPo = selectPoWithEnvironment(poReader);

        //read environment
        EnvironmentHolderStructureDto environment = EnvironmentHolderStructureParser.parse(calypsoPo.getFileBySfi(SFI_EnvironmentAndHolder).getData().getContent());

        if(environment.getEnvVersionNumber() != VersionNumber.CURRENT_VERSION){
            //reject card
            logger.warn("Version Number of card is invalid, reject card");
            return;
        }

        if(environment.getEnvEndDate().getDaysSinceReference()< new DateCompact(Instant.now()).getDaysSinceReference()){
            //reject card
            logger.warn("EnvEndDate of card is invalid, reject card");
            return;

        }

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

        poTransaction.processOpening(PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_LOAD);

        /*
         * Unpack environment structure from the binary present in the environment record.
         */

        // Read and unpack the last event record.
        poTransaction.prepareReadRecordFile(
                CalypsoClassicInfo.SFI_EventLog, CalypsoClassicInfo.RECORD_NUMBER_1);

        poTransaction.prepareReadRecordFile(
                SFI_Contracts, CalypsoClassicInfo.RECORD_NUMBER_1);
        poTransaction.prepareReadRecordFile(
                SFI_Contracts, CalypsoClassicInfo.RECORD_NUMBER_2);
        poTransaction.prepareReadRecordFile(
                SFI_Contracts, CalypsoClassicInfo.RECORD_NUMBER_3);
        poTransaction.prepareReadRecordFile(
                SFI_Contracts, CalypsoClassicInfo.RECORD_NUMBER_4);

        poTransaction.prepareReadCounterFile(
                SFI_Counters, CalypsoClassicInfo.RECORD_NUMBER_4);

        logger.info("Read Card...");

        poTransaction.processPoCommands();

        EventStructureDto lastEvent = EventStructureParser.parse(
                calypsoPo.getFileBySfi(SFI_EventLog).getData().getContent());


        if(lastEvent.getEventVersionNumber().getValue()!=VersionNumber.CURRENT_VERSION.getValue() &&
                lastEvent.getEventVersionNumber().getValue()!=VersionNumber.FORBIDDEN_UNDEFINED.getValue()){
            //reject card
            logger.warn("EventVersionNumber of card is invalid, reject card");
            return;
        }

        Object[][] contractUpdate = {
        {lastEvent.getContractPriority1(), false},
        {lastEvent.getContractPriority2(), false},
        {lastEvent.getContractPriority3(), false},
        {lastEvent.getContractPriority4(), false}};

        for(int i=0 ; i<4; i++){
            Integer calypsoIndex = i+1;
            ContractStructureDto contract = ContractStructureParser.parse(
                    calypsoPo.getFileBySfi(SFI_Contracts).getData().getContent(calypsoIndex));
            logger.info("Contract at index {} : {} {}", calypsoIndex, contract.getContractTariff(), contract.getContactSaleDate());

            if(contract.getContractVersionNumber()==VersionNumber.FORBIDDEN_UNDEFINED){
                //  If ContractVersionNumber is 0 ensure that the associated ContractPriority field value is 0 and
                //  move on to the next contract.
                if(contract.getContractTariff().getCode() != PriorityCode.FORBIDDEN.getCode()){
                    logger.warn("Contract tariff is not valid for this contract");
                    return;
                }

            }else{

                if(contract.getContractAuthenticator()!=null){
                    //PSO Verify Signature command of the SAM.
                }
                //If ContractValidityEndDate points to a date in the past
                if(contract.getContractValidityEndDate().getDaysSinceReference()<
                        new DateCompact(Instant.now()).getDaysSinceReference()){
                    //  Update the associated ContractPriorty field present
                    //  in the persistent object to 31 and set the change flag to true.
                    contractUpdate[i][0] = PriorityCode.EXPIRED;
                    contractUpdate[i][1] = true;
                }
                if(contract.getContractTariff() == PriorityCode.MULTI_TRIP_TICKET){
                    counter = CounterStructureParser.parse(calypsoPo.getFileBySfi(SFI_Counters).getData().getContent());
                }
                validContracts.add(contract);
            }
        }

        /*
         * Close Calypso session
         */
        poTransaction.processClosing();

        logger.info("Calypso Session Closed.");

        //results
    }

    private static ContractStructureDto getSeasonPass() {
        //calculate issuing date
        Instant now = Instant.now();

        //calculate env end date
        LocalDate envEndDate = now.atZone(ZoneId.systemDefault()).toLocalDate()
                .plusDays(30);

        PriorityCode contractTariff = PriorityCode.SEASON_PASS;

        return ContractStructureDto.newBuilder()
                .setContractVersionNumber(VersionNumber.CURRENT_VERSION)
                .setContractTariff(contractTariff)
                .setContactSaleDate(new DateCompact(Instant.now()))
                .setContractValidityEndDate(new DateCompact(envEndDate.atStartOfDay().toInstant(ZoneOffset.UTC)))
                .build();
    }



}
