package org.cna.keyple.demo.remote.server.transaction;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.UUID;

/**
 * Transaction logs
 */
public class Transaction {

    String id;
    String poSn;
    String plugin;
    String startedAt;
    String type;
    String status;

    /**
     * Default contructor
     */
    public Transaction() {
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
                DateTimeFormatter.ofLocalizedDateTime( FormatStyle.MEDIUM )
                        .withLocale( Locale.UK )
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

    public Transaction setPoSn(String poSn) {
        this.poSn = poSn;
        return this;
    }

    public Transaction setPlugin(String plugin) {
        this.plugin = plugin;
        return this;
    }

    public Transaction setType(String type) {
        this.type = type;
        return this;
    }

    public Transaction setStatus(String status) {
        this.status = status;
        return this;
    }
}
