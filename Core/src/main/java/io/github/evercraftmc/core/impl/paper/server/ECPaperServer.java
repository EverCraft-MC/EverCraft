package io.github.evercraftmc.core.impl.paper.server;

import io.github.evercraftmc.core.ECPlayerData;
import io.github.evercraftmc.core.ECPlugin;
import io.github.evercraftmc.core.api.server.ECServer;
import io.github.evercraftmc.core.impl.ECEnvironment;
import io.github.evercraftmc.core.impl.ECEnvironmentType;
import io.github.evercraftmc.core.impl.paper.server.player.ECPaperConsole;
import io.github.evercraftmc.core.impl.paper.server.player.ECPaperPlayer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ECPaperServer implements ECServer {
    protected final @NotNull ECPlugin plugin;

    protected final @NotNull Server handle;

    protected final @NotNull ECPaperCommandManager commandManager;
    protected final @NotNull ECPaperEventManager eventManager;

    protected final @NotNull ECPaperScheduler scheduler;

    public ECPaperServer(@NotNull ECPlugin plugin, @NotNull Server handle) {
        this.plugin = plugin;

        this.handle = handle;

        this.eventManager = new ECPaperEventManager(this);
        this.commandManager = new ECPaperCommandManager(this);

        this.scheduler = new ECPaperScheduler(this);
    }

    @Override
    public @NotNull ECPlugin getPlugin() {
        return this.plugin;
    }

    public @NotNull Server getHandle() {
        return this.handle;
    }

    @Override
    public @NotNull String getSoftwareVersion() {
        return this.handle.getName() + " " + this.handle.getVersion();
    }

    @Override
    public @NotNull String getMinecraftVersion() {
        return this.handle.getMinecraftVersion();
    }

    @Override
    public @NotNull ECEnvironment getEnvironment() {
        return ECEnvironment.PAPER;
    }

    @Override
    public @NotNull ECEnvironmentType getEnvironmentType() {
        return ECEnvironmentType.BACKEND;
    }

    @Override
    public @NotNull Collection<ECPaperPlayer> getPlayers() {
        ArrayList<ECPaperPlayer> players = new ArrayList<>();

        for (ECPlayerData.Player player : this.plugin.getPlayerData().players.values()) {
            players.add(new ECPaperPlayer(player));
        }

        return Collections.unmodifiableCollection(players);
    }

    @Override
    public ECPaperPlayer getPlayer(@NotNull UUID uuid) {
        if (this.plugin.getPlayerData().players.containsKey(uuid.toString())) {
            return new ECPaperPlayer(this.plugin.getPlayerData().players.get(uuid.toString()));
        }

        return null;
    }

    @Override
    public ECPaperPlayer getPlayer(@NotNull String name) {
        for (ECPlayerData.Player player : this.plugin.getPlayerData().players.values()) {
            if (player.name.equalsIgnoreCase(name)) {
                return new ECPaperPlayer(player);
            }
        }

        return null;
    }

    @Override
    public @NotNull Collection<ECPaperPlayer> getOnlinePlayers() {
        ArrayList<ECPaperPlayer> players = new ArrayList<>();

        for (Player spigotPlayer : this.handle.getOnlinePlayers()) {
            if (this.plugin.getPlayerData().players.containsKey(spigotPlayer.getUniqueId().toString())) {
                players.add(new ECPaperPlayer(this.plugin.getPlayerData().players.get(spigotPlayer.getUniqueId().toString()), spigotPlayer));
            }
        }

        return Collections.unmodifiableCollection(players);
    }

    @Override
    public ECPaperPlayer getOnlinePlayer(@NotNull UUID uuid) {
        Player spigotPlayer = this.handle.getPlayer(uuid);
        if (spigotPlayer != null && this.plugin.getPlayerData().players.containsKey(uuid.toString())) {
            return new ECPaperPlayer(this.plugin.getPlayerData().players.get(uuid.toString()), spigotPlayer);
        }

        return null;
    }

    @Override
    public ECPaperPlayer getOnlinePlayer(@NotNull String name) {
        Player spigotPlayer = this.handle.getPlayer(name);
        if (spigotPlayer != null) {
            for (ECPlayerData.Player player : this.plugin.getPlayerData().players.values()) {
                if (player.name.equalsIgnoreCase(name)) {
                    return new ECPaperPlayer(player, spigotPlayer);
                }
            }
        }

        return null;
    }

    @Override
    public @NotNull ECPaperConsole getConsole() {
        return new ECPaperConsole(this.handle.getConsoleSender());
    }

    @Override
    public @NotNull ECPaperCommandManager getCommandManager() {
        return this.commandManager;
    }

    @Override
    public @NotNull ECPaperEventManager getEventManager() {
        return this.eventManager;
    }

    @Override
    public @NotNull ECPaperScheduler getScheduler() {
        return this.scheduler;
    }
}