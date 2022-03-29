package com.jftse.emulator.server.core.packet.packets.matchplay;

import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;

import java.util.List;

public class S2CGameNetworkSettingsPacket extends Packet {
    public S2CGameNetworkSettingsPacket(String host, int port, Room room, List<Client> clientsInRoom) {
        super(PacketOperations.S2CGameNetworkSettings.getValueAsChar());

        this.write(host);
        this.write((char) port);

        this.write((int) room.getRoomId()); // session id actually

        int clientsInRoomSize = clientsInRoom.size();
        int maxClientsInRoom = 4;
        int missingClientsCount = maxClientsInRoom - clientsInRoomSize;

        clientsInRoom.forEach(c -> {
            if (c.getActivePlayer() != null)
                this.write(Math.toIntExact(c.getActivePlayer().getId()));
        });

        for (int i = 1; i <= missingClientsCount; i++) {
            this.write(0);
        }
    }
}
