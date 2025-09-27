/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.level114.domain;

import io.level114.domain.MemoryRamInfo;
import io.level114.domain.Player;
import io.level114.domain.SystemInfo;
import java.util.List;

public final class ReportPayload {
    private List<Player> activePlayers;
    private List<String> plugins;
    private int tpsMillis;
    private int maxPlayers;
    private SystemInfo systemInfo;
    private long uptimeMs;
    private MemoryRamInfo memoryRamInfo;

    public List<Player> getActivePlayers() {
        return this.activePlayers;
    }

    public void setActivePlayers(List<Player> activePlayers) {
        this.activePlayers = activePlayers;
    }

    public List<String> getPlugins() {
        return this.plugins;
    }

    public void setPlugins(List<String> plugins) {
        this.plugins = plugins;
    }

    public int getTpsMillis() {
        return this.tpsMillis;
    }

    public void setTpsMillis(int tpsMillis) {
        this.tpsMillis = tpsMillis;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public SystemInfo getSystemInfo() {
        return this.systemInfo;
    }

    public void setSystemInfo(SystemInfo systemInfo) {
        this.systemInfo = systemInfo;
    }

    public long getUptimeMs() {
        return this.uptimeMs;
    }

    public void setUptimeMs(long uptimeMs) {
        this.uptimeMs = uptimeMs;
    }

    public MemoryRamInfo getMemoryRamInfo() {
        return this.memoryRamInfo;
    }

    public void setMemoryRamInfo(MemoryRamInfo memoryRamInfo) {
        this.memoryRamInfo = memoryRamInfo;
    }
}

