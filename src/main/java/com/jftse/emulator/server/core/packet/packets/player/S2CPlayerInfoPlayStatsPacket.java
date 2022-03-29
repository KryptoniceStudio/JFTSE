package com.jftse.emulator.server.core.packet.packets.player;

import com.jftse.emulator.server.database.model.player.PlayerStatistic;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CPlayerInfoPlayStatsPacket extends Packet {
    public S2CPlayerInfoPlayStatsPacket(PlayerStatistic playerStatistic) {
        super(PacketOperations.S2CPlayerInfoPlayStatsData.getValueAsChar());

        this.write(playerStatistic.getBasicRecordWin());
        this.write(playerStatistic.getBasicRecordLoss());
        this.write(playerStatistic.getBattleRecordWin());
        this.write(playerStatistic.getBattleRecordLoss());

        this.write(playerStatistic.getConsecutiveWins());
        this.write(0); // ??
        this.write(playerStatistic.getNumberOfDisconnects());
        this.write(playerStatistic.getTotalGames());
        this.write(0); // ??
        this.write(0); // ??
    }
}
