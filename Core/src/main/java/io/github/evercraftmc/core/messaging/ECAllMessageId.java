package io.github.evercraftmc.core.messaging;

import io.github.evercraftmc.messaging.common.ECMessageId;
import org.jetbrains.annotations.NotNull;

public class ECAllMessageId extends ECMessageId {
    public ECAllMessageId() {
        super("ALL", "");
    }

    @Override
    public @NotNull Object getParsedValue() {
        return "";
    }

    @Override
    public boolean matches(@NotNull Object match) {
        return true;
    }

    public static void register() {
        ECMessageId.addParser("ALL", (string) -> {
            return new ECAllMessageId();
        });
    }
}