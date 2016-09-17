package ai.other.PailakaDevilsLegacy;

import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.ai.Fighter;
import l2p.gameserver.model.World;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.tables.SkillTable;

/**
 * - AI мобов Followers Lematan, миньёны-лекари Боса Lematan в пайлаке 61-67. -
 * Не умеют ходить, лечат Боса.
 */
public class FollowersLematan extends Fighter {

    private static int LEMATAN = 18633;

    public FollowersLematan(NpcInstance actor) {
        super(actor);
        startSkillTimer();
    }

    private void findBoss() {
        NpcInstance minion = getActor();
        if (minion == null) {
            return;
        }

        World.getAroundNpc(minion, 1000, 1000).stream().filter(target -> target.getNpcId() == LEMATAN && target.getCurrentHpPercents() < 65).forEach(target -> {
            minion.doCast(SkillTable.getInstance().getInfo(5712, 1), target, true);
        });
        return;
    }

    public void startSkillTimer() {
        if (getActor() != null) {
            ScheduleTimerTask(20000);
        }
    }

    public void ScheduleTimerTask(long time) {
        ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
            @Override
            public void runImpl() throws Exception {
                findBoss();
                startSkillTimer();
            }
        }, time);
    }

    @Override
    protected void onEvtDead(Creature killer) {
        // stop timers if any
        super.onEvtDead(killer);
    }

    @Override
    protected boolean randomWalk() {
        return false;
    }
}