package org.cna.keyple.demo.distributed.server.calypso;

import org.calypsonet.terminal.calypso.card.FileData;
import org.cna.keyple.demo.distributed.server.util.CalypsoConstants;
import org.cna.keyple.demo.sale.data.model.ContractStructureDto;
import org.cna.keyple.demo.sale.data.model.CounterStructureDto;
import org.cna.keyple.demo.sale.data.model.EnvironmentHolderStructureDto;
import org.cna.keyple.demo.sale.data.model.EventStructureDto;
import org.cna.keyple.demo.sale.data.model.parser.ContractStructureParser;
import org.cna.keyple.demo.sale.data.model.parser.CounterStructureParser;
import org.cna.keyple.demo.sale.data.model.parser.EnvironmentHolderStructureParser;
import org.cna.keyple.demo.sale.data.model.parser.EventStructureParser;
import org.cna.keyple.demo.sale.data.model.type.DateCompact;
import org.cna.keyple.demo.sale.data.model.type.PriorityCode;
import org.cna.keyple.demo.sale.data.model.type.VersionNumber;
import org.calypsonet.terminal.calypso.card.CalypsoCard;

import org.eclipse.keyple.core.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.cna.keyple.demo.distributed.server.util.CalypsoConstants.*;

/**
 * Holds a PO Content and provides method to prepare an update of the card. Use the {@link #parse(CalypsoCard)} method to build this object.
 */
public class CalypsoCardRepresentation {

    private static final Logger logger = LoggerFactory.getLogger(CalypsoCardRepresentation.class);

    private EventStructureDto event;
    private final List<ContractStructureDto> contracts ;
    private EnvironmentHolderStructureDto environment;

    private final List<ContractStructureDto> updatedContracts;//Updated contracts in this object
    private Boolean isEventUpdated;

    private CalypsoCardRepresentation(){
        contracts = new ArrayList<>();
        updatedContracts = new ArrayList<>();
        isEventUpdated = false;
    }

    /**
     * Parse a Calypso PO SmartCard object to a card content object
     * @param calypsoCard not null calypsoCard object
     * @return cardSession not null object
     */
    public static CalypsoCardRepresentation parse(CalypsoCard calypsoCard){
        CalypsoCardRepresentation card = new CalypsoCardRepresentation();

        //parse event
        card.event = EventStructureParser.parse(
                calypsoCard.getFileBySfi(SFI_EVENT_LOG).getData().getContent());

        //parse contracts
        FileData fileData = calypsoCard.getFileBySfi(SFI_CONTRACTS).getData();
        if(fileData!=null){
            for(int i=1;i<5;i++){
                ContractStructureDto contract = ContractStructureParser.parse(fileData.getContent(i));
                card.contracts.add(contract);

                //update counter tied to contract
                FileData counterFile = calypsoCard.getFileBySfi(SFI_Counters_simulated.get(i-1)).getData();
                if(counterFile!=null){
                    contract.setCounter(CounterStructureParser.parse(counterFile.getContent()));
                }
            }
        }

        //parse environment
        card.environment =
                EnvironmentHolderStructureParser.parse(calypsoCard.getFileBySfi(
                        CalypsoConstants.SFI_ENVIRONMENT_AND_HOLDER).getData().getContent());

        return card;
    }


    /**
     * Insert a new contract in the card content
     * @param contractTariff priority code of the contract to write (mandatory)
     * @param ticketToLoad number of ticket to load (optional)
     */
    public void insertNewContract(PriorityCode contractTariff, Integer ticketToLoad){
        if(contractTariff!=PriorityCode.SEASON_PASS && contractTariff!=PriorityCode.MULTI_TRIP_TICKET){
            throw new IllegalArgumentException("Only Season Pass or Multi Trip ticket can be loaded");
        }

        //find contract in card
        int existingContractIndex = isReload(contractTariff);
        int newContractIndex = 0;

        EventStructureDto currentEvent = getEvent();
        ContractStructureDto newContract=null;

        //if is a renew
        if(existingContractIndex > 0){
            newContractIndex = existingContractIndex;
            ContractStructureDto currentContract = getContractByCalypsoIndex(existingContractIndex);

            //build new contract
            if(PriorityCode.MULTI_TRIP_TICKET == contractTariff){
                newContract = buildMultiTripContract(environment.getEnvEndDate(), currentContract.getCounter().getCounterValue()+ticketToLoad);
            }else if(PriorityCode.SEASON_PASS == contractTariff){
                newContract = buildSeasonContract(environment.getEnvEndDate());
            }

        }else{
            //is a new contract
            int newPosition = findAvailablePosition();

            if(newPosition == 0){
                //no available position, reject card
                return;
            }
            newContractIndex = newPosition;

            //build new contract
            if(PriorityCode.MULTI_TRIP_TICKET == contractTariff){
                newContract = buildMultiTripContract(environment.getEnvEndDate(), ticketToLoad);
            }else if(PriorityCode.SEASON_PASS == contractTariff){
                newContract = buildSeasonContract(environment.getEnvEndDate());
            }
        }

        currentEvent.setContractPriorityAt(newContractIndex, newContract.getContractTariff());
        updateContract(newContractIndex,newContract);
        updateEvent(currentEvent);

    }

