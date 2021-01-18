package org.cna.keyple.demo.remote.integration;

import io.quarkus.test.junit.QuarkusTest;
import org.cna.keyple.demo.remote.integration.client.EndpointClient;
import org.cna.keyple.demo.remote.integration.client.HeartbeatClient;
import org.cna.keyple.demo.remote.server.util.CalypsoClassicInfo;
import org.cna.keyple.demo.sale.data.endpoint.CompatibleContractInput;
import org.cna.keyple.demo.sale.data.endpoint.CompatibleContractOutput;
import org.cna.keyple.demo.sale.data.endpoint.WriteTitleInput;
import org.cna.keyple.demo.sale.data.endpoint.WriteTitleOutput;
import org.cna.keyple.demo.sale.data.model.type.PriorityCode;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.PoSelection;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.core.card.selection.CardSelectionsService;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.distributed.LocalServiceClient;
import org.eclipse.keyple.distributed.RemoteServiceParameters;
import org.eclipse.keyple.distributed.impl.LocalServiceClientFactory;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.plugin.pcsc.PcscSupportedContactlessProtocols;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class TransactionTest {

    @Inject  @RestClient
    HeartbeatClient heartbeatClient;

    @Inject @RestClient
    EndpointClient endpointClient;

    static Reader poReader;

    private static String poReaderFilter = ".*(ASK|ACS).*";

    @Test
    public void basicTest() {
        assertTrue("ok".equals(heartbeatClient.ping()));
    }
/*
    @BeforeAll
    public static void setUp(){}
*/
    @Test
    public void execute_successful_scenario() {
        /* Get PO Reader */
        poReader = initPoReader();

        /* Select PO */
        CalypsoPo calypsoPo = selectPo(poReader);

        /* Initialize Local Service */
        LocalServiceClient localService =
                LocalServiceClientFactory.builder()
                        .withDefaultServiceName()
                        .withSyncNode(endpointClient)
                        .withoutReaderObservation()
                        .getService();

        CompatibleContractInput compatibleContractInput = new CompatibleContractInput().setPluginType("Android NFC");

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

        //example..
        WriteTitleInput writeTitleInput2 =
                new WriteTitleInput().setContractTariff(PriorityCode.MULTI_TRIP_TICKET)
                        .setTicketToLoad(10);

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

    private static Reader initPoReader() {
        Pattern p = Pattern.compile(poReaderFilter);
        for (Reader reader : SmartCardService.getInstance().getPlugin("PcscPlugin").getReaders().values()) {
            if (p.matcher(reader.getName()).matches()) {

                // Get and configure the PO reader
                ((PcscReader) reader).setContactless(true).setIsoProtocol(PcscReader.IsoProtocol.T1);

                // activate protocols
                reader.activateProtocol(
                        PcscSupportedContactlessProtocols.ISO_14443_4.name(),
                        ContactlessCardCommonProtocols.ISO_14443_4.name());
                return reader;
            }
        }
        throw new KeypleReaderNotFoundException("Reader name pattern: " + poReaderFilter);
    }

    /**
     * Operate the PO selection
     *
     * @param poReader the reader where to operate the PO selection
     * @return a CalypsoPo object if the selection succeed
     * @throws IllegalStateException if the selection fails
     */
     private static CalypsoPo selectPo(Reader poReader) {
        // Check if a PO is present in the reader
        if (!poReader.isCardPresent()) {
            throw new IllegalStateException("No PO is present in the reader " + poReader.getName());
        }

        // Prepare a Calypso PO selection
         CardSelectionsService cardSelectionsService = new CardSelectionsService();

         // Setting of an AID based selection of a Calypso REV3 PO
         // Select the first application matching the selection AID whatever the card communication
         // protocol keep the logical channel open after the selection

         // Calypso selection: configures a PoSelection with all the desired attributes to
         // make the selection and read additional information afterwards
         PoSelection poSelection =
                 new PoSelection(
                         PoSelector.builder()
                                 .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
                                 .aidSelector(
                                         CardSelector.AidSelector.builder().aidToSelect(CalypsoClassicInfo.AID).build())
                                 .invalidatedPo(PoSelector.InvalidatedPo.REJECT)
                                 .build());


         // Add the selection case to the current selection
         // (we could have added other cases here)
         cardSelectionsService.prepareSelection(poSelection);

        // Actual PO communication: operate through a single request the Calypso PO selection
        // and the file read
        CalypsoPo calypsoPo =
                (CalypsoPo) cardSelectionsService.processExplicitSelections(poReader).getActiveSmartCard();

        return calypsoPo;
    }

}
