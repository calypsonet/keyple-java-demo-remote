package org.cna.keyple.demo.remote.integration;

import io.quarkus.test.junit.QuarkusTest;
import org.cna.keyple.demo.remote.integration.client.EndpointClient;
import org.cna.keyple.demo.remote.server.util.PcscReaderUtils;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.eclipse.keyple.distributed.impl.LocalServiceClientFactory;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

@QuarkusTest
public class WaitTransactionTest {

    private static String LOCAL_SERVICE_NAME = "TransactionTest";
    private static String PO_READER_FILTER = ".*(ASK|ACS).*";

    static EndpointClient endpointClient;

    static {
        try {
            endpointClient = RestClientBuilder.newBuilder()
                    .baseUrl(new URL("http://0.0.0.0:8081/"))
                    .build(EndpointClient.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    Reader poReader;

    @BeforeAll
    public static void setUpAll(){
        LocalServiceClientFactory.builder()
                .withServiceName(LOCAL_SERVICE_NAME)
                .withSyncNode(endpointClient)
                .withoutReaderObservation()
                .getService();
    }

    @BeforeEach
    public void setUp(){
        /* Get PO Reader */
        poReader = PcscReaderUtils.initPoReader(PO_READER_FILTER);
    }

    @Test
    public void execute_wait_for_po() throws InterruptedException {

        ((ObservableReader)poReader).addObserver(new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                switch (event.getEventType()){
                    case CARD_INSERTED:

                        TransactionTest.load_tickets(poReader);
                        break;
                }
            }
        });

        ((ObservableReader) poReader).startCardDetection(ObservableReader.PollingMode.REPEATING);

        // Wait indefinitely. CTRL-C to exit.
        synchronized (waitForEnd) {
            waitForEnd.wait();
        }
    }


    private static final Object waitForEnd = new Object();

}
