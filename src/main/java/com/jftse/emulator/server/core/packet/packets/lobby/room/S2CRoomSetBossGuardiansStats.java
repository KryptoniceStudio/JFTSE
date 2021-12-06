package com.jftse.emulator.server.core.packet.packets.lobby.room;

import com.jftse.emulator.server.core.matchplay.battle.GuardianBattleState;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.database.model.battle.BossGuardian;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class S2CRoomSetBossGuardiansStats extends Packet {
    public S2CRoomSetBossGuardiansStats(ConcurrentLinkedDeque<GuardianBattleState> guardianBattleStates, BossGuardian bossGuardian, List<Byte> guardians) {
        super(PacketOperations.S2CRoomSetBossGuardiansStats.getValueAsChar());

        // prepare determined guardians
        final List<Byte> finalGuardians = new ArrayList<>();
        finalGuardians.add(bossGuardian.getId().byteValue());
        finalGuardians.add(guardians.get(0));
        finalGuardians.add(guardians.get(1));

        final ConcurrentLinkedDeque<GuardianBattleState> finalGuardianBattleStates = new ConcurrentLinkedDeque<>();

        // sort by determined guardians
        finalGuardians.forEach(g -> {
            for (Iterator<GuardianBattleState> it = guardianBattleStates.iterator(); it.hasNext(); ) {
                GuardianBattleState guardianBattleState = it.next();

                if (g == (byte) guardianBattleState.getId())
                    finalGuardianBattleStates.add(guardianBattleState);
            }
        });

        this.write((byte) finalGuardianBattleStates.size());

        Iterator<GuardianBattleState> it;
        byte i;
        for (it = finalGuardianBattleStates.iterator(), i = 0; it.hasNext(); i++)
        {
            GuardianBattleState guardianBattleState = it.next();

            this.write(i);
            this.write((short) guardianBattleState.getMaxHealth().get());
            this.write((byte) guardianBattleState.getStr().get());
            this.write((byte) guardianBattleState.getSta().get());
            this.write((byte) guardianBattleState.getDex().get());
            this.write((byte) guardianBattleState.getWill().get());
        }
    }
}