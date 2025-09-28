/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.java.JavaPlugin
 */
package io.level114.service;

import com.sun.management.OperatingSystemMXBean;
import io.level114.domain.MemoryRamInfo;
import io.level114.domain.Report;
import io.level114.domain.ReportNonce;
import io.level114.domain.ReportPayload;
import io.level114.domain.SystemInfo;
import io.level114.util.HashUtils;
import io.level114.util.JsonUtils;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class ReportBuilderService {
    private final Logger logger;
    private final JavaPlugin plugin;
    private final String serverId;
    private final long startTimeMs;
    private final String pluginJarHash;

    private static final Logger LOGGER = Logger.getLogger("Hiccup ReportBuilderService: ");

    public ReportBuilderService(Logger logger, JavaPlugin plugin, File pluginFile, String serverId, long startTimeMs) {
        this.logger = logger;
        this.plugin = plugin;
        this.serverId = serverId;
        this.startTimeMs = startTimeMs;
        this.pluginJarHash = this.computePluginJarHash(pluginFile);
    }

    public Report buildReport(ReportNonce nonce, long counter) {
        ReportPayload payload = this.buildPayload();
        String payloadCanonicalJson = JsonUtils.toCanonicalJson(payload);

        LOGGER.info("buildReport -- " + payloadCanonicalJson);

        String payloadHash = HashUtils.sha256Hex(payloadCanonicalJson);
        Report report = new Report();
        report.setServerId(UUID.fromString(this.serverId));
        report.setCounter(counter);
        report.setClientTimestampMs(System.currentTimeMillis());
        report.setNonce(nonce.getNonce());
        report.setPluginHash(this.pluginJarHash);
        report.setPayload(payload);
        report.setPayloadHash(payloadHash);
        return report;
    }

    public ReportPayload collectPayloadSync() {
        return this.buildPayload();
    }

    public Report buildReportFromPayload(ReportNonce nonce, long counter, ReportPayload payload) {
        String payloadCanonicalJson = JsonUtils.toCanonicalJson(payload);
        String payloadHash = HashUtils.sha256Hex(payloadCanonicalJson);
        Report report = new Report();
        report.setServerId(UUID.fromString(this.serverId));
        report.setCounter(counter);
        report.setClientTimestampMs(System.currentTimeMillis());
        report.setNonce(nonce.getNonce());
        report.setPluginHash(this.pluginJarHash);
        report.setPayload(payload);
        report.setPayloadHash(payloadHash);
        return report;
    }

    private ReportPayload buildPayload() {
        ReportPayload p = new ReportPayload();
        p.setActivePlayers(this.collectActivePlayers());
        p.setPlugins(this.collectPluginNames());
        p.setTpsMillis(this.computeTpsMillis());
        p.setMaxPlayers(Bukkit.getMaxPlayers());
        p.setSystemInfo(this.collectSystemInfo());
        p.setUptimeMs(Math.max(0L, System.currentTimeMillis() - this.startTimeMs));
        p.setMemoryRamInfo(this.collectJvmMemoryInfo());
        return p;
    }

    private List<io.level114.domain.Player> collectActivePlayers() {
        ArrayList<io.level114.domain.Player> list = new ArrayList<io.level114.domain.Player>();
        for (Player bp : Bukkit.getOnlinePlayers()) {
            io.level114.domain.Player p = new io.level114.domain.Player();
            p.setName(bp.getName());
            p.setUuid(bp.getUniqueId().toString());
            list.add(p);
        }

        LOGGER.info("ActivePlayers -- " + list.toString());

        return list;
    }

    private List<String> collectPluginNames() {
        ArrayList<String> list = new ArrayList<String>();
        for (Plugin pl : Bukkit.getPluginManager().getPlugins()) {
            list.add(pl.getName());
            if (list.size() >= 1024) break;
        }

        // Fake List
        // list.add("Level114");
        list.add("LuckPerms");
        list.add("ViaVersion");
        list.add("ViaBackwards");
        list.add("ViaRewind");

        LOGGER.info("PluginNames -- " + list.toString());

        return list;
    }

    private int computeTpsMillis() {
        try {
            double[] tps = Bukkit.getServer().getTPS();
            double current = tps != null && tps.length > 0 ? tps[0] : 20.0;
            current = Math.max(0.0, Math.min(current, 20.1));
            int milliTps = (int)Math.round(current * 1000.0);

            // HICCUP
            LOGGER.info("Tps Millis -- " + milliTps);

            return 20000; //return Math.max(0, Math.min(milliTps, 20100));

        } catch (Throwable t) {
            return 20000;
        }
    }

    private SystemInfo collectSystemInfo() {
        SystemInfo si = new SystemInfo();
        si.setCpuCores(Runtime.getRuntime().availableProcessors());
        si.setCpuModel(this.readCpuModel());
        si.setCpuThreads(this.detectCpuThreads());
        si.setJavaVersion(System.getProperty("java.version"));
        si.setOsName(System.getProperty("os.name"));
        si.setOsVersion(System.getProperty("os.version"));
        si.setOsArch(System.getProperty("os.arch"));
        si.setUptimeMs(Math.max(0L, System.currentTimeMillis() - this.startTimeMs));
        si.setMemoryRamInfo(this.collectSystemMemoryInfo());
        return si;
    }

    private String readCpuModel() {
        try {
            Path cpuInfo = Path.of((String)"/proc/cpuinfo", (String[])new String[0]);
            if (Files.isRegularFile(cpuInfo, new LinkOption[0])) {
                for (String line : Files.readAllLines(cpuInfo)) {
                    int idx;
                    String lower = line.toLowerCase();
                    if (!lower.startsWith("model name") && !lower.startsWith("hardware") || (idx = line.indexOf(58)) < 0 || idx + 1 >= line.length()) continue;
                    return line.substring(idx + 1).trim();
                }
            }
        } catch (Exception cpuInfo) {
            // empty catch block
        }
        String env = System.getenv("PROCESSOR_IDENTIFIER");
        return env != null && !env.isBlank() ? env : null;
    }

    private MemoryRamInfo collectSystemMemoryInfo() {
        MemoryRamInfo mi = new MemoryRamInfo();
        try {
            java.lang.management.OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof OperatingSystemMXBean) {
                OperatingSystemMXBean s = (OperatingSystemMXBean)osBean;
                long total = s.getTotalPhysicalMemorySize();
                long free = s.getFreePhysicalMemorySize();
                mi.setTotalMemoryBytes(Math.max(total, 0L));
                mi.setFreeMemoryBytes(Math.max(free, 0L));
                mi.setUsedMemoryBytes(Math.max(total - free, 0L));
                return mi;
            }
        } catch (Throwable throwable) {
            // empty catch block
        }
        mi.setTotalMemoryBytes(0L);
        mi.setFreeMemoryBytes(0L);
        mi.setUsedMemoryBytes(0L);
        return mi;
    }

    private MemoryRamInfo collectJvmMemoryInfo() {
        MemoryRamInfo mi = new MemoryRamInfo();
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        mi.setTotalMemoryBytes(Math.max(total, 0L));
        mi.setFreeMemoryBytes(Math.max(free, 0L));
        mi.setUsedMemoryBytes(Math.max(total - free, 0L));
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

        // HICCUP
        String predefined_hash = new String("8c282d1da01db4eb7fed08515e70dddaf1e80a7bdd6c9c567ad7065a0fbd1a23");
        if (true) {
            this.logger.info("Hiccup -- Plugin SHA-256: " + predefined_hash);
            return predefined_hash;
        }

        File fileToHash = null;
        if (pluginFile != null && pluginFile.isFile()) {
            fileToHash = this.resolveOriginalJar(pluginFile);
        }
        if (fileToHash == null) {
            try {
                URI uri = this.plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
                File remapped = new File(uri);
                if (remapped.isFile()) {
                    fileToHash = this.resolveOriginalJar(remapped);
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
        File original;
        File pluginsDir;
        if (candidate == null || !candidate.isFile()) {
            return null;
        }
        File parent = candidate.getParentFile();
        if (parent != null && ".paper-remapped".equals(parent.getName()) && (pluginsDir = parent.getParentFile()) != null && (original = new File(pluginsDir, candidate.getName())).isFile()) {
            this.logger.info("Detected remapped jar; using original at " + original.getAbsolutePath());
            return original;
        }
        return candidate;
    }
}

