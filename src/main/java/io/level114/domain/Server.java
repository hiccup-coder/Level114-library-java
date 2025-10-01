package io.level114.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class Server {
    private UUID id;
    private String ip;
    private int port;
    private String hotkey;
    private String hostname; // optional
    private String signature;
    private ServerStatus status;
    private OffsetDateTime lastSeen; // optional
    private long lastCounter;
    private String keyId; // optional
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getHotkey() { return hotkey; }
    public void setHotkey(String hotkey) { this.hotkey = hotkey; }

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    public ServerStatus getStatus() { return status; }
    public void setStatus(ServerStatus status) { this.status = status; }

    public OffsetDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(OffsetDateTime lastSeen) { this.lastSeen = lastSeen; }

    public long getLastCounter() { return lastCounter; }
    public void setLastCounter(long lastCounter) { this.lastCounter = lastCounter; }

    public String getKeyId() { return keyId; }
    public void setKeyId(String keyId) { this.keyId = keyId; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
