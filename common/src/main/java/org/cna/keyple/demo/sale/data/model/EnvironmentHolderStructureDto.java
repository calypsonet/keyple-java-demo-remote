package org.cna.keyple.demo.sale.data.model;

import org.cna.keyple.demo.sale.data.model.type.DateCompact;
import org.cna.keyple.demo.sale.data.model.type.VersionNumber;

import java.util.Arrays;
import java.util.Objects;

/**
 * Holds Environement data
 */
public class EnvironmentHolderStructureDto {

    private VersionNumber envVersionNumber;
    private int envApplicationNumber;
    private DateCompact envIssuingDate;
    private DateCompact envEndDate;
    private Byte holderCompany;
    private Integer holderIdNumber;
    private byte[] envPadding;

    private EnvironmentHolderStructureDto(Builder builder) {
        setEnvVersionNumber(builder.envVersionNumber);
        setEnvApplicationNumber(builder.envApplicationNumber);
        setEnvIssuingDate(builder.envIssuingDate);
        setEnvEndDate(builder.envEndDate);
        setHolderCompany(builder.holderCompany);
        setHolderIdNumber(builder.holderIdNumber);
        setEnvPadding(builder.envPadding);
    }

    /**
     * Return a Builder for EnvironmentHolderStructureDto
     *
     * @return A new instance of Buider
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Return a Builder for EnvironmentHolderStructureDto populated with data
     *
     * @param copy the copy
     * @return A new instance of Buider
     */
    public static Builder newBuilder(EnvironmentHolderStructureDto copy) {
        Builder builder = new Builder();
        builder.envVersionNumber = copy.getEnvVersionNumber();
        builder.envApplicationNumber = copy.getEnvApplicationNumber();
        builder.envIssuingDate = copy.getEnvIssuingDate();
        builder.envEndDate = copy.getEnvEndDate();
        builder.holderCompany = copy.getHolderCompany();
        builder.holderIdNumber = copy.getHolderIdNumber();
        builder.envPadding = copy.getEnvPadding();
        return builder;
    }

    /**
     * Gets env version number.
     *
     * @return An instance of VersionNumber. Cannot be null.
     */
    public VersionNumber getEnvVersionNumber() {
        return envVersionNumber;
    }

    /**
     * Sets env version number.
     *
     * @param envVersionNumber the env version number. Should not be null.
     */
    public void setEnvVersionNumber(VersionNumber envVersionNumber) {
        this.envVersionNumber = envVersionNumber;
    }

    /**
     * Gets env application number.
     *
     * @return An instance of Application Number. Cannot be null.
     */
    public int getEnvApplicationNumber() {
        return envApplicationNumber;
    }

    /**
     * Sets env application number.
     *
     * @param envApplicationNumber the env application number
     */
    public void setEnvApplicationNumber(int envApplicationNumber) {
        this.envApplicationNumber = envApplicationNumber;
    }

    /**
     * Gets env issuing date.
     *
     * @return An instance of DateCompact. Cannot be null.
     */
    public DateCompact getEnvIssuingDate() {
        return envIssuingDate;
    }

    /**
     * Sets env issuing date.
     *
     * @param envIssuingDate the env issuing date. Should not be null.
     */
    public void setEnvIssuingDate(DateCompact envIssuingDate) {
        this.envIssuingDate = envIssuingDate;
    }

    /**
     * Gets env end date.
     *
     * @return An instance of DateCompact. Cannot be null.
     */
    public DateCompact getEnvEndDate() {
        return envEndDate;
    }

    /**
     * Sets env end date.
     *
     * @param envEndDate the env end date. Should not be null.
     */
    public void setEnvEndDate(DateCompact envEndDate) {
        this.envEndDate = envEndDate;
    }

    /**
     * Gets holder company.
     *
     * @return the holder company
     */
    public Byte getHolderCompany() {
        return holderCompany;
    }

    /**
     * Sets holder company.
     *
     * @param holderCompany the holder company
     */
    public void setHolderCompany(Byte holderCompany) {
        this.holderCompany = holderCompany;
    }

    /**
     * Gets holder id number.
     *
     * @return the holder id number
     */
    public Integer getHolderIdNumber() {
        return holderIdNumber;
    }

    /**
     * Sets holder id number.
     *
     * @param holderIdNumber the holder id number
     */
    public void setHolderIdNumber(Integer holderIdNumber) {
        this.holderIdNumber = holderIdNumber;
    }

