package org.cna.keyple.demo.sale.data.model;

import java.nio.ByteBuffer;

/**
 * Parse/Unparse ContractStructureDto to an array of bytes
 */
public class ContractStructureParser {

    public static byte[] getEmpty(){
        return ByteBuffer.allocate(29).array();
    }
}
