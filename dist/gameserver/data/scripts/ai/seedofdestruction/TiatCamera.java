package ai.seedofdestruction;

import java.util.ArrayList;
import java.util.List;

import l2p.gameserver.ai.DefaultAI;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.World;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.serverpackets.ExStartScenePlayer;

public class TiatCamera extends DefaultAI {

    private List<Player> _players = new ArrayList<>();

    public TiatCamera(NpcInstance actor) {
        super(actor);
        actor.startImmobilized();
        actor.startDamageBlocked();
    }

    @Override
    protected boolean thinkActive() {
        NpcInstance actor = getActor();
        World.getAroundPlayers(actor, 300, 300).stream().filter(p -> !_players.contains(p)).forEach(p -> {
            p.showQuestMovie(ExStartScenePlayer.SCENE_TIAT_OPENING);
            _players.add(p);
        });
        return true;
    }
}