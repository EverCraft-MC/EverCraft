package io.github.evercraftmc.core.impl.waterfall.server;

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
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.core.impl.waterfall.server.player.ECWaterfallPlayer;
import io.github.evercraftmc.core.impl.waterfall.util.ECWaterfallComponentFormatter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class ECWaterfallEventManager implements ECEventManager {
    protected class BungeeListeners implements Listener {
        protected final @NotNull ECWaterfallEventManager parent = ECWaterfallEventManager.this;

        protected final @NotNull Map<InetAddress, Boolean> allowedIps = new HashMap<>();

        public BungeeListeners() {
            try {
                Path path = parent.getServer().getPlugin().getDataDirectory().toPath().resolve("allowedIps.txt");
                if (Files.exists(path)) {
                    List<String> lines = Files.readAllLines(path);
                    for (String line : lines) {
                        String ip = line.split("/")[0];
                        boolean allowAny = Boolean.parseBoolean(line.split("/")[1]);

                        allowedIps.put(InetAddress.getByName(ip), allowAny);
                    }
                }
            } catch (ClassCastException | IOException e) {
                throw new RuntimeException(e);
            }
        }

        @EventHandler
        public void onPlayerPreConnect(@NotNull PreLoginEvent event) {
            InetAddress ip = ((InetSocketAddress) event.getConnection().getSocketAddress()).getAddress();
            if (allowedIps.containsKey(ip) && (allowedIps.get(ip) || event.getConnection().getName().startsWith("Tester_"))) {
                event.getConnection().setOnlineMode(false);
            }
        }

        @EventHandler
        public void onPlayerConnect(@NotNull LoginEvent event) {
            String uuid = event.getConnection().getUniqueId().toString();
            if (!parent.server.getPlugin().getPlayerData().players.containsKey(uuid)) {
                parent.server.getPlugin().getPlayerData().players.put(uuid, new ECPlayerData.Player(UUID.fromString(uuid), event.getConnection().getName()));
            }

            io.github.evercraftmc.core.api.events.player.PlayerLoginEvent newEvent = new io.github.evercraftmc.core.api.events.player.PlayerLoginEvent(new ECWaterfallPlayer(parent.server.getPlugin().getPlayerData().players.get(uuid)));
            parent.emit(newEvent);

            if (newEvent.isCancelled()) {
                event.setCancelled(true);
                event.setReason(ECWaterfallComponentFormatter.stringToComponent(newEvent.getCancelReason()));
            }
        }

        @EventHandler
        public void onPlayerJoin(@NotNull PostLoginEvent event) {
            PlayerJoinEvent newEvent = new PlayerJoinEvent(new ECWaterfallPlayer(parent.server.getPlugin().getPlayerData().players.get(event.getPlayer().getUniqueId().toString()), event.getPlayer()), "");
            parent.emit(newEvent);

            if (newEvent.isCancelled()) {
                event.getPlayer().disconnect(ECWaterfallComponentFormatter.stringToComponent(newEvent.getCancelReason()));
            } else if (!newEvent.getJoinMessage().isEmpty()) {
                parent.server.broadcastMessage(newEvent.getJoinMessage());
            }
        }

        @EventHandler
        public void onPlayerLeave(@NotNull PlayerDisconnectEvent event) {
            PlayerLeaveEvent newEvent = new PlayerLeaveEvent(new ECWaterfallPlayer(parent.server.getPlugin().getPlayerData().players.get(event.getPlayer().getUniqueId().toString()), event.getPlayer()), "");
            parent.emit(newEvent);

            if (!newEvent.getLeaveMessage().isEmpty()) {
                parent.server.broadcastMessage(newEvent.getLeaveMessage());
            }
        }

        @EventHandler
        public void onPlayerServerConnect(@NotNull ServerConnectEvent event) {
            if (event.getReason() == ServerConnectEvent.Reason.JOIN_PROXY) {
                PlayerProxyJoinEvent newEvent = new PlayerProxyJoinEvent(new ECWaterfallPlayer(parent.server.getPlugin().getPlayerData().players.get(event.getPlayer().getUniqueId().toString()), event.getPlayer()), "", event.getTarget().getName());
                parent.emit(newEvent);

                if (newEvent.isCancelled()) {
                    event.setCancelled(true);

                    event.getPlayer().disconnect(ECWaterfallComponentFormatter.stringToComponent(newEvent.getCancelReason()));
                } else {
                    event.setTarget(parent.server.getHandle().getServerInfo(newEvent.getTargetServer()));
                }
            } else {
                PlayerServerConnectEvent newEvent = new PlayerServerConnectEvent(new ECWaterfallPlayer(parent.server.getPlugin().getPlayerData().players.get(event.getPlayer().getUniqueId().toString()), event.getPlayer()), event.getTarget().getName());
                parent.emit(newEvent);

                if (newEvent.isCancelled()) {
                    event.setCancelled(true);

                    event.getPlayer().sendMessage(ECWaterfallComponentFormatter.stringToComponent(newEvent.getCancelReason()));
                } else {
                    event.setTarget(parent.server.getHandle().getServerInfo(newEvent.getTargetServer()));
                }
            }
        }

        @EventHandler
        public void onPlayerServerConnect(@NotNull ServerConnectedEvent event) {
            PlayerServerConnectedEvent newEvent = new PlayerServerConnectedEvent(new ECWaterfallPlayer(parent.server.getPlugin().getPlayerData().players.get(event.getPlayer().getUniqueId().toString()), event.getPlayer()), event.getServer().getInfo().getName(), "");
            parent.emit(newEvent);

            if (!newEvent.getConnectMessage().isEmpty()) {
                parent.server.broadcastMessage(newEvent.getConnectMessage());
            }
        }

        @EventHandler
        public void onPlayerServerConnect(@NotNull ProxyPingEvent event) {
            Map<UUID, String> players = new HashMap<>();
            if (event.getResponse().getPlayers().getSample() != null) {
                for (ServerPing.PlayerInfo player : event.getResponse().getPlayers().getSample()) {
                    players.put(player.getUniqueId(), player.getName());
                }
            }
            PlayerProxyPingEvent newEvent = new PlayerProxyPingEvent(ECWaterfallComponentFormatter.componentToString(event.getResponse().getDescriptionComponent()), event.getResponse().getPlayers().getOnline(), event.getResponse().getPlayers().getMax(), players, event.getConnection().getSocketAddress() instanceof InetSocketAddress socketAddress ? socketAddress.getAddress() : null, event.getConnection().getVirtualHost());
            parent.emit(newEvent);

            String[] motd = newEvent.getMotd().split("\n");
            for (int i = 0; i < motd.length; i++) {
                if (newEvent.getCenterMotd()) {
                    String padding = " ".repeat((48 - ECTextFormatter.stripColors(motd[i]).length()) / 2);
                    motd[i] = padding + motd[i] + padding;
                }
            }
            event.getResponse().setDescriptionComponent(ECWaterfallComponentFormatter.stringToComponent(String.join("\n", motd)));

            List<ServerPing.PlayerInfo> newPlayers = new ArrayList<>();
            for (Map.Entry<UUID, String> entry : newEvent.getPlayers().entrySet()) {
                newPlayers.add(new ServerPing.PlayerInfo(entry.getValue(), entry.getKey()));
            }
            event.getResponse().setPlayers(new ServerPing.Players(newEvent.getMaxPlayers(), newEvent.getOnlinePlayers(), newPlayers.toArray(new ServerPing.PlayerInfo[] { })));
        }

        @EventHandler
        public void onPlayerChat(@NotNull ChatEvent event) {
            String message = event.getMessage();
            if (message.isEmpty() || message.equalsIgnoreCase("/")) {
                event.setCancelled(true);
                return;
            }

            ECWaterfallPlayer player = parent.server.getOnlinePlayer(event.getSender());

            if (message.charAt(0) != '/') {
                PlayerChatEvent newEvent = new PlayerChatEvent(new ECWaterfallPlayer(parent.server.getPlugin().getPlayerData().players.get(player.getUuid().toString()), player.getHandle()), message, PlayerChatEvent.MessageType.CHAT, new ArrayList<>());
                parent.emit(newEvent);

                event.setCancelled(true);

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
                PlayerCommandEvent newEvent = new PlayerCommandEvent(new ECWaterfallPlayer(parent.server.getPlugin().getPlayerData().players.get(player.getUuid().toString()), player.getHandle()), message);
                parent.emit(newEvent);

                if (newEvent.isCancelled()) {
                    event.setCancelled(true);

                    if (!newEvent.getCancelReason().isEmpty()) {
                        player.sendMessage(newEvent.getCancelReason());
                    }
                }
            }
        }
    }

    protected final @NotNull ECWaterfallServer server;

    protected final @NotNull Map<Class<? extends ECEvent>, List<Map.Entry<ECListener, Method>>> listeners = new HashMap<>();

    public ECWaterfallEventManager(@NotNull ECWaterfallServer server) {
        this.server = server;

        this.server.getHandle().getPluginManager().registerListener((Plugin) this.server.getPlugin().getHandle(), new BungeeListeners());
    }

    public @NotNull ECWaterfallServer getServer() {
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
        if (listener instanceof net.md_5.bungee.api.plugin.Listener bungeeListener) {
            this.server.getHandle().getPluginManager().registerListener((Plugin) this.server.getPlugin().getHandle(), bungeeListener);
        }

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

        return listener;
    }

    @Override
    public void unregisterAll() {
        this.listeners.clear();
    }
}