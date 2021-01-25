package org.cna.keyple.demo.remote.server.util;

import org.cna.keyple.demo.remote.server.session.CardSession;
import org.cna.keyple.demo.sale.data.endpoint.CompatibleContractOutput;
import org.cna.keyple.demo.sale.data.endpoint.WriteTitleInput;
import org.cna.keyple.demo.sale.data.endpoint.WriteTitleOutput;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.cna.keyple.demo.remote.server.util.CalypsoClassicInfo.*;
import static org.cna.keyple.demo.remote.server.util.CalypsoUtils.selectPoWithEnvironment;
import static org.cna.keyple.demo.remote.server.util.CalypsoUtils.selectSam;

public class LoadingMain {
    private static String poReaderFilter = ".*(ASK|ACS).*";
    private static String samReaderFilter = ".*(Cherry TC|SCM Microsystems|Identive|HID|Generic).*";
    private static final Logger logger = LoggerFactory.getLogger(LoadingMain.class);

    public static void main(String[] args) {

        SmartCardService.getInstance().registerPlugin(new PcscPluginFactory(null, null));

        Reader poReader = PcscReaderUtils.initPoReader(poReaderFilter);
        Reader samReader = PcscReaderUtils.initSamReader(samReaderFilter);

        CalypsoSam calypsoSam = selectSam(samReader);
        CalypsoPo calypsoPo = selectPoWithEnvironment(poReader);

        CardResource<CalypsoSam> samResource = new CardResource<CalypsoSam>(samReader, calypsoSam);

        listValidContracts(calypsoPo,poReader, samResource);

    }


    private static CompatibleContractOutput listValidContracts(CalypsoPo calypsoPo , Reader poReader, CardResource<CalypsoSam> samResource){
        //output
        List<ContractStructureDto> validContracts = new ArrayList<ContractStructureDto>();
        CounterStructureDto counter = null;
        //read environment
        EnvironmentHolderStructureDto environment = EnvironmentHolderStructureParser.parse(calypsoPo.getFileBySfi(SFI_EnvironmentAndHolder).getData().getContent());

        if(environment.getEnvVersionNumber() != VersionNumber.CURRENT_VERSION){
            //reject card
            logger.warn("Version Number of card is invalid, reject card");
            return new CompatibleContractOutput().setStatusCode(2);
        }

        if(environment.getEnvEndDate().getDaysSinceReference()< new DateCompact(Instant.now()).getDaysSinceReference()){
            //reject card
            logger.warn("EnvEndDate of card is invalid, reject card");
            return new CompatibleContractOutput().setStatusCode(2);
        }


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

        CardSession cardSession = new CardSession(calypsoPo);

        EventStructureDto lastEvent = cardSession.getEvent();

        if(lastEvent.getEventVersionNumber().getValue()!=VersionNumber.CURRENT_VERSION.getValue() &&
                lastEvent.getEventVersionNumber().getValue()!=VersionNumber.FORBIDDEN_UNDEFINED.getValue()){
            //reject card
            logger.warn("EventVersionNumber of card is invalid, reject card");
            return new CompatibleContractOutput().setStatusCode(2);
        }

        int calypsoIndex = 1;
        /* Iterate through the contracts in the card session */
        for(ContractStructureDto contract : cardSession.getContracts()){
            logger.info("Contract at index {} : {} {}", calypsoIndex,  contract.getContractTariff(), contract.getContactSaleDate());

            if(contract.getContractVersionNumber()==VersionNumber.FORBIDDEN_UNDEFINED){
                //  If ContractVersionNumber is 0 ensure that the associated ContractPriority field value is 0 and
                //  move on to the next contract.
                if(contract.getContractTariff().getCode() != PriorityCode.FORBIDDEN.getCode()){
                    logger.warn("Contract tariff is not valid for this contract");
                    return new CompatibleContractOutput().setStatusCode(2);
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
                    contract.setContractTariff(PriorityCode.EXPIRED);
                    cardSession.registerUpdate(contract);
                }
                if(contract.getContractTariff() == PriorityCode.MULTI_TRIP_TICKET){
                    counter = cardSession.getCounter();
                }
                validContracts.add(contract);
            }
            calypsoIndex++;
        }

        /*
         * Close Calypso session
         */
        poTransaction.processClosing();

        logger.info("Calypso Session Closed.");

        //results
        logger.info("Counter {}",counter!=null?counter:"null");
        logger.info("Contracts {}", Arrays.deepToString(validContracts.toArray()));

        return new CompatibleContractOutput().setStatusCode(0).setValidContracts(validContracts);

    }

