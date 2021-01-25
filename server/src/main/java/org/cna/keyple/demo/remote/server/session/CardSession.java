package org.cna.keyple.demo.remote.server.session;

import org.cna.keyple.demo.remote.server.util.CalypsoClassicInfo;
import org.cna.keyple.demo.sale.data.model.*;
import org.cna.keyple.demo.sale.data.model.type.PriorityCode;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;

import java.util.ArrayList;
import java.util.List;

import static org.cna.keyple.demo.remote.server.util.CalypsoClassicInfo.*;

public class CardSession {

    EventStructureDto event;
    List<ContractStructureDto> contracts;
    EnvironmentHolderStructureDto environment;
    CounterStructureDto counter;

    List<ContractStructureDto> contractUpdated;//Contracts updated in this session

    public CardSession(CalypsoPo calypsoPo){
        //parse event
        this.event = EventStructureParser.parse(
                calypsoPo.getFileBySfi(SFI_EventLog).getData().getContent());
        //parse contracts
        for(int i=1;i<5;i++){
            contracts.add(ContractStructureParser.parse(
                    calypsoPo.getFileBySfi(SFI_Contracts).getData().getContent(i)));
        }

        //parse counter
        counter = CounterStructureParser.parse(calypsoPo.getFileBySfi(SFI_Counters).getData().getContent());

        //parse environment
        environment =
                EnvironmentHolderStructureParser.parse(calypsoPo.getFileBySfi(CalypsoClassicInfo.SFI_EnvironmentAndHolder).getData().getContent());

        //init contracts updated
        this.contractUpdated = new ArrayList<>();
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

    /**
     * Register a contract that needs to be updated.
     * @param contractUpdated
     */
    public void registerUpdate(ContractStructureDto contractUpdated){
        if(!contracts.contains(contractUpdated)){
            throw new IllegalArgumentException("contractUpdated should be part of the card contracts list");
        }
        this.contractUpdated.add(contractUpdated);
    }

    /**
     * Return the calypso index of the contractTariff if present in the card
     * @param contractTariff
     * @return calypso index (1-4), 0 if none
     */
    public int isReload(PriorityCode contractTariff){
        for(int i=0 ; i<4 ; i++){
            if(contractTariff.equals(contracts.get(i).getContractTariff())){
                return i+1;
            }
        }
        return 0;
    }

    /**
     * Find the position for a new contract
     * @return calypso index (1-4), 0 if none
     */
    public int findAvailablePosition(){
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
}
