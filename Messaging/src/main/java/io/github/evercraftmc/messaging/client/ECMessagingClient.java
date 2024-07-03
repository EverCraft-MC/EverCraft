package io.github.evercraftmc.messaging.client;

import io.github.evercraftmc.messaging.client.netty.ECMessagingClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
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
    protected EventLoopGroup serverWorker;

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
            this.running = true;

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

            Future<?> serverFuture = null;
            if (this.serverWorker != null) {
                serverFuture = this.serverWorker.shutdownGracefully(500, 3000, TimeUnit.MILLISECONDS);
            }
            if (serverFuture != null) {
                serverFuture.syncUninterruptibly();
            }
        }
    }

    protected void run() {
        try {
            Bootstrap bootstrap = new Bootstrap();

            this.serverWorker = new NioEventLoopGroup(4);

            bootstrap.channel(NioSocketChannel.class).group(this.serverWorker);

            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                public void initChannel(NioSocketChannel channel) {
                    channel.pipeline().addLast(new ECMessagingClientHandler(ECMessagingClient.this));
                }
            });

            bootstrap.option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.validate();

            {
                Channel channel = bootstrap.connect(this.address).syncUninterruptibly().channel();

                {
                    channel.closeFuture().syncUninterruptibly();
                }
            }
        } catch (Exception e) {
            this.logger.error("Exception in Messaging client", e);

            throw e;
        }
    }
}