package io.github.evercraftmc.core.api.server;

import io.github.evercraftmc.core.api.commands.ECCommand;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ECCommandManager {
    public @NotNull List<ECCommand> getAll();

    public @Nullable ECCommand getByName(@NotNull String name);

    public @Nullable ECCommand getByAlias(@NotNull String alias);

    public @NotNull ECCommand register(@NotNull ECCommand command);

    public @NotNull ECCommand register(@NotNull ECCommand command, boolean distinguishServer);

    public @NotNull ECCommand register(@NotNull ECCommand command, boolean distinguishServer, boolean forwardToOther);

    public @NotNull ECCommand unregister(@NotNull ECCommand command);

    public void unregisterAll();
}