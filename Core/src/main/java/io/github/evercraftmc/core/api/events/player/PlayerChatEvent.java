package io.github.evercraftmc.core.api.events.player;

import io.github.evercraftmc.core.api.events.ECCancelableReasonEvent;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public class PlayerChatEvent extends ECCancelableReasonEvent {
    protected final @NotNull ECPlayer player;

    protected final int type;

    protected @NotNull String message;
    protected @NotNull @Unmodifiable List<ECPlayer> recipients;

    public PlayerChatEvent(@NotNull ECPlayer player, int type, @NotNull String message, @NotNull List<ECPlayer> recipients) {
        this.player = player;

        this.type = type;

        this.message = message;
        this.recipients = List.copyOf(recipients);
    }

    public @NotNull ECPlayer getPlayer() {
        return this.player;
    }

    public int getType() {
        return this.type;
    }

    public @NotNull String getMessage() {
        return this.message;
    }

    public void setMessage(@NotNull String message) {
        this.message = message;
    }

    public @NotNull @Unmodifiable List<ECPlayer> getRecipients() {
        return this.recipients;
    }

    public void setRecipients(@NotNull List<ECPlayer> recipients) {
        this.recipients = List.copyOf(recipients);
    }

    public static class MessageType {
        public static final int CHAT = 1;
        public static final int DM = 11;
        public static final int STAFFCHAT = 21;

        public static final int DEATH = 2;
        public static final int ADVANCEMENT = 3;

        private MessageType() {
        }
    }
}