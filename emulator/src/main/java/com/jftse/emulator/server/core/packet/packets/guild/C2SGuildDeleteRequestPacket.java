package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SGuildDeleteRequestPacket extends Packet {

    public C2SGuildDeleteRequestPacket(Packet packet) {
        super(packet);
    }
}