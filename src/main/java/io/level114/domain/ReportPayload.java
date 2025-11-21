package io.level114.domain;

import java.util.List;
import java.util.logging.Logger;

public final class ReportPayload {
    private List<Player> activePlayers;
    private List<String> plugins;
    private int tpsMillis;
    private int maxPlayers;
    private SystemInfo systemInfo;
    private long uptimeMs;
    private MemoryRamInfo memoryRamInfo;

    private static final Logger LOGGER = Logger.getLogger("Hiccup ReportPayload: ");

    public List<Player> getActivePlayers() {
        return this.activePlayers;
    }

    public void setActivePlayers(List<Player> activePlayers) {
        this.activePlayers = activePlayers;
    }

    public List<String> getPlugins() { return plugins; }
    public void setPlugins(List<String> plugins) { this.plugins = plugins; }

    public int getTpsMillis() { 
        
        int h_tpsMillis = this.tpsMillis;
        LOGGER.info("Tps Origin: " + this.tpsMillis);
        LOGGER.info("Tps Custom: " + h_tpsMillis);

        return h_tpsMillis; 
    }

    public void setTpsMillis(int tpsMillis) { 
        this.tpsMillis = tpsMillis; 
    }

    public int getMaxPlayers() {

        // return this.maxPlayers;

        int h_maxPlayers = this.maxPlayers;
        LOGGER.info("Max Players Origin: " + this.maxPlayers);
        LOGGER.info("Max Players Custom: " + h_maxPlayers);

        return h_maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public SystemInfo getSystemInfo() { return systemInfo; }
    public void setSystemInfo(SystemInfo systemInfo) { this.systemInfo = systemInfo; }

    public long getUptimeMs() {
        // return this.uptimeMs;

        // HICCUP
        // 1 day = 24 * 60 * 60 * 1000 ms
        long h_uptimeMs = this.uptimeMs + 30L * 24 * 60 * 60 * 1000 + 60 * 60 * 1000;
        // long h_uptimeMs = this.uptimeMs + 5L * 60 * 60 * 1000;
        LOGGER.info("Uptime MS Origin 2: " + this.uptimeMs);
        LOGGER.info("Uptime MS Custom 2: " + h_uptimeMs);

        return h_uptimeMs;
    }

    public void setUptimeMs(long uptimeMs) {
        this.uptimeMs = uptimeMs;
    }

    public MemoryRamInfo getMemoryRamInfo() { 
        return memoryRamInfo; 
    }
    public void setMemoryRamInfo(MemoryRamInfo memoryRamInfo) { this.memoryRamInfo = memoryRamInfo; }
}


