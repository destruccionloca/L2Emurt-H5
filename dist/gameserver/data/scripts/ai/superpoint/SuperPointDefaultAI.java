package ai.superpoint;

import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.ai.DefaultAI;
import l2p.gameserver.data.xml.holder.SuperPointHolder;
import l2p.gameserver.model.AggroList.AggroInfo;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.MinionList;
import l2p.gameserver.model.World;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.serverpackets.SocialAction;
import l2p.gameserver.templates.superpoint.SuperPointNode;
import l2p.gameserver.templates.superpoint.SuperPointRoute;
import l2p.gameserver.utils.ChatUtils;
import l2p.gameserver.utils.Location;

import java.util.List;

public class SuperPointDefaultAI extends DefaultAI
{
	protected SuperPointRoute _superPointRoute;
	protected SuperPointNode _destination = null;
	protected int _currentNodeIndex = 0;
	protected boolean _incPoint = true;

	public SuperPointDefaultAI(NpcInstance actor) {
		super(actor);

		String moveRoute = actor.getParameter("superpoint", null);

		_superPointRoute = moveRoute == null ? null : SuperPointHolder.getInstance().getRoute(moveRoute);
	}

	@Override
	protected boolean thinkActive() {
		if (_superPointRoute == null) {
			return super.thinkActive();
		}

		NpcInstance actor = getActor();
		if(actor.isActionsDisabled()) {
			return true;
		}

		if(_randomAnimationEnd > System.currentTimeMillis()) {
			return true;
		}

		if(_def_think) {
			if(doTask()) {
				clearTasks();
			}
			return true;
		}

		long now = System.currentTimeMillis();
		if(now - _checkAggroTimestamp > Config.AGGRO_CHECK_INTERVAL) {
			_checkAggroTimestamp = now;

			boolean aggressive = Rnd.chance(actor.getParameter("SelfAggressive", isAggressive() ? 100 : 0));
			if(!actor.getAggroList().isEmpty() || aggressive) {
				List<Creature> targets = World.getAroundCharacters(actor);
				while(!targets.isEmpty()) {
					Creature target = getNearestTarget(targets);
					if(target == null) {
						break;
					}

					if(aggressive || actor.getAggroList().get(target) != null) {
						if (checkAggression(target)) {
							actor.getAggroList().addDamageHate(target, 0, 2);

							if (target.isSummon()) {
								actor.getAggroList().addDamageHate(target.getPlayer(), 0, 1);
							}

							startRunningTask(AI_TASK_ATTACK_DELAY);
							setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);

							return true;
						}
					}

					targets.remove(target);
				}
			}
		}

		if(actor.isMinion()) {
			NpcInstance leader = actor.getLeader();
			if(leader != null) {
				double distance = actor.getDistance(leader.getX(), leader.getY());
				if(distance > 1000) {
					actor.teleToLocation(leader.getRndMinionPosition());
					return true;
				} else if(distance > 200) {
					addTaskMove(leader.getRndMinionPosition(), true);
					return true;
				}
			}
		}

		if (actor.isMoving) {
			return true;
		}

		int prevIndex = _currentNodeIndex;

		if(_incPoint) {
			_currentNodeIndex++;
		} else {
			_currentNodeIndex--;
		}

		switch(_superPointRoute.getType())
		{
			case CIRCLE:
				if(_currentNodeIndex >= _superPointRoute.getNodes().size()) {
					_incPoint = true;
					_currentNodeIndex = 0;					
				}
				break;
			case LOOP:
				if(_currentNodeIndex >= _superPointRoute.getNodes().size()) {
					_incPoint = false;
					// мы сейчас вне границы, масива, отнимаеш, что б ввойти в границу, и берем преведущею точку
					_currentNodeIndex = _superPointRoute.getNodes().size() - 2;
				} else if(_currentNodeIndex < 0) {
					_incPoint = true;
					_currentNodeIndex = 1;
				}
				break;
			case DELETE:
				if(_currentNodeIndex >= _superPointRoute.getNodes().size()) {
					actor.decayOrDelete();
					return false;
				}
				break;
			case FINISH:
				if(_currentNodeIndex >= _superPointRoute.getNodes().size()) {
					actor.stopMove();
					return false;
				}
				break;
			case RANDOM:
				_currentNodeIndex = Rnd.get(_superPointRoute.getNodes().size());
				break;
		}

