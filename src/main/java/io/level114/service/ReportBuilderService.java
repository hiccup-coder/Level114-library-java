package io.level114.service;

import io.level114.domain.MemoryRamInfo;
import io.level114.domain.Player;
import io.level114.domain.Report;
import io.level114.domain.ReportNonce;
import io.level114.domain.ReportPayload;
import io.level114.domain.SystemInfo;
import io.level114.util.HashUtils;
import io.level114.util.JsonUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public final class ReportBuilderService {
    private final Logger logger;
    private final JavaPlugin plugin;
    private final String serverId;
    private final long startTimeMs;
    private final String pluginJarHash;

    public ReportBuilderService(Logger logger, JavaPlugin plugin, File pluginFile, String serverId, long startTimeMs) {
        this.logger = logger;
        this.plugin = plugin;
        this.serverId = serverId;
        this.startTimeMs = startTimeMs;
        this.pluginJarHash = computePluginJarHash(pluginFile);
    }

    public Report buildReport(ReportNonce nonce, long counter) {
        ReportPayload payload = buildPayload();
        String payloadCanonicalJson = JsonUtils.toCanonicalJson(payload);
        String payloadHash = HashUtils.sha256Hex(payloadCanonicalJson);

        Report report = new Report();
        report.setServerId(UUID.fromString(serverId));
        report.setCounter(counter);
        report.setClientTimestampMs(System.currentTimeMillis());
        report.setNonce(nonce.getNonce());
        report.setPluginHash(pluginJarHash);
        report.setPayload(payload);
        report.setPayloadHash(payloadHash);
        return report;
    }

    public ReportPayload collectPayloadSync() {
        return buildPayload();
    }

    public Report buildReportFromPayload(ReportNonce nonce, long counter, ReportPayload payload) {
        String payloadCanonicalJson = JsonUtils.toCanonicalJson(payload);
        String payloadHash = HashUtils.sha256Hex(payloadCanonicalJson);

        Report report = new Report();
        report.setServerId(UUID.fromString(serverId));
        report.setCounter(counter);
        report.setClientTimestampMs(System.currentTimeMillis());
        report.setNonce(nonce.getNonce());
        report.setPluginHash(pluginJarHash);
        report.setPayload(payload);
        report.setPayloadHash(payloadHash);
        return report;
    }

    private ReportPayload buildPayload() {
        ReportPayload p = new ReportPayload();
        p.setActivePlayers(collectActivePlayers());
        p.setPlugins(collectPluginNames());
        p.setTpsMillis(computeTpsMillis());
        p.setMaxPlayers(Bukkit.getMaxPlayers());
        p.setSystemInfo(collectSystemInfo());
        p.setUptimeMs(Math.max(0L, System.currentTimeMillis() - startTimeMs));
        // Payload memory: JVM (Minecraft server process) memory usage
        p.setMemoryRamInfo(collectJvmMemoryInfo());
        return p;
    }

    private List<Player> collectActivePlayers() {
        List<Player> list = new ArrayList<>();
        for (org.bukkit.entity.Player bp : Bukkit.getOnlinePlayers()) {
            Player p = new Player();
            p.setName(bp.getName());
            p.setUuid(bp.getUniqueId().toString());
            list.add(p);
        }
        return list;
    }

    private List<String> collectPluginNames() {
        List<String> list = new ArrayList<>();
        for (Plugin pl : Bukkit.getPluginManager().getPlugins()) {
            list.add(pl.getName());
            if (list.size() >= 1024)
                break;
        }
        return list;
    }

    private int computeTpsMillis() {
        try {
            double[] tps = Bukkit.getServer().getTPS();
            double current = tps != null && tps.length > 0 ? tps[0] : 20.0;
            current = Math.max(0.0, Math.min(current, 20.1));
            int milliTps = (int) Math.round(current * 1000.0);
            return Math.max(0, Math.min(milliTps, 20100));
        } catch (Throwable t) {
            return 20000;
        }
    }

    private SystemInfo collectSystemInfo() {
        SystemInfo si = new SystemInfo();
        si.setCpuCores(Runtime.getRuntime().availableProcessors());
        si.setCpuModel(readCpuModel());
        si.setCpuThreads(detectCpuThreads());
        si.setJavaVersion(System.getProperty("java.version"));
        si.setOsName(System.getProperty("os.name"));
        si.setOsVersion(System.getProperty("os.version"));
        si.setOsArch(System.getProperty("os.arch"));
        si.setUptimeMs(Math.max(0L, System.currentTimeMillis() - startTimeMs));
        // System info memory: host machine physical memory
        si.setMemoryRamInfo(collectSystemMemoryInfo());
        return si;
    }

    private String readCpuModel() {
        try {
            Path cpuInfo = Path.of("/proc/cpuinfo");
            if (Files.isRegularFile(cpuInfo)) {
                for (String line : Files.readAllLines(cpuInfo)) {
                    String lower = line.toLowerCase();
                    if (lower.startsWith("model name") || lower.startsWith("hardware")) {
                        int idx = line.indexOf(':');
                        if (idx >= 0 && idx + 1 < line.length()) {
                            return line.substring(idx + 1).trim();
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        String env = System.getenv("PROCESSOR_IDENTIFIER");
        return env != null && !env.isBlank() ? env : null;
    }

    private MemoryRamInfo collectSystemMemoryInfo() {
        MemoryRamInfo mi = new MemoryRamInfo();
        try {
            java.lang.management.OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean s) {
                long total = s.getTotalPhysicalMemorySize();
                long free = s.getFreePhysicalMemorySize();
                mi.setTotalMemoryBytes(Math.max(total, 0));
                mi.setFreeMemoryBytes(Math.max(free, 0));
                mi.setUsedMemoryBytes(Math.max(total - free, 0));
                return mi;
            }
        } catch (Throwable ignored) {
        }
        mi.setTotalMemoryBytes(0);
        mi.setFreeMemoryBytes(0);
        mi.setUsedMemoryBytes(0);
        return mi;
    }

    private MemoryRamInfo collectJvmMemoryInfo() {
        MemoryRamInfo mi = new MemoryRamInfo();
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        mi.setTotalMemoryBytes(Math.max(total, 0));
        mi.setFreeMemoryBytes(Math.max(free, 0));
        mi.setUsedMemoryBytes(Math.max(total - free, 0));
        return mi;
    }

    private int detectCpuThreads() {
        try {
            return Runtime.getRuntime().availableProcessors();
        } catch (Throwable t) {
            return 1;
        }
    }

    private String computePluginJarHash(File pluginFile) {
        File fileToHash = null;
        if (pluginFile != null && pluginFile.isFile()) {
            fileToHash = resolveOriginalJar(pluginFile);
        }

        if (fileToHash == null) {
            try {
                URI uri = plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
                File remapped = new File(uri);
                if (remapped.isFile()) {
                    fileToHash = resolveOriginalJar(remapped);
                }
            } catch (Exception e) {
                this.logger.warning("Unable to resolve plugin jar for hashing: " + e.getMessage());
            }
        }

        if (fileToHash != null) {
            String hash = HashUtils.sha256Hex(fileToHash.toPath());
            if (hash != null) {
                this.logger.info("Plugin jar: " + fileToHash.getAbsolutePath());
                this.logger.info("Plugin SHA-256: " + hash);
            } else {
                this.logger.warning("Failed to compute plugin hash for " + fileToHash.getAbsolutePath());
            }
            return hash;
        }

        this.logger.warning("Plugin file handle is not available for hashing");
        return null;
    }

    private File resolveOriginalJar(File candidate) {
        if (candidate == null || !candidate.isFile()) {
            return null;
        }

        File parent = candidate.getParentFile();
        if (parent != null && ".paper-remapped".equals(parent.getName())) {
            File pluginsDir = parent.getParentFile();
            if (pluginsDir != null) {
                File original = new File(pluginsDir, candidate.getName());
                if (original.isFile()) {
                    this.logger.info("Detected remapped jar; using original at " + original.getAbsolutePath());
                    return original;
                }
            }
        }

        return candidate;
    }
}
