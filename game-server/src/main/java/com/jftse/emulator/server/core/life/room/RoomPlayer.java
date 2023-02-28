package com.jftse.emulator.server.core.life.room;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.ClothEquipment;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.StatusPointsAddedDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RoomPlayer {
    private Long playerId;
    private Long guildMemberId;
    private Long coupleId;
    private Long clothEquipmentId;
    private Long specialSlotEquipmentId;
    private Long cardSlotEquipmentId;
    private StatusPointsAddedDto statusPointsAddedDto;
    private short position;
    private boolean master;
    private boolean ready;
    private boolean fitting;
    private boolean gameAnimationSkipReady = false;

    public Player getPlayer() {
        return ServiceManager.getInstance().getPlayerService().findById(playerId);
    }

    public GuildMember getGuildMember() {
        if (guildMemberId == null)
            return null;
        return ServiceManager.getInstance().getGuildMemberService().findById(guildMemberId);
    }

    public Friend getCouple() {
        if (coupleId == null)
            return null;
        return ServiceManager.getInstance().getFriendService().findById(coupleId);
    }

    public ClothEquipment getClothEquipment() {
        return ServiceManager.getInstance().getClothEquipmentService().findClothEquipmentById(clothEquipmentId);
    }

    public List<Integer> getSpecialSlotEquipment() {
        return ServiceManager.getInstance().getSpecialSlotEquipmentService().getEquippedSpecialSlots(getPlayer());
    }

    public List<Integer> getCardSlotEquipment() {
        return ServiceManager.getInstance().getCardSlotEquipmentService().getEquippedCardSlots(getPlayer());
    }
}
