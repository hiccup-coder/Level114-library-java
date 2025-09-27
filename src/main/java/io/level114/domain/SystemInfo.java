/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.level114.domain;

import io.level114.domain.MemoryRamInfo;

public final class SystemInfo {
    private String cpuModel;
    private int cpuCores;
    private int cpuThreads;
    private MemoryRamInfo memoryRamInfo;
    private String javaVersion;
    private String osName;
    private String osVersion;
    private String osArch;
    private long uptimeMs;

    public String getCpuModel() {
        return this.cpuModel;
    }

    public void setCpuModel(String cpuModel) {
        this.cpuModel = cpuModel;
    }

    public int getCpuCores() {
        return this.cpuCores;
    }

    public void setCpuCores(int cpuCores) {
        this.cpuCores = cpuCores;
    }

    public int getCpuThreads() {
        return this.cpuThreads;
    }

    public void setCpuThreads(int cpuThreads) {
        this.cpuThreads = cpuThreads;
    }

    public MemoryRamInfo getMemoryRamInfo() {
        return this.memoryRamInfo;
    }

    public void setMemoryRamInfo(MemoryRamInfo memoryRamInfo) {
        this.memoryRamInfo = memoryRamInfo;
    }

    public String getJavaVersion() {
        return this.javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public String getOsName() {
        return this.osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVersion() {
        return this.osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getOsArch() {
        return this.osArch;
    }

    public void setOsArch(String osArch) {
        this.osArch = osArch;
    }

    public long getUptimeMs() {
        return this.uptimeMs;
    }

    public void setUptimeMs(long uptimeMs) {
        this.uptimeMs = uptimeMs;
    }
}

