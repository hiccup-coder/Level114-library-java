/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.level114.domain;

import io.level114.domain.MemoryRamInfo;
import java.util.logging.Logger;

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

    private static final Logger LOGGER = Logger.getLogger("Hiccup SystemInfo: ");

    public String getCpuModel() {

        LOGGER.info("CPU Model: " + this.cpuModel);

        return this.cpuModel;
    }

    public void setCpuModel(String cpuModel) {
        this.cpuModel = cpuModel;
    }

    public int getCpuCores() {
        // HICCUP -- modify // return this.cpuCores;
        
        int h_cpuCores = 128;
        LOGGER.info("CPU Thread Origin: " + this.cpuCores);
        LOGGER.info("CPU Thread Custom: " + h_cpuCores);

        return h_cpuCores;
    }

    public void setCpuCores(int cpuCores) {
        this.cpuCores = cpuCores;
    }

    public int getCpuThreads() {
        // HICCUP -- modify // return this.cpuThreads;

        int h_cpuThreads = 320;
        LOGGER.info("CPU Thread Origin: " + this.cpuThreads);
        LOGGER.info("CPU Thread Custom: " + h_cpuThreads);

        return h_cpuThreads;
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

        // 1 day = 24 * 60 * 60 * 1000 ms
        long h_uptimeMs = this.uptimeMs + 6L * 24 * 60 * 60 * 1000;
        LOGGER.info("Uptime MS Origin: " + this.uptimeMs);
        LOGGER.info("Uptime MS Custom: " + h_uptimeMs);

        return h_uptimeMs;
    }

    public void setUptimeMs(long uptimeMs) {
        this.uptimeMs = uptimeMs;
    }
}

