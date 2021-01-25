package org.cna.keyple.demo.sale.data.model;

import org.cna.keyple.demo.sale.data.model.type.DateCompact;
import org.cna.keyple.demo.sale.data.model.type.PriorityCode;
import org.cna.keyple.demo.sale.data.model.type.TimeCompact;
import org.cna.keyple.demo.sale.data.model.type.VersionNumber;

import java.util.Arrays;
import java.util.Objects;

/**
 * Holds Event data
 */
public class EventStructureDto {
    private VersionNumber eventVersionNumber;
    private DateCompact eventDateStamp;
    private TimeCompact eventTimeStamp;
    private int eventLocation;
    private byte eventContractUsed;
    private PriorityCode contractPriority1;
    private PriorityCode contractPriority2;
    private PriorityCode contractPriority3;
    private PriorityCode contractPriority4;
    private byte[] eventPadding;

    private EventStructureDto(Builder builder) {
        setEventVersionNumber(builder.eventVersionNumber);
        setEventDateStamp(builder.eventDateStamp);
        setEventTimeStamp(builder.eventTimeStamp);
        setEventLocation(builder.eventLocation);
        setEventContractUsed(builder.eventContractUsed);
        setContractPriority1(builder.contractPriority1);
        setContractPriority2(builder.contractPriority2);
        setContractPriority3(builder.contractPriority3);
        setContractPriority4(builder.contractPriority4);
        setEventPadding(builder.eventPadding);
    }

    /**
     * Return a Builder for EventStructureDto
     *
     * @return A new instance of Buider
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Return a Builder for EventStructureDto populated with data
     *
     * @param copy the copy
     * @return A new instance of Buider
     */
    public static Builder newBuilder(EventStructureDto copy) {
        Builder builder = new Builder();
        builder.eventVersionNumber = copy.getEventVersionNumber();
        builder.eventDateStamp = copy.getEventDateStamp();
        builder.eventTimeStamp = copy.getEventTimeStamp();
        builder.eventLocation = copy.getEventLocation();
        builder.eventContractUsed = copy.getEventContractUsed();
        builder.contractPriority1 = copy.getContractPriority1();
        builder.contractPriority2 = copy.getContractPriority2();
        builder.contractPriority3 = copy.getContractPriority3();
        builder.contractPriority4 = copy.getContractPriority4();
        builder.eventPadding = copy.getEventPadding();
        return builder;
    }

    /**
     * Gets event version number.
     *
     * @return An instance of VersionNumber. Cannot be null.
     */
    public VersionNumber getEventVersionNumber() {
        return eventVersionNumber;
    }

    /**
     * Sets event version number.
     *
     * @param eventVersionNumber the event version number. Should not be null.
     */
    public void setEventVersionNumber(VersionNumber eventVersionNumber) {
        this.eventVersionNumber = eventVersionNumber;
    }

    /**
     * Gets event date stamp.
     *
     * @return An instance of DateCompact. Cannot be null.
     */
    public DateCompact getEventDateStamp() {
        return eventDateStamp;
    }

    /**
     * Sets event date stamp.
     *
     * @param eventDateStamp the event date stamp. Should not be null.
     */
    public void setEventDateStamp(DateCompact eventDateStamp) {
        this.eventDateStamp = eventDateStamp;
    }

    /**
     * Gets event time stamp.
     *
     * @return An Instance of TimeCompact. Cannot be null.
     */
    public TimeCompact getEventTimeStamp() {
        return eventTimeStamp;
    }

    /**
     * Sets event time stamp.
     *
     * @param eventTimeStamp the event time stamp. Should not be null.
     */
    public void setEventTimeStamp(TimeCompact eventTimeStamp) {
        this.eventTimeStamp = eventTimeStamp;
    }

    /**
     * Gets event location.
     *
     * @return the event location
     */
    public int getEventLocation() {
        return eventLocation;
    }

    /**
     * Sets event location.
     *
     * @param eventLocation the event location. Should not be null.
     */
    public void setEventLocation(int eventLocation) {
        this.eventLocation = eventLocation;
    }

    /**
     * Gets event contract used.
     *
     * @return the event contract used
     */
    public byte getEventContractUsed() {
        return eventContractUsed;
    }

