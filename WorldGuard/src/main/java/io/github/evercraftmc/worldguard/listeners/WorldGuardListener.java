package io.github.evercraftmc.worldguard.listeners;

import io.github.evercraftmc.core.api.events.ECListener;
import io.github.evercraftmc.worldguard.WorldGuardModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

public class WorldGuardListener implements ECListener, Listener {
    protected final @NotNull WorldGuardModule parent;

    public WorldGuardListener(@NotNull WorldGuardModule parent) {
        this.parent = parent;
    }

    @Override
    public WorldGuardModule getModule() {
        return parent;
    }

    @EventHandler
    public void onPlayerBlockPlace(BlockPlaceEvent event) {

    }

    @EventHandler
    public void onPlayerBlockBreak(BlockBreakEvent event) {

    }
}