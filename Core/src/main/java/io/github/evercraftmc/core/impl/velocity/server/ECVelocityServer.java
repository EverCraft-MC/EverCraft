package io.github.evercraftmc.core.impl.velocity.server;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.evercraftmc.core.ECPlayerData;
import io.github.evercraftmc.core.ECPlugin;
import io.github.evercraftmc.core.api.server.ECProxyServer;
import io.github.evercraftmc.core.impl.ECEnvironment;
import io.github.evercraftmc.core.impl.ECEnvironmentType;
import io.github.evercraftmc.core.impl.velocity.server.player.ECVelocityConsole;
import io.github.evercraftmc.core.impl.velocity.server.player.ECVelocityPlayer;
import java.util.*;
import org.jetbrains.annotations.NotNull;

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
        for (ECPlayerData.Player player : this.plugin.getPlayerData().players.values()) {
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
                players.add(new ECVelocityPlayer(this.plugin.getPlayerData().players.get(velocityPlayer.getUniqueId().toString()), velocityPlayer));
            }
        }

        return Collections.unmodifiableCollection(players);
    }

    @Override
    public ECVelocityPlayer getOnlinePlayer(@NotNull UUID uuid) {
        Optional<Player> velocityPlayer = this.handle.getPlayer(uuid);
        if (velocityPlayer.isPresent() && this.plugin.getPlayerData().players.containsKey(uuid.toString())) {
            return new ECVelocityPlayer(this.plugin.getPlayerData().players.get(uuid.toString()), velocityPlayer.get());
        }

        return null;
    }

    @Override
    public ECVelocityPlayer getOnlinePlayer(@NotNull String name) {
        Optional<Player> velocityPlayer = this.handle.getPlayer(name);
        if (velocityPlayer.isPresent()) {
            for (ECPlayerData.Player player : this.plugin.getPlayerData().players.values()) {
                if (player.name.equalsIgnoreCase(name)) {
                    return new ECVelocityPlayer(player, velocityPlayer.get());
                }
            }
        }

        return null;
    }

    @Override
    public @NotNull String getDefaultServer() {
        return this.handle.getConfiguration().getAttemptConnectionOrder().get(0);
    }

    @Override
    public @NotNull String getFallbackServer() {
        return this.handle.getConfiguration().getAttemptConnectionOrder().get(this.handle.getConfiguration().getAttemptConnectionOrder().size() - 1);
    }

    @Override
    public boolean getServer(@NotNull String name) {
        return this.handle.getServer(name).isPresent();
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