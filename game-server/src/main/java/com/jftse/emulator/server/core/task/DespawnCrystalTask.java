package com.jftse.emulator.server.core.task;

import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.event.RunnableEvent;
import com.jftse.emulator.server.core.matchplay.event.RunnableEventHandler;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayLetCrystalDisappear;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.matchplay.battle.SkillCrystal;
import com.jftse.server.core.thread.AbstractTask;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class DespawnCrystalTask extends AbstractTask {
    private final FTConnection connection;
    private SkillCrystal skillCrystal;
    private final short gameFieldSide;

    private final RunnableEventHandler runnableEventHandler;

    public DespawnCrystalTask(FTConnection connection, SkillCrystal skillCrystal, short gameFieldSide) {
        this.connection = connection;
        this.skillCrystal = skillCrystal;
        this.gameFieldSide = gameFieldSide;

        runnableEventHandler = GameManager.getInstance().getRunnableEventHandler();
    }

    public DespawnCrystalTask(FTConnection connection, SkillCrystal skillCrystal) {
        this.connection = connection;
        this.skillCrystal = skillCrystal;
        this.gameFieldSide = -1;

        runnableEventHandler = GameManager.getInstance().getRunnableEventHandler();
    }

    @Override
    public void run() {
        if (connection.getClient() == null) return;

        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession == null) return;

        MatchplayGame game = gameSession.getMatchplayGame();
        boolean isBattleGame = false;
        if (gameSession.getMatchplayGame() instanceof MatchplayBattleGame)
            isBattleGame = true;

        boolean isCrystalStillAvailable =
                isBattleGame && (((MatchplayBattleGame) game).getSkillCrystals().stream().anyMatch(x -> x.getId() == skillCrystal.getId())) ||
                !isBattleGame && (((MatchplayGuardianGame) game).getSkillCrystals().stream().anyMatch(x -> x.getId() == skillCrystal.getId()));

        if (isCrystalStillAvailable) {
            S2CMatchplayLetCrystalDisappear letCrystalDisappearPacket = new S2CMatchplayLetCrystalDisappear(skillCrystal.getId());
            GameManager.getInstance().sendPacketToAllClientsInSameGameSession(letCrystalDisappearPacket, connection);

            ArrayList<SkillCrystal> skillCrystals = isBattleGame ? ((MatchplayBattleGame) game).getSkillCrystals() : ((MatchplayGuardianGame) game).getSkillCrystals();
            skillCrystals.stream().collect(Collectors.toList()).forEach(current -> {
                if (current.getId() == skillCrystal.getId()) {
                    skillCrystals.remove(current);
                    return;
                }
            });

            RunnableEvent runnableEvent = null;
            if (isBattleGame)
                runnableEvent = runnableEventHandler.createRunnableEvent(new PlaceCrystalRandomlyTask(connection, gameFieldSide), ((MatchplayBattleGame) game).getCrystalSpawnInterval());
            else
                runnableEvent = runnableEventHandler.createRunnableEvent(new PlaceCrystalRandomlyTask(connection), ((MatchplayGuardianGame) game).getCrystalSpawnInterval());

            gameSession.getRunnableEvents().add(runnableEvent);
        }
    }
}