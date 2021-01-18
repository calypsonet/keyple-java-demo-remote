package org.cna.keyple.demo.sale.data.model;

import org.cna.keyple.demo.sale.data.model.type.DateCompact;
import org.cna.keyple.demo.sale.data.model.type.VersionNumber;

import java.nio.ByteBuffer;

/**
 * Parse/Unparse EnvironmentHolderStructureDto to an array of bytes
 */
public class EnvironmentHolderStructureParser {

    public static byte[] unparse(EnvironmentHolderStructureDto dto){
        ByteBuffer out = ByteBuffer.allocate(29);
        out.put(dto.getEnvVersionNumber().getValue());
        out.putInt(dto.getEnvApplicationNumber());
        out.putShort(dto.getEnvIssuingDate().getDaysSinceReference());
        out.putShort(dto.getEnvEndDate().getDaysSinceReference());
        if(dto.getHolderCompany()!=null){
            out.put(9, dto.getHolderCompany());
        }
        if(dto.getHolderIdNumber()!=null){
            out.putInt(10, dto.getHolderIdNumber());
        }
        return out.array();
    }

    public static EnvironmentHolderStructureDto parse(byte[] environmentFile){
        if(environmentFile==null || environmentFile.length != 29){
            throw new IllegalArgumentException("environmentFile should not be null and its length should be 29");
        }

        ByteBuffer input = ByteBuffer.wrap(environmentFile);
        return EnvironmentHolderStructureDto.newBuilder()
                .setEnvVersionNumber(VersionNumber.valueOf(input.get()))
                .setEnvApplicationNumber(input.getInt())
                .setEnvIssuingDate(new DateCompact(input.getShort()))
                .setEnvEndDate(new DateCompact(input.getShort()))
                .setHolderCompany(input.get(9)!=0 ? input.get(9):null)
                .setHolderIdNumber(input.getInt(10)!=0 ? input.getInt(10):null)
                .build();
    }

    public static byte[] getEmpty(){
        return ByteBuffer.allocate(29).array();
    }
}
