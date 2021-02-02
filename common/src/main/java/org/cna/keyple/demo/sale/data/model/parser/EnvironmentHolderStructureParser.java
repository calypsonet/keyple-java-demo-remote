package org.cna.keyple.demo.sale.data.model.parser;

import org.cna.keyple.demo.sale.data.model.EnvironmentHolderStructureDto;
import org.cna.keyple.demo.sale.data.model.type.DateCompact;
import org.cna.keyple.demo.sale.data.model.type.VersionNumber;

import java.nio.ByteBuffer;

/**
 * Parse/Unparse EnvironmentHolderStructureDto to an array of bytes
 */
public class EnvironmentHolderStructureParser {

    /**
     * Unparse dto to an array of byte
     * @param dto (mandatory)
     * @return array of byte
     */
    public static byte[] unparse(EnvironmentHolderStructureDto dto){
        if(dto==null){
            throw new IllegalArgumentException("dto must not be null");
        }
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

    /**
     * Parse dto from an array of byte
     * @param file array of byte (mandatory)
     * @return parsed object
     */
    public static EnvironmentHolderStructureDto parse(byte[] file){
        if(file==null || file.length != 29){
            throw new IllegalArgumentException("file should not be null and its length should be 29");
        }

        ByteBuffer input = ByteBuffer.wrap(file);
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
