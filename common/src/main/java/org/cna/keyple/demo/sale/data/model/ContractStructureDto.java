package org.cna.keyple.demo.sale.data.model;

import org.cna.keyple.demo.sale.data.model.type.DateCompact;
import org.cna.keyple.demo.sale.data.model.type.PriorityCode;
import org.cna.keyple.demo.sale.data.model.type.VersionNumber;

import java.util.Arrays;
import java.util.Objects;

/**
 * Holds Data of a contract
 */
public class ContractStructureDto {

    //mandatory
    private VersionNumber contractVersionNumber;
    private PriorityCode contractTariff;
    private DateCompact contactSaleDate;
    private DateCompact contractValidityEndDate;

    //optional
    private Integer contractSaleSam;
    private Integer contractSaleCounter;
    private Byte contractAuthKvc;
    private Integer contractAuthenticator;
    private byte[] contractPadding;

    private ContractStructureDto(Builder builder) {
        setContractVersionNumber(builder.contractVersionNumber);
        setContractTariff(builder.contractTariff);
        setContactSaleDate(builder.contactSaleDate);
        setContractValidityEndDate(builder.contractValidityEndDate);
        setContractSaleSam(builder.contractSaleSam);
        setContractSaleCounter(builder.contractSaleCounter);
        setContractAuthKvc(builder.contractAuthKvc);
        setContractAuthenticator(builder.contractAuthenticator);
        setContractPadding(builder.contractPadding);
    }

    /**
     * Return a Builder for ContractStructureDto
     *
     * @return A new instance of Buider
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Return a Builder for ContractStructureDto populated with data
     *
     * @param copy the copy
     * @return A new instance of Buider
     */
    public static Builder newBuilder(ContractStructureDto copy) {
        Builder builder = new Builder();
        builder.contractVersionNumber = copy.getContractVersionNumber();
        builder.contractTariff = copy.getContractTariff();
        builder.contactSaleDate = copy.getContactSaleDate();
        builder.contractValidityEndDate = copy.getContractValidityEndDate();
        builder.contractSaleSam = copy.getContractSaleSam();
        builder.contractSaleCounter = copy.getContractSaleCounter();
        builder.contractAuthKvc = copy.getContractAuthKvc();
        builder.contractAuthenticator = copy.getContractAuthenticator();
        builder.contractPadding = copy.getContractPadding();
        return builder;
    }

    /**
     * Get Contract version number
     *
     * @return An instance of VersionNumber. Cannot be null;
     */
    public VersionNumber getContractVersionNumber() {
        return contractVersionNumber;
    }

    /**
     * Sets contract version number.
     *
     * @param contractVersionNumber the contract version number. Should not be null
     */
    public void setContractVersionNumber(VersionNumber contractVersionNumber) {
        this.contractVersionNumber = contractVersionNumber;
    }

    /**
     * Gets contract tariff.
     *
     * @return An instance of PriorityCode. Cannot be null.
     */
    public PriorityCode getContractTariff() {
        return contractTariff;
    }

    /**
     * Sets contract tariff.
     *
     * @param contractTariff the contract tariff. Should not be null.
     */
    public void setContractTariff(PriorityCode contractTariff) {
        this.contractTariff = contractTariff;
    }

    /**
     * Gets contact sale date.
     *
     * @return An instance of DateCompact. Cannot be null.
     */
    public DateCompact getContactSaleDate() {
        return contactSaleDate;
    }

    /**
     * Sets contact sale date.
     *
     * @param contactSaleDate the contact sale date. Should not be null
     */
    public void setContactSaleDate(DateCompact contactSaleDate) {
        this.contactSaleDate = contactSaleDate;
    }

    /**
     * Gets contract validity end date.
     *
     * @return An instance of DateCompact. Cannot be null.
     */
    public DateCompact getContractValidityEndDate() {
        return contractValidityEndDate;
    }

    /**
     * Sets contract validity end date.
     *
     * @param contractValidityEndDate the contract validity end date. Should not be null.
     */
    public void setContractValidityEndDate(DateCompact contractValidityEndDate) {
        this.contractValidityEndDate = contractValidityEndDate;
    }

    /**
     * Gets contract sale sam.
     *
     * @return the contract sale sam
     */
    public Integer getContractSaleSam() {
        return contractSaleSam;
    }

    /**
     * Sets contract sale sam.
     *
     * @param contractSaleSam the contract sale sam
     */
    public void setContractSaleSam(Integer contractSaleSam) {
        this.contractSaleSam = contractSaleSam;
    }

    /**
     * Gets contract sale counter.
     *
     * @return the contract sale counter
     */
    public Integer getContractSaleCounter() {
        return contractSaleCounter;
    }

    /**
     * Sets contract sale counter.
     *
     * @param contractSaleCounter the contract sale counter
     */
    public void setContractSaleCounter(Integer contractSaleCounter) {
        this.contractSaleCounter = contractSaleCounter;
    }

    /**
     * Gets contract auth kvc.
     *
     * @return the contract auth kvc
     */
    public Byte getContractAuthKvc() {
        return contractAuthKvc;
    }

    /**
     * Sets contract auth kvc.
     *
     * @param contractAuthKvc the contract auth kvc
     */
    public void setContractAuthKvc(Byte contractAuthKvc) {
        this.contractAuthKvc = contractAuthKvc;
    }

