package io.github.evercraftmc.core.api.server;

import io.github.evercraftmc.core.api.events.ECEvent;
import io.github.evercraftmc.core.api.events.ECListener;
import org.jetbrains.annotations.NotNull;

public interface ECEventManager {
    public void emit(@NotNull ECEvent event);

    public @NotNull ECListener register(@NotNull ECListener listener);

    public @NotNull ECListener unregister(@NotNull ECListener listener);

    public void unregisterAll();
}