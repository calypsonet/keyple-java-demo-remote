package org.cna.keyple.demo.sale.data.endpoint;

public class CardIssuanceOutput {

    private Integer statusCode;

    /**
     * get status code
     * - 0 if successful
     * - 1 if error
     * @return not null status code
     */
    public Integer getStatusCode() {
        return statusCode;
    }

    public CardIssuanceOutput setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
        return this;
    }
}
