package org.cna.keyple.demo.distributed.server.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Store transaction logs in server cache
 */
@ApplicationScoped
public class TransactionLogStore {

    private static final Logger logger = LoggerFactory.getLogger(TransactionLogStore.class);

    List<TransactionLog> transactionLogs;
    BlockingQueue<TransactionLog> transactionLogQueue;

    TransactionLogStore(){
        transactionLogs = new ArrayList<>();
        transactionLogQueue = new ArrayBlockingQueue<>(1);
    }

    /**
     * Return all transactionLogs
     * @return not nullable list of transactionLogs
     */
    public List<TransactionLog> list(){
        return (List<TransactionLog>) ((ArrayList<TransactionLog>) transactionLogs).clone();
    }

    /**
     * Store a new transaction.
     * @param t
     */
    public void store(@NotNull TransactionLog t){
        //store the new transaction
        transactionLogs.add(t);

        //make it available in the queue
        if(!transactionLogQueue.isEmpty()){
            transactionLogQueue.clear();
        }
        if(transactionLogQueue.offer(t)){
            logger.trace("A new transaction is available in the queue");
        };
    }

    /**
     * Blocking call, wait for a new transaction to be published.
     * Timeout of 10 seconds
     * @return transaction when published, or null if no transaction were published
     */
    public TransactionLog waitForNew() {
        try {
            return transactionLogQueue.poll(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return null;
        }
    }
}
