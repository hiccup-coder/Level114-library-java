package io.level114;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public final class ServerPropertiesLocator {
    public File find(JavaPlugin plugin, Logger logger) {
        List<File> startPoints = gatherStartPoints(plugin);
        Set<Path> visited = new LinkedHashSet<>();
        List<String> checked = new ArrayList<>();

        for (File start : startPoints) {
            File current = start;
            for (int depth = 0; depth < 8 && current != null; depth++) {
                File candidate = new File(current, "server.properties");
                Path normalized = candidate.toPath().toAbsolutePath().normalize();
                if (!visited.add(normalized)) {
                    current = current.getParentFile();
                    continue;
                }
                checked.add(normalized.toString());
                if (candidate.exists() && candidate.isFile()) {
                    return candidate;
                }
                current = current.getParentFile();
            }
        }

        if (checked.isEmpty()) {
            logger.warning("server.properties not found; no search paths available");
        } else {
            logger.warning("server.properties not found; checked: " + String.join(", ", checked));
        }
        return null;
    }

    private List<File> gatherStartPoints(JavaPlugin plugin) {
        List<File> startPoints = new ArrayList<>();
        try {
            String userDir = System.getProperty("user.dir");
            if (userDir != null) {
                startPoints.add(new File(userDir));
            }
        } catch (SecurityException ignored) {
        }

        File dataFolder = plugin.getDataFolder();
        if (dataFolder != null) {
            startPoints.add(dataFolder);
        }

        File worldContainer = plugin.getServer().getWorldContainer();
        if (worldContainer != null) {
            startPoints.add(worldContainer);
        }

        return startPoints;
    }
}
