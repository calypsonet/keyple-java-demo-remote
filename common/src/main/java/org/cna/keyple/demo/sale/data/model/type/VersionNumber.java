package org.cna.keyple.demo.sale.data.model.type;

import java.security.InvalidParameterException;

/**
 * Holde version number
 */
public enum VersionNumber {

    FORBIDDEN_UNDEFINED("FORBIDDEN_UNDEFINED", (byte)0),
    CURRENT_VERSION("CURRENT_VERSION", (byte)1),
    FORBIDDEN_RESERVED("FORBIDDEN_RESERVED", (byte)255);

    private final String name;
    private final byte number;

    VersionNumber(String name, byte number){
        this.name = name;
        this.number = number;
    }

    /**
     * Get priority code value
     * @return a non null byte
     */
    public byte getValue() {
        return number;
    }

    /**
     * Get priority code name
     * @return a non null String
     */
    public String getName() {
        return name;
    }

    /**
     * Return the
     * @param number
     * @return A instance of VersionNumber. Cannot be null
     * @throws InvalidParameterException if number does not match an enum
     */
    public static VersionNumber valueOf(byte number) throws InvalidParameterException {
        for(VersionNumber v: values()){
            if(v.number == number){
                return v;
            }
        }
        throw new InvalidParameterException("Invalid VersionNumber: " + (int)number);
    }
}
