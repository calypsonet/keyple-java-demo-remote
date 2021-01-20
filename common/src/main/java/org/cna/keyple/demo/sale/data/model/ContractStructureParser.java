package org.cna.keyple.demo.sale.data.model;

import org.cna.keyple.demo.sale.data.model.type.DateCompact;
import org.cna.keyple.demo.sale.data.model.type.PriorityCode;
import org.cna.keyple.demo.sale.data.model.type.VersionNumber;

import java.nio.ByteBuffer;

/**
 * Parse/Unparse ContractStructureDto to an array of bytes
 */
public class ContractStructureParser {

    /**
     * Unparse dto to an array of byte
     * @param dto (mandatory)
     * @return array of byte
     */
    public static byte[] unparse(ContractStructureDto dto){
        ByteBuffer out = ByteBuffer.allocate(29);
        out.put(dto.getContractVersionNumber().getValue());
        out.put(dto.getContractTariff().getCode());
        out.putShort(dto.getContactSaleDate().getDaysSinceReference());
        out.putShort(dto.getContractValidityEndDate().getDaysSinceReference());
        if(dto.getContractSaleSam()!=null){
            out.putInt(6, dto.getContractSaleCounter());
        }
        if(dto.getContractSaleCounter()!=null){
            out.putInt(10, dto.getContractSaleCounter());
        }
        if(dto.getContractAuthKvc()!=null){
            out.put(11, dto.getContractAuthKvc());
        }
        if(dto.getContractAuthenticator()!=null){
            out.putInt(12, dto.getContractAuthenticator());
        }
        return out.array();
    }

    /**
     * Parse dto from an array of byte
     * @param file array of byte (mandatory)
     * @return parsed object
     */
    public static ContractStructureDto parse(byte[] file){
        if(file==null || file.length != 29){
            throw new IllegalArgumentException("file should not be null and its length should be 29");
        }

        ByteBuffer input = ByteBuffer.wrap(file);
        return ContractStructureDto.newBuilder()
                .setContractVersionNumber(VersionNumber.valueOf(input.get()))
                .setContractTariff(PriorityCode.valueOf(input.get()))
                .setContactSaleDate(new DateCompact(input.getShort()))
                .setContractValidityEndDate(new DateCompact(input.getShort()))
                .setContractSaleSam(input.get(6)!=0 ? input.getInt(6):null)
                .setContractSaleCounter(input.getInt(10)!=0 ? input.getInt(10):null)
                .setContractAuthKvc(input.get(11)!=0 ? input.get(11):null)
                .setContractAuthenticator(input.getInt(12)!=0 ? input.getInt(12):null)
                .build();
    }

    public static byte[] getEmpty(){
        return ByteBuffer.allocate(29).array();
    }
}
