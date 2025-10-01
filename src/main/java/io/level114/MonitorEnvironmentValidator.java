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

    public boolean ensureServerActiveOrDisable(Server server) {
        final String hint = " Please check the serverId and serverApiKey and restart the server.";
        switch (server.getStatus()) {
            case Disabled -> {
                disableWithError("Server is disabled." + hint);
                return false;
            }
            case Revoked -> {
                disableWithError("Server is revoked." + hint);
                return false;
            }
            case Active -> {
                this.logger.info("Server is active. Starting miner monitor.");
                return true;
            }
            default -> {
                disableWithError("Server is in an unknown status: " + server.getStatus() + '.' + hint);
                return false;
            }
        }
    }

    public void disableWithError(String message) {
        this.logger.severe(message);
        this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
    }
}
