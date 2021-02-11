package org.cna.keyple.demo.remote.integration;

import io.quarkus.test.junit.QuarkusTest;
import org.cna.keyple.demo.remote.integration.client.EndpointClient;
import org.cna.keyple.demo.remote.integration.client.HeartbeatClient;
import org.cna.keyple.demo.remote.server.util.CalypsoUtils;
import org.cna.keyple.demo.remote.server.util.PcscReaderUtils;
import org.cna.keyple.demo.sale.data.endpoint.*;
import org.cna.keyple.demo.sale.data.model.ContractStructureDto;
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

    private static final String LOCAL_SERVICE_NAME = "TransactionTest";
    private static final String PO_READER_FILTER = ".*(ASK|ACS).*";
    private static final Integer TICKETS_TO_LOAD = 10;

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

        /* Execute Remote Service : Get Compatible Title */
        CardIssuanceOutput cardIssuanceOutput = localService.executeRemoteService(
                RemoteServiceParameters
                        .builder("CARD_ISSUANCE", poReader)
                        .withInitialCardContent(calypsoPo)
                        .build(),
                CardIssuanceOutput.class);

        assertEquals(0, cardIssuanceOutput.getStatusCode());

        AnalyzeContractsInput compatibleContractInput =
                new AnalyzeContractsInput().setPluginType("Android NFC");

        /* Execute Remote Service : Get Compatible Title */
        AnalyzeContractsOutput contractAnalysisOutput = localService.executeRemoteService(
                RemoteServiceParameters
                        .builder("CONTRACT_ANALYSIS", poReader)
                        .withUserInputData(compatibleContractInput)
                        .withInitialCardContent(calypsoPo)
                        .build(),
                AnalyzeContractsOutput.class);

        assertNotNull(contractAnalysisOutput);
        assertEquals(0, contractAnalysisOutput.getStatusCode());
        assertEquals(0, contractAnalysisOutput.getValidContracts().size());

        /*
         * User select the title....
         */

        WriteContractInput writeContractInput =
                new WriteContractInput()
                        .setContractTariff(PriorityCode.MULTI_TRIP_TICKET)
                        .setTicketToLoad(TICKETS_TO_LOAD);


        /* Execute Remote Service : Write Title */
        WriteContractOutput writeTitleOutput = localService.executeRemoteService(
                RemoteServiceParameters
                        .builder("WRITE_CONTRACT", poReader)
                        .withInitialCardContent(calypsoPo)
                        .withUserInputData(writeContractInput)
                        .build(),
                WriteContractOutput.class);

        assertNotNull(writeTitleOutput);
        assertEquals(0, writeTitleOutput.getStatusCode());

        /* Execute Remote Service : Check that MULTI-TRIP is written in the card */
        AnalyzeContractsOutput passExpected = localService.executeRemoteService(
                RemoteServiceParameters
                        .builder("CONTRACT_ANALYSIS", poReader)
                        .withUserInputData(compatibleContractInput)
                        .withInitialCardContent(calypsoPo)
                        .build(),
                AnalyzeContractsOutput.class);

        assertNotNull(passExpected);
        assertEquals(0, passExpected.getStatusCode());
        assertEquals(1, passExpected.getValidContracts().size());
        ContractStructureDto writtenContract = passExpected.getValidContracts().get(0);
        assertEquals(PriorityCode.MULTI_TRIP_TICKET, writtenContract.getContractTariff());
        assertEquals(TICKETS_TO_LOAD, writtenContract.getCounter().getCounterValue());
    }

    @Test
    public void execute_successful_load_pass() {
        /* Select PO */
        CalypsoPo calypsoPo = CalypsoUtils.selectPo(poReader);

        LocalServiceClient localService = LocalServiceClientUtils.getLocalService(LOCAL_SERVICE_NAME);

        /* Execute Remote Service : Get Compatible Title */
        CardIssuanceOutput cardIssuanceOutput = localService.executeRemoteService(
                RemoteServiceParameters
                        .builder("CARD_ISSUANCE", poReader)
                        .withInitialCardContent(calypsoPo)
                        .build(),
                CardIssuanceOutput.class);

        assertEquals(0, cardIssuanceOutput.getStatusCode());

        AnalyzeContractsInput compatibleContractInput =
                new AnalyzeContractsInput().setPluginType("Android NFC");

        /* Execute Remote Service : Get Compatible Title */
        AnalyzeContractsOutput contractAnalysisOutput = localService.executeRemoteService(
                RemoteServiceParameters
                        .builder("CONTRACT_ANALYSIS", poReader)
                        .withUserInputData(compatibleContractInput)
                        .withInitialCardContent(calypsoPo)
                        .build(),
                AnalyzeContractsOutput.class);

        assertNotNull(contractAnalysisOutput);
        assertEquals(0, contractAnalysisOutput.getStatusCode());

        /*
         * User select the title....
         */

        WriteContractInput writeContractInput =
                new WriteContractInput()
                        .setContractTariff(PriorityCode.SEASON_PASS);


        /* Execute Remote Service : Write Title */
        WriteContractOutput writeTitleOutput = localService.executeRemoteService(
                RemoteServiceParameters
                        .builder("WRITE_CONTRACT", poReader)
                        .withInitialCardContent(calypsoPo)
                        .withUserInputData(writeContractInput)
                        .build(),
                WriteContractOutput.class);

        assertNotNull(writeTitleOutput);
        assertEquals(0, writeTitleOutput.getStatusCode());

        /* Execute Remote Service : Check that SEASON_PASS is written in the card */
        AnalyzeContractsOutput passExpected = localService.executeRemoteService(
                RemoteServiceParameters
                        .builder("CONTRACT_ANALYSIS", poReader)
                        .withUserInputData(compatibleContractInput)
                        .withInitialCardContent(calypsoPo)
                        .build(),
                AnalyzeContractsOutput.class);

        assertNotNull(passExpected);
        assertEquals(0, passExpected.getStatusCode());
        assertEquals(1, passExpected.getValidContracts().size());
        ContractStructureDto writtenContract = passExpected.getValidContracts().get(0);
        assertEquals(PriorityCode.SEASON_PASS, writtenContract.getContractTariff());
        assertEquals(0,writtenContract.getCounter().getCounterValue());
    }

    @Test
    public void execute_successful_load_tickets_N_times() {
        final int N = 3;
        for (int i=0;i<N;i++){
            execute_successful_load_tickets();
        }
    }



}
