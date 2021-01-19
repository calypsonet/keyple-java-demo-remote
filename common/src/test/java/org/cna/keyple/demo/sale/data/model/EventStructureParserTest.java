package org.cna.keyple.demo.sale.data.model;

import org.cna.keyple.demo.sale.data.model.type.DateCompact;
import org.cna.keyple.demo.sale.data.model.type.PriorityCode;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EventStructureParserTest {


    private String EVENT_1 =
            "01 0F BF 03 48 00 00 00 01 01 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";

    @Test
    public void parse_environment_test(){
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
}
