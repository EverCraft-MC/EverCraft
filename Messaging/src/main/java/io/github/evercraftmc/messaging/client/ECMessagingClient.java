package io.github.evercraftmc.messaging.client;

import io.github.evercraftmc.messaging.client.netty.ECMessagingClientHandler;
import io.github.evercraftmc.messaging.common.ECMessage;
import io.github.evercraftmc.messaging.common.ECMessageId;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class ECMessagingClient {
    protected final @NotNull Logger logger;

    protected final @NotNull InetSocketAddress address;

    protected final @NotNull Object statusLock = new Object();
    protected boolean running = false;

    protected Thread thread;
    protected EventLoopGroup clientWorker;
    protected Channel channel;

    public ECMessagingClient(@NotNull Logger logger, @NotNull InetSocketAddress address) {
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

            this.thread = new Thread(this::run, this.getClass().getSimpleName() + "[address=" + this.address + "]");
            this.thread.setDaemon(true);
            this.thread.start();
        }
    }

    public void stop() {
        synchronized (this.statusLock) {
            if (!this.running) {
                throw new RuntimeException(this.getClass().getSimpleName() + " is not running!");
            }
            this.running = false;

            Future<?> clientFuture = null;
            if (this.clientWorker != null) {
                clientFuture = this.clientWorker.shutdownGracefully(500, 3000, TimeUnit.MILLISECONDS);
            }
            if (clientFuture != null) {
                clientFuture.syncUninterruptibly();
            }
        }
    }

    public @NotNull Channel getChannel() {
        return this.channel;
    }

    protected void run() {
        try {
            synchronized (this.statusLock) {
                this.running = true;

                this.clientWorker = new NioEventLoopGroup(4);

                this.reconnect(0);
            }
        } catch (Exception e) {
            this.logger.error("Exception in Messaging client", e);

            throw e;
        }
    }

    protected void reconnect(long delay) {
        Runnable runnable = () -> {
            Bootstrap bootstrap = new Bootstrap();

            bootstrap.channel(NioSocketChannel.class).group(this.clientWorker);

            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                public void initChannel(@NotNull NioSocketChannel channel) {
                    channel.pipeline().addLast(new ECMessagingClientHandler(ECMessagingClient.this));
                }
            });

            bootstrap.option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.validate();

            ChannelFuture channelFuture = bootstrap.connect(this.address);

            channelFuture.addListener((future) -> {
                this.channel = channelFuture.channel();

                if (!future.isSuccess()) {
                    this.reconnect(delay <= 0 ? 256 : delay * 3);
                } else {
                    this.logger.info("Connected to Messaging server");

                    this.channel.closeFuture().addListener((closeFuture) -> {
                        this.reconnect(256);
                    });
                }
            });
        };

        if (delay > 0) {
            this.logger.warn("Attempting reconnect in {}ms", delay);

            this.clientWorker.schedule(runnable, delay, TimeUnit.MILLISECONDS);
        } else {
            runnable.run();
        }
    }

    public void send(@NotNull ECMessage message) {
        synchronized (this.statusLock) {
            if (!this.running) {
                throw new RuntimeException(this.getClass().getSimpleName() + " is not running!");
            }
        }

        this.channel.writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public void send(@NotNull ECMessageId from, @NotNull ECMessageId to, byte @NotNull [] data) {
        this.send(new ECMessage(from, to, data));
    }
}