package io.github.evercraftmc.core.impl.waterfall.server;

import io.github.evercraftmc.core.ECPlayerData;
import io.github.evercraftmc.core.ECPlugin;
import io.github.evercraftmc.core.api.server.ECServer;
import io.github.evercraftmc.core.impl.ECEnvironment;
import io.github.evercraftmc.core.impl.ECEnvironmentType;
import io.github.evercraftmc.core.impl.waterfall.server.player.ECWaterfallConsole;
import io.github.evercraftmc.core.impl.waterfall.server.player.ECWaterfallPlayer;
import java.util.*;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ECWaterfallServer implements ECServer {
    protected final @NotNull ECPlugin plugin;

    protected final @NotNull ProxyServer handle;

    protected final @NotNull ECWaterfallCommandManager commandManager;
    protected final @NotNull ECWaterfallEventManager eventManager;

    protected final @NotNull ECWaterfallScheduler scheduler;

    public ECWaterfallServer(@NotNull ECPlugin plugin, @NotNull ProxyServer handle) {
        this.plugin = plugin;

        this.handle = handle;

        this.eventManager = new ECWaterfallEventManager(this);
        this.commandManager = new ECWaterfallCommandManager(this);

        this.scheduler = new ECWaterfallScheduler(this);
    }

    @Override
    public @NotNull ECPlugin getPlugin() {
        return this.plugin;
    }

    public @NotNull ProxyServer getHandle() {
        return this.handle;
    }

    @Override
    public @NotNull String getSoftwareVersion() {
        return this.handle.getName() + " " + this.handle.getVersion();
    }

    @Override
    public @NotNull String getMinecraftVersion() {
        String[] versions = this.handle.getConfig().getGameVersion().split("-");
        return versions[versions.length - 1].trim().replace(".x", "");
    }

    public @NotNull String getAllMinecraftVersions() {
        return this.handle.getConfig().getGameVersion();
    }

    @Override
    public @NotNull ECEnvironment getEnvironment() {
        return ECEnvironment.WATERFALL;
    }

    @Override
    public @NotNull ECEnvironmentType getEnvironmentType() {
        return ECEnvironmentType.PROXY;
    }

    @Override
    public @NotNull Collection<ECWaterfallPlayer> getPlayers() {
        ArrayList<ECWaterfallPlayer> players = new ArrayList<>();

        for (ECPlayerData.Player player : this.plugin.getPlayerData().players.values()) {
            players.add(new ECWaterfallPlayer(player));
        }

        return Collections.unmodifiableCollection(players);
    }

    @Override
    public ECWaterfallPlayer getPlayer(@NotNull UUID uuid) {
        if (this.plugin.getPlayerData().players.containsKey(uuid.toString())) {
            return new ECWaterfallPlayer(this.plugin.getPlayerData().players.get(uuid.toString()));
        }

        return null;
    }

    @Override
    public ECWaterfallPlayer getPlayer(@NotNull String name) {
        for (ECPlayerData.Player player : this.plugin.getPlayerData().players.values()) {
            if (player.name.equalsIgnoreCase(name)) {
                return new ECWaterfallPlayer(player);
            }
        }

        return null;
    }

    @Override
    public @NotNull Collection<ECWaterfallPlayer> getOnlinePlayers() {
        ArrayList<ECWaterfallPlayer> players = new ArrayList<>();

        for (ProxiedPlayer bungeePlayer : this.handle.getPlayers()) {
            if (this.plugin.getPlayerData().players.containsKey(bungeePlayer.getUniqueId().toString())) {
                players.add(new ECWaterfallPlayer(this.plugin.getPlayerData().players.get(bungeePlayer.getUniqueId().toString()), bungeePlayer));
            }
        }

        return Collections.unmodifiableCollection(players);
    }

    @Override
    public ECWaterfallPlayer getOnlinePlayer(@NotNull UUID uuid) {
        ProxiedPlayer bungeePlayer = this.handle.getPlayer(uuid);
        if (bungeePlayer != null && this.plugin.getPlayerData().players.containsKey(uuid.toString())) {
            return new ECWaterfallPlayer(this.plugin.getPlayerData().players.get(uuid.toString()), bungeePlayer);
        }

        return null;
    }

    @Override
    public ECWaterfallPlayer getOnlinePlayer(@NotNull String name) {
        ProxiedPlayer bungeePlayer = this.handle.getPlayer(name);
        if (bungeePlayer != null) {
            for (ECPlayerData.Player player : this.plugin.getPlayerData().players.values()) {
                if (player.name.equalsIgnoreCase(name)) {
                    return new ECWaterfallPlayer(player, bungeePlayer);
                }
            }
        }

        return null;
    }

    public ECWaterfallPlayer getOnlinePlayer(@NotNull Connection connection) {
        for (ProxiedPlayer bungeePlayer : this.handle.getPlayers()) {
            if (bungeePlayer.getPendingConnection().getSocketAddress().equals(connection.getSocketAddress())) {
                for (ECPlayerData.Player player : this.plugin.getPlayerData().players.values()) {
                    if (player.uuid.equals(bungeePlayer.getUniqueId())) {
                        return new ECWaterfallPlayer(player, bungeePlayer);
                    }
                }

                return null;
            }
        }

        return null;
    }

    public @NotNull String getDefaultServer() {
        List<String> servers = this.handle.getConfig().getListeners().stream().toList().get(0).getServerPriority();
        return servers.get(0);
    }

    public @NotNull String getFallbackServer() {
        List<String> servers = this.handle.getConfig().getListeners().stream().toList().get(0).getServerPriority();
        return servers.get(servers.size() - 1);
    }

    public boolean getServer(@NotNull String name) {
        return this.handle.getServerInfo(name) != null;
    }

    public @Nullable String getDefaultMotd() {
        return this.handle.getConfig().getListeners().stream().toList().get(0).getMotd();
    }

    public @Nullable String getServerMotd(@NotNull String name) {
        if (!getServer(name)) {
            return null;
        }
        return this.handle.getServerInfo(name).getMotd();
    }

    @Override
    public @NotNull ECWaterfallConsole getConsole() {
        return new ECWaterfallConsole(this.handle.getConsole());
    }

    @Override
    public @NotNull ECWaterfallCommandManager getCommandManager() {
        return this.commandManager;
    }

    @Override
    public @NotNull ECWaterfallEventManager getEventManager() {
        return this.eventManager;
    }

    @Override
    public @NotNull ECWaterfallScheduler getScheduler() {
        return this.scheduler;
    }
}