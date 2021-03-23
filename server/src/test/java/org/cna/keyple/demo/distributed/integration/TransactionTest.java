package org.cna.keyple.demo.distributed.integration;

import io.quarkus.test.junit.QuarkusTest;
import org.cna.keyple.demo.distributed.integration.client.EndpointClient;
import org.cna.keyple.demo.distributed.integration.client.SamClient;
import org.cna.keyple.demo.distributed.server.util.CalypsoUtils;
import org.cna.keyple.demo.distributed.server.util.PcscReaderUtils;
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

/**
 * Test real transactions. Requires a Sam and Po pcsc reader along with a SAM and PO smartcard.
 */
@QuarkusTest
public class TransactionTest {

    private static final String LOCAL_SERVICE_NAME = "TransactionTest";
    private static final String PO_READER_FILTER = ".*(ASK|ACS).*";
    private static final Integer TICKETS_TO_LOAD = 10;

    @Inject  @RestClient
    SamClient samClient;

    static EndpointClient endpointClient;

    static {
        try {
            endpointClient = RestClientBuilder.newBuilder()
                    .baseUrl(new URL("http://0.0.0.0:8080/"))
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
    public void is_sam_ready() {
        assertEquals("{\"isSamReady\":true}", samClient.ping());
    }

    @Test
    public void execute_successful_load_tickets() {
        reset_load_tickets(poReader);
    }

    @Test
    public void execute_successful_load_tickets_N_times() {
        final int N = 3;
        for (int i=0;i<N;i++){
            execute_successful_load_tickets();
        }
    }

    /**
     * Reset card, read valid contract and write a MULTI TRIP CONTRACT with X titles
     * @param poReader
     */
    static void reset_load_tickets(Reader poReader){
        /* Select PO */
        CalypsoPo calypsoPo = CalypsoUtils.selectPo(poReader);

        LocalServiceClient localService = LocalServiceClientUtils.getLocalService(LOCAL_SERVICE_NAME);

        /* Execute Remote Service : Reset card */
        CardIssuanceOutput cardIssuanceOutput = localService.executeRemoteService(
                RemoteServiceParameters
                        .builder("CARD_ISSUANCE", poReader)
                        .withInitialCardContent(calypsoPo)
                        .build(),
                CardIssuanceOutput.class);

        assertEquals(0, cardIssuanceOutput.getStatusCode());

        AnalyzeContractsInput compatibleContractInput =
                new AnalyzeContractsInput().setPluginType("Android NFC");

        /* Execute Remote Service : Get Valid Contracts */
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

        load_tickets(poReader);

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

    /**
     * write a MULTI TRIP CONTRACT with X titles
     * @param poReader
     */
    static void load_tickets(Reader poReader){
        /* Select PO */
        CalypsoPo calypsoPo = CalypsoUtils.selectPo(poReader);

        LocalServiceClient localService = LocalServiceClientUtils.getLocalService(LOCAL_SERVICE_NAME);

        AnalyzeContractsInput compatibleContractInput =
                new AnalyzeContractsInput().setPluginType("Android NFC");

        /*
         * User select the title....
         */

        WriteContractInput writeContractInput =
                new WriteContractInput()
                        .setContractTariff(PriorityCode.MULTI_TRIP_TICKET)
                        .setTicketToLoad(TICKETS_TO_LOAD)
                        .setPluginType("Android NFC");


        /* Execute Remote Service : Write Contract */
        WriteContractOutput writeTitleOutput = localService.executeRemoteService(
                RemoteServiceParameters
                        .builder("WRITE_CONTRACT", poReader)
                        .withInitialCardContent(calypsoPo)
                        .withUserInputData(writeContractInput)
                        .build(),
                WriteContractOutput.class);

        assertNotNull(writeTitleOutput);
        assertEquals(0, writeTitleOutput.getStatusCode());

    }

    /**
     * write a SEASON PASS
     * @param poReader
     */
    static void load_season_pass(Reader poReader){
        /* Select PO */
        CalypsoPo calypsoPo = CalypsoUtils.selectPo(poReader);

        LocalServiceClient localService = LocalServiceClientUtils.getLocalService(LOCAL_SERVICE_NAME);

        AnalyzeContractsInput compatibleContractInput =
                new AnalyzeContractsInput().setPluginType("Android NFC");

        /*
         * User select the title....
         */

        WriteContractInput writeContractInput =
                new WriteContractInput()
                        .setContractTariff(PriorityCode.SEASON_PASS)
                        .setPluginType("Android NFC");


        /* Execute Remote Service : Write Contract */
        WriteContractOutput writeTitleOutput = localService.executeRemoteService(
                RemoteServiceParameters
                        .builder("WRITE_CONTRACT", poReader)
                        .withInitialCardContent(calypsoPo)
                        .withUserInputData(writeContractInput)
                        .build(),
                WriteContractOutput.class);

        assertNotNull(writeTitleOutput);
        assertEquals(0, writeTitleOutput.getStatusCode());

    }

}
