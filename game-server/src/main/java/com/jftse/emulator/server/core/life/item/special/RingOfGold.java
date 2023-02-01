package com.jftse.emulator.server.core.life.item.special;

import com.jftse.emulator.server.core.life.item.BaseItem;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryWearSpecialAnswerPacket;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.item.ItemSpecial;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.service.*;
import com.jftse.server.core.shared.packets.inventory.S2CInventoryItemRemoveAnswerPacket;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class RingOfGold extends BaseItem {
    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;
    private final PlayerService playerService;
    private final SpecialSlotEquipmentService specialSlotEquipmentService;
    private final ItemSpecialService itemSpecialService;

    public RingOfGold(int itemIndex, String name, String category) {
        super(itemIndex, name, category);

        pocketService = ServiceManager.getInstance().getPocketService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        playerService = ServiceManager.getInstance().getPlayerService();
        specialSlotEquipmentService = ServiceManager.getInstance().getSpecialSlotEquipmentService();
        itemSpecialService = ServiceManager.getInstance().getItemSpecialService();
    }

    @Override
    public boolean processPlayer(Player player) {
        player = playerService.findById(player.getId());
        if (player == null)
            return false;

        this.localPlayerId = player.getId();

        log.info("Ring of Gold, now trying to process player, PlayerId is: " + localPlayerId);
        return true;
    }

    @Override
    public boolean processPocket(Pocket pocket) {
        pocket = pocketService.findById(pocket.getId());
        if (pocket == null)
            return false;

        Player player = playerService.findById(localPlayerId);
        List<Integer> playersSpecialSlots = specialSlotEquipmentService.getEquippedSpecialSlots(player);
        int i = 1;
        for (Integer idSpecialSlot : playersSpecialSlots) {
            log.info("processing players special slots " + i + " ... id on that slot is: " + idSpecialSlot);
            i++;
        }

        // Item-Index for Gold Ring is 2
        ItemSpecial itemSpecial = itemSpecialService.findByItemIndex(2);

        log.info("found special item: " + itemSpecial.getName() + " with item-index: " + itemSpecial.getItemIndex() + " and item-id: " + itemSpecial.getId());
        PlayerPocket playerPocketROGold = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(itemSpecial.getItemIndex(), EItemCategory.SPECIAL.getName(), pocket);
        if (playerPocketROGold == null) {
            log.info("no Gold Ring found in players pocket");
            return false;
        }

        boolean playerSpecialSlotHasGoldRingEquipped = false;
        long idOfGoldRingInPlayersPocket = playerPocketROGold.getId();
        log.info("Ring of Gold in players pocket has id: " + idOfGoldRingInPlayersPocket + " and item index: " + playerPocketROGold.getItemIndex());
        for (Integer idSpecialSlot : playersSpecialSlots) {
            if (idSpecialSlot == idOfGoldRingInPlayersPocket) {
                playerSpecialSlotHasGoldRingEquipped = true;
                break;
            }
        }

        if (!playerSpecialSlotHasGoldRingEquipped)
            return false;

        log.info("Ring of Gold, itemCount before: " + playerPocketROGold.getItemCount());
        int itemCount = playerPocketROGold.getItemCount() - 1;
        if (itemCount <= 0) {
            final FTConnection connectionByPlayerId = GameManager.getInstance().getConnectionByPlayerId(localPlayerId);
            playerPocketService.remove(playerPocketROGold.getId());
            pocketService.decrementPocketBelongings(pocket);

            List<Integer> playersSpecialSlotsToSet = new ArrayList<>();
            List<Integer> playersSpecialSlotsToRemove = specialSlotEquipmentService.getEquippedSpecialSlots(playerService.findById(localPlayerId));

            for (Integer playersSpecialSlot : playersSpecialSlotsToRemove) {
                if (playersSpecialSlot == (Integer)playerPocketROGold.getId().intValue()){
                    log.info("Special Slot value 0 added");
                    playersSpecialSlotsToSet.add(0);
                } else {
                    log.info("Special Slot value: " + playersSpecialSlot + " added");
                    playersSpecialSlotsToSet.add(playersSpecialSlot);
                }
            }
            specialSlotEquipmentService.updateSpecialSlots(player, playersSpecialSlotsToSet);
            S2CInventoryWearSpecialAnswerPacket inventoryWearSpecialAnswerPacket = new S2CInventoryWearSpecialAnswerPacket(playersSpecialSlotsToSet);
            connectionByPlayerId.sendTCP(inventoryWearSpecialAnswerPacket);

            S2CInventoryItemRemoveAnswerPacket inventoryItemRemoveAnswerPacket = new S2CInventoryItemRemoveAnswerPacket(Math.toIntExact(playerPocketROGold.getId()));
            packetsToSend.add(localPlayerId, inventoryItemRemoveAnswerPacket);

            packetsToSend.forEach((localPlayerId, packets) -> {
                if (connectionByPlayerId != null)
                    connectionByPlayerId.sendTCP(packets.toArray(Packet[]::new));
            });
        } else {
            playerPocketROGold.setItemCount(itemCount);
            playerPocketService.save(playerPocketROGold);
        }
        log.info("Ring of Gold, itemCount now: " + itemCount);
        return true;
    }
}
