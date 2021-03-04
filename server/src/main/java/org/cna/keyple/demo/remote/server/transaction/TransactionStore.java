package org.cna.keyple.demo.remote.server.transaction;

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
public class TransactionStore {

    private static final Logger logger = LoggerFactory.getLogger(TransactionStore.class);

    List<Transaction> transactions;
    BlockingQueue<Transaction> transactionQueue;

    TransactionStore(){
        transactions = new ArrayList<>();
        transactionQueue = new ArrayBlockingQueue<>(1);
    }

    /**
     * Return all transactions
     * @return not nullable list of transactions
     */
    public List<Transaction> list(){
        return (List<Transaction>) ((ArrayList<Transaction>) transactions).clone();
    }

    /**
     * Store a new transaction.
     * @param t
     */
    public void store(@NotNull Transaction t){
        //store the new transaction
        transactions.add(t);

        //make it available in the queue
        if(!transactionQueue.isEmpty()){
            transactionQueue.clear();
        }
        if(transactionQueue.offer(t)){
            logger.trace("A new transaction is available in the queue");
        };
    }

    /**
     * Blocking call, wait for a new transaction to be published.
     * Timeout of 10 seconds
     * @return transaction when published, or null if no transaction were published
     */
    public Transaction waitForNew() {
        try {
            return transactionQueue.poll(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return null;
        }
    }
}
