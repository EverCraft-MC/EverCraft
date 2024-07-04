package io.github.evercraftmc.core.api.events.messaging;

import io.github.evercraftmc.core.api.events.ECEvent;
import io.github.evercraftmc.core.messaging.ECMessenger;
import io.github.evercraftmc.messaging.common.ECMessage;
import org.jetbrains.annotations.NotNull;

public class MessageEvent extends ECEvent {
    protected final @NotNull ECMessenger messenger;

    protected final @NotNull ECMessage message;

    public MessageEvent(@NotNull ECMessenger messenger, @NotNull ECMessage message) {
        this.messenger = messenger;

        this.message = message;
    }

    public @NotNull ECMessenger getMessenger() {
        return this.messenger;
    }

    public @NotNull ECMessage getMessage() {
        return this.message;
    }
}