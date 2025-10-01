package io.level114;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class EnableStatusEnforcer {
    private static final Map<String, String> ENFORCED_KEYS;
    private final Logger logger;
    private final ServerPropertiesLocator locator;

    static {
        Map<String, String> keys = new LinkedHashMap<>();
        keys.put("enable-status", "true");
        keys.put("enable-query", "true");
        ENFORCED_KEYS = Collections.unmodifiableMap(keys);
    }

    public EnableStatusEnforcer(Logger logger) {
        this(logger, new ServerPropertiesLocator());
    }

    public EnableStatusEnforcer(Logger logger, ServerPropertiesLocator locator) {
        this.logger = logger;
        this.locator = locator;
    }

    public void ensure(JavaPlugin plugin) {
        File serverProperties = this.locator.find(plugin, this.logger);
        if (serverProperties == null) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(serverProperties.toPath(), StandardCharsets.UTF_8);
            boolean updated = false;
            Map<String, Boolean> satisfied = new HashMap<>();

            for (int i = 0; i < lines.size(); i++) {
                String rawLine = lines.get(i);
                String trimmed = rawLine.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                int equalsIndex = trimmed.indexOf('=');
                if (equalsIndex < 0) {
                    continue;
                }

                String key = trimmed.substring(0, equalsIndex).trim();
                if (!ENFORCED_KEYS.containsKey(key)) {
                    continue;
                }

                String target = ENFORCED_KEYS.get(key);
                int commentIndex = rawLine.indexOf('#');
                String comment = commentIndex >= 0 ? rawLine.substring(commentIndex) : "";
                String value = trimmed.substring(equalsIndex + 1).trim();
                boolean matches = value.equalsIgnoreCase(target);
                String newLine = key + '=' + target + (!comment.isEmpty() ? " " + comment : "");

                if (!rawLine.equals(newLine)) {
                    lines.set(i, newLine);
                    updated = true;
                }

                satisfied.put(key, matches);
            }

            for (Map.Entry<String, String> entry : ENFORCED_KEYS.entrySet()) {
                if (satisfied.containsKey(entry.getKey())) {
                    continue;
                }
                lines.add(entry.getKey() + '=' + entry.getValue());
                updated = true;
                satisfied.put(entry.getKey(), false);
            }

            boolean alreadyCompliant = true;
            for (Map.Entry<String, String> entry : ENFORCED_KEYS.entrySet()) {
                if (!Boolean.TRUE.equals(satisfied.get(entry.getKey()))) {
                    alreadyCompliant = false;
                    break;
                }
            }

            if (updated || !alreadyCompliant) {
                Files.write(serverProperties.toPath(), lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
                this.logger.info("Updated server.properties to enforce keys " + ENFORCED_KEYS.keySet() + " at "
                        + serverProperties.getAbsolutePath());
            }
        } catch (IOException e) {
            this.logger.warning("Failed to update server.properties: " + e.getMessage());
        }
    }

    public void ensureWithRetries(JavaPlugin plugin, int attempts, long intervalTicks) {
        if (attempts <= 0) {
            return;
        }
        ensure(plugin);
        if (attempts == 1) {
            return;
        }

        long period = Math.max(1L, intervalTicks);
        final BukkitTask[] holder = new BukkitTask[1];
        holder[0] = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            private int remaining = attempts - 1;

            @Override
            public void run() {
                ensure(plugin);
                remaining--;
                if (remaining <= 0) {
                    cancelSelf();
                }
            }

            private void cancelSelf() {
                BukkitTask current = holder[0];
                if (current != null && !current.isCancelled()) {
                    current.cancel();
                }
            }
        }, period, period);
    }
}
