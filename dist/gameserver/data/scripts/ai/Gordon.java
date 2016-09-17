package ai;

import l2p.commons.util.Rnd;
import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.ai.Fighter;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.utils.Location;

public class Gordon extends Fighter {

    static final Location[] points = {
        new Location(141569, -45908, -2387),
		new Location(142494, -45456, -2397),
		new Location(142922, -44561, -2395),
		new Location(143672, -44130, -2398),
		new Location(144557, -43378, -2325),
		new Location(145839, -43267, -2301),
		new Location(147044, -43601, -2307),
		new Location(148140, -43206, -2303),
		new Location(148815, -43434, -2328),
		new Location(149862, -44151, -2558),
		new Location(151037, -44197, -2708),
		new Location(152555, -42756, -2836),
		new Location(154808, -39546, -3236),
		new Location(155333, -39962, -3272),
		new Location(156531, -41240, -3470),
		new Location(156863, -43232, -3707),
		new Location(156783, -44198, -3764),
		new Location(158169, -45163, -3541),
		new Location(158952, -45479, -3473),
		new Location(160039, -46514, -3634),
		new Location(160244, -47429, -3656),
		new Location(159155, -48109, -3665), 
		new Location(159558, -51027, -3523),
		new Location(159396, -53362, -3244),
		new Location(160872, -56556, -2789),
		new Location(160857, -59072, -2613),
		new Location(160410, -59888, -2647),
		new Location(158770, -60173, -2673),
		new Location(156368, -59557, -2638),
		new Location(155188, -59868, -2642),
		new Location(154118, -60591, -2731),
		new Location(153571, -61567, -2821),
		new Location(153457, -62819, -2886), 
		new Location(152939, -63778, -3003),
		new Location(151816, -64209, -3120), 
		new Location(147655, -64826, -3433), 
		new Location(145422, -64576, -3369),
		new Location(144097, -64320, -3404), 
		new Location(140780, -61618, -3096), 
		new Location(139688, -61450, -3062), 
		new Location(138267, -61743, -3056),
		new Location(138613, -58491, -3465), 
		new Location(138139, -57252, -3517),
		new Location(139555, -56044, -3310),
		new Location(139107, -54537, -3240), 
		new Location(139279, -53781, -3091),
		new Location(139810, -52687, -2866),
		new Location(139657, -52041, -2793),
		new Location(139215, -51355, -2698),
		new Location(139334, -50514, -2594),
		new Location(139817, -49715, -2449),
		new Location(139824, -48976, -2263), 
		new Location(140130, -47578, -2213),
		new Location(140483, -46339, -2382),
		new Location(141569, -45908, -2387)};
    private int current_point = -1;
    private long wait_timeout = 0;
    private boolean wait = false;

    public Gordon(NpcInstance actor) {
        super(actor);
    }

    @Override
    public boolean isGlobalAI() {
        return true;
    }

    @Override
    public boolean checkAggression(Creature target) {
        // Агрится только на носителей проклятого оружия
        if (!target.isCursedWeaponEquipped()) {
            return false;
        }
        // Продолжит идти с предыдущей точки
        if (getIntention() != CtrlIntention.AI_INTENTION_ACTIVE && current_point > -1) {
            current_point--;
        }
        return super.checkAggression(target);
    }

    @Override
    protected boolean thinkActive() {
        NpcInstance actor = getActor();
        if (actor.isDead()) {
            return true;
        }

        if (_def_think) {
            if (doTask()) {
                clearTasks();
            }
            return true;
        }

        // BUFF
        if (super.thinkActive()) {
            return true;
        }

        if (System.currentTimeMillis() > wait_timeout && (current_point > -1 || Rnd.chance(5))) {
            if (!wait && current_point == 55) {
                wait_timeout = System.currentTimeMillis() + 60000;
                wait = true;
                return true;
            }

            wait_timeout = 0;
            wait = false;
            current_point++;

            if (current_point >= points.length) {
                current_point = 0;
            }

            actor.setWalking();

            addTaskMove(points[current_point], true);
            doTask();
            return true;
        }

        if (randomAnimation()) {
            return false;
        }

        return false;
    }

    @Override
    protected boolean randomWalk() {
        return false;
    }
}