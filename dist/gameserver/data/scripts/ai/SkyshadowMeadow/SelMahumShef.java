package ai.SkyshadowMeadow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import l2p.commons.util.Rnd;
import l2p.gameserver.ai.Fighter;
import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.utils.Location;


public class SelMahumShef extends Fighter {

    private Location targetLoc;
    private long wait_timeout = 0;

    public SelMahumShef(NpcInstance actor) {
        super(actor);
        MAX_PURSUE_RANGE = Integer.MAX_VALUE;
    }

    @Override
    protected boolean thinkActive() {
        NpcInstance actor = getActor();
        if (actor.isDead()) {
            return true;
        }

        if (_def_think) {
            doTask();
            return true;
        }
        if (System.currentTimeMillis() > wait_timeout) {
            wait_timeout = System.currentTimeMillis() + 2000;
            actor.setWalking();
            targetLoc = findFirePlace(actor);
            addTaskMove(targetLoc, true);
            doTask();
            return true;
        }
        return false;
    }

    private Location findFirePlace(NpcInstance actor) {
        Location loc = new Location();
        List<NpcInstance> list = actor.getAroundNpc(3000, 600).stream().filter(npc -> npc.getNpcId() == 18927 && GeoEngine.canSeeTarget(actor, npc, false)).collect(Collectors.toList());
        if (!list.isEmpty()) {
            loc = list.get(Rnd.get(list.size())).getLoc();
        } else {
            loc = Location.findPointToStay(actor, 1000, 1500);
        }
        return loc;
    }

    @Override
    protected boolean maybeMoveToHome() {
        return false;
    }

    @Override
    public boolean isGlobalAI() {
        return true;
    }
}
