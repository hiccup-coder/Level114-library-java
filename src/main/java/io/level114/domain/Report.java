package io.level114.domain;

import java.util.UUID;

public final class Report {
    private UUID serverId;
    private long counter;
    private long clientTimestampMs;
    private String nonce;
    private String pluginHash;
    private ReportPayload payload;
    private String payloadHash;

    public UUID getServerId() { return serverId; }
    public void setServerId(UUID serverId) { this.serverId = serverId; }

    public long getCounter() { return counter; }
    public void setCounter(long counter) { this.counter = counter; }

    public long getClientTimestampMs() { return clientTimestampMs; }
    public void setClientTimestampMs(long clientTimestampMs) { this.clientTimestampMs = clientTimestampMs; }

    public String getNonce() { return nonce; }
    public void setNonce(String nonce) { this.nonce = nonce; }

    public String getPluginHash() { return pluginHash; }
    public void setPluginHash(String pluginHash) { this.pluginHash = pluginHash; }

    public ReportPayload getPayload() { return payload; }
    public void setPayload(ReportPayload payload) { this.payload = payload; }

    public String getPayloadHash() { return payloadHash; }
    public void setPayloadHash(String payloadHash) { this.payloadHash = payloadHash; }
}


