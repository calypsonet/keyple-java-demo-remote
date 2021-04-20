package org.cna.keyple.demo.sale.data.endpoint;


public class WriteContractOutput {

    private Integer statusCode;

    /**
     * get status code
     * - 0 successful
     * - 1 server is not ready
     * - 2 card rejected
     * - 3 please present the same card
     * @return not null status code
     */
    public Integer getStatusCode() {
        return statusCode;
    }

    public WriteContractOutput setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
        return this;
    }
}
