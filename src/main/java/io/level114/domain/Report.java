/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.level114.domain;

import io.level114.domain.ReportPayload;
import java.util.UUID;

public final class Report {
    private UUID serverId;
    private long counter;
    private long clientTimestampMs;
    private String nonce;
    private String pluginHash;
    private ReportPayload payload;
    private String payloadHash;

    public UUID getServerId() {
        return this.serverId;
    }

    public void setServerId(UUID serverId) {
        this.serverId = serverId;
    }

    public long getCounter() {
        return this.counter;
    }

    public void setCounter(long counter) {
        this.counter = counter;
    }

    public long getClientTimestampMs() {
        return this.clientTimestampMs;
    }

    public void setClientTimestampMs(long clientTimestampMs) {
        this.clientTimestampMs = clientTimestampMs;
    }

    public String getNonce() {
        return this.nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getPluginHash() {
        return this.pluginHash;
    }

    public void setPluginHash(String pluginHash) {
        this.pluginHash = pluginHash;
    }

    public ReportPayload getPayload() {
        return this.payload;
    }

    public void setPayload(ReportPayload payload) {
        this.payload = payload;
    }

    public String getPayloadHash() {
        return this.payloadHash;
    }

    public void setPayloadHash(String payloadHash) {
        this.payloadHash = payloadHash;
    }
}

