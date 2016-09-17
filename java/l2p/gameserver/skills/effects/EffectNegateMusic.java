package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Effect;
import l2p.gameserver.stats.Env;

public class EffectNegateMusic extends Effect {

    public EffectNegateMusic(Env env, EffectTemplate template) {
        super(env, template);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onExit() {
        super.onExit();
    }

    @Override
    public boolean onActionTime() {
        _effected.getEffectList().getAllEffects().stream().filter(e -> e.getSkill().isMusic()).forEach(Effect::exit);
        return false;
    }
}