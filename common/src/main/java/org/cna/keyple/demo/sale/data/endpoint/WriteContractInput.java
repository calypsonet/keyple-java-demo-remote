package org.cna.keyple.demo.sale.data.endpoint;

import org.cna.keyple.demo.sale.data.model.type.PriorityCode;

/**
 * Input object of the
 */
public class WriteContractInput {

    //mandatory
    PriorityCode contractTariff;
    Integer ticketToLoad;
    String pluginType;

    public PriorityCode getContractTariff() {
        return contractTariff;
    }

    public Integer getTicketToLoad() { return ticketToLoad; }

    public WriteContractInput setContractTariff(PriorityCode contractTariff) {
        this.contractTariff = contractTariff;
        return this;
    }

    public WriteContractInput setTicketToLoad(Integer ticketToLoad) {
        this.ticketToLoad = ticketToLoad;
        return this;
    }

    /**
     * Return the type of plugin used for the po reader
     * @return nullable plugin type
     */
    public String getPluginType() {
        return pluginType;
    }

    public WriteContractInput setPluginType(String pluginType) {
        this.pluginType = pluginType;
        return this;
    }
}
