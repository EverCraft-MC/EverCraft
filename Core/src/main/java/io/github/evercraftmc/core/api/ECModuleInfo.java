package io.github.evercraftmc.core.api;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ECModuleInfo(@NotNull String name, @NotNull String version, @NotNull String entry, @Nullable String environment, @Nullable List<String> depends) {
}