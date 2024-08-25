package io.github.evercraftmc.core.impl.velocity.server;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.util.Favicon;
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
import io.github.evercraftmc.core.api.server.player.ECProxyPlayer;
import io.github.evercraftmc.core.impl.util.ECComponentFormatter;
import io.github.evercraftmc.core.impl.util.ECTextFormatter;
import io.github.evercraftmc.core.impl.velocity.server.player.ECVelocityPlayer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
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

            this.setupPacketListener();
        }

        @Subscribe(order=PostOrder.LATE)
        public void onPlayerPreConnect(@NotNull PreLoginEvent event) {
            InetAddress ip = event.getConnection().getRemoteAddress().getAddress();
            if (allowedIps.containsKey(ip) && allowedIps.get(ip).matcher(event.getUsername()).matches()) {
                event.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());
            }
        }

        @Subscribe(order=PostOrder.LATE)
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

        @Subscribe(order=PostOrder.LATE)
        public void onPlayerJoin(@NotNull PostLoginEvent event) {
            PlayerJoinEvent newEvent = new PlayerJoinEvent(new ECVelocityPlayer(parent.server.getPlugin().getPlayerData().players.get(event.getPlayer().getUniqueId().toString()), parent.server, event.getPlayer()), "");
            parent.emit(newEvent);

            if (newEvent.isCancelled()) {
                event.getPlayer().disconnect(ECComponentFormatter.stringToComponent(newEvent.getCancelReason()));
            } else if (!newEvent.getJoinMessage().isEmpty()) {
                parent.server.broadcastMessage(newEvent.getJoinMessage());
            }
        }

        @Subscribe(order=PostOrder.LATE)
        public void onPlayerLeave(@NotNull DisconnectEvent event) {
            PlayerLeaveEvent newEvent = new PlayerLeaveEvent(new ECVelocityPlayer(parent.server.getPlugin().getPlayerData().players.get(event.getPlayer().getUniqueId().toString()), parent.server, event.getPlayer()), "");
            parent.emit(newEvent);

            if (!newEvent.getLeaveMessage().isEmpty()) {
                parent.server.broadcastMessage(newEvent.getLeaveMessage());
            }
        }

        @Subscribe(order=PostOrder.LATE)
        public void onPlayerServerConnect(@NotNull ServerPreConnectEvent event) {
            if (event.getPreviousServer() == null) {
                PlayerProxyJoinEvent newEvent = new PlayerProxyJoinEvent(new ECVelocityPlayer(parent.server.getPlugin().getPlayerData().players.get(event.getPlayer().getUniqueId().toString()), parent.server, event.getPlayer()), "", event.getResult().getServer().orElseThrow().getServerInfo().getName());
                parent.emit(newEvent);

                if (newEvent.isCancelled()) {
                    event.setResult(ServerPreConnectEvent.ServerResult.denied());

                    event.getPlayer().disconnect(ECComponentFormatter.stringToComponent(newEvent.getCancelReason()));
                } else {
                    event.setResult(ServerPreConnectEvent.ServerResult.allowed(parent.server.getHandle().getServer(newEvent.getTargetServer()).orElseThrow()));
                }
            } else {
                PlayerServerConnectEvent newEvent = new PlayerServerConnectEvent(new ECVelocityPlayer(parent.server.getPlugin().getPlayerData().players.get(event.getPlayer().getUniqueId().toString()), parent.server, event.getPlayer()), event.getResult().getServer().orElseThrow().getServerInfo().getName());
                parent.emit(newEvent);

                if (newEvent.isCancelled()) {
                    event.setResult(ServerPreConnectEvent.ServerResult.denied());

                    event.getPlayer().sendMessage(ECComponentFormatter.stringToComponent(newEvent.getCancelReason()));
                } else {
                    event.setResult(ServerPreConnectEvent.ServerResult.allowed(parent.server.getHandle().getServer(newEvent.getTargetServer()).orElseThrow()));
                }
            }
        }

        @Subscribe(order=PostOrder.LATE)
        public void onPlayerServerConnect(@NotNull ServerConnectedEvent event) {
            PlayerServerConnectedEvent newEvent = new PlayerServerConnectedEvent(new ECVelocityPlayer(parent.server.getPlugin().getPlayerData().players.get(event.getPlayer().getUniqueId().toString()), parent.server, event.getPlayer()), event.getServer().getServerInfo().getName(), "");
            parent.emit(newEvent);

            if (!newEvent.getConnectMessage().isEmpty()) {
                parent.server.broadcastMessage(newEvent.getConnectMessage());
            }
        }

        @Subscribe(order=PostOrder.LATE)
        public void onPlayerServerConnect(@NotNull ProxyPingEvent event) {
            Map<UUID, String> players = new HashMap<>();

            Optional<ServerPing.Players> pingPlayers = event.getPing().getPlayers();
            if (pingPlayers.isPresent()) {
                for (ServerPing.SamplePlayer player : pingPlayers.get().getSample()) {
                    players.put(player.getId(), player.getName());
                }
            }

            BufferedImage favicon = null;

            Optional<Favicon> pingFavicon = event.getPing().getFavicon();
            if (pingFavicon.isPresent()) {
                String bash64Url = pingFavicon.get().getBase64Url();
                if (bash64Url.startsWith("data:image/png;base64,")) {
                    String bash64 = bash64Url.substring("data:image/png;base64,".length());
                    byte[] data = Base64.getDecoder().decode(bash64);

                    try (InputStream byteArrayInputStream = new ByteArrayInputStream(data)) {
                        favicon = ImageIO.read(byteArrayInputStream);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    throw new RuntimeException("Favicon isn't a PNG!");
                }
            }

            PlayerProxyPingEvent newEvent = new PlayerProxyPingEvent(ECComponentFormatter.componentToString(event.getPing().getDescriptionComponent()), false, favicon, pingPlayers.isPresent() ? pingPlayers.get().getOnline() : -1, pingPlayers.isPresent() ? pingPlayers.get().getMax() : -1, players, event.getConnection().getRemoteAddress().getAddress(), event.getConnection().getVirtualHost().orElse(null));
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

            if (newEvent.getFavicon() != null) {
                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    ImageIO.write(newEvent.getFavicon(), "PNG", outputStream);

                    String base64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());
                    String base64Url = "data:image/png;base64," + base64;

                    serverPing.favicon(new Favicon(base64Url));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                serverPing.clearFavicon();
            }

            serverPing.clearMods();

            event.setPing(serverPing.build());
        }

        @Subscribe(order=PostOrder.LATE)
        public void onPlayerChat(@NotNull com.velocitypowered.api.event.player.PlayerChatEvent event) {
            String message = event.getMessage();
            if (message.isEmpty() || message.equalsIgnoreCase("/")) {
                event.setResult(com.velocitypowered.api.event.player.PlayerChatEvent.ChatResult.denied());
                return;
            }

            ECVelocityPlayer player = parent.server.getOnlinePlayer(event.getPlayer().getUniqueId());

            PlayerChatEvent newEvent = new PlayerChatEvent(player, PlayerChatEvent.MessageType.CHAT, message, new ArrayList<>());
            parent.emit(newEvent);

            event.setResult(com.velocitypowered.api.event.player.PlayerChatEvent.ChatResult.denied());

            if (newEvent.isCancelled()) {
                if (!newEvent.getCancelReason().isEmpty()) {
                    player.sendMessage(newEvent.getCancelReason());
                }
            } else if (!newEvent.getMessage().isEmpty()) {
                for (ECPlayer player2 : (!newEvent.getRecipients().isEmpty() ? newEvent.getRecipients() : parent.getServer().getOnlinePlayers())) {
                    if (player.getServer() == null || !(player2 instanceof ECProxyPlayer proxyPlayer2 && player.getServer().equals(proxyPlayer2.getServer()))) {
                        player2.sendMessage(ECTextFormatter.translateColors("&r[" + player.getServer().name().substring(0, 1).toUpperCase() + player.getServer().name().substring(1).toLowerCase() + "&r] " + newEvent.getMessage()));
                    } else {
                        player2.sendMessage(ECTextFormatter.translateColors("&r" + newEvent.getMessage()));
                    }
                }
            }
        }

        @Subscribe(order=PostOrder.LATE)
        public void onPlayerCommand(@NotNull CommandExecuteEvent event) {
            String message = event.getCommand();
            if (message.isEmpty() || message.equalsIgnoreCase("/")) {
                event.setResult(CommandExecuteEvent.CommandResult.denied());
                return;
            }
            if (event.getResult().isForwardToServer()) {
                return;
            }

            if (event.getCommandSource() instanceof Player velocityPlayer) {
                ECVelocityPlayer player = parent.server.getOnlinePlayer(velocityPlayer.getUniqueId());

                PlayerCommandEvent newEvent = new PlayerCommandEvent(player, message);
                parent.emit(newEvent);

                if (newEvent.isCancelled()) {
                    event.setResult(CommandExecuteEvent.CommandResult.denied());

                    if (!newEvent.getCancelReason().isEmpty()) {
                        player.sendMessage(newEvent.getCancelReason());
                    }
                }
            }
        }

        @SuppressWarnings("unchecked")
        protected void setupPacketListener() {
            try {
                Field cmField = parent.getServer().getHandle().getClass().getDeclaredField("cm");
                cmField.setAccessible(true);

                Object cmValue = cmField.get(parent.getServer().getHandle());

                Field serverChannelInitializerHolderField = cmValue.getClass().getDeclaredField("serverChannelInitializer");
                serverChannelInitializerHolderField.setAccessible(true);

                Object serverChannelInitializerHolder = serverChannelInitializerHolderField.get(cmValue);

                Field serverChannelInitializerField = serverChannelInitializerHolder.getClass().getDeclaredField("initializer");
                serverChannelInitializerField.setAccessible(true);

                ChannelInitializer<Channel> oldServerChannelInitializer = (ChannelInitializer<Channel>) serverChannelInitializerField.get(serverChannelInitializerHolder);

                Method oldServerChannelInitializerMethod = oldServerChannelInitializer.getClass().getDeclaredMethod("initChannel", Channel.class);
                oldServerChannelInitializerMethod.setAccessible(true);

                ChannelInitializer<Channel> serverInitializer = new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel channel) {
                        try {
                            oldServerChannelInitializerMethod.invoke(oldServerChannelInitializer, channel);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }

                        channel.pipeline().addBefore("handler", "evercraft-handler", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object packet) {
                                if (packet.getClass().getName().equals("com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerChatPacket")) {
                                    // Unsign all ChatPackets
                                    try {
                                        Object newPacket = packet.getClass().getConstructors()[0].newInstance();

                                        Field messageField = newPacket.getClass().getDeclaredField("message");
                                        messageField.setAccessible(true);
                                        messageField.set(newPacket, messageField.get(packet));

                                        Field timestampField = newPacket.getClass().getDeclaredField("timestamp");
                                        timestampField.setAccessible(true);
                                        timestampField.set(newPacket, timestampField.get(packet));

                                        Field saltField = newPacket.getClass().getDeclaredField("salt");
                                        saltField.setAccessible(true);
                                        saltField.set(newPacket, saltField.get(packet));

                                        Field signedField = newPacket.getClass().getDeclaredField("signed");
                                        signedField.setAccessible(true);
                                        signedField.set(newPacket, false);

                                        Field signatureField = newPacket.getClass().getDeclaredField("signature");
                                        signatureField.setAccessible(true);
                                        signatureField.set(newPacket, new byte[0]);

                                        Field lastSeenMessagesField = newPacket.getClass().getDeclaredField("lastSeenMessages");
                                        lastSeenMessagesField.setAccessible(true);
                                        lastSeenMessagesField.set(newPacket, null);

                                        ctx.fireChannelRead(newPacket);
                                    } catch (NoSuchFieldException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else if (packet.getClass().getName().equals("com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerCommandPacket")) {
                                    // Unsign all CommandPackets
                                    try {
                                        Object newPacket = packet.getClass().getConstructors()[0].newInstance();

                                        Field commandField = newPacket.getClass().getDeclaredField("command");
                                        commandField.setAccessible(true);
                                        commandField.set(newPacket, commandField.get(packet));

                                        Field timestampField = newPacket.getClass().getDeclaredField("timeStamp");
                                        timestampField.setAccessible(true);
                                        timestampField.set(newPacket, timestampField.get(packet));

                                        Field saltField = newPacket.getClass().getDeclaredField("salt");
                                        saltField.setAccessible(true);
                                        saltField.set(newPacket, saltField.get(packet));

                                        Field signaturesField = newPacket.getClass().getDeclaredField("argumentSignatures");
                                        signaturesField.setAccessible(true);

                                        Class<?> signaturesClazz = Class.forName("com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerCommandPacket$ArgumentSignatures");
                                        Object signatures = signaturesClazz.getConstructors()[0].newInstance();
                                        signaturesField.set(newPacket, signatures);

                                        Field lastSeenMessagesField = newPacket.getClass().getDeclaredField("lastSeenMessages");
                                        lastSeenMessagesField.setAccessible(true);
                                        lastSeenMessagesField.set(newPacket, null);

                                        ctx.fireChannelRead(newPacket);
                                    } catch (NoSuchFieldException | InstantiationException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    ctx.fireChannelRead(packet);
                                }
                            }
                        });
                    }
                };

                serverChannelInitializerField.set(serverChannelInitializerHolder, serverInitializer);
            } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
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
                    this.getServer().getPlugin().getLogger().error("Error while invoking event handler {}.", entry.getValue(), e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull ECListener register(@NotNull ECListener listener) {
        this.server.getHandle().getEventManager().register(this.server.getPlugin().getHandle(), listener);

        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(ECHandler.class)) {
                if (method.getParameterCount() == 1 && ECEvent.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    if (!this.listeners.containsKey((Class<? extends ECEvent>) method.getParameterTypes()[0])) {
                        this.listeners.put((Class<? extends ECEvent>) method.getParameterTypes()[0], new ArrayList<>());
                    }
                    this.listeners.get((Class<? extends ECEvent>) method.getParameterTypes()[0]).add(new AbstractMap.SimpleEntry<>(listener, method));

                    this.listeners.get((Class<? extends ECEvent>) method.getParameterTypes()[0]).sort(Comparator.comparingInt(a -> a.getValue().getDeclaredAnnotationsByType(ECHandler.class)[0].order().getValue()));
                } else {
                    this.getServer().getPlugin().getLogger().warn("Warning registered listener, method is annotated with ECHandler but is not valid! {}.", method);
                }
            }
        }

        return listener;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull ECListener unregister(@NotNull ECListener listener) {
        this.server.getHandle().getEventManager().unregisterListener(this.server.getPlugin().getHandle(), listener);

        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(ECHandler.class) && method.getParameterCount() == 1 && ECEvent.class.isAssignableFrom(method.getParameterTypes()[0]) && this.listeners.containsKey((Class<? extends ECEvent>) method.getParameterTypes()[0])) {
                this.listeners.get((Class<? extends ECEvent>) method.getParameterTypes()[0]).remove(new AbstractMap.SimpleEntry<>(listener, method));
            }
        }

        return listener;
    }

    @Override
    public void unregisterAll() {
        for (Map.Entry<Class<? extends ECEvent>, List<Map.Entry<ECListener, Method>>> classEntry : List.copyOf(this.listeners.entrySet())) {
            for (ECListener listener : classEntry.getValue().stream().map(Map.Entry::getKey).collect(Collectors.toSet())) {
                this.unregister(listener);
            }
        }
    }
}