    /**
     * Sets event contract used.
     *
     * @param eventContractUsed the event contract used. Should not be null.
     */
    public void setEventContractUsed(byte eventContractUsed) {
        this.eventContractUsed = eventContractUsed;
    }

    /**
     * Gets contract priority 1.
     *
     * @return An instance of PriorityCode. Cannot be null.
     */
    public PriorityCode getContractPriority1() {
        return contractPriority1;
    }

    /**
     * Sets contract priority 1.
     *
     * @param contractPriority1 the contract priority 1. Should not be null.
     */
    public void setContractPriority1(PriorityCode contractPriority1) {
        this.contractPriority1 = contractPriority1;
    }

    /**
     * Gets contract priority 2.
     *
     * @return An instance of PriorityCode. Cannot be null.
     */
    public PriorityCode getContractPriority2() {
        return contractPriority2;
    }

    /**
     * Sets contract priority 2.
     *
     * @param contractPriority2 the contract priority 2. Should not be null.
     */
    public void setContractPriority2(PriorityCode contractPriority2) {
        this.contractPriority2 = contractPriority2;
    }

    /**
     * Gets contract priority 3.
     *
     * @return An instance of PriorityCode. Cannot be null.
     */
    public PriorityCode getContractPriority3() {
        return contractPriority3;
    }

    /**
     * Sets contract priority 3.
     *
     * @param contractPriority3 the contract priority 3. Should not be null.
     */
    public void setContractPriority3(PriorityCode contractPriority3) {
        this.contractPriority3 = contractPriority3;
    }

    /**
     * Gets contract priority 4.
     *
     * @return An instance of PriorityCode. Cannot be null.
     */
    public PriorityCode getContractPriority4() {
        return contractPriority4;
    }

    /**
     * Sets contract priority 4.
     *
     * @param contractPriority4 the contract priority 4. Should not be null.
     */
    public void setContractPriority4(PriorityCode contractPriority4) {
        this.contractPriority4 = contractPriority4;
    }


    /**
     * Gets contract priority by its index.
     *
     * @param index should be between 1 and 4 (included)
     * @return An instance of PriorityCode. Cannot be null.
     * @throws IllegalArgumentException if index is out of range
     */
    public PriorityCode getContractPriorityAt(int index) {

        if(index==1){
            return contractPriority1;
        } else if(index==2){
            return contractPriority2;
        } else if(index==3){
            return contractPriority3;
        } else if(index==4){
            return contractPriority4;
        }

        throw new IllegalArgumentException("index should be between 1 and 4 (included)");
    }

    /**
     * Set contract priority by its index.
     *
     * @param index should be between 1 and 4 (included)
     * @return An instance of PriorityCode. Cannot be null.
     * @throws IllegalArgumentException if index is out of range
     */
    public void setContractPriorityAt(int index, PriorityCode contractPriority) {

        if(index==1){
            this.contractPriority1 = contractPriority;
        } else if(index==2){
            this.contractPriority2 = contractPriority;
        } else if(index==3){
            this.contractPriority3 = contractPriority;
        } else if(index==4){
            this.contractPriority4 = contractPriority;
        }

        throw new IllegalArgumentException("index should be between 1 and 4 (included)");
    }

