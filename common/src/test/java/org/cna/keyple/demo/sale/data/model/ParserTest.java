package org.cna.keyple.demo.sale.data.model;

import org.cna.keyple.demo.sale.data.model.type.DateCompact;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.*;

public class ParserTest {

    private String DATA_ENV_2 =
        "01 00 00 00 01 0F BF 18 4E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";


    @Test
    public void parse_environment_test(){
        EnvironmentHolderStructureDto environment =
                EnvironmentHolderStructureParser.parse(ByteArrayUtil.fromHex(DATA_ENV_2));
        assertNotNull(environment);
        assertEquals(1, environment.getEnvApplicationNumber());
        assertEquals(1, environment.getEnvVersionNumber().getValue());
        assertEquals(1, environment.getEnvVersionNumber().getValue());
        assertEquals(new DateCompact(Instant.parse("2021-01-14T00:00:00Z")), environment.getEnvIssuingDate());
        assertEquals(new DateCompact(Instant.parse("2027-01-14T00:00:00Z")), environment.getEnvEndDate());
        assertNull(environment.getHolderCompany());
        assertNull(environment.getHolderIdNumber());
    }
}
