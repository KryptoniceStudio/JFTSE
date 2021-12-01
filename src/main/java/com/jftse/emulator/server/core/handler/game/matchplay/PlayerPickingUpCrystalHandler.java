package com.jftse.emulator.server.core.handler.game.matchplay;

import com.jftse.emulator.server.core.constants.GameFieldSide;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.battle.SkillCrystal;
import com.jftse.emulator.server.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.core.matchplay.event.RunnableEventHandler;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.packets.matchplay.C2SMatchplayPlayerPicksUpCrystal;
import com.jftse.emulator.server.core.packet.packets.matchplay.S2CMatchplayGiveRandomSkill;
import com.jftse.emulator.server.core.task.PlaceCrystalRandomlyTask;
import com.jftse.emulator.server.networking.packet.Packet;

public class PlayerPickingUpCrystalHandler extends AbstractHandler {
    private C2SMatchplayPlayerPicksUpCrystal playerPicksUpCrystalPacket;

    private final RunnableEventHandler runnableEventHandler;

    public PlayerPickingUpCrystalHandler() {
        runnableEventHandler = GameManager.getInstance().getRunnableEventHandler();
    }

    @Override
    public boolean process(Packet packet) {
        playerPicksUpCrystalPacket = new C2SMatchplayPlayerPicksUpCrystal(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActiveGameSession() == null
                || connection.getClient().getActiveRoom() == null || connection.getClient().getActivePlayer() == null)
            return;

        Room room = connection.getClient().getActiveRoom();
        RoomPlayer roomPlayer = room.getRoomPlayerList().stream()
                .filter(rp -> rp.getPlayer().getId().equals(connection.getClient().getActivePlayer().getId()))
                .findFirst()
                .orElse(null);
        if (roomPlayer == null)
            return;

        short playerPosition = roomPlayer.getPosition();
        GameSession gameSession = connection.getClient().getActiveGameSession();
        MatchplayGame game = gameSession.getActiveMatchplayGame();

        boolean isBattleGame = game instanceof MatchplayBattleGame;

        SkillCrystal skillCrystal = isBattleGame ?
                ((MatchplayBattleGame) game).getSkillCrystals().stream()
                        .filter(x -> x.getId() == playerPicksUpCrystalPacket.getCrystalId())
                        .findFirst()
                        .orElse(null) :
                ((MatchplayGuardianGame) game).getSkillCrystals().stream()
                        .filter(x -> x.getId() == playerPicksUpCrystalPacket.getCrystalId())
                        .findFirst()
                        .orElse(null);

        if (skillCrystal != null) {
            short gameFieldSide = -1;
            if (isBattleGame)
                gameFieldSide = game.isRedTeam(playerPosition) ? GameFieldSide.RedTeam : GameFieldSide.BlueTeam;

            S2CMatchplayGiveRandomSkill randomGuardianSkill = new S2CMatchplayGiveRandomSkill(playerPicksUpCrystalPacket.getCrystalId(), (byte) playerPosition);
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(randomGuardianSkill, connection);

            if (isBattleGame) {
                int skillCrystalsSize = ((MatchplayBattleGame) game).getSkillCrystals().size();
                for (int i = 0; i < skillCrystalsSize; i++) {
                    SkillCrystal current = ((MatchplayBattleGame) game).getSkillCrystals().poll();

                    if (current.getId() == skillCrystal.getId()) {
                        skillCrystalsSize--;
                        continue;
                    }
                    ((MatchplayBattleGame) game).getSkillCrystals().offer(current);
                }
            } else {
                int skillCrystalsSize = ((MatchplayGuardianGame) game).getSkillCrystals().size();
                for (int i = 0; i < skillCrystalsSize; i++) {
                    SkillCrystal current = ((MatchplayGuardianGame) game).getSkillCrystals().poll();

                    if (current.getId() == skillCrystal.getId()) {
                        skillCrystalsSize--;
                        continue;
                    }
                    ((MatchplayGuardianGame) game).getSkillCrystals().offer(current);
                }
            }

            PlaceCrystalRandomlyTask placeCrystalRandomlyTask = isBattleGame ? new PlaceCrystalRandomlyTask(connection, gameFieldSide) : new PlaceCrystalRandomlyTask(connection);
            long crystalSpawnInterval = isBattleGame ? ((MatchplayBattleGame) game).getCrystalSpawnInterval().get() : ((MatchplayGuardianGame) game).getCrystalSpawnInterval().get();

            RunnableEvent runnableEvent = runnableEventHandler.createRunnableEvent(placeCrystalRandomlyTask, crystalSpawnInterval);
            gameSession.getRunnableEvents().add(runnableEvent);
        }
    }
}
