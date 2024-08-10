package io.github.evercraftmc.messaging.server;

import io.github.evercraftmc.messaging.server.netty.ECMessagingServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class ECMessagingServer {
    protected final @NotNull Logger logger;

    protected final @NotNull InetSocketAddress address;

    protected final @NotNull Object statusLock = new Object();
    protected boolean running = false;

    protected Thread thread;
    protected EventLoopGroup serverWorker;
    protected EventLoopGroup connectionWorker;
    protected ServerChannel channel;
    protected final @NotNull List<Channel> channelGroup = new ArrayList<>();

    public ECMessagingServer(@NotNull Logger logger, @NotNull InetSocketAddress address) {
        this.logger = logger;

        this.address = address;
    }

    public @NotNull Logger getLogger() {
        return this.logger;
    }

    public @NotNull InetSocketAddress getAddress() {
        return this.address;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void start() {
        synchronized (this.statusLock) {
            if (this.running) {
                throw new RuntimeException(this.getClass().getSimpleName() + " is already running!");
            }

            this.logger.info("Starting Messaging server on port {}", this.address.getPort());

            this.thread = new Thread(this::run, this.getClass().getSimpleName() + "[address=" + this.address + "]");
            this.thread.setDaemon(true);
            this.thread.start();
        }
    }

    public void stop() {
        synchronized (this.statusLock) {
            if (!this.running) {
                throw new RuntimeException(this.getClass().getSimpleName() + " is already running!");
            }
            this.running = false;

            this.logger.info("Stopping Messaging server");

            Future<?> serverFuture = null;
            Future<?> connectionFuture = null;
            if (this.serverWorker != null) {
                serverFuture = this.serverWorker.shutdownGracefully(500, 5000, TimeUnit.MILLISECONDS);
            }
            if (this.connectionWorker != null) {
                connectionFuture = this.connectionWorker.shutdownGracefully(500, 5000, TimeUnit.MILLISECONDS);
            }

            if (serverFuture != null) {
                serverFuture.syncUninterruptibly();
            }
            if (connectionFuture != null) {
                connectionFuture.syncUninterruptibly();
            }
        }
    }

    public @NotNull ServerChannel getChannel() {
        return this.channel;
    }

    public @NotNull List<Channel> getChannelGroup() {
        return this.channelGroup;
    }

    public @NotNull EventLoopGroup getServerWorker() {
        return this.serverWorker;
    }

    public @NotNull EventLoopGroup getConnectionWorker() {
        return this.connectionWorker;
    }

    protected void run() {
        try {
            synchronized (this.statusLock) {
                this.running = true;

                this.serverWorker = new NioEventLoopGroup(12);
                this.connectionWorker = new NioEventLoopGroup(128);

                this.channelGroup.clear();

                ServerBootstrap bootstrap = new ServerBootstrap();

                bootstrap.channel(NioServerSocketChannel.class).group(this.serverWorker, this.connectionWorker);

                bootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
                    private final @NotNull ECMessagingServer parent = ECMessagingServer.this;

                    @Override
                    public void initChannel(NioSocketChannel channel) {
                        channel.pipeline().addLast(new ECMessagingServerHandler(ECMessagingServer.this));

                        synchronized (parent.channelGroup) {
                            parent.channelGroup.add(channel);
                        }

                        channel.closeFuture().addListener((future) -> {
                            synchronized (parent.channelGroup) {
                                parent.channelGroup.remove(channel);
                            }
                        });
                    }
                });

                bootstrap.option(ChannelOption.SO_BACKLOG, 16).childOption(ChannelOption.TCP_NODELAY, true).childOption(ChannelOption.SO_KEEPALIVE, true);
                bootstrap.validate();

                {
                    this.channel = (ServerChannel) bootstrap.bind(this.address).syncUninterruptibly().channel();

                    this.logger.info("Successfully started Messaging server");
                }
            }

            {
                this.channel.closeFuture().syncUninterruptibly();

                this.logger.info("Successfully stopped Messaging server");
            }
        } catch (Exception e) {
            this.logger.error("Exception in Messaging server", e);

            throw e;
        }
    }
}