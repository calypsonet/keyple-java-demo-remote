package org.cna.keyple.demo.sale.data.model.parser;

import org.cna.keyple.demo.sale.data.model.EventStructureDto;
import org.cna.keyple.demo.sale.data.model.type.DateCompact;
import org.cna.keyple.demo.sale.data.model.type.PriorityCode;
import org.cna.keyple.demo.sale.data.model.type.TimeCompact;
import org.cna.keyple.demo.sale.data.model.type.VersionNumber;

import java.nio.ByteBuffer;

/**
 * Parse/Unparse EventStructureDto to an array of bytes
 */
public class EventStructureParser {

    /**
     * Unparse dto to an array of byte
     * @param dto (mandatory)
     * @return array of byte
     */
    public static byte[] unparse(EventStructureDto dto){
        ByteBuffer out = ByteBuffer.allocate(29);
        out.put(dto.getEventVersionNumber().getValue());
        out.putShort(dto.getEventDateStamp().getDaysSinceReference());
        out.putShort(dto.getEventTimeStamp().getMinutesSinceReference());
        out.putInt(dto.getEventLocation());
        out.put(dto.getEventContractUsed());
        out.put(dto.getContractPriority1().getCode());
        out.put(dto.getContractPriority2().getCode());
        out.put(dto.getContractPriority3().getCode());
        out.put(dto.getContractPriority4().getCode());
        return out.array();
    }

    /**
     * Parse dto from an array of byte
     * @param file array of byte (mandatory)
     * @return parsed object
     */
    public static EventStructureDto parse(byte[] file){
        if(file==null || file.length != 29){
            throw new IllegalArgumentException("file should not be null and its length should be 29");
        }

        ByteBuffer input = ByteBuffer.wrap(file);
        return EventStructureDto.newBuilder()
                .setEventVersionNumber(VersionNumber.valueOf(input.get()))
                .setEventDateStamp(new DateCompact(input.getShort()))
                .setEventTimeStamp(new TimeCompact(input.getShort()))
                .setEventLocation(input.getInt())
                .setEventContractUsed(input.get())
                .setContractPriority1(PriorityCode.valueOf(input.get()))
                .setContractPriority2(PriorityCode.valueOf(input.get()))
                .setContractPriority3(PriorityCode.valueOf(input.get()))
                .setContractPriority4(PriorityCode.valueOf(input.get()))
                .build();
    }


    public static byte[] getEmpty(){
        return ByteBuffer.allocate(29).array();
    }
}
