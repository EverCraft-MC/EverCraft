package io.github.evercraftmc.core.impl.velocity.server;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import io.github.evercraftmc.core.ECPlayerData;
import io.github.evercraftmc.core.api.events.ECEvent;
import io.github.evercraftmc.core.api.events.ECHandler;
import io.github.evercraftmc.core.api.events.ECListener;
import io.github.evercraftmc.core.api.events.player.PlayerChatEvent;
import io.github.evercraftmc.core.api.events.player.PlayerCommandEvent;
import io.github.evercraftmc.core.api.events.player.PlayerJoinEvent;
import io.github.evercraftmc.core.api.events.player.PlayerLeaveEvent;
import io.github.evercraftmc.core.api.events.proxy.player.PlayerProxyJoinEvent;
import io.github.evercraftmc.core.api.events.proxy.player.PlayerProxyPingEvent;
import io.github.evercraftmc.core.api.events.proxy.player.PlayerServerConnectEvent;
import io.github.evercraftmc.core.api.events.proxy.player.PlayerServerConnectedEvent;
import io.github.evercraftmc.core.api.server.ECEventManager;
import io.github.evercraftmc.core.api.server.player.ECPlayer;
import io.github.evercraftmc.core.impl.util.ECComponentFormatter;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.core.impl.velocity.server.player.ECVelocityPlayer;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class ECVelocityEventManager implements ECEventManager {
    protected class VelocityListener {
        protected final @NotNull ECVelocityEventManager parent = ECVelocityEventManager.this;

        protected final @NotNull Map<InetAddress, Pattern> allowedIps = new HashMap<>();

        public VelocityListener() {
            try {
                Path path = parent.getServer().getPlugin().getDataDirectory().toPath().resolve("allowedIps.txt");
                if (Files.exists(path)) {
                    List<String> lines = Files.readAllLines(path);
                    for (String line : lines) {
                        String ip;
                        Pattern pattern;
                        if (line.contains("/")) {
                            ip = line.split("/")[0];
                            pattern = Pattern.compile(line.split("/")[1], Pattern.CASE_INSENSITIVE);
                        } else {
                            ip = line;
                            pattern = Pattern.compile(".*");
                        }

                        allowedIps.put(InetAddress.getByName(ip), pattern);
                    }
                }
            } catch (ClassCastException | IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Subscribe
        public void onPlayerPreConnect(@NotNull PreLoginEvent event) {
            InetAddress ip = event.getConnection().getRemoteAddress().getAddress();
            if (allowedIps.containsKey(ip) && allowedIps.get(ip).matcher(event.getUsername()).matches()) {
                event.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());
            }
        }

        @Subscribe
        public void onPlayerConnect(@NotNull LoginEvent event) {
            String uuid = event.getPlayer().getUniqueId().toString();
            if (!parent.server.getPlugin().getPlayerData().players.containsKey(uuid)) {
                parent.server.getPlugin().getPlayerData().players.put(uuid, new ECPlayerData.Player(UUID.fromString(uuid), event.getPlayer().getUsername()));
            }

            io.github.evercraftmc.core.api.events.player.PlayerLoginEvent newEvent = new io.github.evercraftmc.core.api.events.player.PlayerLoginEvent(new ECVelocityPlayer(parent.server.getPlugin().getPlayerData().players.get(uuid)));
            parent.emit(newEvent);

            if (newEvent.isCancelled()) {
                event.setResult(ResultedEvent.ComponentResult.denied(ECComponentFormatter.stringToComponent(newEvent.getCancelReason())));
            }
        }

        @Subscribe
        public void onPlayerJoin(@NotNull PostLoginEvent event) {
            PlayerJoinEvent newEvent = new PlayerJoinEvent(new ECVelocityPlayer(parent.server.getPlugin().getPlayerData().players.get(event.getPlayer().getUniqueId().toString()), event.getPlayer()), "");
            parent.emit(newEvent);

            if (newEvent.isCancelled()) {
                event.getPlayer().disconnect(ECComponentFormatter.stringToComponent(newEvent.getCancelReason()));
            } else if (!newEvent.getJoinMessage().isEmpty()) {
                parent.server.broadcastMessage(newEvent.getJoinMessage());
            }
        }

        @Subscribe
        public void onPlayerLeave(@NotNull DisconnectEvent event) {
            PlayerLeaveEvent newEvent = new PlayerLeaveEvent(new ECVelocityPlayer(parent.server.getPlugin().getPlayerData().players.get(event.getPlayer().getUniqueId().toString()), event.getPlayer()), "");
            parent.emit(newEvent);

            if (!newEvent.getLeaveMessage().isEmpty()) {
                parent.server.broadcastMessage(newEvent.getLeaveMessage());
            }
        }

        @Subscribe
        public void onPlayerServerConnect(@NotNull ServerPreConnectEvent event) {
            if (event.getPreviousServer() == null) {
                PlayerProxyJoinEvent newEvent = new PlayerProxyJoinEvent(new ECVelocityPlayer(parent.server.getPlugin().getPlayerData().players.get(event.getPlayer().getUniqueId().toString()), event.getPlayer()), "", event.getResult().getServer().orElseThrow().getServerInfo().getName());
                parent.emit(newEvent);

                if (newEvent.isCancelled()) {
                    event.setResult(ServerPreConnectEvent.ServerResult.denied());

                    event.getPlayer().disconnect(ECComponentFormatter.stringToComponent(newEvent.getCancelReason()));
                } else {
                    event.setResult(ServerPreConnectEvent.ServerResult.allowed(parent.server.getHandle().getServer(newEvent.getTargetServer()).orElseThrow()));
                }
            } else {
                PlayerServerConnectEvent newEvent = new PlayerServerConnectEvent(new ECVelocityPlayer(parent.server.getPlugin().getPlayerData().players.get(event.getPlayer().getUniqueId().toString()), event.getPlayer()), event.getResult().getServer().orElseThrow().getServerInfo().getName());
                parent.emit(newEvent);

                if (newEvent.isCancelled()) {
                    event.setResult(ServerPreConnectEvent.ServerResult.denied());

                    event.getPlayer().sendMessage(ECComponentFormatter.stringToComponent(newEvent.getCancelReason()));
                } else {
                    event.setResult(ServerPreConnectEvent.ServerResult.allowed(parent.server.getHandle().getServer(newEvent.getTargetServer()).orElseThrow()));
                }
            }
        }

        @Subscribe
        public void onPlayerServerConnect(@NotNull ServerConnectedEvent event) {
            PlayerServerConnectedEvent newEvent = new PlayerServerConnectedEvent(new ECVelocityPlayer(parent.server.getPlugin().getPlayerData().players.get(event.getPlayer().getUniqueId().toString()), event.getPlayer()), event.getServer().getServerInfo().getName(), "");
            parent.emit(newEvent);

            if (!newEvent.getConnectMessage().isEmpty()) {
                parent.server.broadcastMessage(newEvent.getConnectMessage());
            }
        }

        @Subscribe
        public void onPlayerServerConnect(@NotNull ProxyPingEvent event) {
            Map<UUID, String> players = new HashMap<>();
            if (event.getPing().getPlayers().isPresent()) {
                for (ServerPing.SamplePlayer player : event.getPing().getPlayers().get().getSample()) {
                    players.put(player.getId(), player.getName());
                }
            }
            PlayerProxyPingEvent newEvent = new PlayerProxyPingEvent(ECComponentFormatter.componentToString(event.getPing().getDescriptionComponent()), event.getPing().getPlayers().get().getOnline(), event.getPing().getPlayers().get().getMax(), players, event.getConnection().getRemoteAddress().getAddress(), event.getConnection().getVirtualHost().orElse(null));
            parent.emit(newEvent);

            ServerPing.Builder serverPing = ServerPing.builder();

            serverPing.version(new ServerPing.Version(event.getConnection().getProtocolVersion().getProtocol(), parent.getServer().getAllMinecraftVersions()));

            String[] motd = newEvent.getMotd().split("\n");
            for (int i = 0; i < motd.length; i++) {
                if (newEvent.getCenterMotd()) {
                    String padding = " ".repeat((48 - ECTextFormatter.stripColors(motd[i]).length()) / 2);
                    motd[i] = padding + motd[i] + padding;
                }
            }
            serverPing.description(ECComponentFormatter.stringToComponent(String.join("\n", motd)));

            serverPing.onlinePlayers(newEvent.getOnlinePlayers());
            serverPing.maximumPlayers(newEvent.getMaxPlayers());

            List<ServerPing.SamplePlayer> newPlayers = new ArrayList<>();
            for (Map.Entry<UUID, String> entry : newEvent.getPlayers().entrySet()) {
                newPlayers.add(new ServerPing.SamplePlayer(entry.getValue(), entry.getKey()));
            }
            serverPing.samplePlayers(newPlayers.toArray(new ServerPing.SamplePlayer[] { }));

            event.setPing(serverPing.build());
        }

        @Subscribe
        public void onPlayerChat(@NotNull com.velocitypowered.api.event.player.PlayerChatEvent event) { // TODO Check if this includes commands
            String message = event.getMessage();
            if (message.isEmpty() || message.equalsIgnoreCase("/")) {
                event.setResult(com.velocitypowered.api.event.player.PlayerChatEvent.ChatResult.denied());
                return;
            }

            ECVelocityPlayer player = parent.server.getOnlinePlayer(event.getPlayer().getUniqueId());

            if (message.charAt(0) != '/') {
                PlayerChatEvent newEvent = new PlayerChatEvent(new ECVelocityPlayer(parent.server.getPlugin().getPlayerData().players.get(player.getUuid().toString()), player.getHandle()), message, PlayerChatEvent.MessageType.CHAT, new ArrayList<>());
                parent.emit(newEvent);

                event.setResult(com.velocitypowered.api.event.player.PlayerChatEvent.ChatResult.denied());

                if (newEvent.isCancelled()) {
                    if (!newEvent.getCancelReason().isEmpty()) {
                        player.sendMessage(newEvent.getCancelReason());
                    }
                } else if (!newEvent.getMessage().isEmpty()) {
                    for (ECPlayer player2 : (!newEvent.getRecipients().isEmpty() ? newEvent.getRecipients() : parent.getServer().getOnlinePlayers())) {
                        if (player.getServer() == null || !player.getServer().equalsIgnoreCase(player2.getServer())) {
                            player2.sendMessage(ECTextFormatter.translateColors("&r[" + player.getServer().substring(0, 1).toUpperCase() + player.getServer().substring(1).toLowerCase() + "&r] " + newEvent.getMessage()));
                        } else {
                            player2.sendMessage(ECTextFormatter.translateColors("&r" + newEvent.getMessage()));
                        }
                    }
                }
            } else {
                PlayerCommandEvent newEvent = new PlayerCommandEvent(new ECVelocityPlayer(parent.server.getPlugin().getPlayerData().players.get(player.getUuid().toString()), player.getHandle()), message);
                parent.emit(newEvent);

                if (newEvent.isCancelled()) {
                    event.setResult(com.velocitypowered.api.event.player.PlayerChatEvent.ChatResult.denied());

                    if (!newEvent.getCancelReason().isEmpty()) {
                        player.sendMessage(newEvent.getCancelReason());
                    }
                }
            }
        }
    }

    protected final @NotNull ECVelocityServer server;

    protected final @NotNull Map<Class<? extends ECEvent>, List<Map.Entry<ECListener, Method>>> listeners = new HashMap<>();

    public ECVelocityEventManager(@NotNull ECVelocityServer server) {
        this.server = server;

        this.server.getHandle().getEventManager().register(this.server.getPlugin().getHandle(), new ECVelocityEventManager.VelocityListener());
    }

    public @NotNull ECVelocityServer getServer() {
        return this.server;
    }

    @Override
    public void emit(@NotNull ECEvent event) {
        if (this.listeners.containsKey(event.getClass())) {
            for (Map.Entry<ECListener, Method> entry : this.listeners.get(event.getClass())) {
                try {
                    entry.getValue().setAccessible(true);
                    entry.getValue().invoke(entry.getKey(), event);
                } catch (Exception e) {
                    this.server.getPlugin().getLogger().error("Failed to emit event", e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull ECListener register(@NotNull ECListener listener) {
        this.server.getHandle().getEventManager().register(this.server.getPlugin().getHandle(), listener);

        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(ECHandler.class) && method.getParameterCount() == 1 && ECEvent.class.isAssignableFrom(method.getParameterTypes()[0])) {
                if (!this.listeners.containsKey((Class<? extends ECEvent>) method.getParameterTypes()[0])) {
                    this.listeners.put((Class<? extends ECEvent>) method.getParameterTypes()[0], new ArrayList<>());
                }
                this.listeners.get((Class<? extends ECEvent>) method.getParameterTypes()[0]).add(new AbstractMap.SimpleEntry<>(listener, method));

                this.listeners.get((Class<? extends ECEvent>) method.getParameterTypes()[0]).sort(Comparator.comparingInt(a -> a.getValue().getDeclaredAnnotationsByType(ECHandler.class)[0].order().getValue()));
            }
        }

        return listener;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull ECListener unregister(@NotNull ECListener listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.getParameterCount() == 1 && ECEvent.class.isAssignableFrom(method.getParameterTypes()[0]) && method.getDeclaredAnnotationsByType(ECHandler.class).length > 0 && this.listeners.containsKey((Class<? extends ECEvent>) method.getParameterTypes()[0])) {
                this.listeners.get((Class<? extends ECEvent>) method.getParameterTypes()[0]).remove(new AbstractMap.SimpleEntry<>(listener, method));
            }
        }

        this.server.getHandle().getEventManager().unregisterListener(this.server.getPlugin().getHandle(), listener);

        return listener;
    }

    @Override
    public void unregisterAll() {
        this.listeners.clear();
    }
}