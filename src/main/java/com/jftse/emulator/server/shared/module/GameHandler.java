package com.jftse.emulator.server.shared.module;

import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.core.matchplay.room.Room;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

@Service
@Getter
@Setter
public class GameHandler {

    private ConcurrentLinkedDeque<Client> clientList;
    private ConcurrentLinkedDeque<Room> roomList;

    @PostConstruct
    public void init() {

        clientList = new ConcurrentLinkedDeque<>();
        roomList = new ConcurrentLinkedDeque<>();
    }

    public void addClient(Client client) {
        clientList.add(client);
    }

    public void removeClient(Client client) {
        clientList.remove(client);
    }

    public List<Player> getPlayersInLobby() {
        return clientList.stream()
                .filter(Client::isInLobby)
                .map(Client::getActivePlayer)
                .collect(Collectors.toList());
    }

    public List<Client> getClientsInLobby() {
        return clientList.stream()
                .filter(Client::isInLobby)
                .collect(Collectors.toList());
    }

    public List<Client> getClientsInRoom(short roomId) {
        return clientList.stream()
                .filter(c -> c.getActiveRoom() != null && c.getActiveRoom().getRoomId() == roomId)
                .collect(Collectors.toList());
    }
}
