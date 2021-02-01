package org.cna.keyple.demo.remote.integration;

import io.quarkus.test.junit.QuarkusTest;
import org.cna.keyple.demo.remote.integration.client.EndpointClient;
import org.cna.keyple.demo.remote.integration.client.HeartbeatClient;
import org.cna.keyple.demo.remote.server.util.CalypsoUtils;
import org.cna.keyple.demo.remote.server.util.PcscReaderUtils;
import org.cna.keyple.demo.sale.data.endpoint.CompatibleContractInput;
import org.cna.keyple.demo.sale.data.endpoint.CompatibleContractOutput;
import org.cna.keyple.demo.sale.data.endpoint.WriteTitleInput;
import org.cna.keyple.demo.sale.data.endpoint.WriteTitleOutput;
import org.cna.keyple.demo.sale.data.model.type.PriorityCode;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.distributed.LocalServiceClient;
import org.eclipse.keyple.distributed.RemoteServiceParameters;
import org.eclipse.keyple.distributed.impl.LocalServiceClientFactory;
import org.eclipse.keyple.distributed.impl.LocalServiceClientUtils;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class TransactionTest {

    private static String LOCAL_SERVICE_NAME = "TransactionTest";
    private static String PO_READER_FILTER = ".*(ASK|ACS).*";

    @Inject  @RestClient
    HeartbeatClient heartbeatClient;

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


    @Test
    public void basicTest() {
        assertTrue("ok".equals(heartbeatClient.ping()));
    }


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
    public void execute_successful_load_tickets() {
        /* Select PO */
        CalypsoPo calypsoPo = CalypsoUtils.selectPo(poReader);

        LocalServiceClient localService = LocalServiceClientUtils.getLocalService(LOCAL_SERVICE_NAME);

        CompatibleContractInput compatibleContractInput =
                new CompatibleContractInput().setPluginType("Android NFC");

        /* Execute Remote Service : Get Compatible Title */
        CompatibleContractOutput compatibleContractOutput = localService.executeRemoteService(
                RemoteServiceParameters
                        .builder("GET_COMPATIBLE_CONTRACT", poReader)
                        .withUserInputData(compatibleContractInput)
                        .withInitialCardContent(calypsoPo)
                        .build(),
                CompatibleContractOutput.class);

        assertNotNull(compatibleContractOutput);
        assertEquals(0, compatibleContractOutput.getStatusCode());

        /*
         * User select the title....
         */

        WriteTitleInput writeTitleInput =
                new WriteTitleInput().setContractTariff(PriorityCode.SEASON_PASS);


        /* Execute Remote Service : Write Title */
        WriteTitleOutput writeTitleOutput = localService.executeRemoteService(
                RemoteServiceParameters
                        .builder("WRITE_TITLE", poReader)
                        .withInitialCardContent(calypsoPo)
                        .withUserInputData(writeTitleInput)
                        .build(),
                WriteTitleOutput.class);

        assertNotNull(writeTitleOutput);
        assertEquals(0, compatibleContractOutput.getStatusCode());

    }

    @Test
    public void execute_successful_load_season_pass() {
        /* Select PO */
        CalypsoPo calypsoPo = CalypsoUtils.selectPo(poReader);

        LocalServiceClient localService = LocalServiceClientUtils.getLocalService(LOCAL_SERVICE_NAME);

        CompatibleContractInput compatibleContractInput =
                new CompatibleContractInput().setPluginType("Android NFC");

        /* Execute Remote Service : Get Compatible Title */
        CompatibleContractOutput compatibleContractOutput = localService.executeRemoteService(
                RemoteServiceParameters
                        .builder("GET_COMPATIBLE_CONTRACT", poReader)
                        .withUserInputData(compatibleContractInput)
                        .withInitialCardContent(calypsoPo)
                        .build(),
                CompatibleContractOutput.class);

        assertNotNull(compatibleContractOutput);
        assertEquals(0, compatibleContractOutput.getStatusCode());

        /*
         * User select the title....
         */

        WriteTitleInput writeTitleInput =
                new WriteTitleInput().setContractTariff(PriorityCode.SEASON_PASS);


        /* Execute Remote Service : Write Title */
        WriteTitleOutput writeTitleOutput = localService.executeRemoteService(
                RemoteServiceParameters
                        .builder("WRITE_TITLE", poReader)
                        .withInitialCardContent(calypsoPo)
                        .withUserInputData(writeTitleInput)
                        .build(),
                WriteTitleOutput.class);

        assertNotNull(writeTitleOutput);
        assertEquals(0, compatibleContractOutput.getStatusCode());

    }

    @Test
    public void execute_successful_load_tickets_N_times() {
        final int N = 3;
        for (int i=0;i<N;i++){
            execute_successful_load_tickets();
        }
    }



}