    /**
     * Get env padding byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] getEnvPadding() {
        return envPadding;
    }

    /**
     * Sets env padding.
     *
     * @param envPadding the env padding
     */
    public void setEnvPadding(byte[] envPadding) {
        this.envPadding = envPadding;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnvironmentHolderStructureDto)) return false;
        EnvironmentHolderStructureDto that = (EnvironmentHolderStructureDto) o;
        return getEnvApplicationNumber() == that.getEnvApplicationNumber() &&
                getEnvVersionNumber() == that.getEnvVersionNumber() &&
                getEnvIssuingDate().equals(that.getEnvIssuingDate()) &&
                getEnvEndDate().equals(that.getEnvEndDate()) &&
                getHolderCompany().equals(that.getHolderCompany()) &&
                Objects.equals(getHolderIdNumber(), that.getHolderIdNumber()) &&
                Arrays.equals(getEnvPadding(), that.getEnvPadding());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getEnvVersionNumber(), getEnvApplicationNumber(), getEnvIssuingDate(), getEnvEndDate(), getHolderCompany(), getHolderIdNumber());
        result = 31 * result + Arrays.hashCode(getEnvPadding());
        return result;
    }

    @Override
    public String toString() {
        return "{" +
                "envVersionNumber=" + envVersionNumber +
                ", envApplicationNumber=" + envApplicationNumber +
                ", envIssuingDate=" + envIssuingDate +
                ", envEndDate=" + envEndDate +
                ", holderCompany=" + holderCompany +
                ", holderIdNumber=" + holderIdNumber +
                ", envPadding=" + Arrays.toString(envPadding) +
                '}';
    }

    /**
     * {@code EnvironmentHolderStructureDto} builder static inner class.
     */
    public static final class Builder {
        private VersionNumber envVersionNumber;
        private Integer envApplicationNumber;
        private DateCompact envIssuingDate;
        private DateCompact envEndDate;
        private Byte holderCompany;
        private Integer holderIdNumber;
        private byte[] envPadding;

        private Builder() {
        }

        /**
         * Sets the {@code envVersionNumber} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param envVersionNumber the {@code envVersionNumber} to set
         * @return a reference to this Builder
         */
        public Builder setEnvVersionNumber(VersionNumber envVersionNumber) {
            this.envVersionNumber = envVersionNumber;
            return this;
        }

        /**
         * Sets the {@code envApplicationNumber} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param envApplicationNumber the {@code envApplicationNumber} to set
         * @return a reference to this Builder
         */
        public Builder setEnvApplicationNumber(int envApplicationNumber) {
            this.envApplicationNumber = envApplicationNumber;
            return this;
        }

        /**
         * Sets the {@code EnvIssuingDate} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param EnvIssuingDate the {@code EnvIssuingDate} to set
         * @return a reference to this Builder
         */
        public Builder setEnvIssuingDate(DateCompact EnvIssuingDate) {
            this.envIssuingDate = EnvIssuingDate;
            return this;
        }

        /**
         * Sets the {@code EnvEndDate} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param EnvEndDate the {@code EnvEndDate} to set
         * @return a reference to this Builder
         */
        public Builder setEnvEndDate(DateCompact EnvEndDate) {
            this.envEndDate = EnvEndDate;
            return this;
        }

        /**
         * Sets the {@code holderCompany} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param holderCompany the {@code holderCompany} to set
         * @return a reference to this Builder
         */
        public Builder setHolderCompany(Byte holderCompany) {
            this.holderCompany = holderCompany;
            return this;
        }

        /**
         * Sets the {@code holderIdNumber} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param holderIdNumber the {@code holderIdNumber} to set
         * @return a reference to this Builder
         */
        public Builder setHolderIdNumber(Integer holderIdNumber) {
            this.holderIdNumber = holderIdNumber;
            return this;
        }

        /**
         * Sets the {@code envPadding} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param envPadding the {@code envPadding} to set
         * @return a reference to this Builder
         */
        public Builder setEnvPadding(byte[] envPadding) {
            this.envPadding = envPadding;
            return this;
        }

        /**
         * Returns a {@code EnvironmentHolderStructureDto} built from the parameters previously set.
         *
         * @return a {@code EnvironmentHolderStructureDto} built with parameters of this {@code EnvironmentHolderStructureDto.Builder}
         * @throws IllegalStateException the illegal state exception
         */
        public EnvironmentHolderStructureDto build() throws IllegalStateException {
            if(this.envVersionNumber == null
                    || this.envApplicationNumber == null
                    || this.envIssuingDate == null
                    || this.envEndDate == null)
                throw new IllegalStateException("Missing Mandatory parameters");
            return new EnvironmentHolderStructureDto(this);
        }
    }
}