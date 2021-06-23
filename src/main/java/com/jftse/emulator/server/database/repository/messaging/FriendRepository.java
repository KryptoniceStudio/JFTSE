package com.jftse.emulator.server.database.repository.messaging;

import com.jftse.emulator.server.database.model.messaging.Friend;
import com.jftse.emulator.server.database.model.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    Optional<Friend> findById(Long id);
    List<Friend> findByPlayer(Player player);
    List<Friend> findByFriend(Player friend);
    Friend findByPlayerIdAndFriendId(Long playerId, Long friendId);
}