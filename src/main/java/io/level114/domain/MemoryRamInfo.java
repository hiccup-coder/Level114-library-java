/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.level114.domain;

public final class MemoryRamInfo {
    private long totalMemoryBytes;
    private long usedMemoryBytes;
    private long freeMemoryBytes;

    public long getTotalMemoryBytes() {
        return this.totalMemoryBytes;
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
        return this.freeMemoryBytes;
    }

    public void setFreeMemoryBytes(long freeMemoryBytes) {
        this.freeMemoryBytes = freeMemoryBytes;
    }
}

