package io.github.evercraftmc.messaging.client.netty;

import io.github.evercraftmc.messaging.client.ECMessagingClient;
import io.github.evercraftmc.messaging.common.ECMessage;
import io.github.evercraftmc.messaging.common.ECMessageId;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.NotNull;

public class ECMessagingClientHandler extends ChannelDuplexHandler {
    protected static final byte PROTOCOL_VERSION = 1;

    protected final @NotNull ECMessagingClient parent;

    public ECMessagingClientHandler(@NotNull ECMessagingClient parent) {
        this.parent = parent;
    }

    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        if (msg instanceof ByteBuf buffer) { // TODO Account for the possibility of split messages
            try {
                if (buffer.readableBytes() < 1) {
                    throw new RuntimeException("Invalid message received");
                }
                byte protocolVersion = buffer.readByte();
                if (protocolVersion != PROTOCOL_VERSION) {
                    throw new RuntimeException("Invalid protocol version");
                }

                if (buffer.readableBytes() < 1) {
                    throw new RuntimeException("Invalid message received");
                }
                short senderLength = buffer.readShort();

                if (buffer.readableBytes() < senderLength) {
                    throw new RuntimeException("Invalid message received");
                }
                byte[] sender = new byte[senderLength];
                buffer.readBytes(sender);

                if (buffer.readableBytes() < 1) {
                    throw new RuntimeException("Invalid message received");
                }
                short recipientLength = buffer.readShort();

                if (buffer.readableBytes() < recipientLength) {
                    throw new RuntimeException("Invalid message received");
                }
                byte[] recipient = new byte[recipientLength];
                buffer.readBytes(recipient);

                if (buffer.readableBytes() < 1) {
                    throw new RuntimeException("Invalid message received");
                }
                short dataLength = buffer.readShort();

                if (buffer.readableBytes() < dataLength) {
                    throw new RuntimeException("Invalid message received");
                }
                byte[] data = new byte[dataLength];
                buffer.readBytes(data);

                String[] senderString = new String(sender, StandardCharsets.UTF_8).split(":", 2);
                String[] recipientString = new String(recipient, StandardCharsets.UTF_8).split(":", 2);

                ctx.write(new ECMessage(ECMessageId.parse(senderString[0], senderString[1]), ECMessageId.parse(recipientString[0], recipientString[1]), data));
            } finally {
                buffer.release();
            }
        } else {
            throw new RuntimeException("Incorrect type passed to " + this.getClass().getSimpleName() + ", " + msg.getClass().getName());
        }
    }

    @Override
    public void channelReadComplete(@NotNull ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void write(@NotNull ChannelHandlerContext ctx, @NotNull Object msg, @NotNull ChannelPromise promise) {
        if (msg instanceof ECMessage message) {
            ByteBuf buffer = ctx.alloc().buffer();
            buffer.retain();

            try {
                buffer.writeByte(PROTOCOL_VERSION);

                byte[] sender = (message.getSender().getType() + ":" + message.getSender().getValue()).getBytes(StandardCharsets.UTF_8);
                buffer.writeShort(sender.length);
                buffer.writeBytes(sender);

                byte[] recipient = (message.getRecipient().getType() + ":" + message.getRecipient().getValue()).getBytes(StandardCharsets.UTF_8);
                buffer.writeShort(recipient.length);
                buffer.writeBytes(recipient);

                byte[] data = message.getData();
                buffer.writeInt(data.length);
                buffer.writeBytes(data);

                ctx.write(buffer);
            } finally {
                buffer.release();
            }
        } else {
            throw new RuntimeException("Incorrect type passed to " + this.getClass().getSimpleName() + ", " + msg.getClass().getName());
        }
    }

    @Override
    public void exceptionCaught(@NotNull ChannelHandlerContext ctx, @NotNull Throwable e) {
        parent.getLogger().error("Exception in Messaging client", e);
    }
}