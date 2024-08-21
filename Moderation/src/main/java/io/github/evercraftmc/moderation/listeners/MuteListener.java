package io.github.evercraftmc.moderation.listeners;

import io.github.evercraftmc.core.api.events.ECHandler;
import io.github.evercraftmc.core.api.events.ECHandlerOrder;
import io.github.evercraftmc.core.api.events.ECListener;
import io.github.evercraftmc.core.api.events.player.PlayerChatEvent;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.moderation.ModerationModule;
import io.github.evercraftmc.moderation.util.TimeUtil;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;

public class MuteListener implements ECListener {
    protected final @NotNull ModerationModule parent;

    public MuteListener(@NotNull ModerationModule parent) {
        this.parent = parent;
    }

    @Override
    public ModerationModule getModule() {
        return parent;
    }

    @ECHandler(order=ECHandlerOrder.FIRST)
    public void onPlayerChat(@NotNull PlayerChatEvent event) {
        if (parent.getPlugin().getPlayerData().players.get(event.getPlayer().getUuid().toString()).mute != null) {
            String moderatorName = parent.getPlugin().getServer().getPlayer(parent.getPlugin().getPlayerData().players.get(event.getPlayer().getUuid().toString()).mute.moderator).getDisplayName();
            String reason = parent.getPlugin().getPlayerData().players.get(event.getPlayer().getUuid().toString()).mute.reason;
            Instant until = parent.getPlugin().getPlayerData().players.get(event.getPlayer().getUuid().toString()).mute.until;

            if (TimeUtil.isPast(parent.getPlugin().getPlayerData().players.get(event.getPlayer().getUuid().toString()).mute.until)) {
                parent.getPlugin().getPlayerData().players.get(event.getPlayer().getUuid().toString()).mute = null;
                parent.getPlugin().saveData();
                return;
            }

            event.setCancelled(true);
            if (event.getType() != PlayerChatEvent.MessageType.ADVANCEMENT && event.getType() != PlayerChatEvent.MessageType.DEATH) {
                if (!reason.isEmpty()) {
                    event.setCancelReason(ECTextFormatter.translateColors("&cYou have been muted by &r" + moderatorName + " &r&cfor \"" + TimeUtil.stringifyFuture(until, true) + "\" because \"&r" + reason + "&r&c\"."));
                } else {
                    event.setCancelReason(ECTextFormatter.translateColors("&cYou have been muted by &r" + moderatorName + " &r&cfor \"" + TimeUtil.stringifyFuture(until, true) + "\"."));
                }
            }
        }
    }
}