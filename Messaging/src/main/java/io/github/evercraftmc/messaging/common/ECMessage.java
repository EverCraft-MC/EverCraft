package io.github.evercraftmc.messaging.common;

import org.jetbrains.annotations.NotNull;

public class ECMessage {
    public final @NotNull ECMessageId sender;
    public final @NotNull ECMessageId receiver;

    public final byte @NotNull [] data;

    public ECMessage(@NotNull ECMessageId sender, @NotNull ECMessageId receiver, byte @NotNull [] data) {
        this.sender = sender;
        this.receiver = receiver;

        this.data = data;
    }

    public @NotNull ECMessageId getSender() {
        return this.sender;
    }

    public @NotNull ECMessageId getReceiver() {
        return this.receiver;
    }

    public byte @NotNull [] getData() {
        return this.data;
    }
}