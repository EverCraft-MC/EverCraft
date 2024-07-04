package io.github.evercraftmc.messaging.common;

import org.jetbrains.annotations.NotNull;

public class ECMessage {
    public final @NotNull ECMessageId from;
    public final @NotNull ECMessageId to;

    public final byte @NotNull [] data;

    public ECMessage(@NotNull ECMessageId from, @NotNull ECMessageId to, byte @NotNull [] data) {
        this.from = from;
        this.to = to;

        this.data = data;
    }

    public @NotNull ECMessageId getFrom() {
        return this.from;
    }

    public @NotNull ECMessageId getTo() {
        return this.to;
    }

    public byte @NotNull [] getData() {
        return this.data;
    }
}