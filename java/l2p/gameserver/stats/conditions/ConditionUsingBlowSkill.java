package l2p.gameserver.stats.conditions;

import l2p.gameserver.stats.Env;

public class ConditionUsingBlowSkill extends Condition {

    private final boolean _flag;

    public ConditionUsingBlowSkill(boolean flag) {
        _flag = flag;
    }

    @Override
    protected boolean testImpl(Env env) {
        if (env.skill == null) {
            return !_flag;
        }
        return env.skill.isBlowSkill() == _flag;
    }
}
