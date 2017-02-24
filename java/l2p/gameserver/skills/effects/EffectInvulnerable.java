package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Skill.SkillType;
import l2p.gameserver.stats.Env;

public final class EffectInvulnerable extends Effect {

    public EffectInvulnerable(Env env, EffectTemplate template) {
        super(env, template);
    }

    @Override
    public boolean checkCondition() {
        Skill skill = _effected.getCastingSkill();

        if (isInvulOrCancel(_effected.isInvul(), skill)) {
            return false;
        }

        if (skill != null && (skill.getSkillType() == SkillType.TAKECASTLE || skill.getSkillType() == SkillType.TAKEFORTRESS || skill.getSkillType() == SkillType.TAKEFLAG)) {
            return false;
        }
        return super.checkCondition();
    }

    @Override
    public void onStart() {
        super.onStart();
        _effected.startHealBlocked();
        _effected.setIsInvul(true);
    }

    @Override
    public void onExit() {
        super.onExit();
        _effected.stopHealBlocked();
        _effected.setIsInvul(false);
    }

    @Override
    public boolean onActionTime() {
        return false;
    }

    /**
     * Method isInvulOrCancel.
     * @return boolean
     */
    private boolean isInvulOrCancel(boolean invul, Skill skill)
    {
        int id = skill.getId();
        int[] ids = {342, 762, 6094, 1056, 1344, 1345, 1350, 1351, 1360, 1361, 1358, 1359, 455, 342, 1440, 3651, 5682, 8331};
        boolean result = false;
        if(invul) {
            result = invul;
            for (int i = 0; i < (ids.length); i++) {
                if (ids[i] == id) {
                    result = false;
                }
            }
        }
        return result;
    }
}