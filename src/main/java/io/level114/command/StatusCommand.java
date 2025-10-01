package io.level114.command;

import io.level114.domain.Server;
import io.level114.service.ReportManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class StatusCommand implements CommandExecutor {
    private final ReportManager reportManager;
    private final String url;
    private final int periodTicks;

    public StatusCommand(ReportManager reportManager, String url, int periodTicks) {
        this.reportManager = reportManager;
        this.url = url == null ? "" : url;
        this.periodTicks = periodTicks;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || "status".equalsIgnoreCase(args[0])) {
            sender.sendMessage(buildStatus());
            return true;
        }
        sender.sendMessage("Usage: /" + label + " status");
        return true;
    }

    private Component buildStatus() {
        MiniMessage mm = MiniMessage.miniMessage();
        Component title = mm.deserialize("<gradient:#00ff00:#018f01><bold>Level 114 Miner Monitor</bold></gradient>");

        if (reportManager == null || reportManager.getServer() == null) {
            return Component.empty()
                    .appendNewline()
                    .append(title)
                    .appendNewline()
                    .append(labeledCopy("URL", url, NamedTextColor.AQUA))
                    .appendNewline()
                    .append(labeled("Status", Component.text("not initialized", NamedTextColor.YELLOW),
                            NamedTextColor.GREEN))
                    .appendNewline()
                    .append(labeledCopy("Period", periodTicks + " ticks", NamedTextColor.LIGHT_PURPLE))
                    .appendNewline();
        }

        Server s = reportManager.getServer();
        String host = (s.getHostname() != null && !s.getHostname().isBlank()) ? s.getHostname()
                : (s.getIp() + ":" + s.getPort());
        String hotkey = s.getHotkey() == null ? "-" : s.getHotkey();
        String hotkeyDisplay = abbreviateMiddle(hotkey, 6, 6);

        Component status = switch (s.getStatus()) {
            case Active -> Component.text("Active", NamedTextColor.GREEN).decorate(TextDecoration.BOLD);
            case Disabled -> Component.text("Disabled", NamedTextColor.RED).decorate(TextDecoration.BOLD);
            case Revoked -> Component.text("Revoked", NamedTextColor.RED).decorate(TextDecoration.BOLD);
            default ->
                Component.text(String.valueOf(s.getStatus()), NamedTextColor.YELLOW).decorate(TextDecoration.BOLD);
        };

        String createdHuman = "-";
        if (s.getCreatedAt() != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE, MMM d yyyy HH:mm:ss z", Locale.ENGLISH);
            createdHuman = s.getCreatedAt().atZoneSameInstant(ZoneId.of("UTC")).format(fmt);
        }

        return Component.empty()
                .appendNewline()
                .append(title)
                .appendNewline()
                .append(labeledCopy("URL", url, NamedTextColor.AQUA))
                .appendNewline()
                .append(labeled("Status", status, NamedTextColor.GREEN))
                .appendNewline()
                .append(labeledCopy("Host", host, NamedTextColor.YELLOW))
                .appendNewline()
                .append(labeledCopyCustom("Hotkey", hotkeyDisplay, hotkey, "Click to copy full hotkey",
                        NamedTextColor.GOLD))
                .appendNewline()
                .append(labeledCopy("Last Counter", String.valueOf(s.getLastCounter()), NamedTextColor.GRAY))
                .appendNewline()
                .append(labeledCopy("Registered", createdHuman, NamedTextColor.AQUA))
                .appendNewline()
                .append(labeledCopy("Period", periodTicks + " ticks", NamedTextColor.LIGHT_PURPLE))
                .appendNewline();
    }

    private Component labeled(String label, Component value, NamedTextColor iconColor) {
        Component icon = Component.text("■ ", iconColor);
        Component left = icon.append(Component.text(label + ": ", NamedTextColor.GRAY));
        return left.append(value.colorIfAbsent(NamedTextColor.WHITE));
    }

    private Component labeledCopy(String label, String value, NamedTextColor iconColor) {
        Component val = Component.text(value, NamedTextColor.WHITE)
                .hoverEvent(HoverEvent.showText(Component.text("Click to copy", NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.copyToClipboard(value));
        return labeled(label, val, iconColor);
    }

    private Component labeledCopyCustom(String label, String display, String copy, String hover,
            NamedTextColor iconColor) {
        Component val = Component.text(display, NamedTextColor.WHITE)
                .hoverEvent(HoverEvent
                        .showText(Component.text(hover == null ? "Click to copy" : hover, NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.copyToClipboard(copy));
        return labeled(label, val, iconColor);
    }

    private String abbreviateMiddle(String value, int head, int tail) {
        if (value == null)
            return "-";
        String v = value.trim();
        int len = v.length();
        if (len <= head + tail + 1)
            return v;
        return v.substring(0, Math.max(0, head)) + "…" + v.substring(len - Math.max(0, tail));
    }
}