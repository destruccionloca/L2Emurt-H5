package l2p.gameserver.skills.skillclasses;

import java.util.List;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Skill;
import l2p.gameserver.templates.StatsSet;

public class AIeffects extends Skill {

    public AIeffects(StatsSet set) {
        super(set);
    }

    @Override
    public void useSkill(Creature activeChar, List<Creature> targets) {
        targets.stream().filter(target -> target != null).forEach(target -> {
            getEffects(activeChar, target, true, false);
        });

        if (isSSPossible()) {
            activeChar.unChargeShots(isMagic());
        }
    }
}
