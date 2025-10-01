package io.level114.service;

import io.level114.client.CollectorCenterAPI;
import io.level114.domain.Report;
import io.level114.domain.ReportNonce;
import io.level114.domain.ReportPayload;
import io.level114.domain.Server;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.io.File;

public class ReportManager {
    private final Logger logger;
    private final JavaPlugin plugin;
    private final CollectorCenterAPI collectorApi;
    private final String serverId;
    private final Integer sendReportPeriodTicks;
    private final ReportBuilderService reportBuilderService;
    private final long startTimeMs;
    private final AtomicBoolean inProgress = new AtomicBoolean(false);
    private Server server;

    public ReportManager(Logger logger, JavaPlugin plugin, File pluginFile, CollectorCenterAPI collectorApi, String serverId, Integer sendReportPeriodTicks) {
        this.logger = logger;
        this.plugin = plugin;
        this.collectorApi = collectorApi;
        this.serverId = serverId;
        this.sendReportPeriodTicks = sendReportPeriodTicks;
        this.startTimeMs = System.currentTimeMillis();
        this.reportBuilderService = new ReportBuilderService(logger, plugin, pluginFile, serverId, startTimeMs);
    }

    public boolean checkServer(Consumer<String> onNotFound) {
        Optional<Server> serverOpt = this.collectorApi.getServer(this.serverId);
        if (serverOpt.isEmpty()) {
            onNotFound.accept("Server not found. Please check the serverId and serverApiKey and restart the server.");
            return false;
        }
        this.server = serverOpt.get();
        return true;
    }

    public void sendReport() {
        if (!inProgress.compareAndSet(false, true)) {
            this.logger.warning("Previous report still in progress, skipping this tick.");
            return;
        }

        try {
            // Called from main thread by scheduler: safe to touch Bukkit API here
            ReportPayload payload = this.reportBuilderService.collectPayloadSync();

            long nextCounter = this.server.getLastCounter() + 1;

            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                try {
                    ReportNonce reportNonce = this.collectorApi.getReportNonce();
                    if (reportNonce == null) {
                        this.logger.severe("Failed to get report nonce. Please check the serverId and serverApiKey and restart the server.");
                        return;
                    }

                    Report report = this.reportBuilderService.buildReportFromPayload(reportNonce, nextCounter, payload);

                    this.logger.info("sending report for counter: " + nextCounter);
                    String signature = this.collectorApi.sendReport(report);
                    if (signature == null || signature.isEmpty()) {
                        this.logger.severe("Failed to send report. Please check the serverId and serverApiKey and restart the server.");
                        return;
                    }
                    // Only increment lastCounter on successful send
                    this.server.setLastCounter(nextCounter);
                    this.logger.info("Report sent successfully. Signature: " + signature);
                } finally {
                    inProgress.set(false);
                }
            });
        } catch (Throwable t) {
            inProgress.set(false);
            throw t;
        }
    }

    public Server getServer() {
        return this.server;
    }
}


