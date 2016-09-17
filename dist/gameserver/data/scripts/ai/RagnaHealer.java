package ai;

import java.util.List;

import l2p.gameserver.ai.CtrlEvent;
import l2p.gameserver.ai.Priest;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.instances.NpcInstance;

/**
 * @author Diamond
 */
public class RagnaHealer extends Priest {

    private long lastFactionNotifyTime;

    public RagnaHealer(NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtAttacked(Creature attacker, int damage) {
        NpcInstance actor = getActor();
        if (attacker == null) {
            return;
        }

        if (System.currentTimeMillis() - lastFactionNotifyTime > 10000) {
            lastFactionNotifyTime = System.currentTimeMillis();
            List<NpcInstance> around = actor.getAroundNpc(500, 300);
            if (around != null && !around.isEmpty()) {
                around.stream().filter(npc -> npc.isMonster() && npc.getNpcId() >= 22691 && npc.getNpcId() <= 22702).forEach(npc -> {
                    npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 5000);
                });
            }
        }

        super.onEvtAttacked(attacker, damage);
    }
}