    /**
     * List valid contracts and counters
     * @return list of valid contracts, null if error
     */
    public List<ContractStructureDto> listValidContracts(){
        //output
        List<ContractStructureDto> validContracts = new ArrayList<>();

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
                    //todo what to do here?
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
                validContracts.add(contract);
            }
            calypsoIndex++;
        }

        //results
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

    public ContractStructureDto getContractByCalypsoIndex(int i){
        return contracts.get(i-1);
    }

    public List<ContractStructureDto> getUpdatedContracts(){
        return updatedContracts;
    }

    public Boolean isEventUpdated() {
        return isEventUpdated;
    }

    @Override
    public String toString() {
        return "CalypsoCardContent{" +
                "event=" + event +
                ", contracts=" + Arrays.deepToString(contracts.toArray()) +
                ", environment=" + environment +
                ", contractUpdated=" + updatedContracts +
                ", eventUpdated=" + isEventUpdated +
                '}';
    }

    /**
     * (private)
     * Update event in this object
     * @param event new event
     */
    private void updateEvent(EventStructureDto event){
        this.event = event;
        this.isEventUpdated = true;
    }

    /**
     * (private)
     * Update contract at a specific index
     * @param contract not nullable contract object
     * @param calypsoIndex calypso index where to update the contract
     */
    private void updateContract(int calypsoIndex,ContractStructureDto contract){
        Assert.getInstance().notNull(contract, "contract should not be null");
        contracts.set(calypsoIndex-1, contract);
        updatedContracts.add(contract);
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
     * @param envEndDate
     * @param counterValue
     * @return a new contract
     */
    private ContractStructureDto buildMultiTripContract(DateCompact envEndDate, Integer counterValue) {
        DateCompact contractSaleDate = new DateCompact(Instant.now());
        DateCompact contractValidityEndDate;

        //calculate ContractValidityEndDate
        contractValidityEndDate = envEndDate;

        ContractStructureDto contract =  ContractStructureDto.newBuilder()
                .setContractVersionNumber(VersionNumber.CURRENT_VERSION)
                .setContractTariff(PriorityCode.MULTI_TRIP_TICKET)
                .setContractSaleDate(contractSaleDate)
                .setContractValidityEndDate(contractValidityEndDate)
                .build();

        contract.setCounter(CounterStructureDto.newBuilder().setCounterValue(counterValue).build());

        return contract;
    }

    /**
     * (private)
     * Fill the contract structure to update:
     * - ContractVersionNumber = 1
     * - ContractTariff = Value provided by upper layer
     * - ContractSaleDate = Current Date converted to DateCompact
     * @param envEndDate
     * @return a new contract
     */
    private ContractStructureDto buildSeasonContract(DateCompact envEndDate) {
        DateCompact contractSaleDate = new DateCompact(Instant.now());
        DateCompact contractValidityEndDate;

        contractValidityEndDate = new DateCompact((short) (contractSaleDate.getDaysSinceReference() + 30));

        return ContractStructureDto.newBuilder()
                .setContractVersionNumber(VersionNumber.CURRENT_VERSION)
                .setContractTariff(PriorityCode.SEASON_PASS)
                .setContractSaleDate(contractSaleDate)
                .setContractValidityEndDate(contractValidityEndDate)
                .build();
    }

}
