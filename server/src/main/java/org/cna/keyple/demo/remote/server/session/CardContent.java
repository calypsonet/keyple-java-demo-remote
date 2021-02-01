package org.cna.keyple.demo.remote.server.session;

import org.cna.keyple.demo.remote.server.util.CalypsoClassicInfo;
import org.cna.keyple.demo.sale.data.model.*;
import org.cna.keyple.demo.sale.data.model.type.DateCompact;
import org.cna.keyple.demo.sale.data.model.type.PriorityCode;
import org.cna.keyple.demo.sale.data.model.type.VersionNumber;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.FileData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.cna.keyple.demo.remote.server.util.CalypsoClassicInfo.*;

/**
 * Holds a smart card content and provides method to prepare an update of the card
 */
public class CardContent {

    private static final Logger logger = LoggerFactory.getLogger(CardContent.class);

    private EventStructureDto event;
    private final List<ContractStructureDto> contracts ;
    private EnvironmentHolderStructureDto environment;
    private CounterStructureDto counter;

    private final List<ContractStructureDto> contractUpdated;//Contracts updated in this session
    private Boolean isEventUpdated;
    private Boolean isCounterUpdated;

    public CardContent(){
        contracts = new ArrayList<>();
        contractUpdated = new ArrayList<>();
        isEventUpdated = false;
        isCounterUpdated = false;
    }

    /**
     * Parse a Calypso PO SmartCard object to a card content object
     * @param calypsoPo not null calypsoPo object
     * @return cardSession not null object
     */
    public CardContent parse(CalypsoPo calypsoPo){
        //parse event
        this.event = EventStructureParser.parse(
                calypsoPo.getFileBySfi(SFI_EventLog).getData().getContent());

        //parse contracts
        FileData fileData = calypsoPo.getFileBySfi(SFI_Contracts).getData();
        if(fileData!=null){
            for(int i=1;i<5;i++){
                contracts.add(ContractStructureParser.parse(fileData.getContent(i)));
            }
        }

        //parse counter
        FileData counterFile = calypsoPo.getFileBySfi(SFI_Counters).getData();
        if(counterFile!=null){
            counter = CounterStructureParser.parse(counterFile.getContent(RECORD_NUMBER_1));
        }

        //parse environment
        environment =
                EnvironmentHolderStructureParser.parse(calypsoPo.getFileBySfi(
                        CalypsoClassicInfo.SFI_EnvironmentAndHolder).getData().getContent());

        return this;
    }


    /**
     * Insert a new contract in the card content
     * @param contractTariff priority code of the contract to write (mandatory)
     * @param ticketToLoad number of ticket to load (optional)
     * @return status code, 0:ok, 2:no position available
     */
    public int insertNewContract(PriorityCode contractTariff, Integer ticketToLoad){
        //build new contract
        ContractStructureDto newContract = buildContract(contractTariff, getEnvironment().getEnvEndDate());

        //find contract in card
        int existingContractIndex = isReload(newContract.getContractTariff());

        EventStructureDto currentEvent = getEvent();
        // update contracts
        if(existingContractIndex > 0){

            //is a reload operation
            if(!currentEvent.getContractPriorityAt(existingContractIndex)
                    .equals(newContract.getContractTariff())){
                //contract was expired
                currentEvent.setContractPriorityAt(existingContractIndex, newContract.getContractTariff());
                updateContract(existingContractIndex,newContract);
                updateEvent(currentEvent);
            };
        }else{
            //is a new contract
            int newPosition = findAvailablePosition();
            if(newPosition == 0){
                //no available position, reject card
                return 2;
            }
            updateContract(newPosition,newContract);
            currentEvent.setContractPriorityAt(newPosition, newContract.getContractTariff());
            updateEvent(currentEvent);
        }

        // update increment
        if(PriorityCode.MULTI_TRIP_TICKET.equals(newContract.getContractTariff()) ||
                PriorityCode.STORED_VALUE.equals(newContract.getContractTariff())){
            addCounterIncrement(ticketToLoad);
        }

        return 0;
    }

