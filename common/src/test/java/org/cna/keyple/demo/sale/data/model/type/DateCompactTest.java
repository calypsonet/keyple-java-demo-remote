package org.cna.keyple.demo.sale.data.model.type;

import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;

public class DateCompactTest {

    @Test
    public void test_construct(){
        Assert.assertEquals(9, new DateCompact(Instant.parse("2010-01-10T00:00:00Z")).getDaysSinceReference());
    }
}
