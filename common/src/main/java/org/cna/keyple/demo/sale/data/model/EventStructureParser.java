package org.cna.keyple.demo.sale.data.model;

import org.cna.keyple.demo.sale.data.model.type.DateCompact;
import org.cna.keyple.demo.sale.data.model.type.PriorityCode;
import org.cna.keyple.demo.sale.data.model.type.TimeCompact;
import org.cna.keyple.demo.sale.data.model.type.VersionNumber;

import java.nio.ByteBuffer;

/**
 * Parse/Unparse EventStructureDto to an array of bytes
 */
public class EventStructureParser {


    public static byte[] unparse(EventStructureDto dto){
        ByteBuffer out = ByteBuffer.allocate(29);
        out.put(dto.getEventVersionNumber().getValue());
        out.putShort(dto.getEventDateStamp().getDaysSinceReference());
        out.putShort(dto.getEventTimeStamp().getMinutesSinceReference());
        out.putInt(dto.getEventLocation());
        out.put(dto.getContractPriority1().getCode());
        out.put(dto.getContractPriority2().getCode());
        out.put(dto.getContractPriority3().getCode());
        out.put(dto.getContractPriority4().getCode());
        return out.array();
    }

    public static EventStructureDto parse(byte[] contract){
        if(contract==null || contract.length != 29){
            throw new IllegalArgumentException("contract should not be null and its length should be 29");
        }

        ByteBuffer input = ByteBuffer.wrap(contract);
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
