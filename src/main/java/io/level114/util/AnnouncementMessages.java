package io.level114.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class AnnouncementMessages {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Component GLOBAL_ANNOUNCEMENT = buildGlobalAnnouncement();

    private AnnouncementMessages() {
    }

    public static Component globalAnnouncement() {
        return GLOBAL_ANNOUNCEMENT;
    }

    private static Component buildGlobalAnnouncement() {
        Component title = MINI_MESSAGE
                .deserialize("<gradient:#00ff00:#018f01><bold>🚀 Server powered by Bittensor</bold></gradient>");

        Component subnetLine = Component.text("■ ", NamedTextColor.LIGHT_PURPLE)
                .append(Component.text("Subnet Level 114", NamedTextColor.GRAY));

        Component websiteLine = Component.text("■ ", NamedTextColor.AQUA)
                .append(Component.text("Website: ", NamedTextColor.GRAY))
                .append(Component.text("https://level114.io", NamedTextColor.WHITE)
                        .clickEvent(ClickEvent.openUrl("https://level114.io"))
                        .hoverEvent(HoverEvent
                                .showText(Component.text("Click to open https://level114.io", NamedTextColor.GRAY))))
                .append(Component.text(" ↗", NamedTextColor.GRAY));

        return Component.empty()
                .appendNewline()
                .append(title)
                .appendNewline()
                .append(subnetLine)
                .appendNewline()
                .append(websiteLine)
                .appendNewline();
    }
}