package com.jftse.emulator.server.core.handler.game;

import com.jftse.emulator.common.utilities.StreamUtils;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.authserver.gameserver.C2SGameServerRequestPacket;
import com.jftse.emulator.server.core.packet.packets.authserver.gameserver.S2CGameServerAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.home.S2CHomeDataPacket;
import com.jftse.emulator.server.core.packet.packets.inventory.*;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CClubMembersListAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CFriendRequestNotificationPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CFriendsListAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CRelationshipAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.pet.S2CPetDataAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.player.*;
import com.jftse.emulator.server.core.service.*;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.entities.database.model.messenger.EFriendshipState;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.pet.Pet;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.PlayerStatistic;
import com.jftse.entities.database.model.player.StatusPointsAddedDto;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;

import java.util.List;
import java.util.Map;

public class GameServerDataRequestPacketHandler extends AbstractHandler {
    private C2SGameServerRequestPacket gameServerRequestPacket;

    private final HomeService homeService;
    private final PetService petService;
    private final GuildMemberService guildMemberService;
    private final PlayerPocketService playerPocketService;
    private final ClothEquipmentService clothEquipmentService;
    private final QuickSlotEquipmentService quickSlotEquipmentService;
    private final ToolSlotEquipmentService toolSlotEquipmentService;
    private final SpecialSlotEquipmentService specialSlotEquipmentService;
    private final CardSlotEquipmentService cardSlotEquipmentService;
    private final BattlemonSlotEquipmentService battlemonSlotEquipmentService;
    private final SocialService socialService;
    private final PocketService pocketService;
    private final PlayerStatisticService playerStatisticService;

    public GameServerDataRequestPacketHandler() {
        homeService = ServiceManager.getInstance().getHomeService();
        petService = ServiceManager.getInstance().getPetService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        clothEquipmentService = ServiceManager.getInstance().getClothEquipmentService();
        quickSlotEquipmentService = ServiceManager.getInstance().getQuickSlotEquipmentService();
        toolSlotEquipmentService = ServiceManager.getInstance().getToolSlotEquipmentService();
        specialSlotEquipmentService = ServiceManager.getInstance().getSpecialSlotEquipmentService();
        cardSlotEquipmentService = ServiceManager.getInstance().getCardSlotEquipmentService();
        battlemonSlotEquipmentService = ServiceManager.getInstance().getBattlemonSlotEquipmentService();
        socialService = ServiceManager.getInstance().getSocialService();
        pocketService = ServiceManager.getInstance().getPocketService();
        playerStatisticService = ServiceManager.getInstance().getPlayerStatisticService();
    }

