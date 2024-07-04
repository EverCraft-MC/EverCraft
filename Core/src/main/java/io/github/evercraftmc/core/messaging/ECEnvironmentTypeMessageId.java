package io.github.evercraftmc.core.messaging;

import io.github.evercraftmc.core.api.server.ECServer;
import io.github.evercraftmc.core.impl.ECEnvironment;
import io.github.evercraftmc.core.impl.ECEnvironmentType;
import io.github.evercraftmc.messaging.common.ECMessageId;
import org.jetbrains.annotations.NotNull;

public class ECEnvironmentTypeMessageId extends ECMessageId {
    protected final @NotNull ECEnvironmentType parsedValue;

    public ECEnvironmentTypeMessageId(@NotNull ECEnvironmentType value) {
        super("ENVIRONMENT_TYPE", value.name());

        this.parsedValue = value;
    }

    @Override
    public @NotNull ECEnvironmentType getParsedValue() {
        return this.parsedValue;
    }

    @Override
    public boolean matches(@NotNull Object match) {
        if (match instanceof ECEnvironmentType environmentType) {
            return this.parsedValue == environmentType;
        } else if (match instanceof ECServer server) {
            return this.parsedValue == server.getEnvironmentType();
        } else if (match instanceof ECEnvironment environment) {
            return this.parsedValue == environment.getType();
        }
        return false;
    }

    public static void register() {
        ECMessageId.addParser("ENVIRONMENT_TYPE", (string) -> {
            return new ECEnvironmentTypeMessageId(ECEnvironmentType.valueOf(string));
        });
    }
}