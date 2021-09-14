package org.cna.keyple.demo.distributed.controller;

import org.calypsonet.terminal.calypso.card.CalypsoCard;
import org.cna.keyple.demo.distributed.server.controller.CalypsoPoRepresentation;
import org.cna.keyple.demo.distributed.server.plugin.SamCardConfiguration;
import org.cna.keyple.demo.distributed.server.controller.CalypsoPoController;
import org.cna.keyple.demo.distributed.server.util.CalypsoConstants;
import org.cna.keyple.demo.distributed.server.util.CalypsoUtils;
import org.cna.keyple.demo.distributed.server.util.PcscReaderUtils;
import org.cna.keyple.demo.sale.data.model.ContractStructureDto;
import org.cna.keyple.demo.sale.data.model.EventStructureDto;
import org.cna.keyple.demo.sale.data.model.type.DateCompact;
import org.cna.keyple.demo.sale.data.model.type.PriorityCode;
import org.cna.keyple.demo.sale.data.model.type.VersionNumber;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.resource.CardResource;
import org.eclipse.keyple.core.service.resource.CardResourceServiceProvider;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Test the read and write operations atomically
 */
public class CalypsoPoControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(CalypsoPoControllerTest.class);

    private Reader poReader;
    private CalypsoPoController calypsoPoController;
    private CalypsoCard calypsoPo;
    private CardResource samResource;
    private static final String poReaderFilter = ".*(ASK|ACS).*";
    public static String samReaderFilter = ".*(Cherry TC|SCM Microsystems|Identive|HID|Generic).*";

    @BeforeAll
    public static void staticSetUp(){
        new SamCardConfiguration(samReaderFilter);
    }

    @BeforeEach
    public void setUp(){
        /* Get PO Reader */
        poReader = PcscReaderUtils.initPoReader(poReaderFilter);

        /* select PO*/
        calypsoPo = CalypsoUtils.selectCard(poReader);

        samResource = CardResourceServiceProvider.getService().getCardResource(CalypsoConstants.SAM_PROFILE_NAME);

        calypsoPoController =  CalypsoPoController.newBuilder()
                .withCalypsoCard(calypsoPo)
                .withCardReader(poReader)
                .withSamResource(samResource)
                .build();
    }

    @AfterEach
    public void tearDown(){
        CardResourceServiceProvider.getService().releaseCardResource(samResource);
    }

    @Test
    public void init_card(){
        //init card
        calypsoPoController.initCard();

        //read card
        CalypsoPoRepresentation card = calypsoPoController.readCard();
        Assertions.assertEquals(4, card.getContracts().size());
        Assertions.assertEquals(0, card.listValidContracts().size());
        Assertions.assertEquals(PriorityCode.FORBIDDEN, card.getContractByCalypsoIndex(1).getContractTariff());
        Assertions.assertEquals(PriorityCode.FORBIDDEN, card.getContractByCalypsoIndex(2).getContractTariff());
        Assertions.assertEquals(PriorityCode.FORBIDDEN, card.getContractByCalypsoIndex(3).getContractTariff());
        Assertions.assertEquals(PriorityCode.FORBIDDEN, card.getContractByCalypsoIndex(4).getContractTariff());
        Assertions.assertEquals(PriorityCode.FORBIDDEN, card.getEvent().getContractPriorityAt(1));
        Assertions.assertEquals(PriorityCode.FORBIDDEN, card.getEvent().getContractPriorityAt(2));
        Assertions.assertEquals(PriorityCode.FORBIDDEN, card.getEvent().getContractPriorityAt(3));
        Assertions.assertEquals(PriorityCode.FORBIDDEN, card.getEvent().getContractPriorityAt(4));

    }

    @Test
    public void load_season_pass_on_empty_card(){
        //prepare card
        init_card();

        //test
        CalypsoPoRepresentation card = calypsoPoController.readCard();
        card.insertNewContract(PriorityCode.SEASON_PASS, null);
        calypsoPoController.writeCard(card);

        //check
        CalypsoPoRepresentation updatedCard = calypsoPoController.readCard();
        ContractStructureDto updatedContract = updatedCard.getContractByCalypsoIndex(1);
        logger.trace("updatedCard : {}", updatedCard);
        Assertions.assertEquals(1, updatedCard.listValidContracts().size());
        Assertions.assertEquals(PriorityCode.SEASON_PASS, updatedContract.getContractTariff());
        Assertions.assertEquals(0, updatedContract.getCounter().getCounterValue());
        Assertions.assertEquals(VersionNumber.CURRENT_VERSION, updatedContract.getContractVersionNumber());
        DateCompact today = new DateCompact(Instant.now());
        Assertions.assertEquals(today, updatedContract.getContactSaleDate());
        Assertions.assertEquals(today.getDaysSinceReference()+30, updatedContract.getContractValidityEndDate().getDaysSinceReference());
        Assertions.assertEquals(PriorityCode.SEASON_PASS, updatedCard.getEvent().getContractPriorityAt(1));

    }

    @Test
    public void renew_season_pass(){
        //prepare card
        load_season_pass_on_empty_card();

        //test
        CalypsoPoRepresentation card = calypsoPoController.readCard();
        card.insertNewContract(PriorityCode.SEASON_PASS, null);
        calypsoPoController.writeCard(card);

        //check
        CalypsoPoRepresentation updatedCard = calypsoPoController.readCard();
        ContractStructureDto updatedContract = updatedCard.getContractByCalypsoIndex(1);
        DateCompact today = new DateCompact(Instant.now());

        Assertions.assertEquals(1, updatedCard.listValidContracts().size());
        Assertions.assertEquals(PriorityCode.SEASON_PASS, updatedContract.getContractTariff());
        Assertions.assertEquals(0, updatedContract.getCounter().getCounterValue());
        Assertions.assertEquals(VersionNumber.CURRENT_VERSION, updatedContract.getContractVersionNumber());
        Assertions.assertEquals(today, updatedContract.getContactSaleDate());
        Assertions.assertEquals(today.getDaysSinceReference()+30, updatedContract.getContractValidityEndDate().getDaysSinceReference());
        Assertions.assertEquals(PriorityCode.FORBIDDEN, updatedCard.getContractByCalypsoIndex(2).getContractTariff());
        Assertions.assertEquals(PriorityCode.FORBIDDEN, updatedCard.getContractByCalypsoIndex(3).getContractTariff());
        Assertions.assertEquals(PriorityCode.FORBIDDEN, updatedCard.getContractByCalypsoIndex(4).getContractTariff());
        Assertions.assertEquals(PriorityCode.SEASON_PASS, updatedCard.getEvent().getContractPriorityAt(1));
        Assertions.assertEquals(PriorityCode.FORBIDDEN, updatedCard.getEvent().getContractPriorityAt(2));
        Assertions.assertEquals(PriorityCode.FORBIDDEN, updatedCard.getEvent().getContractPriorityAt(3));
        Assertions.assertEquals(PriorityCode.FORBIDDEN, updatedCard.getEvent().getContractPriorityAt(4));
    }

    @Test
    public void load_ticket_on_empty_card(){
        //prepare card
        init_card();

        //test
        CalypsoPoRepresentation card = calypsoPoController.readCard();
        card.insertNewContract(PriorityCode.MULTI_TRIP_TICKET, 1);
        calypsoPoController.writeCard(card);

        //check
        CalypsoPoRepresentation updatedCard = calypsoPoController.readCard();
        ContractStructureDto updatedContract = updatedCard.getContractByCalypsoIndex(1);

        //logger.trace("updatedCard : {}", updatedCard);
        Assertions.assertEquals(1, updatedCard.listValidContracts().size());
        Assertions.assertEquals(PriorityCode.MULTI_TRIP_TICKET, updatedContract.getContractTariff());
        Assertions.assertEquals(1, updatedContract.getCounter().getCounterValue());
        Assertions.assertEquals(PriorityCode.MULTI_TRIP_TICKET, updatedCard.getEvent().getContractPriorityAt(1));
    }

    @Test
    public void renew_load_ticket(){
        //prepare card
        load_ticket_on_empty_card();

        //test
        CalypsoPoRepresentation card = calypsoPoController.readCard();
        card.insertNewContract(PriorityCode.MULTI_TRIP_TICKET, 1);
        calypsoPoController.writeCard(card);

        //check
        CalypsoPoRepresentation updatedCard = calypsoPoController.readCard();
        ContractStructureDto updatedContract = updatedCard.getContractByCalypsoIndex(1);
        EventStructureDto event = updatedCard.getEvent();

        Assertions.assertEquals(1, updatedCard.listValidContracts().size());
        Assertions.assertEquals(PriorityCode.MULTI_TRIP_TICKET, updatedContract.getContractTariff());
        Assertions.assertEquals(2, updatedContract.getCounter().getCounterValue());
        Assertions.assertEquals(PriorityCode.FORBIDDEN, updatedCard.getContractByCalypsoIndex(2).getContractTariff());
        Assertions.assertEquals(PriorityCode.FORBIDDEN, updatedCard.getContractByCalypsoIndex(3).getContractTariff());
        Assertions.assertEquals(PriorityCode.FORBIDDEN, updatedCard.getContractByCalypsoIndex(4).getContractTariff());
        Assertions.assertEquals(PriorityCode.MULTI_TRIP_TICKET, event.getContractPriorityAt(1));
        Assertions.assertEquals(PriorityCode.FORBIDDEN, event.getContractPriorityAt(2));
        Assertions.assertEquals(PriorityCode.FORBIDDEN, event.getContractPriorityAt(3));
        Assertions.assertEquals(PriorityCode.FORBIDDEN, event.getContractPriorityAt(4));
    }

    @Test
    public void load_season_pass_on_card_with_tickets(){
        load_ticket_on_empty_card();

        //test
        CalypsoPoRepresentation card = calypsoPoController.readCard();
        card.insertNewContract(PriorityCode.SEASON_PASS, null);
        calypsoPoController.writeCard(card);

        //check
        CalypsoPoRepresentation updatedCard = calypsoPoController.readCard();
        ContractStructureDto updatedContract = updatedCard.getContractByCalypsoIndex(2);
        EventStructureDto event = updatedCard.getEvent();

        Assertions.assertEquals(2, updatedCard.listValidContracts().size());
        Assertions.assertEquals(PriorityCode.MULTI_TRIP_TICKET, updatedCard.getContractByCalypsoIndex(1).getContractTariff());
        Assertions.assertEquals(PriorityCode.SEASON_PASS, updatedContract.getContractTariff());
        Assertions.assertEquals(PriorityCode.MULTI_TRIP_TICKET, event.getContractPriorityAt(1));
        Assertions.assertEquals(PriorityCode.SEASON_PASS, event.getContractPriorityAt(2));
    }

    @Test
    public void load_ticket_on_card_with_season_pass(){
        //prepare card
        init_card();
        CalypsoPoRepresentation initCard = calypsoPoController.readCard();
        initCard.insertNewContract(PriorityCode.SEASON_PASS, null);
        calypsoPoController.writeCard(initCard);

        //test
        CalypsoPoRepresentation card = calypsoPoController.readCard();
        card.insertNewContract(PriorityCode.MULTI_TRIP_TICKET, 1);
        calypsoPoController.writeCard(card);

        //check
        CalypsoPoRepresentation updatedCard = calypsoPoController.readCard();
        ContractStructureDto updatedContract = updatedCard.getContractByCalypsoIndex(2);
        EventStructureDto event = updatedCard.getEvent();


        Assertions.assertEquals(2, updatedCard.listValidContracts().size());
        Assertions.assertEquals(PriorityCode.SEASON_PASS, updatedCard.getContractByCalypsoIndex(1).getContractTariff());
        Assertions.assertEquals(0, updatedCard.getContractByCalypsoIndex(1).getCounter().getCounterValue());
        Assertions.assertEquals(PriorityCode.MULTI_TRIP_TICKET, updatedContract.getContractTariff());
        Assertions.assertEquals(1, updatedContract.getCounter().getCounterValue());
        Assertions.assertEquals(PriorityCode.SEASON_PASS, event.getContractPriorityAt(1));
        Assertions.assertEquals(PriorityCode.MULTI_TRIP_TICKET, event.getContractPriorityAt(2));
    }

    @Test
    public void load_ticket_with_expired_season_contract(){
        //todo
    }

}
