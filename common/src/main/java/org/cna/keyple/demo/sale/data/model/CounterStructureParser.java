package org.cna.keyple.demo.sale.data.model;

import org.cna.keyple.demo.sale.data.util.ByteArrayParserUtil;
import org.eclipse.keyple.core.util.ByteArrayUtil;

import java.nio.ByteBuffer;

/**
 * Parse/Unparse CounterStructureDto to an array of bytes
 */
public class CounterStructureParser {

    private static Integer COUNTER_VALUE_SIZE = 3;

    /**
     * Unparse dto to an array of byte
     * @param dto (mandatory)
     * @return array of byte
     */
    public static byte[] unparse(CounterStructureDto dto){
        if(dto==null){
            throw new IllegalArgumentException("dto must not be null");
        }
        ByteBuffer out = ByteBuffer.allocate(COUNTER_VALUE_SIZE);
        out.put(ByteArrayParserUtil.toThreeBits(dto.getCounterValue()));
        return out.array();
    }

    /**
     * Parse dto from an array of byte
     * @param file array of byte (mandatory)
     * @return parsed object
     */
    public static CounterStructureDto parse(byte[] file){
        if(file==null || file.length != COUNTER_VALUE_SIZE){
            throw new IllegalArgumentException("file should not be null and its length should be " + COUNTER_VALUE_SIZE);
        }

        return CounterStructureDto.newBuilder()
                .setCounterValue(ByteArrayUtil.threeBytesSignedToInt(file,0))
                .build();
    }
    public static byte[] getEmpty(){
        return ByteBuffer.allocate(COUNTER_VALUE_SIZE).array();
    }
}
