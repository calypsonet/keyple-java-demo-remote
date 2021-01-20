package org.cna.keyple.demo.sale.data.model;

import java.nio.ByteBuffer;

/**
 * Parse/Unparse CounterStructureDto to an array of bytes
 */
public class CounterStructureParser {

    /**
     * Unparse dto to an array of byte
     * @param dto (mandatory)
     * @return array of byte
     */
    public static byte[] unparse(CounterStructureDto dto){
        if(dto==null){
            throw new IllegalArgumentException("dto must not be null");
        }
        ByteBuffer out = ByteBuffer.allocate(4);
        out.putInt(dto.getCounterValue());
        return out.array();
    }

    /**
     * Parse dto from an array of byte
     * @param file array of byte (mandatory)
     * @return parsed object
     */
    public static CounterStructureDto parse(byte[] file){
        if(file==null || file.length != 4){
            throw new IllegalArgumentException("file should not be null and its length should be 29");
        }

        ByteBuffer input = ByteBuffer.wrap(file);
        return CounterStructureDto.newBuilder()
                .setCounterValue(input.getInt())
                .build();
    }
    public static byte[] getEmpty(){
        return ByteBuffer.allocate(4).array();
    }
}
