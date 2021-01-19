package org.cna.keyple.demo.sale.data.model;

import org.cna.keyple.demo.sale.data.model.type.DateCompact;
import org.cna.keyple.demo.sale.data.model.type.PriorityCode;
import org.cna.keyple.demo.sale.data.model.type.VersionNumber;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.*;

public class ContractStructureParserTest {


    private String DATA_CONTRACT_1 =
            "01 01 0F BF 0F DD 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";

    @Test
    public void parse_environment_test(){
        ContractStructureDto contract =
                ContractStructureParser.parse(ByteArrayUtil.fromHex(DATA_CONTRACT_1));
        assertNotNull(contract);
        assertEquals(VersionNumber.CURRENT_VERSION, contract.getContractVersionNumber());
        assertEquals(PriorityCode.SEASON_PASS, contract.getContractTariff());
        assertEquals(new DateCompact(Instant.parse("2021-01-14T00:00:00Z")), contract.getContactSaleDate());
        assertEquals(new DateCompact(Instant.parse("2021-02-13T00:00:00Z")), contract.getContractValidityEndDate());
        assertNull(contract.getContractSaleSam());
        assertNull(contract.getContractSaleCounter());
        assertNull(contract.getContractAuthKvc());
        assertNull(contract.getContractAuthenticator());
    }
}
