package l2p.gameserver.skills.skillclasses;

import java.util.List;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.instances.TrapInstance;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.templates.StatsSet;

public class DefuseTrap extends Skill {

    public DefuseTrap(StatsSet set) {
        super(set);
    }

    @Override
    public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first) {
        if (target == null || !target.isTrap()) {
            activeChar.sendPacket(SystemMsg.INVALID_TARGET);
            return false;
        }

        return super.checkCondition(activeChar, target, forceUse, dontMove, first);
    }

    @Override
    public void useSkill(Creature activeChar, List<Creature> targets) {
        targets.stream().filter(target -> target != null && target.isTrap()).forEach(target -> {

            TrapInstance trap = (TrapInstance) target;
            if (trap.getLevel() <= getPower()) {
                trap.deleteMe();
            }
        });

        if (isSSPossible()) {
            activeChar.unChargeShots(isMagic());
        }
    }
}