		onEvtArrivedToNode(prevIndex, _currentNodeIndex);
		return false;
	}

	protected void onEvtArrivedToNode(int prev, int index) {
		NpcInstance actor = getActor();

		SuperPointNode node = _superPointRoute.getNodes().get(prev);
		if(node.getSocialId() > 0)
			actor.broadcastPacketToOthers(new SocialAction(actor.getObjectId(), node.getSocialId()));

		if(node.getNpcString() != null) {
			ChatUtils.chat(actor, node.getChatType(), node.getNpcString());
		}

		if(node.getDelay() > 0) {
			setIsInRandomAnimation(node.getDelay());
		}

		if (_superPointRoute.isRunning()) {
			getActor().setRunning();
		} else if (!_superPointRoute.isRunning()) {
			getActor().setWalking();
		}

		_destination = _superPointRoute.getNodes().get(index);
		addTaskMove(_destination, true);
		if (actor.hasMinions()) {
			MinionList minionList = actor.getMinionList();
			if(minionList.hasAliveMinions()) {
				minionList.getAliveMinions().stream().filter(minion -> !minion.isInCombat() && !minion.isAfraid()).forEach(minion -> {
					if (_superPointRoute.isRunning()) {
						minion.setRunning();
					}
					minion.followToCharacter(getActor(), 500, true);
				});
			}
		}
	}

	@Override
	protected boolean isInAggroRange(Creature target) {
		if (_superPointRoute == null) {
			return super.isInAggroRange(target);
		}

		NpcInstance actor = getActor();
		AggroInfo ai = actor.getAggroList().get(target);
		if (ai != null && ai.hate > 0) {
			Location loc = _destination != null ? _destination : actor.getSpawnedLoc();
			if (!target.isInRangeZ(loc, MAX_PURSUE_RANGE)) {
				return false;
			}
		} else if (!isAggressive() || !target.isInRangeZ(actor.getLoc(), actor.getAggroRange())) {
			return false;
		}

		return true;
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();

		if (_superPointRoute == null) {
			return;
		}

		_currentNodeIndex = 0;
		_incPoint = true;
		if (_superPointRoute.isRunning()) {
			getActor().setRunning();
		}

		onEvtArrivedToNode(_currentNodeIndex, _currentNodeIndex);
	}

	@Override
	protected void returnHome(boolean clearAggro, boolean teleport) {
		if (_superPointRoute == null) {
			super.returnHome(clearAggro, teleport);
			return;
		}

		NpcInstance actor = getActor();

		clearTasks();
		actor.stopMove();

		if(clearAggro) {
			actor.getAggroList().clear(true);
		}

		setAttackTimeout(Long.MAX_VALUE);
		setAttackTarget(null);

		changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
		if (_superPointRoute.isRunning()) {
			getActor().setRunning();
		} else if (!_superPointRoute.isRunning()) {
			getActor().setWalking();
		}

		_destination = _superPointRoute.getNodes().get(_currentNodeIndex); // TODO: DS: поиск ближайшей точки ?
		addTaskMove(_destination, true);
		if (actor.hasMinions()) {
			MinionList minionList = actor.getMinionList();
			if(minionList.hasAliveMinions()) {
				minionList.getAliveMinions().stream().filter(minion -> !minion.isInCombat() && !minion.isAfraid()).forEach(minion -> {
					if (_superPointRoute.isRunning()) {
						minion.setRunning();
					}
					minion.getAI().changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
					minion.followToCharacter(getActor(), 500, true);
				});
			}
		}
	}

	@Override
	public boolean isGlobalAI()
	{
		return _superPointRoute != null;
	}
}
