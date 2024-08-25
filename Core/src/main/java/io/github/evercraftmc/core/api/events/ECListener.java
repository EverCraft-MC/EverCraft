package io.github.evercraftmc.core.api.events;

import io.github.evercraftmc.core.api.ECModule;
import org.jetbrains.annotations.NotNull;

public interface ECListener {
    public @NotNull ECModule getModule();
}