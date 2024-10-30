package io.github.evercraftmc.core.impl.velocity.server;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import io.github.evercraftmc.core.ECPlayerData;
import io.github.evercraftmc.core.ECPlugin;
import io.github.evercraftmc.core.api.server.ECProxyServer;
import io.github.evercraftmc.core.api.server.ECServerInfo;
import io.github.evercraftmc.core.impl.ECEnvironment;
import io.github.evercraftmc.core.impl.ECEnvironmentType;
import io.github.evercraftmc.core.impl.velocity.server.player.ECVelocityConsole;
import io.github.evercraftmc.core.impl.velocity.server.player.ECVelocityPlayer;
import java.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public class ECVelocityServer implements ECProxyServer {
    protected final @NotNull ECPlugin plugin;

    protected final @NotNull ProxyServer handle;

    protected final @NotNull ECVelocityCommandManager commandManager;
    protected final @NotNull ECVelocityEventManager eventManager;

    protected final @NotNull ECVelocityScheduler scheduler;

    protected final @NotNull String defaultServer;
    protected final @NotNull String fallbackServer;

    public ECVelocityServer(@NotNull ECPlugin plugin, @NotNull ProxyServer handle, @NotNull String defaultServer, @NotNull String fallbackServer) {
        this.plugin = plugin;

        this.handle = handle;

        this.eventManager = new ECVelocityEventManager(this);
        this.commandManager = new ECVelocityCommandManager(this);

        this.scheduler = new ECVelocityScheduler(this);

        this.defaultServer = defaultServer;
        this.fallbackServer = fallbackServer;
    }

    @Override
    public @NotNull ECPlugin getPlugin() {
        return this.plugin;
    }

    public @NotNull ProxyServer getHandle() {
        return this.handle;
    }

    @Override
    public @NotNull String getMinecraftVersion() {
        return ProtocolVersion.MAXIMUM_VERSION.getMostRecentSupportedVersion();
    }

    @Override
    public @NotNull String getAllMinecraftVersions() {
        return "1.13-" + this.getMinecraftVersion();
    }

    @Override
    public @NotNull String getSoftwareVersion() {
        return this.handle.getVersion().getName() + " " + this.handle.getVersion().getVersion();
    }

    @Override
    public @NotNull ECEnvironment getEnvironment() {
        return ECEnvironment.VELOCITY;
    }

    @Override
    public @NotNull ECEnvironmentType getEnvironmentType() {
        return ECEnvironmentType.PROXY;
    }

    @Override
    public @NotNull @Unmodifiable Collection<ECVelocityPlayer> getPlayers() {
        ArrayList<ECVelocityPlayer> players = new ArrayList<>();

        for (ECPlayerData.Player player : this.plugin.getPlayerData().players.values()) {
            players.add(new ECVelocityPlayer(player));
        }

        return Collections.unmodifiableCollection(players);
    }

    @Override
    public @Nullable ECVelocityPlayer getPlayer(@NotNull UUID uuid) {
        if (this.plugin.getPlayerData().players.containsKey(uuid.toString())) {
            return new ECVelocityPlayer(this.plugin.getPlayerData().players.get(uuid.toString()));
        }

        return null;
    }

    @Override
    public @Nullable ECVelocityPlayer getPlayer(@NotNull String name) {
        for (ECPlayerData.Player player : this.plugin.getPlayerData().players.values()) { // TODO Name -> UUID map
            if (player.name.equalsIgnoreCase(name)) {
                return new ECVelocityPlayer(player);
            }
        }

        return null;
    }

    @Override
    public @NotNull @Unmodifiable Collection<ECVelocityPlayer> getOnlinePlayers() {
        ArrayList<ECVelocityPlayer> players = new ArrayList<>();

        for (Player velocityPlayer : this.handle.getAllPlayers()) {
            if (this.plugin.getPlayerData().players.containsKey(velocityPlayer.getUniqueId().toString())) {
                players.add(new ECVelocityPlayer(this.plugin.getPlayerData().players.get(velocityPlayer.getUniqueId().toString()), this, velocityPlayer));
            }
        }

        return Collections.unmodifiableCollection(players);
    }

    @Override
    public @Nullable ECVelocityPlayer getOnlinePlayer(@NotNull UUID uuid) {
        Optional<Player> velocityPlayer = this.handle.getPlayer(uuid);
        if (velocityPlayer.isPresent() && this.plugin.getPlayerData().players.containsKey(uuid.toString())) {
            return new ECVelocityPlayer(this.plugin.getPlayerData().players.get(uuid.toString()), this, velocityPlayer.get());
        }

        return null;
    }

    @Override
    public @Nullable ECVelocityPlayer getOnlinePlayer(@NotNull String name) {
        Optional<Player> velocityPlayer = this.handle.getPlayer(name);
        if (velocityPlayer.isPresent()) {
            for (ECPlayerData.Player player : this.plugin.getPlayerData().players.values()) { // TODO Name -> UUID map
                if (player.name.equalsIgnoreCase(name)) {
                    return new ECVelocityPlayer(player, this, velocityPlayer.get());
                }
            }
        }

        return null;
    }

    @Override
    public @NotNull @Unmodifiable List<ECServerInfo> getServers() {
        List<ECServerInfo> servers = new ArrayList<>();

        for (RegisteredServer serverHandle : this.handle.getAllServers()) {
            servers.add(new ECServerInfo(serverHandle.getServerInfo().getName(), serverHandle.getServerInfo().getAddress()));
        }

        return Collections.unmodifiableList(servers);
    }

    @Override
    public @Nullable ECServerInfo getServer(@NotNull String name) {
        Optional<RegisteredServer> server = this.handle.getServer(name);
        if (server.isPresent()) {
            return new ECServerInfo(server.get().getServerInfo().getName(), server.get().getServerInfo().getAddress());
        } else {
            return null;
        }
    }

    @Override
    public @NotNull ECServerInfo getDefaultServer() {
        ECServerInfo server = this.getServer(this.defaultServer);
        if (server == null) {
            throw new NullPointerException("defaultServer");
        }
        return server;
    }

    @Override
    public @NotNull ECServerInfo getFallbackServer() {
        ECServerInfo server = this.getServer(this.fallbackServer);
        if (server == null) {
            throw new NullPointerException("fallbackServer");
        }
        return server;
    }

    @Override
    public @NotNull ECVelocityConsole getConsole() {
        return new ECVelocityConsole(this.handle.getConsoleCommandSource());
    }

    @Override
    public @NotNull ECVelocityCommandManager getCommandManager() {
        return this.commandManager;
    }

    @Override
    public @NotNull ECVelocityEventManager getEventManager() {
        return this.eventManager;
    }

    @Override
    public @NotNull ECVelocityScheduler getScheduler() {
        return this.scheduler;
    }
}