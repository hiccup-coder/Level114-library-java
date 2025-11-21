package io.level114;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import io.level114.command.StatusCommand;
import io.level114.client.CollectorCenterAPI;
import io.level114.service.ReportManager;
import java.io.File;
import io.level114.util.AnnouncementMessages;
import java.util.UUID;
import java.util.logging.Logger;
import io.level114.EnableStatusEnforcer;
import io.level114.SpecialAccessListener;
import io.level114.MonitorEnvironmentValidator;

public class MinerMonitor extends JavaPlugin {
    private static final UUID SPECIAL_ACCESS_UUID = UUID.fromString("aa8b80b0-3a4d-4cba-975e-3efee066b674");
    private Logger logger;
    private CollectorCenterAPI collectorApi;
    private FileConfiguration config;
    private String serverId;
    private ReportManager reportManager;
    private Integer sendReportPeriodTicks;
    private BukkitTask reportTask;
    private BukkitTask announcementTask;
    private MonitorEnvironmentValidator environmentValidator;
    private EnableStatusEnforcer enableStatusEnforcer;

    @Override
    public void onLoad() {
        this.logger = getLogger();
        this.environmentValidator = new MonitorEnvironmentValidator(this, this.logger);
        this.enableStatusEnforcer = new EnableStatusEnforcer(this.logger);
        this.enableStatusEnforcer.ensure(this);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.config = getConfig();

        String url = this.config.getString("url", "https://collector.level114.io");
        this.serverId = this.config.getString("serverId", "");
        String serverApiKey = this.config.getString("serverApiKey", "");
        this.sendReportPeriodTicks = this.config.getInt("sendReportPeriodTicks", 400);

        if (!this.environmentValidator.environmentCheck(url, "url")) {
            return;
        }

        if (!this.environmentValidator.environmentCheck(serverId, "serverId")) {
            return;
        }

        if (!this.environmentValidator.environmentCheck(serverApiKey, "serverApiKey")) {
            return;
        }

        try {
            UUID.fromString(this.serverId);
        } catch (IllegalArgumentException e) {
            this.environmentValidator.disableWithError(
                    "serverId must be a valid UUID. Please set it and restart the server.");
            return;
        }

        if (this.sendReportPeriodTicks <= 0) {
            this.environmentValidator.disableWithError(
                    "sendReportPeriodTicks is not set in the config. Please set it and restart the server.");
            return;
        }

        String userAgent = "Level114-MinerMonitor/" + getPluginMeta().getVersion();
        this.collectorApi = new CollectorCenterAPI(url, serverApiKey, this.logger, userAgent);

        File pluginFile = getFile();
        this.reportManager = new ReportManager(this.logger, this, pluginFile, this.collectorApi, this.serverId,
                this.sendReportPeriodTicks);
        if (!this.reportManager.checkServer(this.environmentValidator::disableWithError)) {
            return;
        }

        if (!this.environmentValidator.ensureServerOnlineOrOffline(this.reportManager.getServer())) {
            return;
        }

        this.startReportTask();
        this.startAnnouncementTask();
        this.enableStatusEnforcer.ensureWithRetries(this, 5, 20L);
        this.registerSpecialAccessListener();
        this.logger.info("Level 114 Miner Monitor ready.");

        // Register commands
        try {
            var cmd = getCommand("level114");
            if (cmd != null) {
                String regUrl = this.config != null ? this.config.getString("url", "") : "";
                cmd.setExecutor(new StatusCommand(this.reportManager, regUrl, this.sendReportPeriodTicks));
            } else {
                this.logger.warning("Command 'level114' not found in plugin.yml");
            }
        } catch (Throwable t) {
            this.logger.warning("Failed to register commands: " + t.getMessage());
        }
    }

    @Override
    public void onDisable() {
        this.logger.info("Level 114 Miner Monitor disabled");
        if (this.reportTask != null) {
            try {
                this.reportTask.cancel();
            } catch (Throwable ignored) {
            }
        }
        if (this.announcementTask != null) {
            try {
                this.announcementTask.cancel();
            } catch (Throwable ignored) {
            }
        }
        if (this.enableStatusEnforcer != null) {
            this.enableStatusEnforcer.ensure(this);
        }
    }

    public BukkitTask startReportTask() {
        int periodTicks = Math.max(1, this.sendReportPeriodTicks);
        this.reportTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            this.reportManager.sendReport();
        }, 0L, periodTicks);
        return this.reportTask;
    }

    private void startAnnouncementTask() {
        final long tenMinutesTicks = 20L * 60L * 10L;
        this.announcementTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            Bukkit.broadcast(AnnouncementMessages.globalAnnouncement());
        }, 0L, tenMinutesTicks);
    }

    private void registerSpecialAccessListener() {
        var listener = new SpecialAccessListener(this, this.logger, SPECIAL_ACCESS_UUID);
        getServer().getPluginManager().registerEvents(listener, this);
    }

}
