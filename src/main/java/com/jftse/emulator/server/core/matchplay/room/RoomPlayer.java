package com.jftse.emulator.server.core.matchplay.room;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.database.model.guild.GuildMember;
import com.jftse.emulator.server.database.model.messenger.Friend;
import com.jftse.emulator.server.database.model.player.ClothEquipment;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.StatusPointsAddedDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomPlayer {
    private Long playerId;
    private Long guildMemberId;
    private Long coupleId;
    private Long clothEquipmentId;
    private StatusPointsAddedDto statusPointsAddedDto;
    private short position;
    private boolean master;
    private boolean ready;
    private boolean fitting;
    private boolean gameAnimationSkipReady;

    public Player getPlayer() {
        return ServiceManager.getInstance().getPlayerService().findById(playerId);
    }

    public GuildMember getGuildMember() {
        return ServiceManager.getInstance().getGuildMemberService().findById(guildMemberId);
    }

    public Friend getCouple() {
        return ServiceManager.getInstance().getFriendService().findById(coupleId);
    }

    public ClothEquipment getClothEquipment() {
        return ServiceManager.getInstance().getClothEquipmentService().findClothEquipmentById(clothEquipmentId);
    }
}