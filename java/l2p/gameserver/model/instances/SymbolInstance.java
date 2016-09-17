package l2p.gameserver.model.instances;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import l2p.commons.threading.RunnableImpl;
import l2p.commons.util.Rnd;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.GameObjectTasks;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.taskmanager.EffectTaskManager;
import l2p.gameserver.templates.npc.NpcTemplate;

public class SymbolInstance extends NpcInstance {

    private final Creature _owner;
    private final Skill _skill;
    private ScheduledFuture<?> _targetTask;
    private ScheduledFuture<?> _destroyTask;

    public SymbolInstance(int objectId, NpcTemplate template, Creature owner, Skill skill) {
        super(objectId, template);
        _owner = owner;
        _skill = skill;

        setReflection(owner.getReflection());
        setLevel(owner.getLevel());
        setTitle(owner.getName());
    }

    @Override
    public Player getPlayer() {
        return _owner != null ? _owner.getPlayer() : null;
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();

        _destroyTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.DeleteTask(this), 120000L);

        _targetTask = EffectTaskManager.getInstance().scheduleAtFixedRate(new RunnableImpl() {
            @Override
            public void runImpl() throws Exception {
                getAroundCharacters(200, 200).stream().filter(target -> _skill.checkTarget(_owner, target, null, false, false) == null).forEach(target -> {
                    List<Creature> targets = new ArrayList<>();

                    if (!_skill.isAoE()) {
                        targets.add(target);
                    } else {
                        for (Creature t : getAroundCharacters(_skill.getSkillRadius(), 128)) {
                            if (_skill.checkTarget(_owner, t, null, false, false) == null) {
                                targets.add(target);
                            }
                        }
                    }

                    _skill.useSkill(SymbolInstance.this, targets);
                });
            }
        }, 1000L, Rnd.get(4000L, 7000L));
    }

    @Override
    protected void onDelete() {
        if (_destroyTask != null) {
            _destroyTask.cancel(false);
        }
        _destroyTask = null;
        if (_targetTask != null) {
            _targetTask.cancel(false);
        }
        _targetTask = null;
        super.onDelete();
    }

    @Override
    public int getPAtk(Creature target) {
        return _owner != null ? _owner.getPAtk(target) : 0;
    }

    @Override
    public int getMAtk(Creature target, Skill skill) {
        return _owner != null ? _owner.getMAtk(target, skill) : 0;
    }

    @Override
    public boolean hasRandomAnimation() {
        return false;
    }

    @Override
    public boolean isAutoAttackable(Creature attacker) {
        return false;
    }

    @Override
    public boolean isAttackable(Creature attacker) {
        return false;
    }

    @Override
    public boolean isInvul() {
        return true;
    }

    @Override
    public boolean isFearImmune() {
        return true;
    }

    @Override
    public boolean isParalyzeImmune() {
        return true;
    }

    @Override
    public boolean isLethalImmune() {
        return true;
    }

    @Override
    public void showChatWindow(Player player, int val, Object... arg) {
    }

    @Override
    public void showChatWindow(Player player, String filename, Object... replace) {
    }

    @Override
    public void onBypassFeedback(Player player, String command) {
    }

    @Override
    public boolean isTargetable() {
        return false;
    }

    @Override
    public Clan getClan() {
        return null;
    }
}
