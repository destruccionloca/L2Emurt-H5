package ai.superpoint;

import l2p.gameserver.model.instances.NpcInstance;

/**
 * Created by JunkyFunky on 17.04.2016.
 */
public class Mystic extends SuperPointDefaultAI {

    public Mystic(NpcInstance actor) {
        super(actor);
    }

    @Override
    protected boolean thinkActive() {
        return super.thinkActive() || defaultThinkBuff(10);
    }

    @Override
    protected boolean createNewTask() {
        return defaultFightTask();
    }

    @Override
    public int getRatePHYS() {
        return _damSkills.length == 0 ? 25 : 0;
    }

    @Override
    public int getRateDOT() {
        return 25;
    }

    @Override
    public int getRateDEBUFF() {
        return 20;
    }

    @Override
    public int getRateDAM() {
        return 100;
    }

    @Override
    public int getRateSTUN() {
        return 10;
    }

    @Override
    public int getRateBUFF() {
        return 10;
    }

    @Override
    public int getRateHEAL() {
        return 20;
    }
}
