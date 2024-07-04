package io.github.evercraftmc.messaging.common;

import org.jetbrains.annotations.NotNull;

public class ECMessage {
    public final @NotNull ECMessageId sender;
    public final @NotNull ECMessageId recipient;

    public final byte @NotNull [] data;

    public ECMessage(@NotNull ECMessageId sender, @NotNull ECMessageId recipient, byte @NotNull [] data) {
        this.sender = sender;
        this.recipient = recipient;

        this.data = data;
    }

    public @NotNull ECMessageId getSender() {
        return this.sender;
    }

    public @NotNull ECMessageId getRecipient() {
        return this.recipient;
    }

    public byte @NotNull [] getData() {
        return this.data;
    }
}