/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package ai.superpoint;

import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.instances.NpcInstance;

/**
 * Created by JunkyFunky on 16.04.2016.
 */
public class CursedAgroFighter extends Fighter {

    public CursedAgroFighter(NpcInstance actor) {
        super(actor);
    }

    @Override
    public boolean checkAggression(Creature target) {
        // Агрится только на носителей проклятого оружия
        if (!target.isCursedWeaponEquipped()) {
            return false;
        }
        // Продолжит идти с предыдущей точки
        if (getIntention() != CtrlIntention.AI_INTENTION_ACTIVE && _currentNodeIndex > -1) {
            _currentNodeIndex--;
        }
        return super.checkAggression(target);
    }
}
