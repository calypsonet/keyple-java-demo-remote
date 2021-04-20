package org.cna.keyple.demo.sale.data.model.type;

import java.security.InvalidParameterException;

/**
 * Holds priority code enums
 */
public enum PriorityCode {

    FORBIDDEN("FORBIDDEN",(byte)0),
    SEASON_PASS("SEASON_PASS",(byte)1),
    MULTI_TRIP_TICKET("MULTI_TRIP_TICKET",(byte)2),
    STORED_VALUE("STORED_VALUE",(byte)3),
    EXPIRED("EXPIRED",(byte)31);

    private final String name;
    private final byte code;

    PriorityCode(String name, byte code){
        this.name = name;
        this.code = code;
    }

    /**
     * Get priority code value
     * @return a non null byte
     */
    public byte getCode() {
        return code;
    }

    /**
     * Get priority code name
     * @return a non null String
     */
    public String getName(){
        return name;
    }

    /**
     * Return the enum value for the code
     * @param code code
     * @return A instance of PriorityCode. Cannot be null
     * @throws InvalidParameterException if code does not match an enum
     */
    public static PriorityCode valueOf(byte code) throws InvalidParameterException {
        for(PriorityCode c: values()){
            if(c.code == code){
                return c;
            }
        }
        throw new InvalidParameterException("Invalid PriorityCode: " + (int)code);
    }

}
