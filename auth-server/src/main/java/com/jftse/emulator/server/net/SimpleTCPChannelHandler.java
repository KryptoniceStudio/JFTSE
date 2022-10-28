package com.jftse.emulator.server.net;

import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketHandlerFactory;
import com.jftse.server.core.net.TCPHandler;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.shared.packets.S2CWelcomePacket;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@ChannelHandler.Sharable
public class SimpleTCPChannelHandler extends TCPHandler {
    private final AttributeKey<FTConnection> FT_CONNECTION_ATTRIBUTE_KEY;

    private final List<String> blockedIP = new ArrayList<>();
    private final Map<String, Pair<Long, Byte>> tracker = new ConcurrentHashMap<>();

    public SimpleTCPChannelHandler(final AttributeKey<FTConnection> ftConnectionAttributeKey, final PacketHandlerFactory phf) {
        super(phf);

        this.FT_CONNECTION_ATTRIBUTE_KEY = ftConnectionAttributeKey;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        String remoteAddress = ctx.channel().remoteAddress().toString();
        log.info("(" + remoteAddress + ") Channel Active");

        if (!checkIp(ctx, remoteAddress))
            return;

        FTConnection connection = ctx.channel().attr(FT_CONNECTION_ATTRIBUTE_KEY).get();
        connection.setChannelHandlerContext(ctx);

        FTClient client = new FTClient();

        client.setConnection(connection);
        connection.setClient(client);

        S2CWelcomePacket welcomePacket = new S2CWelcomePacket(connection.getDecryptionKey(), connection.getEncryptionKey(), 0, 0);
        connection.sendTCP(welcomePacket);
    }

    @Override
    protected void handlerNotFound(ChannelHandlerContext ctx, Packet packet) throws Exception {
        log.warn("(" + ctx.channel().remoteAddress() + ") There is no implementation registered for " + PacketOperations.getNameByValue(packet.getPacketId()) + " packet (id " + String.format("0x%X", (int) packet.getPacketId()) + ")");
    }

    @Override
    protected void packetNotProcessed(ChannelHandlerContext ctx, AbstractPacketHandler handler) throws Exception {
        log.warn(handler.getClass().getSimpleName() + " packet has not been processed");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("(" + ctx.channel().remoteAddress() + ") Channel Inactive");

        FTConnection connection = ctx.channel().attr(FT_CONNECTION_ATTRIBUTE_KEY).get();
        if (connection != null && connection.getClient() != null) {
            connection.setClient(null);
            ctx.channel().attr(FT_CONNECTION_ATTRIBUTE_KEY).set(null);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("(" + ctx.channel().remoteAddress() + ") exceptionCaught: " + cause.getMessage(), cause);
    }

    private boolean checkIp(ChannelHandlerContext ctx, String remoteAddress) {
        String address = remoteAddress.substring(1, remoteAddress.lastIndexOf(":"));

        if (blockedIP.contains(address)) {
            ctx.close();
            return false;
        }

        final Pair<Long, Byte> track = tracker.get(address);
        byte count;
        if (track == null) {
            count = 1;
        } else {
            count = track.getRight();

            final long difference = System.currentTimeMillis() - track.getLeft();
            if (difference < 7000) {
                count++;
            } else if (difference > 20000) {
                count = 1;
            }
            if (count >= 5) {
				log.info("adding to blocked ip: " + address);
                blockedIP.add(address);
                tracker.remove(address);
                ctx.close();
                return false;
            }
        }
        tracker.put(address, Pair.of(System.currentTimeMillis(), count));
        return true;
    }
}