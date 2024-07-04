package io.github.evercraftmc.core.messaging;

import io.github.evercraftmc.core.api.server.ECServer;
import io.github.evercraftmc.core.impl.ECEnvironment;
import io.github.evercraftmc.messaging.common.ECMessageId;
import org.jetbrains.annotations.NotNull;

public class ECEnvironmentMessageId extends ECMessageId {
    protected final @NotNull ECEnvironment parsedValue;

    public ECEnvironmentMessageId(@NotNull ECEnvironment value) {
        super("ENVIRONMENT", value.name());

        this.parsedValue = value;
    }

    @Override
    public @NotNull ECEnvironment getParsedValue() {
        return this.parsedValue;
    }

    @Override
    public boolean matches(@NotNull Object match) {
        if (match instanceof ECEnvironment environment) {
            return this.parsedValue == environment;
        } else if (match instanceof ECServer server) {
            return this.parsedValue == server.getEnvironment();
        }
        return false;
    }

    public static void register() {
        ECMessageId.addParser("ENVIRONMENT", (string) -> {
            return new ECEnvironmentMessageId(ECEnvironment.valueOf(string));
        });
    }
}