package org.cna.keyple.demo.sale.data.model;

import java.util.Objects;

/**
 * Holds Data of counter
 */
public class CounterStructureDto {

    private int counterValue;

    private CounterStructureDto(Builder builder) {
        setCounterValue(builder.counterValue);
    }

    /**
     * Return a Builder for CounterStructureDto
     *
     * @return A new instance of Buider
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Return a Builder for CounterStructureDto populated with data
     *
     * @param copy the copy
     * @return A new instance of Buider
     */
    public static Builder newBuilder(CounterStructureDto copy) {
        Builder builder = new Builder();
        builder.counterValue = copy.getCounterValue();
        return builder;
    }

    /**
     * Gets counter value.
     *
     * @return the counter value
     */
    public int getCounterValue() {
        return counterValue;
    }

    /**
     * Sets counter value.
     *
     * @param counterValue the counter value
     */
    public void setCounterValue(int counterValue) {
        this.counterValue = counterValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CounterStructureDto)) return false;
        CounterStructureDto that = (CounterStructureDto) o;
        return getCounterValue() == that.getCounterValue();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCounterValue());
    }

    @Override
    public String toString() {
        return String.valueOf(counterValue);
    }

    /**
     * {@code CounterStructureDto} builder static inner class.
     */
    public static final class Builder {
        private Integer counterValue;

        private Builder() {
        }

        /**
         * Sets the {@code counterValue} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param counterValue the {@code counterValue} to set
         * @return a reference to this Builder
         */
        public Builder setCounterValue(int counterValue) {
            this.counterValue = counterValue;
            return this;
        }

        /**
         * Returns a {@code CounterStructureDto} built from the parameters previously set.
         *
         * @return a {@code CounterStructureDto} built with parameters of this {@code CounterStructureDto.Builder}
         * @throws IllegalStateException the illegal state exception
         */
        public CounterStructureDto build() throws IllegalStateException{
            if(this.counterValue == null) throw new IllegalStateException ("Counter value is mandatory");
            return new CounterStructureDto(this);
        }
    }
}
