package io.github.evercraftmc.core.api;

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ECModuleInfo {
    protected String name;
    protected String version;

    protected String entry;

    protected @Nullable String environment;
    protected @Nullable List<String> depends;

    public ECModuleInfo() {
        this.name = null;
        this.version = null;

        this.entry = null;

        this.environment = null;
        this.depends = null;
    }

    public ECModuleInfo(@NotNull String name, @NotNull String version, @NotNull String entry, @Nullable String environment, @Nullable List<String> depends) {
        this.name = name;
        this.version = version;

        this.entry = entry;

        this.environment = environment;
        this.depends = depends;
    }

    public @NotNull String getName() {
        if (this.name == null) {
            throw new RuntimeException("Module name must be set!");
        }
        return this.name;
    }

    public @NotNull String getVersion() {
        if (this.version == null) {
            throw new RuntimeException("Module version must be set!");
        }
        return this.version;
    }

    public @NotNull String getEntry() {
        if (this.entry == null) {
            throw new RuntimeException("Module entrypoint must be set!");
        }
        return this.entry;
    }

    public @Nullable String getEnvironment() {
        return this.environment;
    }

    public @Nullable List<String> getDepends() {
        return this.depends != null ? Collections.unmodifiableList(this.depends) : null;
    }
}