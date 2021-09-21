package org.cna.keyple.demo.distributed.server;

import org.calypsonet.terminal.calypso.card.CalypsoCard;
import org.calypsonet.terminal.reader.selection.spi.SmartCard;
import org.cna.keyple.demo.distributed.server.controller.CalypsoPoController;
import org.cna.keyple.demo.distributed.server.controller.CalypsoPoRepresentation;
import org.cna.keyple.demo.distributed.server.log.TransactionLog;
import org.cna.keyple.demo.distributed.server.log.TransactionLogStore;
import org.cna.keyple.demo.distributed.server.plugin.SamCardConfiguration;
import org.cna.keyple.demo.distributed.server.util.CalypsoConstants;
import org.cna.keyple.demo.sale.data.endpoint.*;
import org.cna.keyple.demo.sale.data.model.ContractStructureDto;
import org.eclipse.keyple.core.service.ObservablePlugin;
import org.eclipse.keyple.core.service.PluginEvent;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.core.service.resource.CardResource;
import org.eclipse.keyple.core.service.resource.CardResourceServiceProvider;
import org.eclipse.keyple.core.service.spi.PluginObserverSpi;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.distributed.RemotePluginServer;
import org.eclipse.keyple.distributed.RemoteReaderServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TicketingLogic implements PluginObserverSpi {

    private static final Logger logger = LoggerFactory.getLogger(TicketingLogic.class);

    private final TransactionLogStore transactionLogStore;

    public TicketingLogic(TransactionLogStore transactionLogStore) {
        this.transactionLogStore = transactionLogStore;
    }

    /** {@inheritDoc} */
    @Override
    public void onPluginEvent(PluginEvent event) {

        // For a RemotePluginServer, the events can only be of type READER_CONNECTED.
        // So there is no need to analyze the event type.
        logger.info(
                "Event received {} {} {}",
                event.getType(),
                event.getPluginName(),
                event.getReaderNames().first());

        // Retrieves the remote plugin using the plugin name contains in the event.
        ObservablePlugin plugin =
                (ObservablePlugin) SmartCardServiceProvider.getService().getPlugin(event.getPluginName());
        RemotePluginServer pluginExtension = plugin.getExtension(RemotePluginServer.class);

        // Retrieves the name of the remote reader using the first reader name contains in the event.
        // Note that for a RemotePluginServer, there can be only one reader per event.
        String readerName = event.getReaderNames().first();

        // Retrieves the remote reader from the plugin using the reader name.
        Reader reader = plugin.getReader(readerName);
        RemoteReaderServer readerExtension = reader.getExtension(RemoteReaderServer.class);

        // Analyses the Service ID contains in the reader to find which business service to execute.
        // The Service ID was specified by the client when executing the remote service.
        Object userOutputData;
        if ("CONTRACT_ANALYSIS".equals(readerExtension.getServiceId())) {

            // Executes the business service using the remote reader.
            userOutputData = analyzeContracts(reader);

        } else if ("WRITE_CONTRACT".equals(readerExtension.getServiceId())) {

            // Executes the business service using the remote reader.
            userOutputData = writeContract(reader);

        } else if ("CARD_ISSUANCE".equals(readerExtension.getServiceId())) {

            // Executes the business service using the remote reader.
            userOutputData = initCard(reader);

        } else{
            throw new IllegalArgumentException("Service ID not recognized");
        }

        // Terminates the business service by providing the reader name and the optional output data.
        pluginExtension.endRemoteService(readerName, userOutputData);
    }

    /**
     * Analyze the contracts from the card inserted into the remote reader
     * @param reader The remote reader on where to execute the business logic.
     * @return a nullable reference to the user output data to transmit to the client.
     */
    private AnalyzeContractsOutput analyzeContracts(Reader reader) {
        RemoteReaderServer readerExtension = reader.getExtension(RemoteReaderServer.class);
        /*
         * Retrieves the compatibleContractInput and initial calypsoPO specified by the client when executing the remote service.
         */
        CalypsoCard calypsoPo = (CalypsoCard) readerExtension.getInitialCardContent();
        AnalyzeContractsInput input = readerExtension.getInputData(AnalyzeContractsInput.class);
        CardResource samResource = CardResourceServiceProvider.getService().getCardResource(CalypsoConstants.SAM_PROFILE_NAME);
        String pluginType = input.getPluginType();

        try{

            CalypsoPoController calypsoPoController = CalypsoPoController.newBuilder()
                    .withCalypsoCard(calypsoPo)
                    .withCardReader(reader)
                    .withSamResource(samResource)
                    .withPluginType(pluginType)
                    .build();


            CalypsoPoRepresentation calypsoPoContent = calypsoPoController.readCard();

            logger.info(calypsoPoContent.toString());

            List<ContractStructureDto> validContracts = calypsoPoContent.listValidContracts();

            //push a transaction log
            transactionLogStore.push(new TransactionLog()
                    .setPlugin(input.getPluginType()==null?"Android NFC":input.getPluginType())
                    .setStatus("SUCCESS")
                    .setType("SECURED READ")
                    .setPoSn(ByteArrayUtil.toHex(calypsoPo.getApplicationSerialNumber())));

            return new AnalyzeContractsOutput()
                    .setValidContracts(validContracts)
                    .setStatusCode(0);

        }catch(RuntimeException e){
            logger.error("An error occurred while analyzing the contracts : {}", e.getMessage());
            //push a transaction log
            transactionLogStore.push(new TransactionLog()
                    .setPlugin(input.getPluginType()==null?"Android NFC":input.getPluginType())
                    .setStatus("FAIL")
                    .setType("SECURED READ")
                    .setPoSn(ByteArrayUtil.toHex(calypsoPo.getApplicationSerialNumber())));

            return new AnalyzeContractsOutput()
                    .setStatusCode(1);
        }finally {
            //deallocate samResource
            CardResourceServiceProvider.getService().releaseCardResource(samResource);

        }
    }

    /**
     * Write a contract into the card inserted into the remote reader
     * @param reader The remote reader on where to execute the business logic.
     * @return a nullable reference to the user output data to transmit to the client.
     */
    private WriteContractOutput writeContract(Reader reader) {
        RemoteReaderServer readerExtension = reader.getExtension(RemoteReaderServer.class);

        /*
         * Retrieves the userInputData and initial calypsoPO specified by the client when executing the remote service.
         */
        WriteContractInput writeContractInput = readerExtension.getInputData(WriteContractInput.class);
        CalypsoCard calypsoPo = (CalypsoCard) readerExtension.getInitialCardContent();
        CardResource samResource = CardResourceServiceProvider.getService().getCardResource(CalypsoConstants.SAM_PROFILE_NAME);
        String pluginType = writeContractInput.getPluginType();

        try{

            CalypsoPoController calypsoPoController = CalypsoPoController.newBuilder()
                    .withCalypsoCard(calypsoPo)
                    .withCardReader(reader)
                    .withSamResource(samResource)
                    .withPluginType(pluginType)
                    .build();

            //re-read card
            CalypsoPoRepresentation calypsoPoContent = calypsoPoController.readCard();

            if(calypsoPoContent ==null){
                //is card has not been read previously, throw error
                return new WriteContractOutput().setStatusCode(3);
            }

            logger.info(calypsoPoContent.toString());

            calypsoPoContent.insertNewContract(
                    writeContractInput.getContractTariff(),
                    writeContractInput.getTicketToLoad());

            int statusCode = calypsoPoController.writeCard(calypsoPoContent);

            //push a transaction log
            transactionLogStore.push(new TransactionLog()
                    //TODO : change default name
                    .setPlugin(writeContractInput.getPluginType()==null?"Android NFC":writeContractInput.getPluginType())
                    .setStatus("SUCCESS")
                    .setType("RELOAD")
                    .setPoSn(ByteArrayUtil.toHex(calypsoPo.getApplicationSerialNumber()))
                    .setContractLoaded(writeContractInput.getContractTariff().toString().replace("_", " ")+
                            ((writeContractInput.getTicketToLoad()!=null && writeContractInput.getTicketToLoad()!=0)? " : " +writeContractInput.getTicketToLoad():""))
            );

            return new WriteContractOutput().setStatusCode(statusCode);

        }catch(RuntimeException e){
            logger.error("An error occurred while writing the contract : {}", e.getMessage());

            //push a transaction log
            transactionLogStore.push(new TransactionLog()
                    //TODO : change default name
                    .setPlugin(writeContractInput.getPluginType()==null?"Android NFC":writeContractInput.getPluginType())
                    .setStatus("FAIL")
                    .setType("RELOAD")
                    .setPoSn(ByteArrayUtil.toHex(calypsoPo.getApplicationSerialNumber()))
                    .setContractLoaded("")
            );
            return new WriteContractOutput().setStatusCode(1);
        }finally {
            //deallocate samResource if needed
            CardResourceServiceProvider.getService().releaseCardResource(samResource);
        }
    }

    /**
     * Init the card inserted into the remote reader
     * @param reader The remote reader on where to execute the business logic.
     * @return a nullable reference to the user output data to transmit to the client.
     */
    private CardIssuanceOutput initCard(Reader reader) {
        RemoteReaderServer readerExtension = reader.getExtension(RemoteReaderServer.class);

        CalypsoCard calypsoCard = (CalypsoCard) readerExtension.getInitialCardContent();

        CardResource samResource = CardResourceServiceProvider.getService().getCardResource(CalypsoConstants.SAM_PROFILE_NAME);

        try{

            //Create a Calypso PO controller
            CalypsoPoController calypsoPoController = CalypsoPoController.newBuilder()
                    .withCalypsoCard(calypsoCard)
                    .withCardReader(reader)
                    .withSamResource(samResource)
                    .withPluginType("Android NFC")
                    .build();

            //init card
            calypsoPoController.initCard();

            //push a transaction log
            transactionLogStore.push(new TransactionLog()
                    .setPlugin("Android NFC")
                    .setStatus("SUCCESS")
                    .setType("ISSUANCE")
                    .setPoSn(ByteArrayUtil.toHex(calypsoCard.getApplicationSerialNumber())));

            return new CardIssuanceOutput().setStatusCode(0);
        }catch (RuntimeException e){

            transactionLogStore.push(new TransactionLog()
                    .setPlugin("Android NFC")
                    .setStatus("FAIL")
                    .setType("ISSUANCE")
                    .setPoSn(ByteArrayUtil.toHex(calypsoCard.getApplicationSerialNumber())));
            return new CardIssuanceOutput().setStatusCode(1);
        }finally {
            //deallocate samResource if needed
            CardResourceServiceProvider.getService().releaseCardResource(samResource);
        }
    }
}