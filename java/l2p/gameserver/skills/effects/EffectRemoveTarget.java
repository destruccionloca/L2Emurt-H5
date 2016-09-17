package l2p.gameserver.skills.effects;

import l2p.gameserver.ai.CtrlEvent;
import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.ai.DefaultAI;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.stats.Env;

/**
 * @author nonam3
 * @date 08/01/2011 17:37
 * @editor JunkyFunky
 * @date 02/11/2015 11:43
 */
public final class EffectRemoveTarget extends Effect {

    private boolean _doStopTarget;

    public EffectRemoveTarget(Env env, EffectTemplate template) {
        super(env, template);
        _doStopTarget = template.getParam().getBool("doStopTarget", false);
    }


    @Override
    public void onStart() {
        if (getEffected().getAI() instanceof DefaultAI) {
            ((DefaultAI) getEffected().getAI()).setGlobalAggro(System.currentTimeMillis() + 3000L);
        }

        getEffected().setTarget(null);
        if (_doStopTarget) {
            getEffected().stopMove();
        }
			
		if (getEffected().isNpc()) {
			NpcInstance npc = (NpcInstance)getEffected();
			if(npc.isAttackingNow()) {
				npc.abortAttack(true, true);
			}
			if (npc.isCastingNow()) {
				npc.abortCast(true, true);
			}
			getEffected().setTarget(null);
			npc.getAI().notifyEvent(CtrlEvent.EVT_THINK);
		} else {
			if(getEffected().isAttackingNow()) {
				getEffected().abortAttack(true, true);
				getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, getEffector());
			}
			if (getEffected().isCastingNow()) {
				getEffected().abortCast(true, true);
			}
			getEffected().setTarget(null);
		}
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}
