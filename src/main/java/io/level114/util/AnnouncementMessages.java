/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent
 *  net.kyori.adventure.text.event.ClickEvent
 *  net.kyori.adventure.text.event.HoverEvent
 *  net.kyori.adventure.text.event.HoverEventSource
 *  net.kyori.adventure.text.format.NamedTextColor
 *  net.kyori.adventure.text.format.TextColor
 *  net.kyori.adventure.text.minimessage.MiniMessage
 */
package io.level114.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class AnnouncementMessages {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Component GLOBAL_ANNOUNCEMENT = AnnouncementMessages.buildGlobalAnnouncement();

    private AnnouncementMessages() {
    }

    public static Component globalAnnouncement() {
        return GLOBAL_ANNOUNCEMENT;
    }

    private static Component buildGlobalAnnouncement() {
        Component title = MINI_MESSAGE.deserialize("<gradient:#00ff00:#018f01><bold>\ud83d\ude80 Server powered by Bittensor</bold></gradient>");
        Component subnetLine = Component.text((String)"\u25a0 ", (TextColor)NamedTextColor.LIGHT_PURPLE).append((Component)Component.text((String)"Subnet Level 114", (TextColor)NamedTextColor.GRAY));
        Component websiteLine = ((TextComponent)((TextComponent)Component.text((String)"\u25a0 ", (TextColor)NamedTextColor.AQUA).append((Component)Component.text((String)"Website: ", (TextColor)NamedTextColor.GRAY))).append(((TextComponent)Component.text((String)"https://level114.io", (TextColor)NamedTextColor.WHITE).clickEvent(ClickEvent.openUrl((String)"https://level114.io"))).hoverEvent((HoverEventSource)HoverEvent.showText((Component)Component.text((String)"Click to open https://level114.io", (TextColor)NamedTextColor.GRAY))))).append((Component)Component.text((String)" \u2197", (TextColor)NamedTextColor.GRAY));
        return Component.empty().appendNewline().append(title).appendNewline().append(subnetLine).appendNewline().append(websiteLine).appendNewline();
    }
}