    private static WriteTitleOutput writeTitle(WriteTitleInput inputDto,
                                               CalypsoPo calypsoPo,
                                               Reader poReader,
                                               CardResource<CalypsoSam> samResource,
                                               CardSession cardSession){
        PriorityCode contractTariff = inputDto.getContractTariff();
        Integer counterIncrement = inputDto.getTicketToLoad();
        WriteTitleOutput output = new WriteTitleOutput();
        ContractStructureDto newContract = buildContract(contractTariff, cardSession.getEnvironment().getEnvEndDate());
        EventStructureDto event = cardSession.getEvent();

        int existingContractIndex = cardSession.isReload(contractTariff);
        int newPosition = 0;

        if(existingContractIndex != 0){
            //is a reload operation
            if(!event.getContractPriorityAt(existingContractIndex)
                    .equals(contractTariff)){
                //contract was expired
                event.setContractPriorityAt(existingContractIndex, contractTariff);
                cardSession.registerUpdate(newContract);
            };
        }else{
            //is a new contract
            newPosition = cardSession.findAvailablePosition();
            if(newPosition == 0){
                //no available position, reject card
                return new WriteTitleOutput().setStatusCode(2);
            }
        }

        // prepare the PO Transaction
        PoTransaction poTransaction =
                new PoTransaction(
                        new CardResource<CalypsoPo>(poReader, calypsoPo),
                        CalypsoUtils.getSecuritySettings(samResource));

        /*
         * Open Calypso session
         */
        logger.info("Open Calypso Session...");
        int contractIndex = existingContractIndex != 0 ? existingContractIndex : newPosition;
        poTransaction.processOpening(PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_LOAD);

        /*
         * Prepare file update
         */


        //update the record of the contract
        poTransaction.prepareUpdateRecord(SFI_Contracts, contractIndex, ContractStructureParser.unparse(newContract));

        if(PriorityCode.MULTI_TRIP_TICKET.equals(newContract.getContractTariff()) ||
                PriorityCode.STORED_VALUE.equals(newContract.getContractTariff())){
            poTransaction.prepareUpdateRecord(SFI_Counters, 1,
                    CounterStructureParser.unparse(CounterStructureDto.newBuilder().setCounterValue(
                            cardSession.getCounter().getCounterValue() + counterIncrement).build()));
        }

        if(!cardSession.getContractUpdated().isEmpty()){
            //update event
            poTransaction.prepareUpdateRecord(SFI_EventLog, 1,
                    EventStructureParser.unparse(buildEvent(cardSession.getEvent(), cardSession.getContracts())));
        }

        return output;
    }


    /**
     * Fill the contract structure to update:
     * - ContractVersionNumber = 1
     * - ContractTariff = Value provided by upper layer
     * - ContractSaleDate = Current Date converted to DateCompact
     * - ContractValidityEndDate = (ContractSaleDate + 30 if ContractTariff == 1) or (EnvEndDate if ContractTariff == 2 or 3)
     * @param contractTariff
     * @param envEndDate
     * @return a new contract
     */
    private static ContractStructureDto buildContract(PriorityCode contractTariff, DateCompact envEndDate) {
        DateCompact contractSaleDate = new DateCompact(Instant.now());
        DateCompact contractValidityEndDate;

        //calculate ContractValidityEndDate
        if(contractTariff == PriorityCode.SEASON_PASS){
            contractValidityEndDate = new DateCompact((short) (contractSaleDate.getDaysSinceReference() + 30));
        }else{
            contractValidityEndDate = envEndDate;
        }

        return ContractStructureDto.newBuilder()
                .setContractVersionNumber(VersionNumber.CURRENT_VERSION)
                .setContractTariff(contractTariff)
                .setContractSaleDate(contractSaleDate)
                .setContractValidityEndDate(contractValidityEndDate)
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
    private static EventStructureDto buildEvent(EventStructureDto oldEvent, List<ContractStructureDto> contracts){
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
