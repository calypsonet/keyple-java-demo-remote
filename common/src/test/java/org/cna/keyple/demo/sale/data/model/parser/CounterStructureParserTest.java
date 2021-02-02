package org.cna.keyple.demo.sale.data.model.parser;

import org.cna.keyple.demo.sale.data.model.CounterStructureDto;
import org.cna.keyple.demo.sale.data.model.parser.CounterStructureParser;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CounterStructureParserTest {


    @Test
    public void parse_counter() {
        CounterStructureDto counter = CounterStructureDto.newBuilder().setCounterValue(17).build();

        CounterStructureDto parsedCounter = CounterStructureParser.parse(CounterStructureParser.unparse(counter));

        assertTrue(parsedCounter.equals(counter));
    }



}
