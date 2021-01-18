package org.cna.keyple.demo.sale.data.endpoint;


public class WriteTitleOutput {

    /*
     * mandatory
     * - 0 successful
     * - 1 server is not ready
     * - 2 card rejected
     */
    private Integer statusCode;

    public Integer getStatusCode() {
        return statusCode;
    }

    public WriteTitleOutput setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
        return this;
    }
}
