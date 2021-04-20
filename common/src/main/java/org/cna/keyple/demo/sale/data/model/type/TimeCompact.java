package org.cna.keyple.demo.sale.data.model.type;

import java.util.Objects;

/**
 * Hold number of minutes in the day
 */
public class TimeCompact {

    private short minutesSinceReference;

    /**
     * Minutes since of the day [(hour*60) + minutes]
     * @param minutesSinceReference
     */
    public TimeCompact(short minutesSinceReference) {
        this.minutesSinceReference = minutesSinceReference;
    }

    /**
     * Get minutes since midnight
     * @return
     */
    public short getMinutesSinceReference() {
        return minutesSinceReference;
    }

    /**
     * Set minutes of the day [(hour*60) + minutes]
     * @param minutesSinceReference
     */
    public void setMinutesSinceReference(short minutesSinceReference) {
        this.minutesSinceReference = minutesSinceReference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeCompact)) return false;
        TimeCompact that = (TimeCompact) o;
        return getMinutesSinceReference() == that.getMinutesSinceReference();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMinutesSinceReference());
    }

    @Override
    public String toString() {
        return String.valueOf(minutesSinceReference);
    }
}
