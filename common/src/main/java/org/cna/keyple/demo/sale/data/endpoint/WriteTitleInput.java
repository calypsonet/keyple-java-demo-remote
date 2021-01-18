package org.cna.keyple.demo.sale.data.endpoint;

import org.cna.keyple.demo.sale.data.model.type.PriorityCode;

public class WriteTitleInput {

    //mandatory
    PriorityCode contractTariff;
    Integer ticketToLoad;

    public PriorityCode getContractTariff() {
        return contractTariff;
    }

    public WriteTitleInput setContractTariff(PriorityCode contractTariff) {
        this.contractTariff = contractTariff;
        return this;
    }

    public Integer getTicketToLoad() {
        return ticketToLoad;
    }

    public WriteTitleInput setTicketToLoad(Integer ticketToLoad) {
        this.ticketToLoad = ticketToLoad;
        return this;
    }
}
