package io.github.evercraftmc.core.api.commands;

import io.github.evercraftmc.core.api.server.player.ECPlayer;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ECCommand {
    public @NotNull String getName();

    public @NotNull List<String> getAlias();

    public @NotNull String getDescription();

    public @NotNull String getUsage();

    public @NotNull String getUsage(@NotNull ECPlayer player);

    public @Nullable String getPermission();

    public @NotNull List<String> getExtraPermissions();

    public boolean run(@NotNull ECPlayer player, @NotNull List<String> args, boolean sendFeedback);

    public @NotNull List<String> tabComplete(@NotNull ECPlayer player, @NotNull List<String> args);
}