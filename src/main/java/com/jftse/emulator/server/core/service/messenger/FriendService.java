package com.jftse.emulator.server.core.service.messenger;

import com.jftse.emulator.server.database.model.messenger.Friend;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.repository.messenger.FriendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class FriendService {
    private final FriendRepository friendRepository;

    public Friend save(Friend friend) {
        return friendRepository.save(friend);
    }

    public void remove(Long friendMemberId) {
        friendRepository.deleteById(friendMemberId);
    }

    public Friend findById(Long id) { return friendRepository.findById(id).orElse(null); }

    public List<Friend> findByPlayer(Player player) { return friendRepository.findByPlayer(player); }

    public List<Friend> findByFriend(Player friend) { return friendRepository.findByFriend(friend); }

    public Friend findByPlayerIdAndFriendId(long playerId, long friendId) { return friendRepository.findByPlayerIdAndFriendId(playerId, friendId); }

    public long deleteByPlayer(Player player) {
        return friendRepository.deleteByPlayer(player);
    }
}
