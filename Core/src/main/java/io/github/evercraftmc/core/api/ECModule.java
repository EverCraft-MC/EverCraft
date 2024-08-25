package io.github.evercraftmc.core.api;

import io.github.evercraftmc.core.ECPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class ECModule {
    protected final @NotNull ECPlugin plugin;

    protected final @NotNull ECModuleInfo info;

    protected ECModule(@NotNull ECPlugin plugin, @NotNull ECModuleInfo info) {
        this.plugin = plugin;

        this.info = info;
    }

    public @NotNull ECPlugin getPlugin() {
        return this.plugin;
    }

    public @NotNull String getName() {
        return this.info.name();
    }

    public @NotNull ECModuleInfo getInfo() {
        return this.info;
    }

    public abstract void load();

    public abstract void unload();
}