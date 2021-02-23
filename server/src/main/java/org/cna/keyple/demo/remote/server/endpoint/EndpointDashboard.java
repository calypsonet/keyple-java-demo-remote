package org.cna.keyple.demo.remote.server.endpoint;

import org.cna.keyple.demo.remote.server.transaction.Transaction;
import org.cna.keyple.demo.remote.server.transaction.TransactionStore;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/dashboard")
public class EndpointDashboard {

    @Inject
    TransactionStore transactionStore;

    /**
     * List all transactions
     * @return not nullable set of transactions
     */
    @GET
    @Path("/transaction")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Transaction> getTransactions(){
        return transactionStore.list();
    }

    /**
     * Long Polling API to get a new transaction
     * http 200 : new transaction is available
     * http 204 : timeout, please renew request
     * @return a new transaction when it occurs
     */
    @GET
    @Path("/transaction/wait")
    @Produces(MediaType.APPLICATION_JSON)
    public Response waitForTransaction() throws InterruptedException {
        Transaction t = transactionStore.waitForNew();
        if(t==null){
            return Response.noContent().build();
        }else{
            return Response.ok(t).build();
        }
    }

}
