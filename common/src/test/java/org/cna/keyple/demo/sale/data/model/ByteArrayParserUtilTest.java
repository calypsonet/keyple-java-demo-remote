package org.cna.keyple.demo.sale.data.model;

import org.cna.keyple.demo.sale.data.util.ByteArrayParserUtil;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ByteArrayParserUtilTest {

    @Test
    public void parse_Integer_threeBits() {
        Integer i = 23;
        assertTrue(i == ByteArrayUtil.threeBytesToInt(ByteArrayParserUtil.toThreeBits(i),0));
    }
}
