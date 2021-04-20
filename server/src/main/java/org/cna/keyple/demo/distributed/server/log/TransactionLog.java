package org.cna.keyple.demo.distributed.server.log;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.UUID;

/**
 * Transaction log object
 */
public class TransactionLog {

    String id;//id of the transaction
    String poSn;//PO Serial Number
    String plugin;//plugin name
    String startedAt;//when the transaction started
    String type;//type of transaction
    String status;//SUCCESS or FAIL
    String contractLoaded;//(opt) description of the contract loaded

    /**
     * Default contructor
     */
    public TransactionLog() {
        this.id = generateId();
        this.startedAt = generateTimestamp();
    }

    /**
     * Generate a 4 char random UUID
     * @return a not nullable id
     */
    private String generateId() {
        return UUID.randomUUID().toString().substring(0,4);
    }

    /**
     * Generate a formatted timestamp
     * @return a not nullable timestamp
     */
    private String generateTimestamp(){
        DateTimeFormatter formatter =
                DateTimeFormatter.ofLocalizedDateTime( FormatStyle.SHORT )
                        //.withLocale( Locale.UK )
                        .withZone( ZoneId.systemDefault() );

        return formatter.format(Instant.now());
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public String getId() {
        return id;
    }

    public String getPoSn() {
        return poSn;
    }

    public String getPlugin() {
        return plugin;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public String getContractLoaded() {
        return contractLoaded;
    }

    public TransactionLog setPoSn(String poSn) {
        this.poSn = poSn;
        return this;
    }

    public TransactionLog setPlugin(String plugin) {
        this.plugin = plugin;
        return this;
    }

    public TransactionLog setType(String type) {
        this.type = type;
        return this;
    }

    public TransactionLog setStatus(String status) {
        this.status = status;
        return this;
    }

    public TransactionLog setContractLoaded(String contractLoaded) {
        this.contractLoaded = contractLoaded;
        return this;
    }
}
