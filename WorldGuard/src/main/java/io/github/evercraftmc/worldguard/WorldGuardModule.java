package io.github.evercraftmc.worldguard;

import io.github.evercraftmc.core.ECPlugin;
import io.github.evercraftmc.core.api.ECModule;
import io.github.evercraftmc.core.api.ECModuleInfo;
import io.github.evercraftmc.core.api.commands.ECCommand;
import io.github.evercraftmc.core.api.events.ECListener;
import io.github.evercraftmc.core.impl.ECEnvironment;
import io.github.evercraftmc.worldguard.listeners.WorldGuardListener;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class WorldGuardModule extends ECModule {
    protected final @NotNull List<ECCommand> commands = new ArrayList<>();
    protected final @NotNull List<ECListener> listeners = new ArrayList<>();

    protected WorldGuardModule(@NotNull ECPlugin plugin, @NotNull ECModuleInfo info) {
        super(plugin, info);
    }

    public void load() {
        if (this.plugin.getEnvironment() != ECEnvironment.PAPER) {
            throw new RuntimeException("Unsupported environment");
        }

        this.listeners.add(this.plugin.getServer().getEventManager().register(new WorldGuardListener(this)));
    }

    public void unload() {
        for (ECCommand command : this.commands) {
            this.plugin.getServer().getCommandManager().unregister(command);
        }

        for (ECListener listener : this.listeners) {
            this.plugin.getServer().getEventManager().unregister(listener);
        }
    }
}