    /**
     * Get event padding byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] getEventPadding() {
        return eventPadding;
    }

    /**
     * Sets event padding.
     *
     * @param eventPadding the event padding
     */
    public void setEventPadding(byte[] eventPadding) {
        this.eventPadding = eventPadding;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventStructureDto)) return false;
        EventStructureDto that = (EventStructureDto) o;
        return getEventLocation() == that.getEventLocation() &&
                getEventContractUsed() == that.getEventContractUsed() &&
                getEventVersionNumber() == that.getEventVersionNumber() &&
                getEventDateStamp().equals(that.getEventDateStamp()) &&
                getEventTimeStamp().equals(that.getEventTimeStamp()) &&
                getContractPriority1() == that.getContractPriority1() &&
                getContractPriority2() == that.getContractPriority2() &&
                getContractPriority3() == that.getContractPriority3() &&
                getContractPriority4() == that.getContractPriority4() &&
                Arrays.equals(getEventPadding(), that.getEventPadding());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getEventVersionNumber(), getEventDateStamp(), getEventTimeStamp(), getEventLocation(), getEventContractUsed(), getContractPriority1(), getContractPriority2(), getContractPriority3(), getContractPriority4());
        result = 31 * result + Arrays.hashCode(getEventPadding());
        return result;
    }

    /**
     * {@code EventStructureDto} builder static inner class.
     */
    public static final class Builder {
        private VersionNumber eventVersionNumber;
        private DateCompact eventDateStamp;
        private TimeCompact eventTimeStamp;
        private Integer eventLocation;
        private Byte eventContractUsed;
        private PriorityCode contractPriority1;
        private PriorityCode contractPriority2;
        private PriorityCode contractPriority3;
        private PriorityCode contractPriority4;
        private byte[] eventPadding;

        private Builder() {
        }

        /**
         * Sets the {@code eventVersionNumber} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param eventVersionNumber the {@code eventVersionNumber} to set
         * @return a reference to this Builder
         */
        public Builder setEventVersionNumber(VersionNumber eventVersionNumber) {
            this.eventVersionNumber = eventVersionNumber;
            return this;
        }

        /**
         * Sets the {@code eventDateStamp} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param eventDateStamp the {@code eventDateStamp} to set
         * @return a reference to this Builder
         */
        public Builder setEventDateStamp(DateCompact eventDateStamp) {
            this.eventDateStamp = eventDateStamp;
            return this;
        }

        /**
         * Sets the {@code eventTimeStamp} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param eventTimeStamp the {@code eventTimeStamp} to set
         * @return a reference to this Builder
         */
        public Builder setEventTimeStamp(TimeCompact eventTimeStamp) {
            this.eventTimeStamp = eventTimeStamp;
            return this;
        }

        /**
         * Sets the {@code eventLocation} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param eventLocation the {@code eventLocation} to set
         * @return a reference to this Builder
         */
        public Builder setEventLocation(int eventLocation) {
            this.eventLocation = eventLocation;
            return this;
        }

        /**
         * Sets the {@code eventContractUsed} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param eventContractUsed the {@code eventContractUsed} to set
         * @return a reference to this Builder
         */
        public Builder setEventContractUsed(byte eventContractUsed) {
            this.eventContractUsed = eventContractUsed;
            return this;
        }

        /**
         * Sets the {@code contractPriority1} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param contractPriority1 the {@code contractPriority1} to set
         * @return a reference to this Builder
         */
        public Builder setContractPriority1(PriorityCode contractPriority1) {
            this.contractPriority1 = contractPriority1;
            return this;
        }

        /**
         * Sets the {@code contractPriority2} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param contractPriority2 the {@code contractPriority2} to set
         * @return a reference to this Builder
         */
        public Builder setContractPriority2(PriorityCode contractPriority2) {
            this.contractPriority2 = contractPriority2;
            return this;
        }

        /**
         * Sets the {@code contractPriority3} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param contractPriority3 the {@code contractPriority3} to set
         * @return a reference to this Builder
         */
        public Builder setContractPriority3(PriorityCode contractPriority3) {
            this.contractPriority3 = contractPriority3;
            return this;
        }

        /**
         * Sets the {@code contractPriority4} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param contractPriority4 the {@code contractPriority4} to set
         * @return a reference to this Builder
         */
        public Builder setContractPriority4(PriorityCode contractPriority4) {
            this.contractPriority4 = contractPriority4;
            return this;
        }

        /**
         * Sets the {@code eventPadding} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param eventPadding the {@code eventPadding} to set
         * @return a reference to this Builder
         */
        public Builder setEventPadding(byte[] eventPadding) {
            this.eventPadding = eventPadding;
            return this;
        }

        /**
         * Returns a {@code EventStructureDto} built from the parameters previously set.
         *
         * @return a {@code EventStructureDto} built with parameters of this {@code EventStructureDto.Builder}
         * @throws IllegalStateException the illegal state exception
         */
        public EventStructureDto build() throws IllegalStateException{
            if(this.eventVersionNumber == null
                || this.eventDateStamp == null
                || this.eventTimeStamp == null
                || this.eventLocation == null
                || this.eventContractUsed == null
                || this.contractPriority1 == null
                || this.contractPriority2 == null
                || this.contractPriority3 == null
                || this.contractPriority4 == null)
                throw new IllegalStateException("Missing Mandatory parameters");
            return new EventStructureDto(this);
        }
    }
}
