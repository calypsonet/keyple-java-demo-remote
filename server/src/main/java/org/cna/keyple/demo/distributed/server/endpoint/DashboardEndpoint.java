package org.cna.keyple.demo.distributed.server.endpoint;

import org.cna.keyple.demo.distributed.server.log.TransactionLog;
import org.cna.keyple.demo.distributed.server.log.TransactionLogStore;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/dashboard")
public class DashboardEndpoint {

    @Inject
    TransactionLogStore transactionLogStore;

    /**
     * List all transactions
     * @return not nullable set of transactions
     */
    @GET
    @Path("/transaction")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TransactionLog> getTransactions(){
        return transactionLogStore.list();
    }

    /**
     * Long Polling API to get a new transaction
     * http 200 : new transaction is available
     * http 204 : timeout, please renew request
     * @return a {@link TransactionLog} if a new log is push
     */
    @GET
    @Path("/transaction/wait")
    @Produces(MediaType.APPLICATION_JSON)
    public Response waitForTransaction() throws InterruptedException {
        TransactionLog t = transactionLogStore.waitForNew();
        if(t==null){
            return Response.noContent().build();
        }else{
            return Response.ok(t).build();
        }
    }

}
