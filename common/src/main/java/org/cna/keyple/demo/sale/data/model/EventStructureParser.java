package org.cna.keyple.demo.sale.data.model;

import java.nio.ByteBuffer;

/**
 * Parse/Unparse EventStructureDto to an array of bytes
 */
public class EventStructureParser {

    public static byte[] getEmpty(){
        return ByteBuffer.allocate(29).array();
    }
}
