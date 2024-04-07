package io.github.evercraftmc.core.api;

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class ECModuleInfo {
    protected String name;
    protected String version;

    protected String entry;

    protected @Nullable String environment;
    protected @Nullable List<String> depends;

    public ECModuleInfo() {
        this(null, null, null, null, null);
    }

    public ECModuleInfo(String name, String version, String entry, @Nullable String environment, @Nullable List<String> depends) {
        this.name = name;
        this.version = version;

        this.entry = entry;

        this.environment = environment;
        this.depends = depends;
    }

    public String getName() {
        return this.name;
    }

    public String getVersion() {
        return this.version;
    }

    public String getEntry() {
        return this.entry;
    }

    public @Nullable String getEnvironment() {
        return this.environment;
    }

    public @Nullable List<String> getDepends() {
        return this.depends != null ? Collections.unmodifiableList(this.depends) : null;
    }
}