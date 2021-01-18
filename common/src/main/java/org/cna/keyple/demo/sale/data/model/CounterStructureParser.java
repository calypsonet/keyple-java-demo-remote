package org.cna.keyple.demo.sale.data.model;

import java.nio.ByteBuffer;

/**
 * Parse/Unparse CounterStructureDto to an array of bytes
 */
public class CounterStructureParser {

    public static byte[] getEmpty(){
        return ByteBuffer.allocate(4).array();
    }
}
