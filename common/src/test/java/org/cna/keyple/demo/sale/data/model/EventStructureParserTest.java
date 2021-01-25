package org.cna.keyple.demo.sale.data.model;

import org.cna.keyple.demo.sale.data.model.type.DateCompact;
import org.cna.keyple.demo.sale.data.model.type.PriorityCode;
import org.cna.keyple.demo.sale.data.model.type.TimeCompact;
import org.cna.keyple.demo.sale.data.model.type.VersionNumber;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.*;

public class EventStructureParserTest {


    private String EVENT_1 =
            "01 0F BF 03 48 00 00 00 01 01 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";

    @Test
    public void parse_event_test(){
        EventStructureDto event =
                EventStructureParser.parse(ByteArrayUtil.fromHex(EVENT_1));
        assertNotNull(event);
        assertEquals(1, event.getEventVersionNumber().getValue());
        assertEquals(new DateCompact(Instant.parse("2021-01-14T00:00:00Z")), event.getEventDateStamp());
        assertEquals(1, event.getEventLocation());
        assertEquals(1, event.getEventContractUsed());
        assertEquals(PriorityCode.SEASON_PASS, event.getContractPriority1());
        assertEquals(PriorityCode.FORBIDDEN, event.getContractPriority2());
        assertEquals(PriorityCode.FORBIDDEN, event.getContractPriority3());
        assertEquals(PriorityCode.FORBIDDEN, event.getContractPriority4());
    }

    @Test
    public void parse_event(){
        EventStructureDto event = EventStructureDto.newBuilder()
                .setEventVersionNumber(VersionNumber.CURRENT_VERSION)
                .setEventDateStamp(new DateCompact((short)100))
                .setEventTimeStamp(new TimeCompact((short)100))
                .setEventLocation(1)
                .setEventContractUsed((byte) 1)
                .setContractPriority1(PriorityCode.EXPIRED)
                .setContractPriority2(PriorityCode.FORBIDDEN)
                .setContractPriority3(PriorityCode.EXPIRED)
                .setContractPriority4(PriorityCode.MULTI_TRIP_TICKET)
                .build();

        EventStructureDto eventParsed =
                EventStructureParser.parse(EventStructureParser.unparse(event));

        assertTrue(event
                .equals(eventParsed));
    }
}
