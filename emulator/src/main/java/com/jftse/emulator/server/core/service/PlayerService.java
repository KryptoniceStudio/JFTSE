package com.jftse.emulator.server.core.service;

import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.repository.player.PlayerRepository;
import com.jftse.emulator.server.core.packet.packets.player.C2SPlayerCreatePacket;
import com.jftse.emulator.server.core.packet.packets.player.C2SPlayerStatusPointChangePacket;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class PlayerService {

    private final PlayerRepository playerRepository;

    public Player save(Player player) {
        return playerRepository.save(player);
    }

    public List<Player> findAll() {
        return playerRepository.findAll();
    }

    public List<Player> findAllByAlreadyCreatedSorted(Sort sort) {
        return playerRepository.findAllByAlreadyCreatedTrue(sort);
    }

    public List<Player> findAllByAlreadyCreatedPageable(Pageable pageable) {
        return playerRepository.findAllByAlreadyCreatedTrue(pageable).getContent();
    }

    public List<Player> findAllByAccount(Account account) {
        return playerRepository.findAllByAccount_Id(account.getId());
    }

    public int getPlayerRankingByName(String name, byte gameMode) {
        return playerRepository.getRankingByNameAndGameMode(name, gameMode);
    }

    public Player findById(Long playerId) {
        Optional<Player> player = playerRepository.findById(playerId);
        return player.orElse(null);
    }

    public Player findByIdFetched(Long playerId) {
        Optional<Player> player = playerRepository.findByIdFetched(playerId);
        return player.orElse(null);
    }

    public Player findByName(String name) {
        List<Player> playerList = playerRepository.findAllByName(name);
        return playerList.size() != 0 ? playerList.get(0) : null;
    }

    public Player findByNameFetched(String name) {
        Optional<Player> player = playerRepository.findAllByNameFetched(name);
        return player.orElse(null);
    }

    public Player updateMoney(Player player, int gold) {
        player.setGold(player.getGold() + gold);
        return save(player);
    }

    public Player setMoney(Player player, int gold) {
        player.setGold(gold);
        return save(player);
    }

    public void remove(Long playerId) {
        playerRepository.deleteById(playerId);
    }

    public boolean isStatusPointHack(C2SPlayerStatusPointChangePacket playerStatusPointChangePacket, Player player) {
        // checking them so we are not 'hacked'
        byte serverStatusPoints = player.getStatusPoints();
        byte clientStatusPoints = playerStatusPointChangePacket.getStatusPoints();

        byte strength = (byte) (playerStatusPointChangePacket.getStrength() - player.getStrength());
        byte stamina = (byte) (playerStatusPointChangePacket.getStamina() - player.getStamina());
        byte dexterity = (byte) (playerStatusPointChangePacket.getDexterity() - player.getDexterity());
        byte willpower = (byte) (playerStatusPointChangePacket.getWillpower() - player.getWillpower());

        byte newStatusPoints = (byte) (strength + stamina + dexterity + willpower + clientStatusPoints);

        return (serverStatusPoints - newStatusPoints) != 0;
    }

    public boolean isStatusPointHack(C2SPlayerCreatePacket playerCreatePacket, Player player) {
        // checking them so we are not 'hacked'
        byte serverStatusPoints = player.getStatusPoints();
        byte clientStatusPoints = playerCreatePacket.getStatusPoints();

        byte strength = (byte) (playerCreatePacket.getStrength() - player.getStrength());
        byte stamina = (byte) (playerCreatePacket.getStamina() - player.getStamina());
        byte dexterity = (byte) (playerCreatePacket.getDexterity() - player.getDexterity());
        byte willpower = (byte) (playerCreatePacket.getWillpower() - player.getWillpower());

        byte newStatusPoints = (byte) (strength + stamina + dexterity + willpower + clientStatusPoints);

        return (serverStatusPoints - newStatusPoints) != 0;
    }
}
