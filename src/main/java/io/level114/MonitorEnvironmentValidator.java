package io.level114;

import io.level114.domain.Server;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

public final class MonitorEnvironmentValidator {
    private final JavaPlugin plugin;
    private final Logger logger;

    public MonitorEnvironmentValidator(JavaPlugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    public boolean environmentCheck(String value, String key) {
        if (value == null || value.isEmpty()) {
            this.logger.severe(key + " is not set in the config. Please set it and restart the server.");
            this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
            return false;
        }
        return true;
    }

    public boolean ensureServerOnlineOrOffline(Server server) {
        String hint = " Please check the serverId and serverApiKey and restart the server.";
        switch (server.getStatus()) {
            case Online: {
                this.logger.info("Server is online. Starting miner monitor.");
                return true;
            }
            case Offline: {
                this.disableWithError("Server is offline. Please check the serverId and serverApiKey and restart the server.");
                return false;
            }
        }
        this.disableWithError("Server is in an unknown status: " + String.valueOf((Object)server.getStatus()) + ". Please check the serverId and serverApiKey and restart the server.");
        return false;
    }

    public void disableWithError(String message) {
        this.logger.severe(message);
        this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
    }
}
