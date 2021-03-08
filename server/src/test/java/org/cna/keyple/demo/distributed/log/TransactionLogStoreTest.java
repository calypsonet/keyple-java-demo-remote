package org.cna.keyple.demo.distributed.log;

import io.quarkus.test.junit.QuarkusTest;
import org.cna.keyple.demo.distributed.server.log.TransactionLog;
import org.cna.keyple.demo.distributed.server.log.TransactionLogStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

/**
 * Test the transaction logs store
 */
@QuarkusTest
public class TransactionLogStoreTest {

    @Inject
    TransactionLogStore transactionLogStore;

    final TransactionLog t = new TransactionLog();


    @Test
    public void wait_for_new_transaction() {
        storeTransactionAsync();
        Assertions.assertEquals(t, transactionLogStore.waitForNew());
        Assertions.assertEquals(1, transactionLogStore.list().size());

    }


    private void storeTransactionAsync(){
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            transactionLogStore.store(t);
        }).start();
    }
}