    /**
     * Gets contract authenticator.
     *
     * @return the contract authenticator
     */
    public Integer getContractAuthenticator() {
        return contractAuthenticator;
    }

    /**
     * Sets contract authenticator.
     *
     * @param contractAuthenticator the contract authenticator
     */
    public void setContractAuthenticator(Integer contractAuthenticator) {
        this.contractAuthenticator = contractAuthenticator;
    }

    /**
     * Get contract padding byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] getContractPadding() {
        return contractPadding;
    }

    /**
     * Sets contract padding.
     *
     * @param contractPadding the contract padding
     */
    public void setContractPadding(byte[] contractPadding) {
        this.contractPadding = contractPadding;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContractStructureDto)) return false;
        ContractStructureDto that = (ContractStructureDto) o;
        return getContractVersionNumber() == that.getContractVersionNumber() &&
                getContractTariff() == that.getContractTariff() &&
                getContactSaleDate().equals(that.getContactSaleDate()) &&
                getContractValidityEndDate().equals(that.getContractValidityEndDate()) &&
                getContractSaleSam().equals(that.getContractSaleSam()) &&
                getContractSaleCounter().equals(that.getContractSaleCounter()) &&
                getContractAuthKvc().equals(that.getContractAuthKvc()) &&
                getContractAuthenticator().equals(that.getContractAuthenticator()) &&
                Arrays.equals(getContractPadding(), that.getContractPadding());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getContractVersionNumber(), getContractTariff(), getContactSaleDate(), getContractValidityEndDate(), getContractSaleSam(), getContractSaleCounter(), getContractAuthKvc(), getContractAuthenticator());
        result = 31 * result + Arrays.hashCode(getContractPadding());
        return result;
    }

    /**
     * {@code ContractStructureDto} builder static inner class.
     */
    public static final class Builder {
        private VersionNumber contractVersionNumber;
        private PriorityCode contractTariff;
        private DateCompact contactSaleDate;
        private DateCompact contractValidityEndDate;
        private Integer contractSaleSam;
        private Integer contractSaleCounter;
        private Byte contractAuthKvc;
        private Integer contractAuthenticator;
        private byte[] contractPadding;

        private Builder() {
        }

        /**
         * Sets the {@code contractVersionNumber} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param contractVersionNumber the {@code contractVersionNumber} to set
         * @return a reference to this Builder
         */
        public Builder setContractVersionNumber(VersionNumber contractVersionNumber) {
            this.contractVersionNumber = contractVersionNumber;
            return this;
        }

        /**
         * Sets the {@code contractTariff} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param contractTariff the {@code contractTariff} to set
         * @return a reference to this Builder
         */
        public Builder setContractTariff(PriorityCode contractTariff) {
            this.contractTariff = contractTariff;
            return this;
        }

        /**
         * Sets the {@code contactSaleDate} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param contactSaleDate the {@code contactSaleDate} to set
         * @return a reference to this Builder
         */
        public Builder setContactSaleDate(DateCompact contactSaleDate) {
            this.contactSaleDate = contactSaleDate;
            return this;
        }

        /**
         * Sets the {@code contractValidityEndDate} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param contractValidityEndDate the {@code contractValidityEndDate} to set
         * @return a reference to this Builder
         */
        public Builder setContractValidityEndDate(DateCompact contractValidityEndDate) {
            this.contractValidityEndDate = contractValidityEndDate;
            return this;
        }

        /**
         * Sets the {@code contractSaleSam} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param contractSaleSam the {@code contractSaleSam} to set
         * @return a reference to this Builder
         */
        public Builder setContractSaleSam(Integer contractSaleSam) {
            this.contractSaleSam = contractSaleSam;
            return this;
        }

        /**
         * Sets the {@code contractSaleCounter} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param contractSaleCounter the {@code contractSaleCounter} to set
         * @return a reference to this Builder
         */
        public Builder setContractSaleCounter(Integer contractSaleCounter) {
            this.contractSaleCounter = contractSaleCounter;
            return this;
        }

        /**
         * Sets the {@code contractAuthKvc} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param contractAuthKvc the {@code contractAuthKvc} to set
         * @return a reference to this Builder
         */
        public Builder setContractAuthKvc(Byte contractAuthKvc) {
            this.contractAuthKvc = contractAuthKvc;
            return this;
        }

        /**
         * Sets the {@code contractAuthenticator} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param contractAuthenticator the {@code contractAuthenticator} to set
         * @return a reference to this Builder
         */
        public Builder setContractAuthenticator(Integer contractAuthenticator) {
            this.contractAuthenticator = contractAuthenticator;
            return this;
        }

        /**
         * Sets the {@code contractPadding} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param contractPadding the {@code contractPadding} to set
         * @return a reference to this Builder
         */
        public Builder setContractPadding(byte[] contractPadding) {
            this.contractPadding = contractPadding;
            return this;
        }

        /**
         * Returns a {@code ContractStructureDto} built from the parameters previously set.
         *
         * @return a {@code ContractStructureDto} built with parameters of this {@code ContractStructureDto.Builder}
         * @throws IllegalStateException the illegal state exception
         */
        public ContractStructureDto build() throws IllegalStateException{
            if(this.contractVersionNumber == null
                    || this.contractTariff == null
                    || this.contactSaleDate == null
                    || this.contractValidityEndDate == null)
                throw new IllegalStateException("Missing mandatory parameter");
            return new ContractStructureDto(this);
        }
    }
}