    /**
     * List valid contracts
     * @return list of valid contracts, null if error
     */
    public List<ContractStructureDto> listValidContracts(){
        //output
        List<ContractStructureDto> validContracts = new ArrayList<>();
        CounterStructureDto counter = null;

        //read environment
        EnvironmentHolderStructureDto environment = getEnvironment();

        if(environment.getEnvVersionNumber() != VersionNumber.CURRENT_VERSION){
            //reject card
            logger.warn("Version Number of card is invalid, reject card");
            return null;
        }

        if(environment.getEnvEndDate().getDaysSinceReference()< new DateCompact(Instant.now()).getDaysSinceReference()){
            //reject card
            logger.warn("EnvEndDate of card is invalid, reject card");
            return null;
        }

        EventStructureDto lastEvent = getEvent();

        if(lastEvent.getEventVersionNumber().getValue()!=VersionNumber.CURRENT_VERSION.getValue() &&
                lastEvent.getEventVersionNumber().getValue()!=VersionNumber.FORBIDDEN_UNDEFINED.getValue()){
            //reject card
            logger.warn("EventVersionNumber of card is invalid, reject card");
            return null;
        }

        int calypsoIndex = 1;
        /* Iterate through the contracts in the card session */
        for(ContractStructureDto contract : getContracts()){
            logger.info("Contract at index {} : {} {}", calypsoIndex,  contract.getContractTariff(), contract.getContactSaleDate());

            if(contract.getContractVersionNumber()==VersionNumber.FORBIDDEN_UNDEFINED){
                //  If ContractVersionNumber is 0 ensure that the associated ContractPriority field value is 0 and
                //  move on to the next contract.
                if(contract.getContractTariff().getCode() != PriorityCode.FORBIDDEN.getCode()){
                    logger.warn("Contract tariff is not valid for this contract");
                    return null;
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
                    updateContract(calypsoIndex,contract);
                }
                if(contract.getContractTariff() == PriorityCode.MULTI_TRIP_TICKET){
                    counter = getCounter();
                }
                validContracts.add(contract);
            }
            calypsoIndex++;
        }

        //results
        logger.info("Counter {}",counter!=null?counter.getCounterValue():"null");
        logger.info("Contracts {}", Arrays.deepToString(validContracts.toArray()));

        return validContracts;
    }

    public EventStructureDto getEvent() {
        return event;
    }

    public List<ContractStructureDto> getContracts() {
        return contracts;
    }

    public EnvironmentHolderStructureDto getEnvironment(){
        return environment;
    }

    public CounterStructureDto getCounter(){
        return counter;
    }

    public ContractStructureDto getContractByCalypsoIndex(int i){
        return contracts.get(i-1);
    }

    public List<ContractStructureDto> getContractUpdated(){
        return contractUpdated;
    }

    public Boolean isEventUpdated() {
        return isEventUpdated;
    }

    public Boolean isCounterUpdated() {
        return isCounterUpdated;
    }

    @Override
    public String toString() {
        return "CardContent{" +
                "event=" + event +
                ", contracts=" + Arrays.deepToString(contracts.toArray()) +
                ", environment=" + environment +
                ", counter=" + counter +
                ", contractUpdated=" + contractUpdated +
                ", eventUpdated=" + isEventUpdated +
                ", counterUpdated=" + isCounterUpdated +
                '}';
    }

    /**
     * (package-private)
     * Update event
     * @param event new event
     */
    void updateEvent(EventStructureDto event){
        this.event = event;
        this.isEventUpdated = true;
    }

    /**
     * (package-private)
     * Update contract at a specific index
     * @param contract
     * @param calypsoIndex
     */
    void updateContract(int calypsoIndex,ContractStructureDto contract){
        contracts.set(calypsoIndex-1, contract);
        contractUpdated.add(contract);
    }


    /**
     * (private)
     * Update counter
     * @param counterIncrement
     */
    private void addCounterIncrement(int counterIncrement){
        counter = CounterStructureDto.newBuilder()
                .setCounterValue(counter.getCounterValue() + counterIncrement).build();
        this.isCounterUpdated = true;
    }

    /**
     * (private)
     * Return the calypso index of the contractTariff if present in the card
     * @param contractTariff
     * @return calypso index (1-4), 0 if none
     */
    private int isReload(PriorityCode contractTariff){
        for(int i=0 ; i<4 ; i++){
            if(contractTariff.equals(contracts.get(i).getContractTariff())){
                return i+1;
            }
        }
        return 0;
    }

    /**
     * (private)
     * Find the position for a new contract
     * @return calypso index (1-4), 0 if none
     */
    private int findAvailablePosition(){
        for(int i=0 ; i<4 ; i++){
            if(PriorityCode.FORBIDDEN.equals(contracts.get(i).getContractTariff())){
                return i+1;
            }
        }

        for(int i=0 ; i<4 ; i++){
            if(PriorityCode.EXPIRED.equals(contracts.get(i).getContractTariff())){
                return i+1;
            }
        }

        return 0;
    }

    /**
     * (private)
     * Fill the contract structure to update:
     * - ContractVersionNumber = 1
     * - ContractTariff = Value provided by upper layer
     * - ContractSaleDate = Current Date converted to DateCompact
     * - ContractValidityEndDate = (ContractSaleDate + 30 if ContractTariff == 1) or (EnvEndDate if ContractTariff == 2 or 3)
     * @param contractTariff
     * @param envEndDate
     * @return a new contract
     */
    private ContractStructureDto buildContract(PriorityCode contractTariff, DateCompact envEndDate) {
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

}
