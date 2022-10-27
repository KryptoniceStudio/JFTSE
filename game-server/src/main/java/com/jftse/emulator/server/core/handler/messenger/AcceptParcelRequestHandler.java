package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.packets.inventory.S2CInventoryDataPacket;
import com.jftse.emulator.server.core.packets.messenger.C2SAcceptParcelRequest;
import com.jftse.emulator.server.core.packets.messenger.S2CAcceptParcelAnswer;
import com.jftse.emulator.server.core.packets.messenger.S2CRemoveParcelFromListPacket;
import com.jftse.emulator.server.core.packets.shop.S2CShopMoneyAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.messenger.Parcel;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.ParcelService;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PlayerService;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SAcceptParcelRequest)
public class AcceptParcelRequestHandler extends AbstractPacketHandler {
    private C2SAcceptParcelRequest c2SAcceptParcelRequest;

    private final ParcelService parcelService;
    private final PlayerService playerService;
    private final PlayerPocketService playerPocketService;

    public AcceptParcelRequestHandler() {
        parcelService = ServiceManager.getInstance().getParcelService();
        playerService = ServiceManager.getInstance().getPlayerService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
    }

    @Override
    public boolean process(Packet packet) {
        c2SAcceptParcelRequest = new C2SAcceptParcelRequest(packet);
        return true;
    }

    @Override
    public void handle() {
        Parcel parcel = parcelService.findById(c2SAcceptParcelRequest.getParcelId().longValue());
        if (parcel == null) return;

        Player receiver = parcel.getReceiver();
        synchronized (receiver) {
            int newGoldReceiver = receiver.getGold() - parcel.getGold();
            if (newGoldReceiver < 0) {
                S2CAcceptParcelAnswer s2CAcceptParcelAnswer = new S2CAcceptParcelAnswer((short) -2);
                connection.sendTCP(s2CAcceptParcelAnswer);
                return;
            }

            receiver.setGold(newGoldReceiver);
        }

        Player sender = parcel.getSender();
        synchronized (sender) {
            int newGoldSender = sender.getGold() + parcel.getGold();
            sender.setGold(newGoldSender);
        }

        Pocket receiverPocket = receiver.getPocket();
        PlayerPocket item = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(parcel.getItemIndex(), parcel.getCategory(), receiverPocket);
        if (item == null) {
            item = new PlayerPocket();
            item.setCategory(parcel.getCategory());
            item.setItemCount(parcel.getItemCount());
            item.setItemIndex(parcel.getItemIndex());
            item.setUseType(parcel.getUseType());
            item.setPocket(receiverPocket);
        } else {
            item.setItemCount(item.getItemCount() + parcel.getItemCount());
        }

        playerPocketService.save(item);
        parcelService.remove(parcel.getId());
        playerService.save(receiver);
        playerService.save(sender);

        FTClient senderClient = GameManager.getInstance().getClients().stream()
                .filter(x -> x.getPlayer() != null && x.getPlayer().getId().equals(sender.getId()))
                .findFirst()
                .orElse(null);
        if (senderClient != null) {
            S2CShopMoneyAnswerPacket senderMoneyPacket = new S2CShopMoneyAnswerPacket(sender);
            senderClient.getConnection().sendTCP(senderMoneyPacket);

            // TODO: Remove parcel from sent list of sender, S2CSentParcelListPacket doesn't work
        }

        S2CRemoveParcelFromListPacket s2CRemoveParcelFromListPacket = new S2CRemoveParcelFromListPacket(parcel.getId().intValue());
        connection.sendTCP(s2CRemoveParcelFromListPacket);

        S2CShopMoneyAnswerPacket receiverMoneyPacket = new S2CShopMoneyAnswerPacket(receiver);
        connection.sendTCP(receiverMoneyPacket);

        List<PlayerPocket> items = playerPocketService.getPlayerPocketItems(receiver.getPocket());
        S2CInventoryDataPacket s2CInventoryDataPacket = new S2CInventoryDataPacket(items);
        connection.sendTCP(s2CInventoryDataPacket);
    }
}
