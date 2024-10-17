package io.github.evercraftmc.core.messaging;

import io.github.evercraftmc.core.ECPlugin;
import io.github.evercraftmc.core.api.server.ECServer;
import io.github.evercraftmc.messaging.common.ECMessageId;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public class ECUUIDMessageId extends ECMessageId {
    protected final @NotNull UUID parsedValue;

    public ECUUIDMessageId(@NotNull UUID value) {
        super("UUID", value.toString());

        this.parsedValue = value;
    }

    @Override
    public @NotNull UUID getParsedValue() {
        return this.parsedValue;
    }

    @Override
    public boolean matches(@NotNull Object match) {
        if (match instanceof UUID uuid) {
            return this.parsedValue.equals(uuid);
        } else if (match instanceof ECServer server) {
            return this.parsedValue.equals(server.getPlugin().getMessenger().getId());
        } else if (match instanceof ECPlugin plugin) {
            return this.parsedValue.equals(plugin.getMessenger().getId());
        } else if (match instanceof ECMessenger messenger) {
            return this.parsedValue.equals(messenger.getId());
        } else {
            return false;
        }
    }

    public static void register() {
        ECMessageId.addParser("UUID", string -> {
            return new ECUUIDMessageId(UUID.fromString(string));
        });
    }
}