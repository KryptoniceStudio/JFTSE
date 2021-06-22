package com.jftse.emulator.server.game.core.game;

import com.jftse.emulator.server.game.core.game.handler.GamePacketHandler;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.ConnectionListener;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

@Log4j2
public class GameServerNetworkListener implements ConnectionListener {
    @Autowired
    private GamePacketHandler gamePacketHandler;

    public void cleanUp() {
        gamePacketHandler.handleCleanUp();
    }

    public void connected(Connection connection) {
        long timeout = TimeUnit.SECONDS.toMillis(30);
        connection.getTcpConnection().setTimeoutMillis((int) timeout);
        
        Client client = new Client();
        client.setConnection(connection);

        connection.setClient(client);
        gamePacketHandler.getGameHandler().addClient(client);

        gamePacketHandler.sendWelcomePacket(connection);
    }

    public void disconnected(Connection connection) {
        gamePacketHandler.handleDisconnected(connection);
    }

    public void received(Connection connection, Packet packet) {
        switch (packet.getPacketId()) {
            case PacketID.C2SDisconnectRequest:
                gamePacketHandler.handleDisconnectPacket(connection, packet);
                break;

            case PacketID.C2SGameLoginData:
                gamePacketHandler.handleGameServerLoginPacket(connection, packet);
                break;

            case PacketID.C2SGameReceiveData:
                gamePacketHandler.handleGameServerDataRequestPacket(connection, packet);
                break;

            case PacketID.C2SHomeItemsLoadReq:
                gamePacketHandler.handleHomeItemsLoadRequestPacket(connection, packet);
                break;

            case PacketID.C2SHomeItemsPlaceReq:
                gamePacketHandler.handleHomeItemsPlaceRequestPacket(connection, packet);
                break;

            case PacketID.C2SHomeItemsClearReq:
                gamePacketHandler.handleHomeItemClearRequestPacket(connection, packet);
                break;

            case PacketID.C2SInventorySellReq:
            case PacketID.C2SInventorySellItemCheckReq:
                gamePacketHandler.handleInventoryItemSellPackets(connection, packet);
                break;

            case PacketID.C2SUnknownInventoryOpenRequest:
                gamePacketHandler.handleUnknownInventoryOpenPacket(connection, packet);
                break;

            case PacketID.C2SInventoryWearClothRequest:
                gamePacketHandler.handleInventoryWearClothPacket(connection, packet);
                break;

            case PacketID.C2SInventoryWearToolRequest:
                gamePacketHandler.handleInventoryWearToolPacket(connection, packet);
                break;

            case PacketID.C2SInventoryWearQuickRequest:
                gamePacketHandler.handleInventoryWearQuickPacket(connection, packet);
                break;

            case PacketID.C2SInventoryWearSpecialRequest:
                gamePacketHandler.handleInventoryWearSpecialPacket(connection, packet);
                break;

            case PacketID.C2SInventoryWearCardRequest:
                gamePacketHandler.handleInventoryWearCardPacket(connection, packet);
                break;

            case PacketID.C2SInventoryItemTimeExpiredRequest:
                gamePacketHandler.handleInventoryItemTimeExpiredPacket(connection, packet);
                break;

            case PacketID.C2SShopMoneyReq:
                gamePacketHandler.handleShopMoneyRequestPacket(connection, packet);
                break;

            case PacketID.C2SShopBuyReq:
                gamePacketHandler.handleShopBuyRequestPacket(connection, packet);
                break;

            case PacketID.C2SShopRequestDataPrepare:
            case PacketID.C2SShopRequestData:
                gamePacketHandler.handleShopRequestDataPackets(connection, packet);
                break;

            case PacketID.C2SPlayerStatusPointChange:
                gamePacketHandler.handlePlayerStatusPointChangePacket(connection, packet);
                break;

            case PacketID.C2SChallengeProgressReq:
                gamePacketHandler.handleChallengeProgressRequestPacket(connection, packet);
                break;

            case PacketID.C2STutorialProgressReq:
                gamePacketHandler.handleTutorialProgressRequestPacket(connection, packet);
                break;

            case PacketID.C2SChallengeBeginReq:
                gamePacketHandler.handleChallengeBeginRequestPacket(connection, packet);
                break;

            case PacketID.C2SChallengeHp:
                gamePacketHandler.handleChallengeHpPacket(connection, packet);
                break;

            case PacketID.C2SChallengePoint:
                gamePacketHandler.handleChallengePointPacket(connection, packet);
                break;

            case PacketID.C2SChallengeDamage:
                gamePacketHandler.handleChallengeDamagePacket(connection, packet);
                break;

            case PacketID.C2SQuickSlotUseRequest:
                gamePacketHandler.handleQuickSlotUseRequest(connection, packet);
                break;

            case PacketID.C2SChallengeSet:
                gamePacketHandler.handleChallengeSetPacket(connection, packet);
                break;

            case PacketID.C2STutorialBegin:
                gamePacketHandler.handleTutorialBeginPacket(connection, packet);
                break;

            case PacketID.C2STutorialEnd:
                gamePacketHandler.handleTutorialEndPacket(connection, packet);
                break;

            case PacketID.C2SLobbyUserListRequest:
                gamePacketHandler.handleLobbyUserListReqPacket(connection, packet);
                break;

            case PacketID.C2SLobbyUserInfoRequest:
                gamePacketHandler.handleLobbyUserInfoReqPacket(connection, packet);
                break;

            case PacketID.C2SLobbyUserInfoClothRequest:
                gamePacketHandler.handleLobbyUserInfoClothReqPacket(connection, packet);
                break;

            case PacketID.C2SChatLobbyReq:
            case PacketID.C2SChatRoomReq:
            case PacketID.C2SWhisperReq:
                gamePacketHandler.handleChatMessagePackets(connection, packet);
                break;

            case PacketID.C2SLobbyJoin:
                gamePacketHandler.handleLobbyJoinLeave(connection, true);
                break;

            case PacketID.C2SLobbyLeave:
                gamePacketHandler.handleLobbyJoinLeave(connection, false);
                break;

            case PacketID.C2SEmblemListRequest:
                gamePacketHandler.handleEmblemListRequestPacket(connection, packet);
                break;

            case PacketID.C2SOpenGachaReq:
                gamePacketHandler.handleOpenGachaRequestPacket(connection, packet);
                break;

            case PacketID.C2SRoomCreate:
                gamePacketHandler.handleRoomCreateRequestPacket(connection, packet);
                break;

            case PacketID.C2SRoomNameChange:
                gamePacketHandler.handleRoomNameChangePacket(connection, packet);
                break;

            case PacketID.C2SRoomGameModeChange:
                gamePacketHandler.handleGameModeChangePacket(connection, packet);
                break;

            case PacketID.C2SRoomIsPrivateChange:
                gamePacketHandler.handleRoomIsPrivateChangePacket(connection, packet);
                break;

            case PacketID.C2SRoomLevelRangeChange:
                gamePacketHandler.handleRoomLevelRangeChangePacket(connection, packet);
                break;

            case PacketID.C2SRoomSkillFreeChange:
                gamePacketHandler.handleRoomSkillFreeChangePacket(connection, packet);
                break;

            case PacketID.C2SRoomAllowBattlemonChange:
                gamePacketHandler.handleRoomAllowBattlemonChangePacket(connection, packet);
                break;

            case PacketID.C2SRoomQuickSlotChange:
                gamePacketHandler.handleRoomQuickSlotChangePacket(connection, packet);
                break;

            case PacketID.C2SRoomJoin:
                gamePacketHandler.handleRoomJoinRequestPacket(connection, packet);
                break;

            case PacketID.C2SRoomLeave:
                gamePacketHandler.handleRoomLeaveRequestPacket(connection, packet);
                break;

            case PacketID.C2SRoomReadyChange:
                gamePacketHandler.handleRoomReadyChangeRequestPacket(connection, packet);
                break;

            case PacketID.C2SRoomMapChange:
                gamePacketHandler.handleRoomMapChangeRequestPacket(connection, packet);
                break;

            case PacketID.C2SRoomPositionChange:
                gamePacketHandler.handleRoomPositionChangeRequestPacket(connection, packet);
                break;

            case PacketID.C2SRoomKickPlayer:
                gamePacketHandler.handleRoomKickPlayerRequestPacket(connection, packet);
                break;

            case PacketID.C2SRoomSlotCloseReq:
                gamePacketHandler.handleRoomSlotCloseRequestPacket(connection, packet);
                break;

            case PacketID.C2SRoomFittingReq:
                gamePacketHandler.handleRoomFittingRequestPacket(connection, packet);
                break;

            case PacketID.C2SRoomTriggerStartGame:
                gamePacketHandler.handleRoomStartGamePacket(connection, packet);
                break;

            case PacketID.C2SRoomCreateQuick:
                gamePacketHandler.handleRoomCreateQuickRequestPacket(connection, packet);
                break;

            case PacketID.C2SRoomListReq:
                gamePacketHandler.handleRoomListRequestPacket(connection, packet);
                break;

            case PacketID.C2SGameAnimationSkipReady:
                gamePacketHandler.handleGameAnimationReadyToSkipPacket(connection, packet);
                break;

            case PacketID.C2SGameAnimationSkipTriggered:
                gamePacketHandler.handleGameAnimationSkipTriggeredPacket(connection, packet);
                break;

            case PacketID.D2SDevPacket:
                gamePacketHandler.handleDevPacket(connection, packet);
                break;

            case PacketID.C2SMatchplayPoint:
                gamePacketHandler.handleMatchplayPointPacket(connection, packet);
                break;

            case PacketID.C2SMatchplayClientBackInRoom:
                gamePacketHandler.handleClientBackInRoomPacket(connection, packet);
                break;

            case PacketID.C2SGuildNoticeRequest:
                gamePacketHandler.handleGuildNoticeRequestPacket(connection, packet);
                break;

            case PacketID.C2SGuildNameCheckRequest:
                gamePacketHandler.handleGuildNameCheckRequestPacket(connection, packet);
                break;

            case PacketID.C2SGuildCreateRequest:
                gamePacketHandler.handleGuildCreateRequestPacket(connection, packet);
                break;

            case PacketID.C2SGuildDataRequest:
                gamePacketHandler.handleGuildDataRequestPacket(connection, packet);
                break;

            case PacketID.C2SGuildListRequest:
                gamePacketHandler.handleGuildListRequestPacket(connection, packet);
                break;

            case PacketID.C2SGuildJoinRequest:
                gamePacketHandler.handleGuildJoinRequestPacket(connection, packet);
                break;

            case PacketID.C2SGuildLeaveRequest:
                gamePacketHandler.handleGuildLeaveRequestPacket(connection, packet);
                break;

            case PacketID.C2SGuildChangeInformationRequest:
                gamePacketHandler.handleGuildChangeInformationRequestPacket(connection, packet);
                break;

            case PacketID.C2SGuildReserveMemberDataRequest:
                gamePacketHandler.handleGuildReverseMemberDataRequestPacket(connection, packet);
                break;

            case PacketID.C2SGuildMemberDataRequest:
                gamePacketHandler.handleGuildMemberDataRequestPacket(connection, packet);
                break;

            case PacketID.C2SGuildChangeMasterRequest:
                gamePacketHandler.handleGuildChangeMasterRequestPacket(connection, packet);
                break;

            case PacketID.C2SGuildChangeSubMasterRequest:
                gamePacketHandler.handleGuildChangeSubMasterRequestPacket(connection, packet);
                break;

            case PacketID.C2SGuildDismissMemberRequest:
                gamePacketHandler.handleGuildDismissMemberRequestPacket(connection, packet);
                break;

            case PacketID.C2SGuildDeleteRequest:
                gamePacketHandler.handleGuildDeleteRequestPacket(connection, packet);
                break;

            case PacketID.C2SGuildChangeNoticeRequest:
                gamePacketHandler.handleGuildChangeNoticeRequestPacket(connection, packet);
                break;

            case PacketID.C2SGuildChatRequest:
                gamePacketHandler.handleGuildChatRequestPacket(connection, packet);
                break;

            case PacketID.C2SMessageListRequest:
                gamePacketHandler.handleMessageListRequest(connection, packet);
                break;

            case PacketID.C2SAddFriendRequest:
                gamePacketHandler.handleAddFriendRequestPacket(connection, packet);
                break;

            case PacketID.C2SAddFriendApprovalRequest:
                gamePacketHandler.handleAddFriendApprovalRequest(connection, packet);
                break;

            case PacketID.C2SDeleteFriendRequest:
                gamePacketHandler.handleDeleteFriendRequest(connection, packet);
                break;

            case PacketID.C2SSendMessageRequest:
                gamePacketHandler.handleSendMessageRequest(connection, packet);
                break;

            case PacketID.C2SDeleteMessagesRequest:
                gamePacketHandler.handleDeleteMessageRequest(connection, packet);
                break;

            case PacketID.C2SMessageSeenRequest:
                gamePacketHandler.handleMessageSeenRequest(connection, packet);
                break;

            case PacketID.C2SSendGiftRequest:
                gamePacketHandler.handleSendGiftRequest(connection, packet);
                break;

            case PacketID.C2SSendParcelRequest:
                gamePacketHandler.handleSendParcelRequest(connection, packet);
                break;

            case PacketID.C2SDenyParcelRequest:
                gamePacketHandler.handleDenyParcelRequest(connection, packet);
                break;

            case PacketID.C2SAcceptParcelRequest:
                gamePacketHandler.handleAcceptParcelRequest(connection, packet);
                break;

            case PacketID.C2SCancelParcelSendingRequest:
                gamePacketHandler.handleCancelSendingParcelRequest(connection, packet);
                break;

            case PacketID.C2SProposalAnswerRequest:
                gamePacketHandler.handleProposalAnswerRequest(connection, packet);
                break;

            case PacketID.C2SSendProposalRequest:
                gamePacketHandler.handleSendProposalRequest(connection, packet);
                break;

            case PacketID.C2SGuildSearchRequest:
                gamePacketHandler.handleGuildSearchRequestPacket(connection, packet);
                break;

            case PacketID.C2SGuildChangeReverseMemberRequest:
                gamePacketHandler.handleGuildChangeReverseMemberRequest(connection, packet);
                break;

            case PacketID.C2SGuildChangeLogoRequest:
                gamePacketHandler.handleGuildChangeLogoRequest(connection, packet);
                break;

            case PacketID.C2SMatchplayPlayerPicksUpCrystal:
                gamePacketHandler.handlePlayerPickingUpCrystal(connection, packet);
                break;

            case PacketID.C2SMatchplayPlayerUseSkill:
                gamePacketHandler.handlePlayerUseSkill(connection, packet);
                break;

            case PacketID.C2SMatchplaySkillHitsTarget:
                gamePacketHandler.handleSkillHitsTarget(connection, packet);
                break;

            case PacketID.C2SMatchplaySwapQuickSlotItems:
                gamePacketHandler.handleSwapQuickSlotItems(connection, packet);
                break;

            case PacketID.C2SRankingPersonalDataReq:
                gamePacketHandler.handleRankingPersonalDataReqPacket(connection, packet);
                break;

            case PacketID.C2SRankingDataReq:
                gamePacketHandler.handleRankingDataReqPacket(connection, packet);
                break;

            case PacketID.C2SHeartbeat:
                // gamePacketHandler.tryDetectSpeedHack(connection);
                gamePacketHandler.handleHeartBeatPacket(connection, packet);
                break;

            case PacketID.C2SLoginAliveClient:
            case 0x1071:
                // empty..
                break;

            default:
                gamePacketHandler.handleUnknown(connection, packet);
                break;
        }
    }

    public void idle(Connection connection) {
        // empty..
    }

    public void onException(Connection connection, Exception exception) {
        switch ("" + exception.getMessage()) {
            case "Connection is closed.":
            case "Connection reset by peer":
            case "Broken pipe":
                break;
            default:
                log.error(exception.getMessage(), exception);
        }
    }

    public void onTimeout(Connection connection) {
        connection.close();
    }
}
