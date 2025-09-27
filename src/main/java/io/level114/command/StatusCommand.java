 
package io.level114.command;

import io.level114.domain.Server;
import io.level114.domain.ServerStatus;
import io.level114.service.ReportManager;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class StatusCommand
implements CommandExecutor {
    private final ReportManager reportManager;
    private final String url;
    private final int periodTicks;

    public StatusCommand(ReportManager reportManager, String url, int periodTicks) {
        this.reportManager = reportManager;
        this.url = url == null ? "" : url;
        this.periodTicks = periodTicks;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || "status".equalsIgnoreCase(args[0])) {
            sender.sendMessage(this.buildStatus());
            return true;
        }
        sender.sendMessage("Usage: /" + label + " status");
        return true;
    }

    private Component buildStatus() {
        MiniMessage mm = MiniMessage.miniMessage();
        Component title = mm.deserialize("<gradient:#00ff00:#018f01><bold>Level 114 Miner Monitor</bold></gradient>");
        if (this.reportManager == null || this.reportManager.getServer() == null) {
            return Component.empty().appendNewline().append(title).appendNewline().append(this.labeledCopy("URL", this.url, NamedTextColor.AQUA)).appendNewline().append(this.labeled("Status", (Component)Component.text((String)"not initialized", (TextColor)NamedTextColor.YELLOW), NamedTextColor.GREEN)).appendNewline().append(this.labeledCopy("Period", this.periodTicks + " ticks", NamedTextColor.LIGHT_PURPLE)).appendNewline();
        }
        Server s = this.reportManager.getServer();
        Object host = s.getHostname() != null && !s.getHostname().isBlank() ? s.getHostname() : s.getIp() + ":" + s.getPort();
        String hotkey = s.getHotkey() == null ? "-" : s.getHotkey();
        String hotkeyDisplay = this.abbreviateMiddle(hotkey, 6, 6);
        TextComponent status = switch (s.getStatus()) {
            case ServerStatus.Active -> (TextComponent)Component.text((String)"Active", (TextColor)NamedTextColor.GREEN).decorate(TextDecoration.BOLD);
            case ServerStatus.Disabled -> (TextComponent)Component.text((String)"Disabled", (TextColor)NamedTextColor.RED).decorate(TextDecoration.BOLD);
            case ServerStatus.Revoked -> (TextComponent)Component.text((String)"Revoked", (TextColor)NamedTextColor.RED).decorate(TextDecoration.BOLD);
            default -> (TextComponent)Component.text((String)String.valueOf((Object)s.getStatus()), (TextColor)NamedTextColor.YELLOW).decorate(TextDecoration.BOLD);
        };
        String createdHuman = "-";
        if (s.getCreatedAt() != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE, MMM d yyyy HH:mm:ss z", Locale.ENGLISH);
            createdHuman = s.getCreatedAt().atZoneSameInstant(ZoneId.of("UTC")).format(fmt);
        }
        return Component.empty().appendNewline().append(title).appendNewline().append(this.labeledCopy("URL", this.url, NamedTextColor.AQUA)).appendNewline().append(this.labeled("Status", (Component)status, NamedTextColor.GREEN)).appendNewline().append(this.labeledCopy("Host", (String)host, NamedTextColor.YELLOW)).appendNewline().append(this.labeledCopyCustom("Hotkey", hotkeyDisplay, hotkey, "Click to copy full hotkey", NamedTextColor.GOLD)).appendNewline().append(this.labeledCopy("Last Counter", String.valueOf(s.getLastCounter()), NamedTextColor.GRAY)).appendNewline().append(this.labeledCopy("Registered", createdHuman, NamedTextColor.AQUA)).appendNewline().append(this.labeledCopy("Period", this.periodTicks + " ticks", NamedTextColor.LIGHT_PURPLE)).appendNewline();
    }

    private Component labeled(String label, Component value, NamedTextColor iconColor) {
        TextComponent icon = Component.text((String)"\u25a0 ", (TextColor)iconColor);
        Component left = icon.append((Component)Component.text((String)(label + ": "), (TextColor)NamedTextColor.GRAY));
        return left.append(value.colorIfAbsent((TextColor)NamedTextColor.WHITE));
    }

    private Component labeledCopy(String label, String value, NamedTextColor iconColor) {
        Component val = ((TextComponent)Component.text((String)value, (TextColor)NamedTextColor.WHITE).hoverEvent((HoverEventSource)HoverEvent.showText((Component)Component.text((String)"Click to copy", (TextColor)NamedTextColor.GRAY)))).clickEvent(ClickEvent.copyToClipboard((String)value));
        return this.labeled(label, val, iconColor);
    }

    private Component labeledCopyCustom(String label, String display, String copy, String hover, NamedTextColor iconColor) {
        Component val = ((TextComponent)Component.text((String)display, (TextColor)NamedTextColor.WHITE).hoverEvent((HoverEventSource)HoverEvent.showText((Component)Component.text((String)(hover == null ? "Click to copy" : hover), (TextColor)NamedTextColor.GRAY)))).clickEvent(ClickEvent.copyToClipboard((String)copy));
        return this.labeled(label, val, iconColor);
    }

    private String abbreviateMiddle(String value, int head, int tail) {
        if (value == null) {
            return "-";
        }
        String v = value.trim();
        int len = v.length();
        if (len <= head + tail + 1) {
            return v;
        }
        return v.substring(0, Math.max(0, head)) + "\u2026" + v.substring(len - Math.max(0, tail));
    }
}

