package l2p.gameserver.skills.effects;

import java.util.List;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.EffectList;
import l2p.gameserver.skills.AbnormalEffect;
import l2p.gameserver.skills.EffectType;
import l2p.gameserver.stats.Env;
import l2p.gameserver.stats.StatTemplate;
import l2p.gameserver.stats.Stats;
import l2p.gameserver.stats.conditions.Condition;
import l2p.gameserver.stats.funcs.FuncTemplate;
import l2p.gameserver.templates.StatsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EffectTemplate extends StatTemplate {

    private static final Logger _log = LoggerFactory.getLogger(EffectTemplate.class);
    public static final EffectTemplate[] EMPTY_ARRAY = new EffectTemplate[0];
    public static final String NO_STACK = "none".intern();
    public static final String HP_RECOVER_CAST = "HpRecoverCast".intern();
    public Condition _attachCond;
    public final double _value;
    public final int _count;
    public final long _period; // in milliseconds
    public AbnormalEffect[] _abnormalEffects = null;
    public final EffectType _effectType;
    public final String _stackType;
    public final String _stackType2;
    public final int _stackOrder;
    public final int _displayId;
    public final int _displayLevel;
    public final boolean _applyOnCaster;
    public final boolean _applyOnSummon;
    public final boolean _cancelOnAction;
    public final boolean _isReflectable;
    private final Boolean _isSaveable;
    private final Boolean _isCancelable;
    private final Boolean _isOffensive;
    private boolean _refreshHpOnAdd = false;
    private boolean _refreshMpOnAdd = false;
    private boolean _refreshCpOnAdd = false;
    private final StatsSet _paramSet;
    private final int _chance;

    public EffectTemplate(StatsSet set) {
        _effectType = set.getEnum("name", EffectType.class);
        _value = set.getDouble("value");
        int count = set.getInteger("count", 1);
        _count = count < 0 ? Integer.MAX_VALUE : count;
        long time = (long) (set.getDouble("time", 1.) * 1000);
        _period = time < 0 ? Integer.MAX_VALUE : time;
        String abnormalEffect = set.getString("abnormal", null);
        boolean noAbnormal = false;
        if (abnormalEffect != null) {
            if (!"none".equalsIgnoreCase(abnormalEffect)) {
                String[] sp = abnormalEffect.split(";");
                _abnormalEffects = new AbnormalEffect[sp.length];
                for (int i = 0; i < sp.length; i++) {
                    _abnormalEffects[i] = AbnormalEffect.getByName(sp[i]);
                }
            } else {
                noAbnormal = true;
            }
        }
        if ((!noAbnormal) && (_effectType.getAbnormal() != null)) {
            if (_abnormalEffects != null) {
                AbnormalEffect[] ef = new AbnormalEffect[_abnormalEffects.length + 1];
                System.arraycopy(_abnormalEffects, 0, ef, 0, _abnormalEffects.length);
                ef[_abnormalEffects.length] = _effectType.getAbnormal();
                _abnormalEffects = ef;
            } else {
                _abnormalEffects = new AbnormalEffect[]{_effectType.getAbnormal()};
            }
        }
        _stackType = set.getString("stackType", NO_STACK);
        _stackType2 = set.getString("stackType2", NO_STACK);
        _stackOrder = set.getInteger("stackOrder", _stackType.equals(NO_STACK) && _stackType2.equals(NO_STACK) ? 1 : 0);
        _applyOnCaster = set.getBool("applyOnCaster", false);
        _applyOnSummon = set.getBool("applyOnSummon", true);
        _cancelOnAction = set.getBool("cancelOnAction", false);
        _isReflectable = set.getBool("isReflectable", true);
        _isSaveable = set.isSet("isSaveable") ? set.getBool("isSaveable") : null;
        _isCancelable = set.isSet("isCancelable") ? set.getBool("isCancelable") : null;
        _isOffensive = set.isSet("isOffensive") ? set.getBool("isOffensive") : null;
        _displayId = set.getInteger("displayId", 0);
        _displayLevel = set.getInteger("displayLevel", 0);

        _chance = set.getInteger("chance", -1);
        _paramSet = set;
    }

    public Effect getEffect(Env env) {
        if (_attachCond != null && !_attachCond.test(env)) {
            return null;
        }
        try {
            return _effectType.makeEffect(env, this);
        } catch (Exception e) {
            _log.error("", e);
        }

        return null;
    }

    @Override
    public void attachFunc(FuncTemplate f) {
        super.attachFunc(f);
        if (f._stat == Stats.MAX_HP) {
            _refreshHpOnAdd = true;
        } else if (f._stat == Stats.MAX_MP) {
            _refreshMpOnAdd = true;
        } else if (f._stat == Stats.MAX_CP) {
            _refreshCpOnAdd = true;
        }
    }

    public void attachCond(Condition c) {
        _attachCond = c;
    }

    public int getCount() {
        return _count;
    }

    public long getPeriod() {
        return _period;
    }

    public EffectType getEffectType() {
        return _effectType;
    }

    public Effect getSameByStackType(List<Effect> list) {
        for (Effect ef : list) {
            if (ef != null && EffectList.checkStackType(ef.getTemplate(), this)) {
                return ef;
            }
        }
        return null;
    }

    public Effect getSameByStackType(EffectList list) {
        return getSameByStackType(list.getAllEffects());
    }

    public Effect getSameByStackType(Creature actor) {
        return getSameByStackType(actor.getEffectList().getAllEffects());
    }

    public StatsSet getParam() {
        return _paramSet;
    }

    public int chance() {
        return _chance;
    }

    public boolean isSaveable(boolean def) {
        return _isSaveable != null ? _isSaveable : def;
    }

    public boolean isCancelable(boolean def) {
        return _isCancelable != null ? _isCancelable : def;
    }

    public boolean isOffensive(boolean def) {
        return _isOffensive != null ? _isOffensive : def;
    }

    public boolean refreshHpOnAdd() {
        return _refreshHpOnAdd;
    }

    public boolean refreshMpOnAdd() {
        return _refreshMpOnAdd;
    }

    public boolean refreshCpOnAdd() {
        return _refreshCpOnAdd;
    }
}
