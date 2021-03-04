package org.cna.keyple.demo.remote.transaction;

import io.quarkus.test.junit.QuarkusTest;
import org.cna.keyple.demo.remote.server.transaction.Transaction;
import org.cna.keyple.demo.remote.server.transaction.TransactionStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
public class TransactionStoreTest {

    @Inject
    TransactionStore transactionStore;

    final Transaction t = new Transaction();


    @Test
    public void wait_for_new_transaction() {
        storeTransactionAsync();
        Assertions.assertEquals(t, transactionStore.waitForNew());
        Assertions.assertEquals(1, transactionStore.list().size());

    }


    private void storeTransactionAsync(){
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            transactionStore.store(t);
        }).start();
    }
}
