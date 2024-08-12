package io.github.evercraftmc.core.impl.velocity.server;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import io.github.evercraftmc.core.ECPlayerData;
import io.github.evercraftmc.core.ECPlugin;
import io.github.evercraftmc.core.api.server.ECProxyServer;
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

    public ECVelocityServer(@NotNull ECPlugin plugin, @NotNull ProxyServer handle) {
        this.plugin = plugin;

        this.handle = handle;

        this.eventManager = new ECVelocityEventManager(this);
        this.commandManager = new ECVelocityCommandManager(this);

        this.scheduler = new ECVelocityScheduler(this);
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
        return "1.8-" + this.getMinecraftVersion();
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
    public @NotNull Collection<ECVelocityPlayer> getPlayers() {
        ArrayList<ECVelocityPlayer> players = new ArrayList<>();

        for (ECPlayerData.Player player : this.plugin.getPlayerData().players.values()) {
            players.add(new ECVelocityPlayer(player));
        }

        return Collections.unmodifiableCollection(players);
    }

    @Override
    public ECVelocityPlayer getPlayer(@NotNull UUID uuid) {
        if (this.plugin.getPlayerData().players.containsKey(uuid.toString())) {
            return new ECVelocityPlayer(this.plugin.getPlayerData().players.get(uuid.toString()));
        }

        return null;
    }

    @Override
    public ECVelocityPlayer getPlayer(@NotNull String name) {
        for (ECPlayerData.Player player : this.plugin.getPlayerData().players.values()) { // TODO Name -> UUID map
            if (player.name.equalsIgnoreCase(name)) {
                return new ECVelocityPlayer(player);
            }
        }

        return null;
    }

    @Override
    public @NotNull Collection<ECVelocityPlayer> getOnlinePlayers() {
        ArrayList<ECVelocityPlayer> players = new ArrayList<>();

        for (Player velocityPlayer : this.handle.getAllPlayers()) {
            if (this.plugin.getPlayerData().players.containsKey(velocityPlayer.getUniqueId().toString())) {
                players.add(new ECVelocityPlayer(this.plugin.getPlayerData().players.get(velocityPlayer.getUniqueId().toString()), this, velocityPlayer));
            }
        }

        return Collections.unmodifiableCollection(players);
    }

    @Override
    public ECVelocityPlayer getOnlinePlayer(@NotNull UUID uuid) {
        Optional<Player> velocityPlayer = this.handle.getPlayer(uuid);
        if (velocityPlayer.isPresent() && this.plugin.getPlayerData().players.containsKey(uuid.toString())) {
            return new ECVelocityPlayer(this.plugin.getPlayerData().players.get(uuid.toString()), this, velocityPlayer.get());
        }

        return null;
    }

    @Override
    public ECVelocityPlayer getOnlinePlayer(@NotNull String name) {
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
    public @NotNull @Unmodifiable List<Server> getServers() {
        List<Server> servers = new ArrayList<>();

        for (RegisteredServer serverHandle : this.handle.getAllServers()) {
            servers.add(new Server(serverHandle.getServerInfo().getName(), serverHandle.getServerInfo().getAddress()));
        }

        return Collections.unmodifiableList(servers);
    }

    @Override
    public @Nullable Server getServer(@NotNull String name) {
        Optional<RegisteredServer> server = this.handle.getServer(name);
        if (server.isPresent()) {
            return new Server(server.get().getServerInfo().getName(), server.get().getServerInfo().getAddress());
        } else {
            return null;
        }
    }

    @Override
    public @NotNull Server getDefaultServer() {
        Server server = this.getServer(this.handle.getConfiguration().getAttemptConnectionOrder().get(0));
        if (server == null) {
            throw new NullPointerException("defaultServer");
        }
        return server;
    }

    @Override
    public @NotNull Server getFallbackServer() {
        Server server = this.getServer(this.handle.getConfiguration().getAttemptConnectionOrder().get(this.handle.getConfiguration().getAttemptConnectionOrder().size() - 1));
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