package ai;

import l2p.commons.lang.reference.HardReference;
import l2p.commons.lang.reference.HardReferences;
import l2p.commons.util.Rnd;
import l2p.gameserver.ai.CtrlEvent;
import l2p.gameserver.ai.Fighter;
import l2p.gameserver.model.MinionList;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.World;
import l2p.gameserver.model.instances.MinionInstance;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.scripts.Functions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Kama56Boss extends Fighter {

    private long _nextOrderTime = 0;
    private HardReference<Player> _lastMinionsTargetRef = HardReferences.emptyRef();

    public Kama56Boss(NpcInstance actor) {
        super(actor);
    }

    private void sendOrderToMinions(NpcInstance actor) {
        if (!actor.isInCombat()) {
            _lastMinionsTargetRef = HardReferences.emptyRef();
            return;
        }

        MinionList ml = actor.getMinionList();
        if (ml == null || !ml.hasMinions()) {
            _lastMinionsTargetRef = HardReferences.emptyRef();
            return;
        }

        long now = System.currentTimeMillis();
        if (_nextOrderTime > now && _lastMinionsTargetRef.get() != null) {
            Player old_target = _lastMinionsTargetRef.get();
            if (old_target != null && !old_target.isAlikeDead()) {
                ml.getAliveMinions().stream().filter(m -> m.getAI().getAttackTarget() != old_target).forEach(m -> {
                    m.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, old_target, 10000000);
                });
                return;
            }
        }

        _nextOrderTime = now + 30000;

        List<Player> pl = World.getAroundPlayers(actor);
        if (pl.isEmpty()) {
            _lastMinionsTargetRef = HardReferences.emptyRef();
            return;
        }

        List<Player> alive = pl.stream().filter(p -> !p.isAlikeDead()).collect(Collectors.toCollection(ArrayList::new));
        if (alive.isEmpty()) {
            _lastMinionsTargetRef = HardReferences.emptyRef();
            return;
        }

        Player target = alive.get(Rnd.get(alive.size()));
        _lastMinionsTargetRef = target.getRef();

        Functions.npcSayCustomMessage(actor, "Kama56Boss.attack", target.getName());
        for (MinionInstance m : ml.getAliveMinions()) {
            m.getAggroList().clear();
            m.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, target, 10000000);
        }
    }

    @Override
    protected void thinkAttack() {
        NpcInstance actor = getActor();
        if (actor == null) {
            return;
        }

        sendOrderToMinions(actor);
        super.thinkAttack();
    }
}