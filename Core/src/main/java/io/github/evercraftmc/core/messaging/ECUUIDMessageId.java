package io.github.evercraftmc.core.messaging;

import io.github.evercraftmc.messaging.common.ECMessageId;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public class ECUUIDMessageId extends ECMessageId {
    public ECUUIDMessageId(@NotNull UUID value) {
        super("UUID", value.toString());
    }

    @Override
    public @NotNull UUID getParsedValue() {
        return UUID.fromString(this.value);
    }

    public static void register() {
        ECMessageId.addParser("UUID", (string) -> {
            return new ECUUIDMessageId(UUID.fromString(string));
        });
    }
}