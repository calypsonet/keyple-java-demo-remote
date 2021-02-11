package org.cna.keyple.demo.sale.data.util;

/**
 * Util class for parsing/unparsing byte array
 */
public class ByteArrayParserUtil {

    /**
     * Convert a integer to a three bytes array
     * @param input integer to be converted
     * @return 3 byte of array
     */
    public static byte[] toThreeBits(int input){
        byte b2 = (byte)((input >> 16) & 0xFF);
        byte b1 = (byte)((input >> 8) & 0xFF);
        byte b0 = (byte)((input) & 0xFF);
        return new byte[]{b2, b1, b0};
    }

}
