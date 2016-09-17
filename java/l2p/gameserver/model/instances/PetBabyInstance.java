package l2p.gameserver.model.instances;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Future;

import l2p.commons.threading.RunnableImpl;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.model.EffectList;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Skill.SkillType;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.tables.PetDataTable;
import l2p.gameserver.tables.PetSkillsTable;
import l2p.gameserver.tables.PetSkillsTable.SupportPetSkill;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.templates.npc.NpcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PetBabyInstance extends PetInstance {

    private static final Logger _log = LoggerFactory.getLogger(PetBabyInstance.class);
    private Future<?> _actionTask;
	private static final int BUFF_CONTROL = 5771;
	private static final int AWAKENING = 5753;
	private static final int HOWL = 6053;
	private static final int BATTLE_CRY = 6049;
	protected List<Skill> _buffs = null;
	private Skill _majorHeal = null;
	private Skill _minorHeal = null;
	private Skill _recharge = null;
    private boolean _buffEnabled = true;

    public PetBabyInstance(int objectId, NpcTemplate template, Player owner, ItemInstance control, int _currentLevel, long exp) {
        super(objectId, template, owner, control, _currentLevel, exp);
    }

    public PetBabyInstance(int objectId, NpcTemplate template, Player owner, ItemInstance control) {
        super(objectId, template, owner, control);
    }

	@Override
	public void onSpawn() {
		super.onSpawn();
		updateAvilableSkill();
	}

    class ActionTask extends RunnableImpl {

        @Override
        public void runImpl() throws Exception {
            Skill skill = onActionTask();
            _actionTask = ThreadPoolManager.getInstance().schedule(new ActionTask(), skill == null ? 1000 : skill.getHitTime() * 333 / Math.max(getMAtkSpd(), 1) - 100);
        }
    }
    
    public void updateAvilableSkill() {    
		
		double healPower = 0;
		if(_buffs != null) {
			_buffs.clear();
		}
		for (SupportPetSkill psl : PetSkillsTable.getAvailableSkills()) {
			int id = psl.getSkillId();
			int lvl = PetSkillsTable.getAvailableSupportLevel(getNpcId(), id, getLevel());
			if (lvl == 0) {
				continue;
			}
			
			final Skill skill = SkillTable.getInstance().getInfo(id, lvl);
			if (skill == null) {
				continue;
			}
			
			if (skill.getId() == BUFF_CONTROL || skill.getId() == AWAKENING || skill.getId() == HOWL || skill.getId() == BATTLE_CRY) {
				continue;
			}
			
			if (skill.getSkillType() == SkillType.MANAHEAL || skill.getSkillType() == SkillType.MANAHEAL_PERCENT) {
				_recharge = skill;
				continue;
			}
			
			if (skill.getSkillType() == SkillType.HEAL || skill.getSkillType() == SkillType.HEAL_PERCENT) {
				if (healPower == 0) {
					// установка скилла лечения
					_majorHeal = skill;
					_minorHeal = _majorHeal;
					healPower = skill.getPower();
				} else {
					// установка сильного лечения если есть скилл который сильнее чем minorHeal
					if (skill.getPower() > healPower) {
						_majorHeal = skill;
					} else {
						_minorHeal = skill;
					}
				}
				continue;
			}
			
			if (skill.getSkillType() == SkillType.BUFF) {
				if (_buffs == null) {
					_buffs = new ArrayList<>();
				}
				_buffs.add(skill);
			}
		}
    }

    public Skill onActionTask() {
        try {
            Player owner = getPlayer();
            if (!owner.isDead() && !owner.isInvul() && !isCastingNow()) {
                if (getEffectList().getEffectsCountForSkill(5753) > 0) {
                    return null;
                }
                if (getEffectList().getEffectsCountForSkill(5771) > 0) {
                    return null;
                }
                boolean improved = PetDataTable.isImprovedBabyPet(getNpcId());
                Skill skill = null;
                if (!Config.ALT_PET_HEAL_BATTLE_ONLY || owner.isInCombat()) {
                    // проверка лечения
					double hpPercent = owner.getCurrentHp() / owner.getMaxHp();
					if (improved && hpPercent < 0.3 || !improved && hpPercent < 0.15) {
						skill = _majorHeal;
                    }
					else if (_majorHeal != _minorHeal && (improved && hpPercent < 0.7 || !improved && hpPercent < 0.8)) {
						skill = _minorHeal;
                    }
					// проверка речарджа
                    // Речардж только у Kookaburra или Fairy Princess или Spirit Shaman и в комбат моде
                    if (skill == null && (getNpcId() == PetDataTable.IMPROVED_BABY_KOOKABURRA_ID || getNpcId() == PetDataTable.FAIRY_PRINCESS_ID || getNpcId() == PetDataTable.SPIRIT_SHAMAN_ID)) {

						if (_recharge != null && owner.getCurrentMp() / owner.getMaxMp() < 0.6 && Rnd.get(100) <= 60) {
							skill = _recharge;
                        }
                    }
                    if (skill != null) {
						if (getCurrentMp() >= skill.getMpConsume2()) {
							castSkill(owner, skill);
						}
                        return skill;
                    }
                }
                if (!improved || /* owner.isInZonePeace() || */ owner.isInOfflineMode() || (owner.getEffectList().getEffectsCountForSkill(5771) > 0)) // Buff Control
                {
                    return null;
                }
				if (_buffs != null && !_buffs.isEmpty()) {
					outer:
					for (Skill buff : _buffs) {
						if (getCurrentMp() < buff.getMpConsume2()) {
							continue;
						}
						for (Effect ef : owner.getEffectList().getAllEffects()) {
							if (checkEffect(ef, buff)) {
								continue outer;
							}
						}
						if (buff != null) {
							castSkill(owner, buff);
							return buff;
						}
						return null;
					}
				}
            }
        } catch (Throwable e) {
            _log.warn("Pet [#" + getNpcId() + "] a buff task error has occurred: " + e);
            _log.error("", e);
        }
        return null;
    }

    /**
     * Возвращает true если эффект для скилла уже есть и заново накладывать не
     * надо
     */
    private boolean checkEffect(Effect ef, Skill skill) {
        if (ef == null || !ef.isInUse() || !EffectList.checkStackType(ef.getTemplate(), skill.getEffectTemplates()[0])) // такого скилла нет
        {
            return false;
        }
        if (ef.getStackOrder() < skill.getEffectTemplates()[0]._stackOrder) // старый слабее
        {
            return false;
        }
        if (ef.getTimeLeft() > 10) // старый не слабее и еще не кончается - ждем
        {
            return true;
        }
        if (ef.getNext() != null) // старый не слабее но уже кончается - проверить рекурсией что там зашедулено
        {
            return checkEffect(ef.getNext(), skill);
        }
        return false;
    }

	private synchronized void castSkill(Player owner, Skill skill) {

		if(skill.checkCondition(this, owner, false, !isFollowMode(), true)) {
			setTarget(owner);
			getAI().Cast(skill, owner, false, !isFollowMode());
		}
		//final SystemMessage msg = new SystemMessage(SystemMsg.THE_PET_USES_S1);
		//msg.addSkillName(skill.getId(), skill.getLevel());
		//owner.sendPacket(msg);
	}

    public synchronized void stopBuffTask() {
        if (_actionTask != null) {
            _actionTask.cancel(false);
            _actionTask = null;
        }
    }

    public synchronized void startBuffTask() {
        if (_actionTask != null) {
            stopBuffTask();
        }

        if (_actionTask == null && !isDead()) {
            _actionTask = ThreadPoolManager.getInstance().schedule(new ActionTask(), 5000);
        }
    }

    public boolean isBuffEnabled() {
        return _buffEnabled;
    }

    public void triggerBuff() {
        _buffEnabled = !_buffEnabled;
    }

    @Override
    protected void onDeath(Creature killer) {
        stopBuffTask();
        super.onDeath(killer);
    }

    @Override
    public void doRevive() {
        super.doRevive();
        startBuffTask();
    }

    @Override
    public void unSummon() {
        stopBuffTask();
        super.unSummon();
    }

    @Override
    public int getSoulshotConsumeCount() {
        return 1;
    }

    @Override
    public int getSpiritshotConsumeCount() {
        return 1;
    }
}
