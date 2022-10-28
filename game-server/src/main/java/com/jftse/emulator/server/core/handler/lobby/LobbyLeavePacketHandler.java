package com.jftse.emulator.server.core.handler.lobby;

import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SLobbyLeave)
public class LobbyLeavePacketHandler extends AbstractPacketHandler {
    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient client = connection.getClient();
        if (client.isInLobby()) {
            client.setInLobby(false);
        }
        client.setLobbyCurrentRoomListPage(-1);

        GameManager.getInstance().refreshLobbyPlayerListForAllClients();
    }
}