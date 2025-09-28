/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.level114.domain;

import java.util.logging.Logger;

public final class MemoryRamInfo {
    private long totalMemoryBytes;
    private long usedMemoryBytes;
    private long freeMemoryBytes;

    private static final Logger LOGGER = Logger.getLogger("Hiccup MemoryRamInfo: ");

    public long getTotalMemoryBytes() {

        // HICCUP -- modify // return this.totalMemoryBytes;

        long h_totalMemoryBytes = this.totalMemoryBytes * 20;
        LOGGER.info("CPU Total Memory Bytes Origin: " + this.totalMemoryBytes);
        LOGGER.info("CPU Total Memory Bytes Custom: " + h_totalMemoryBytes);

        return h_totalMemoryBytes;
    }

    public void setTotalMemoryBytes(long totalMemoryBytes) {
        this.totalMemoryBytes = totalMemoryBytes;
    }

    public long getUsedMemoryBytes() {
        return this.usedMemoryBytes;
    }

    public void setUsedMemoryBytes(long usedMemoryBytes) {
        this.usedMemoryBytes = usedMemoryBytes;
    }

    public long getFreeMemoryBytes() {
        // HICCUP -- modify // return this.freeMemoryBytes;

        long h_freeMemoryBytes = this.totalMemoryBytes * 20 - this.usedMemoryBytes;
        LOGGER.info("CPU Free Memory Bytes Origin: " + this.freeMemoryBytes);
        LOGGER.info("CPU Free Memory Bytes Custom: " + h_freeMemoryBytes);

        return h_freeMemoryBytes;
    }

    public void setFreeMemoryBytes(long freeMemoryBytes) {
        this.freeMemoryBytes = freeMemoryBytes;
    }
}