    @Override
    public boolean process(Packet packet) {
        gameServerRequestPacket = new C2SGameServerRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        Client client = connection.getClient();
        Player player = client.getPlayer();
        Account account = client.getAccount();

        byte requestType = gameServerRequestPacket.getRequestType();

        // init data request packets and pass level & exp and home/house data
        if (requestType == 0) {
            S2CGameServerAnswerPacket gameServerAnswerPacket = new S2CGameServerAnswerPacket(requestType, (byte) 0);
            connection.sendTCP(gameServerAnswerPacket);

            StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);
            Pocket pocket = pocketService.findById(player.getPocket().getId());
            PlayerStatistic playerStatistic = playerStatisticService.findPlayerStatisticById(player.getPlayerStatistic().getId());

            S2CUnknownPlayerInfoDataPacket unknownPlayerInfoDataPacket = new S2CUnknownPlayerInfoDataPacket(player, pocket, statusPointsAddedDto, playerStatistic);
            S2CPlayerLevelExpPacket playerLevelExpPacket = new S2CPlayerLevelExpPacket(player.getLevel(), player.getExpPoints());
            S2CCouplePointsDataPacket couplePointsDataPacket = new S2CCouplePointsDataPacket(player.getCouplePoints());
            connection.sendTCP(unknownPlayerInfoDataPacket);
            connection.sendTCP(playerLevelExpPacket, couplePointsDataPacket);

            player.setOnline(true);
            client.savePlayer(player);

            List<Friend> friends = socialService.getFriendList(player, EFriendshipState.Friends);
            S2CFriendsListAnswerPacket s2CFriendsListAnswerPacket = new S2CFriendsListAnswerPacket(friends);
            connection.sendTCP(s2CFriendsListAnswerPacket);

            // update friend list for other online friends
            friends.stream()
                    .filter(f -> f.getFriend().getOnline())
                    .forEach(f -> {
                        List<Friend> onlineFriends = socialService.getFriendList(f.getFriend(), EFriendshipState.Friends);
                        S2CFriendsListAnswerPacket friendListAnswerPacket = new S2CFriendsListAnswerPacket(onlineFriends);
                        GameManager.getInstance().getClients().stream()
                                .filter(c -> c.getPlayer() != null && c.getPlayer().getId().equals(f.getFriend().getId()))
                                .findFirst()
                                .ifPresent(c -> {
                                    if (c.getConnection() != null && c.getConnection().isConnected()) {
                                        c.getConnection().sendTCP(friendListAnswerPacket);
                                    }
                                });
                    });

            List<Friend> friendsWaitingForApproval = socialService.getFriendListByFriend(player, EFriendshipState.WaitingApproval);
            friendsWaitingForApproval.forEach(f -> {
                S2CFriendRequestNotificationPacket s2CFriendRequestNotificationPacket = new S2CFriendRequestNotificationPacket(f.getPlayer().getName());
                connection.sendTCP(s2CFriendRequestNotificationPacket);
            });

            Friend myRelation = socialService.getRelationship(player);
            if (myRelation != null) {
                S2CRelationshipAnswerPacket s2CRelationshipAnswerPacket = new S2CRelationshipAnswerPacket(myRelation);
                connection.sendTCP(s2CRelationshipAnswerPacket);

                Client friendRelationClient = GameManager.getInstance().getClients().stream()
                        .filter(x -> x.getPlayer() != null && x.getPlayer().getId().equals(myRelation.getFriend().getId()))
                        .findFirst()
                        .orElse(null);
                Friend friendRelation = socialService.getRelationship(myRelation.getFriend());
                if (friendRelationClient != null && friendRelation != null) {
                    s2CRelationshipAnswerPacket = new S2CRelationshipAnswerPacket(friendRelation);
                    friendRelationClient.getConnection().sendTCP(s2CRelationshipAnswerPacket);
                }
            }

            GuildMember guildMember = guildMemberService.getByPlayer(player);
            if (guildMember != null && guildMember.getGuild() != null) {
                guildMember.getGuild().getMemberList().stream()
                        .filter(gm -> !gm.getPlayer().getId().equals(guildMember.getPlayer().getId()) && !gm.getWaitingForApproval())
                        .forEach(x -> {
                            List<GuildMember> guildMembers = socialService.getGuildMemberList(x.getPlayer());

                            S2CClubMembersListAnswerPacket s2CClubMembersListAnswerPacket = new S2CClubMembersListAnswerPacket(guildMembers);
                            GameManager.getInstance().getClients().stream()
                                    .filter(c -> c.getPlayer() != null && c.getPlayer().getId().equals(x.getPlayer().getId()))
                                    .findFirst()
                                    .ifPresent(c -> {
                                        if (c.getConnection() != null && c.getConnection().isConnected()) {
                                            c.getConnection().sendTCP(s2CClubMembersListAnswerPacket);
                                        }
                                    });
                        });
            }

            AccountHome accountHome = homeService.findAccountHomeByAccountId(account.getId());

            S2CHomeDataPacket homeDataPacket = new S2CHomeDataPacket(accountHome);
            connection.sendTCP(homeDataPacket);

            List<Pet> petList = petService.findAllByPlayerId(player.getId());

            S2CPetDataAnswerPacket petDataAnswerPacket = new S2CPetDataAnswerPacket(petList);
            connection.sendTCP(petDataAnswerPacket);
        } else if (requestType == 1) {
            S2CGameServerAnswerPacket gameServerAnswerPacket = new S2CGameServerAnswerPacket(requestType, (byte) 0);
            connection.sendTCP(gameServerAnswerPacket);
        }
        // pass inventory & equipped items
        else if (requestType == 2) {
            S2CGameServerAnswerPacket gameServerAnswerPacket = new S2CGameServerAnswerPacket(requestType, (byte) 0);
            connection.sendTCP(gameServerAnswerPacket);

            StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);
            Map<String, Integer> equippedCloths = clothEquipmentService.getEquippedCloths(player);
            List<Integer> equippedQuickSlots = quickSlotEquipmentService.getEquippedQuickSlots(player);
            List<Integer> equippedToolSlots = toolSlotEquipmentService.getEquippedToolSlots(player);
            List<Integer> equippedSpecialSlots = specialSlotEquipmentService.getEquippedSpecialSlots(player);
            List<Integer> equippedCardSlots = cardSlotEquipmentService.getEquippedCardSlots(player);
            List<Integer> equippedBattlemonSlots = battlemonSlotEquipmentService.getEquippedBattlemonSlots(player);

            List<PlayerPocket> playerPocketList = playerPocketService.getPlayerPocketItems(player.getPocket());
            StreamUtils.batches(playerPocketList, 10).forEach(pocketList -> {
                S2CInventoryDataPacket inventoryDataPacket = new S2CInventoryDataPacket(pocketList);
                connection.sendTCP(inventoryDataPacket);
            });

            S2CPlayerStatusPointChangePacket playerStatusPointChangePacket = new S2CPlayerStatusPointChangePacket(player, statusPointsAddedDto);
            S2CPlayerInfoPlayStatsPacket playerInfoPlayStatsPacket = new S2CPlayerInfoPlayStatsPacket(player.getPlayerStatistic());
            S2CInventoryWearClothAnswerPacket inventoryWearClothAnswerPacket = new S2CInventoryWearClothAnswerPacket((char) 0, equippedCloths, player, statusPointsAddedDto);
            S2CInventoryWearQuickAnswerPacket inventoryWearQuickAnswerPacket = new S2CInventoryWearQuickAnswerPacket(equippedQuickSlots);
            S2CInventoryWearToolAnswerPacket inventoryWearToolAnswerPacket = new S2CInventoryWearToolAnswerPacket(equippedToolSlots);
            S2CInventoryWearSpecialAnswerPacket inventoryWearSpecialAnswerPacket = new S2CInventoryWearSpecialAnswerPacket(equippedSpecialSlots);
            S2CInventoryWearCardAnswerPacket inventoryWearCardAnswerPacket = new S2CInventoryWearCardAnswerPacket(equippedCardSlots);
            S2CInventoryWearBattlemonAnswerPacket inventoryWearBattlemonAnswerPacket = new S2CInventoryWearBattlemonAnswerPacket(equippedBattlemonSlots);

            connection.sendTCP(
                    playerStatusPointChangePacket, playerInfoPlayStatsPacket, inventoryWearClothAnswerPacket,
                    inventoryWearQuickAnswerPacket, inventoryWearToolAnswerPacket, inventoryWearSpecialAnswerPacket,
                    inventoryWearCardAnswerPacket, inventoryWearBattlemonAnswerPacket);
        } else {
            S2CGameServerAnswerPacket gameServerAnswerPacket = new S2CGameServerAnswerPacket(requestType, (byte) 0);
            connection.sendTCP(gameServerAnswerPacket);
        }
    }
}
