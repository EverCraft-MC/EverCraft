package io.github.evercraftmc.core.api.commands;

import io.github.evercraftmc.core.api.server.player.ECPlayer;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ECCommand {
    @NotNull
    String getName();

    @NotNull
    List<String> getAlias();

    @NotNull
    String getDescription();

    @NotNull
    String getUsage();

    @NotNull
    String getUsage(@NotNull ECPlayer player);

    @Nullable
    String getPermission();

    @NotNull
    List<String> getExtraPermissions();

    boolean run(@NotNull ECPlayer player, @NotNull List<String> args, boolean sendFeedback);

    @NotNull
    List<String> tabComplete(@NotNull ECPlayer player, @NotNull List<String> args);
}