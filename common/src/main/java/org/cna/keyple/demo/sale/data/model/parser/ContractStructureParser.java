package org.cna.keyple.demo.sale.data.model.parser;

import org.cna.keyple.demo.sale.data.model.ContractStructureDto;
import org.cna.keyple.demo.sale.data.model.type.DateCompact;
import org.cna.keyple.demo.sale.data.model.type.PriorityCode;
import org.cna.keyple.demo.sale.data.model.type.VersionNumber;
import org.cna.keyple.demo.sale.data.util.ByteArrayParserUtil;
import org.eclipse.keyple.core.util.ByteArrayUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Parse/Unparse ContractStructureDto to an array of bytes
 */
public class ContractStructureParser {

    public static int CONTRACT_RECORD_SIZE = 29;
    /**
     * Unparse dto to an array of byte
     * @param dto (mandatory)
     * @return array of byte
     */
    public static byte[] unparse(ContractStructureDto dto){
        ByteBuffer out = ByteBuffer.allocate(CONTRACT_RECORD_SIZE);
        out.put(dto.getContractVersionNumber().getValue());
        out.put(dto.getContractTariff().getCode());
        out.putShort(dto.getContactSaleDate().getDaysSinceReference());
        out.putShort(dto.getContractValidityEndDate().getDaysSinceReference());
        if(dto.getContractSaleSam()!=null) {
            out.putInt(6, dto.getContractSaleSam());
        }
        if(dto.getContractSaleCounter()!=null){
            out.position(10);
            out.put(ByteArrayParserUtil.toThreeBits(dto.getContractSaleCounter()));
        }
        if(dto.getContractAuthKvc()!=null){
            out.put(13, dto.getContractAuthKvc());
        }
        if(dto.getContractAuthenticator()!=null){
            out.position(14);
            out.put(ByteArrayParserUtil.toThreeBits(dto.getContractAuthenticator()));
        }
        return out.array();
    }

    /**
     * Parse dto from an array of byte
     * @param file array of byte (mandatory)
     * @return parsed object
     */
    public static ContractStructureDto parse(byte[] file){
        if(file==null || file.length != CONTRACT_RECORD_SIZE){
            throw new IllegalArgumentException("file should not be null and its length should be 29");
        }

        ByteBuffer input = ByteBuffer.wrap(file);
        return ContractStructureDto.newBuilder()
                .setContractVersionNumber(VersionNumber.valueOf(input.get()))
                .setContractTariff(PriorityCode.valueOf(input.get()))
                .setContractSaleDate(new DateCompact(input.getShort()))
                .setContractValidityEndDate(new DateCompact(input.getShort()))
                .setContractSaleSam(input.getInt(6))
                .setContractSaleCounter(ByteArrayUtil.threeBytesToInt(file, 10))
                .setContractAuthKvc(input.get(13))
                .setContractAuthenticator(ByteArrayUtil.threeBytesToInt(file, 14))
                .build();
    }

    /**
     * Parse dto from an array of byte
     * @param file array of byte (mandatory)
     * @return parsed object
     */
    public static List<ContractStructureDto> parse(byte[] file, int recordSize){
        if(file==null || file.length != CONTRACT_RECORD_SIZE*recordSize){
            throw new IllegalArgumentException("file should not be null and its length should be 29*recordSize");
        }
        List<ContractStructureDto> contracts = new ArrayList<>();

        for(int i=0; i<recordSize; i++){
            //trunc file
            byte[] subFile = Arrays.copyOfRange(file,i*CONTRACT_RECORD_SIZE,i+1*CONTRACT_RECORD_SIZE);

            ByteBuffer input = ByteBuffer.wrap(subFile);
            ContractStructureDto contract =  ContractStructureDto.newBuilder()
                    .setContractVersionNumber(VersionNumber.valueOf(input.get()))
                    .setContractTariff(PriorityCode.valueOf(input.get()))
                    .setContractSaleDate(new DateCompact(input.getShort()))
                    .setContractValidityEndDate(new DateCompact(input.getShort()))
                    .setContractSaleSam(input.getInt(6))
                    .setContractSaleCounter(ByteArrayUtil.threeBytesToInt(subFile, 10))
                    .setContractAuthKvc(input.get(13))
                    .setContractAuthenticator(ByteArrayUtil.threeBytesToInt(subFile, 14))
                    .build();
            contracts.add(contract);
        }
        return contracts;
    }

    public static byte[] getEmpty(){
        return ByteBuffer.allocate(29).array();
    }
}
