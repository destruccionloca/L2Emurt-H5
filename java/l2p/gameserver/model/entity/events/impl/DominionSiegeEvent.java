package l2p.gameserver.model.entity.events.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import l2p.commons.collections.LazyArrayList;
import l2p.commons.collections.MultiValueSet;
import l2p.commons.dao.JdbcEntityState;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.dao.DominionRewardDAO;
import l2p.gameserver.dao.SiegeClanDAO;
import l2p.gameserver.dao.SiegePlayerDAO;
import l2p.gameserver.data.xml.holder.EventHolder;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.instancemanager.QuestManager;
import l2p.gameserver.listener.actor.OnDeathListener;
import l2p.gameserver.listener.actor.OnKillListener;
import l2p.gameserver.model.*;
import l2p.gameserver.model.base.RestartType;
import l2p.gameserver.model.entity.events.EventType;
import l2p.gameserver.model.entity.events.objects.DoorObject;
import l2p.gameserver.model.entity.events.objects.SiegeClanObject;
import l2p.gameserver.model.entity.events.objects.ZoneObject;
import l2p.gameserver.model.entity.residence.Dominion;
import l2p.gameserver.model.entity.residence.Residence;
import l2p.gameserver.model.instances.DoorInstance;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;
import l2p.gameserver.serverpackets.ExDominionWarEnd;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.PledgeShowInfoUpdate;
import l2p.gameserver.serverpackets.RelationChanged;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.templates.DoorTemplate;
import l2p.gameserver.utils.Location;
import org.napile.pair.primitive.IntObjectPair;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;

public class DominionSiegeEvent extends SiegeEvent<Dominion, SiegeClanObject> {

    public class DoorDeathListener implements OnDeathListener {

        @Override
        public void onDeath(Creature actor, Creature killer) {
            if (!isInProgress()) {
                return;
            }

            DoorInstance door = (DoorInstance) actor;
            if (door.getDoorType() == DoorTemplate.DoorType.WALL) {
                return;
            }

            Player player = killer.getPlayer();
            if (player != null) {
                player.sendPacket(SystemMsg.THE_CASTLE_GATE_HAS_BEEN_DESTROYED);
            }

            Clan owner = getResidence().getOwner();
            if (owner != null && owner.getLeader().isOnline()) {
                owner.getLeader().getPlayer().sendPacket(SystemMsg.THE_CASTLE_GATE_HAS_BEEN_DESTROYED);
            }
        }
    }

    public class KillListener implements OnKillListener {

        @Override
        public void onKill(Creature actor, Creature victim) {
            Player winner = actor.getPlayer();

            if (winner == null || !victim.isPlayer() || winner.getLevel() < 40 || winner == victim || victim.getEvent(DominionSiegeEvent.class) == DominionSiegeEvent.this || !actor.isInZone(Zone.ZoneType.SIEGE) || !victim.isInZone(Zone.ZoneType.SIEGE)) {
                return;
            }

            if (winner.isInParty()) {
                for (Player partyMem : winner.getParty()) {
                    addReward(partyMem, KILL_REWARD, 1);
                }
            } else {
                addReward(winner, KILL_REWARD, 1);
            }
            if (victim.getLevel() >= 61) {
                Quest q = _runnerEvent.getClassQuest(((Player) victim).getClassId());
                if (q == null) {
                    return;
                }
                QuestState questState = winner.getQuestState(q.getClass());
                if (questState == null) {
                    questState = q.newQuestState(winner, Quest.CREATED);
                    q.notifyKill(((Player) victim), questState);
                }
            }
        }

        @Override
        public boolean ignorePetOrSummon() {
            return true;
        }
    }

    public static final int KILL_REWARD = 0;
    public static final int ONLINE_REWARD = 1;
    public static final int STATIC_BADGES = 2;
    //
    public static final int REWARD_MAX = 3;
    // object name
    public static final String ATTACKER_PLAYERS = "attacker_players";
    public static final String DEFENDER_PLAYERS = "defender_players";
    public static final String DISGUISE_PLAYERS = "disguise_players";
    public static final String TERRITORY_NPC = "territory_npc";
    public static final String CATAPULT = "catapult";
    public static final String CATAPULT_DOORS = "catapult_doors";

    private DominionSiegeRunnerEvent _runnerEvent;
    private Quest _forSakeQuest;

    private IntObjectMap<int[]> _playersRewards = new CHashIntObjectMap<>();

    public DominionSiegeEvent(MultiValueSet<String> set) {
        super(set);
        _killListener = new KillListener();
        _doorDeathListener = new DoorDeathListener();
    }

    @Override
    public void initEvent() {
        _runnerEvent = EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1);
        super.initEvent();
        SiegeEvent castleSiegeEvent = getResidence().getCastle().getSiegeEvent();

        addObjects("mass_gatekeeper", castleSiegeEvent.getObjects("mass_gatekeeper"));
        addObjects(CastleSiegeEvent.CONTROL_TOWERS, castleSiegeEvent.getObjects(CastleSiegeEvent.CONTROL_TOWERS));

        List<DoorObject> doorObjects = getObjects(DOORS);
        for (DoorObject doorObject : doorObjects) {
            doorObject.getDoor().setHealBlocked(true);
            doorObject.getDoor().addListener(_doorDeathListener);
        }
    }

    @Override
    public void reCalcNextTime(boolean onInit) {
        //
    }

    @Override
    public void startEvent() {
        List<Dominion> registeredDominions = _runnerEvent.getRegisteredDominions();
        List<DominionSiegeEvent> dominions = new ArrayList<>(9);
        registeredDominions.stream().filter(d -> d.getSiegeDate().getTimeInMillis() != 0 && d != getResidence()).forEach(d -> {
            dominions.add(d.<DominionSiegeEvent>getSiegeEvent());
        });

        SiegeClanObject ownerClan = new SiegeClanObject(DEFENDERS, getResidence().getOwner(), 0);

        addObject(DEFENDERS, ownerClan);

        for (DominionSiegeEvent d : dominions) {
            // овнер текущей територии, аттакер , в всех других
            d.addObject(ATTACKERS, ownerClan);

            // все наёмники, являются аттакерами для других територий
            List<Integer> defenderPlayers = d.getObjects(DEFENDER_PLAYERS);
            for (int i : defenderPlayers) {
                addObject(ATTACKER_PLAYERS, i);
            }

            List<SiegeClanObject> otherDefenders = d.getObjects(DEFENDERS);
            otherDefenders.stream().filter(siegeClan -> siegeClan.getClan() != d.getResidence().getOwner()).forEach(siegeClan -> {
                addObject(ATTACKERS, siegeClan);
            });
        }

        int[] flags = getResidence().getFlags();
        if (flags.length > 0) {
            getResidence().removeSkills();
            getResidence().getOwner().broadcastToOnlineMembers(SystemMsg.THE_EFFECT_OF_TERRITORY_WARD_IS_DISAPPEARING);
        }

        SiegeClanDAO.getInstance().delete(getResidence());
        SiegePlayerDAO.getInstance().delete(getResidence());

        for (int i : flags) {
            spawnAction("ward_" + i, true);
        }

        updateParticles(true);
        super.startEvent();
    }

    @Override
    public void stopEvent(boolean t) {
        getObjects(DISGUISE_PLAYERS).clear();

        int[] flags = getResidence().getFlags();
        for (int i : flags) {
            spawnAction("ward_" + i, false);
        }

        getResidence().rewardSkills();
        getResidence().setJdbcState(JdbcEntityState.UPDATED);
        getResidence().update();

        updateParticles(false);

        List<SiegeClanObject> defenders = getObjects(DEFENDERS);
        defenders.forEach(SiegeClanObject::deleteFlag);

        super.stopEvent(t);

        DominionRewardDAO.getInstance().insert(getResidence());
    }

    @Override
    public void loadSiegeClans() {
        addObjects(DEFENDERS, SiegeClanDAO.getInstance().load(getResidence(), DEFENDERS));
        addObjects(DEFENDER_PLAYERS, SiegePlayerDAO.getInstance().select(getResidence(), 0));

        DominionRewardDAO.getInstance().select(getResidence());
    }

    @Override
    public void updateParticles(boolean start, String... arg) {
        boolean battlefieldChat = _runnerEvent.isBattlefieldChatActive();
        List<SiegeClanObject> siegeClans = getObjects(DEFENDERS);
        for (SiegeClanObject s : siegeClans) {
            if (battlefieldChat) {
                s.getClan().setWarDominion(start ? getId() : 0);
                PledgeShowInfoUpdate packet = new PledgeShowInfoUpdate(s.getClan());
                for (Player player : s.getClan().getOnlineMembers(0)) {
                    player.sendPacket(packet);
                    updatePlayer(player, start);
                }
            } else {
                for (Player player : s.getClan().getOnlineMembers(0)) {
                    updatePlayer(player, start);
                }
            }
        }
        List<Integer> players = getObjects(DEFENDER_PLAYERS);
        for (int i : players) {
            Player player = GameObjectsStorage.getPlayer(i);
            if (player != null) {
                updatePlayer(player, start);
            }
        }
    }

    public void updatePlayer(Player player, boolean start) {
        player.setBattlefieldChatId(_runnerEvent.isBattlefieldChatActive() ? getId() : 0);

        if (_runnerEvent.isBattlefieldChatActive()) {
            if (start) {
                player.addEvent(this);
                // за старт ТВ по 6
                addReward(player, STATIC_BADGES, 5);
            } else {
                player.removeEvent(this);
                // за конец ТВ по 6
                addReward(player, STATIC_BADGES, 5);

                player.getEffectList().stopEffect(Skill.SKILL_BATTLEFIELD_DEATH_SYNDROME);
                player.addExpAndSp(270000, 27000);
            }
            player.broadcastCharInfo();
            if (!start) {
                player.sendPacket(ExDominionWarEnd.STATIC);
            }
            questUpdate(player, start);
        }
    }

    public void questUpdate(Player player, boolean start) {
        if (start) {
            QuestState sakeQuestState = _forSakeQuest.newQuestState(player, Quest.CREATED);
            sakeQuestState.setState(Quest.STARTED);
            sakeQuestState.setCond(1);

            Quest protectCatapultQuest = QuestManager.getQuest("_729_ProtectTheTerritoryCatapult");
            if (protectCatapultQuest == null) {
                return;
            }

            QuestState questState = protectCatapultQuest.newQuestStateAndNotSave(player, Quest.CREATED);
            questState.setCond(1, false);
            questState.setStateAndNotSave(Quest.STARTED);

        } else {
            for (Quest q : _runnerEvent.getBreakQuests()) {
                QuestState questState = player.getQuestState(q.getClass());
                if (questState != null) {
                    questState.abortQuest();
                }
            }
        }
    }

    @Override
    public boolean isParticle(Player player) {
        if (isInProgress() || _runnerEvent.isBattlefieldChatActive()) {
            boolean registered = getObjects(DEFENDER_PLAYERS).contains(player.getObjectId()) || getSiegeClan(DEFENDERS, player.getClan()) != null;
            if (!registered) {
                return false;
            } else {
                if (isInProgress()) {
                    return true;
                } else {
                    player.setBattlefieldChatId(getId());
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    //========================================================================================================================================================================
    //                                                                   Overrides GlobalEvent
    //========================================================================================================================================================================
    @Override
    public int getRelation(Player thisPlayer, Player targetPlayer, int result) {
        DominionSiegeEvent event2 = targetPlayer.getEvent(DominionSiegeEvent.class);
        if (!Config.ALT_VISIBLE_SIEGE_IN_ICONS && event2 == null) {
            return result;
        }

        result |= RelationChanged.RELATION_TERRITORY_WAR;
        return result;
    }

    @Override
    public int getUserRelation(Player thisPlayer, int oldRelation) {
        oldRelation |= 0x1000;
        return oldRelation;
    }

    @Override
    public SystemMsg checkForAttack(Creature target, Creature attacker, Skill skill, boolean force) {
        try {
            DominionSiegeEvent dominionSiegeEventTarget = target.getEvent(DominionSiegeEvent.class);
            DominionSiegeEvent dominionSiegeEventAttacker = attacker.getEvent(DominionSiegeEvent.class);
            if (dominionSiegeEventTarget == null || dominionSiegeEventAttacker == null) {
                return null;
            }
            if (dominionSiegeEventAttacker != this || dominionSiegeEventTarget != this) {
                return null;
            }
            // DS: игнорируем соклан, группу, командный канал, альянс
            if (attacker.getPlayer().isInParty() && target.getPlayer().isInParty() && target.getPlayer().getParty() == attacker.getPlayer().getParty()) {
                return SystemMsg.YOU_CANNOT_FORCE_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY;
            }
            if (attacker.getPlayer().getClan() != null && target.getPlayer().getClan() != null && attacker.getPlayer().getClan() == target.getPlayer().getClan()) {
                return SystemMsg.YOU_CANNOT_FORCE_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY;
            }
            if (attacker.getPlayer().isInParty() && attacker.getPlayer().getParty().getCommandChannel() != null && target.getPlayer().isInParty() && target.getPlayer().getParty().getCommandChannel() != null && attacker.getPlayer().getParty().getCommandChannel() == target.getPlayer().getParty().getCommandChannel()) {
                return SystemMsg.YOU_CANNOT_FORCE_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY;
            }
            if (attacker.getPlayer().getClan() != null && attacker.getPlayer().getClan().getAlliance() != null && target.getPlayer().getClan() != null && target.getPlayer().getClan().getAlliance() != null && attacker.getPlayer().getClan().getAlliance() == target.getPlayer().getClan().getAlliance()) {
                return SystemMsg.YOU_CANNOT_FORCE_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY;
            }
            if (getObjects(DEFENDER_PLAYERS).contains(attacker.getObjectId()) && getObjects(DEFENDER_PLAYERS).contains(target.getObjectId())) {
                return SystemMsg.YOU_CANNOT_FORCE_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY;
            }
            SiegeClanObject attackerSiegeClan = null;
            SiegeClanObject targetSiegeClan = null;
            if (attacker.getPlayer().getClan() != null && getSiegeClan(DEFENDERS, attacker.getClan()) != null) {
                attackerSiegeClan = dominionSiegeEventAttacker.getSiegeClan(DEFENDERS, attacker.getPlayer().getClan());
            }
            if (target.getPlayer().getClan() != null && getSiegeClan(DEFENDERS, target.getClan()) != null) {
                targetSiegeClan = dominionSiegeEventAttacker.getSiegeClan(DEFENDERS, target.getPlayer().getClan());
            }
            if (attackerSiegeClan != null && targetSiegeClan != null) // своих во время ТВ бить нельзя
            {
                if ((this == dominionSiegeEventAttacker && attackerSiegeClan == targetSiegeClan) || targetSiegeClan.getType().equals(DEFENDERS)) {
                    return SystemMsg.YOU_CANNOT_FORCE_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY;
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @Override
    public boolean canAttack(Creature target, Creature attacker, Skill skill, boolean force) {
        try {
            DominionSiegeEvent dominionSiegeEventTarget = target.getEvent(DominionSiegeEvent.class);
            DominionSiegeEvent dominionSiegeEventAttacker = attacker.getEvent(DominionSiegeEvent.class);
            if (dominionSiegeEventTarget == null || dominionSiegeEventAttacker == null) {
                return true;
            }
            if (dominionSiegeEventAttacker != this || dominionSiegeEventTarget != this) {
                return true;
            }
            // DS: игнорируем соклан, группу, командный канал, альянс
            if (attacker.getPlayer().isInParty() && target.getPlayer().isInParty() && target.getPlayer().getParty() == attacker.getPlayer().getParty()) {
                return false;
            }
            if (attacker.getPlayer().getClan() != null && target.getPlayer().getClan() != null && attacker.getPlayer().getClan() == target.getPlayer().getClan()) {
                return false;
            }
            if (attacker.getPlayer().isInParty() && attacker.getPlayer().getParty().getCommandChannel() != null && target.getPlayer().isInParty() && target.getPlayer().getParty().getCommandChannel() != null && attacker.getPlayer().getParty().getCommandChannel() == target.getPlayer().getParty().getCommandChannel()) {
                return false;
            }
            if (attacker.getPlayer().getClan() != null && attacker.getPlayer().getClan().getAlliance() != null && target.getPlayer().getClan() != null && target.getPlayer().getClan().getAlliance() != null && attacker.getPlayer().getClan().getAlliance() == target.getPlayer().getClan().getAlliance()) {
                return false;
            }
            if (getObjects(DEFENDER_PLAYERS).contains(attacker.getObjectId()) && getObjects(DEFENDER_PLAYERS).contains(target.getObjectId())) {
                attacker.sendPacket(SystemMsg.YOU_CANNOT_FORCE_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY);
                return false;
            }
            SiegeClanObject attackerSiegeClan = null;
            SiegeClanObject targetSiegeClan = null;
            if (attacker.getPlayer().getClan() != null && getSiegeClan(DEFENDERS, attacker.getClan()) != null) {
                attackerSiegeClan = dominionSiegeEventAttacker.getSiegeClan(DEFENDERS, attacker.getPlayer().getClan());
            }
            if (target.getPlayer().getClan() != null && getSiegeClan(DEFENDERS, target.getClan()) != null) {
                targetSiegeClan = dominionSiegeEventAttacker.getSiegeClan(DEFENDERS, target.getPlayer().getClan());
            }
            if (attackerSiegeClan != null && targetSiegeClan != null) {
                if ((this == dominionSiegeEventAttacker && attackerSiegeClan == targetSiegeClan) || targetSiegeClan.getType().equals(DEFENDERS)) {
                    attacker.sendPacket(SystemMsg.YOU_CANNOT_FORCE_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY);
                    return false;
                }
            }
        } catch (Exception e) {
            return true;
        }
        return true;
    }

    @Override
    public void broadcastTo(IStaticPacket packet, String... types) {
        List<SiegeClanObject> siegeClans = getObjects(DEFENDERS);
        for (SiegeClanObject siegeClan : siegeClans) {
            siegeClan.broadcast(packet);
        }

        List<Integer> players = getObjects(DEFENDER_PLAYERS);
        for (int i : players) {
            Player player = GameObjectsStorage.getPlayer(i);
            if (player != null) {
                player.sendPacket(packet);
            }
        }
    }

    @Override
    public void broadcastTo(L2GameServerPacket packet, String... types) {
        List<SiegeClanObject> siegeClans = getObjects(DEFENDERS);
        for (SiegeClanObject siegeClan : siegeClans) {
            siegeClan.broadcast(packet);
        }

        List<Integer> players = getObjects(DEFENDER_PLAYERS);
        for (int i : players) {
            Player player = GameObjectsStorage.getPlayer(i);
            if (player != null) {
                player.sendPacket(packet);
            }
        }
    }

    @Override
    public void giveItem(Player player, int itemId, long count) {
        Zone zone = player.getZone(Zone.ZoneType.SIEGE);
        if (zone == null) {
            count = 0;
        } else {
            int id = zone.getParams().getInteger("residence");
            if (id < 100) {
                count = 125;
            } else {
                count = 31;
            }
        }

        addReward(player, ONLINE_REWARD, 1);
        super.giveItem(player, itemId, count);
    }

    @Override
    public List<Player> itemObtainPlayers() {
        List<Player> playersInZone = getPlayersInZone();

        List<Player> list = new LazyArrayList<>(playersInZone.size());
        list.addAll(getPlayersInZone().stream().filter(player -> player.getEvent(DominionSiegeEvent.class) != null).collect(Collectors.toList()));
        return list;
    }

    @Override
    public void checkRestartLocs(Player player, Map<RestartType, Boolean> r) {
        if (getObjects(FLAG_ZONES).isEmpty()) {
            return;
        }

        SiegeClanObject clan = getSiegeClan(DEFENDERS, player.getClan());
        if (clan != null && clan.getFlag() != null) {
            r.put(RestartType.TO_FLAG, Boolean.TRUE);
        }
    }

    @Override
    public Location getRestartLoc(Player player, RestartType type) {
        if (type == RestartType.TO_FLAG) {
            SiegeClanObject defenderClan = getSiegeClan(DEFENDERS, player.getClan());

            if (defenderClan != null && defenderClan.getFlag() != null) {
                return Location.findPointToStay(defenderClan.getFlag(), 50, 75);
            } else {
                player.sendPacket(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
            }

            return null;
        }

        return super.getRestartLoc(player, type);
    }

    @Override
    public Location getEnterLoc(Player player) {
        Zone zone = player.getZone(Zone.ZoneType.SIEGE);
        if (zone == null) {
            return player.getLoc();
        }

        SiegeClanObject siegeClan = getSiegeClan(DEFENDERS, player.getClan());
        if (siegeClan != null) {
            if (siegeClan.getFlag() != null) {
                return Location.findAroundPosition(siegeClan.getFlag(), 50, 75);
            }
        }

        Residence r = ResidenceHolder.getInstance().getResidence(zone.getParams().getInteger("residence"));
        if (r == null) {
            error(toString(), new Exception("Not find residence: " + zone.getParams().getInteger("residence")));
            return player.getLoc();
        }
        return r.getNotOwnerRestartPoint(player);
    }

    @Override
    public void teleportPlayers(String t) {
        List<ZoneObject> zones = getObjects(SIEGE_ZONES);
        for (ZoneObject zone : zones) {
            Residence r = ResidenceHolder.getInstance().getResidence(zone.getZone().getParams().getInteger("residence"));

            r.banishForeigner();
        }
    }

    @Override
    public boolean canRessurect(Player resurrectPlayer, Creature target, boolean force) {
        Player targetPlayer = target.getPlayer();
        // если оба вне зоны - рес разрешен
        if (!resurrectPlayer.isOnSiegeField() && !targetPlayer.isOnSiegeField()) {
            return true;
        }
        // если таргет вне осадный зоны - рес разрешен
        if (!targetPlayer.isOnSiegeField()) {
            return true;
        }

        if (resurrectPlayer.getClan() == null) {
            return false;
        }

        // если таргет не с нашей осады(или вообще нету осады) - рес запрещен
        DominionSiegeEvent siegeEvent = target.getEvent(DominionSiegeEvent.class);
        if (siegeEvent == null) {
            if (force) {
                targetPlayer.sendPacket(SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE);
            }
            resurrectPlayer.sendPacket(force ? SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE : SystemMsg.INVALID_TARGET);
            return false;
        }

        SiegeClanObject targetSiegeClan = siegeEvent.getSiegeClan(DEFENDERS, targetPlayer.getClan());

        if (targetSiegeClan.getType().equals(ATTACKERS)) {
            // если не твой клан или али рес запрещён
            if (resurrectPlayer.getClan() != targetSiegeClan.getClan()) {
                if (resurrectPlayer.getAlliance() != null && resurrectPlayer.getAlliance() != targetPlayer.getAlliance()) {
                    if (force) {
                        targetPlayer.sendPacket(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
                    }
                    resurrectPlayer.sendPacket(force ? SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE : SystemMsg.INVALID_TARGET);
                    return false;
                }
            }
            // если нету флага - рес запрещен
            if (targetSiegeClan.getFlag() == null) {
                if (force) {
                    targetPlayer.sendPacket(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
                }
                resurrectPlayer.sendPacket(force ? SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE : SystemMsg.INVALID_TARGET);
                return false;
            } else {
                return true;
            }
        } else if (targetSiegeClan.getType().equals(DEFENDERS)) {
            if (resurrectPlayer.getClan() != targetSiegeClan.getClan()) {
                if (resurrectPlayer.getAlliance() != null && resurrectPlayer.getAlliance() != targetPlayer.getAlliance()) {
                    if (force) {
                        targetPlayer.sendPacket(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
                    }
                    resurrectPlayer.sendPacket(force ? SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE : SystemMsg.INVALID_TARGET);
                    return false;
                }
            }
        }

        if (force) {
            return true;
        } else {
            resurrectPlayer.sendPacket(SystemMsg.INVALID_TARGET);
            return false;
        }
    }

    //========================================================================================================================================================================
    //                                                                   Rewards
    //========================================================================================================================================================================
    public void setReward(int objectId, int type, int v) {
        int val[] = _playersRewards.get(objectId);
        if (val == null) {
            _playersRewards.put(objectId, val = new int[REWARD_MAX]);
        }

        val[type] = v;
    }

    public void addReward(Player player, int type, int v) {
        int val[] = _playersRewards.get(player.getObjectId());
        if (val == null) {
            _playersRewards.put(player.getObjectId(), val = new int[REWARD_MAX]);
        }

        val[type] += v;
    }

    public int getReward(Player player, int type) {
        int val[] = _playersRewards.get(player.getObjectId());
        if (val == null) {
            return 0;
        } else {
            return val[type];
        }
    }

    public void clearReward(int objectId) {
        if (_playersRewards.containsKey(objectId)) {
            _playersRewards.remove(objectId);
            DominionRewardDAO.getInstance().delete(getResidence(), objectId);
        }
    }

    public Collection<IntObjectPair<int[]>> getRewards() {
        return _playersRewards.entrySet();
    }

    public int[] calculateReward(Player player) {
        int rewards[] = _playersRewards.get(player.getObjectId());
        if (rewards == null) {
            return null;
        }

        int[] out = new int[3];
        // статичные (старт, стоп, квесты, прочее)
        out[0] += rewards[STATIC_BADGES];
        // если онлайн ревард больше 14(70 мин в зоне) это 7 макс
        out[0] += rewards[ONLINE_REWARD] >= 14 ? 7 : rewards[ONLINE_REWARD] / 2;

        // насчитаем за убийство
        if (rewards[KILL_REWARD] < 50) {
            out[0] += rewards[KILL_REWARD] * 0.1;
        } else if (rewards[KILL_REWARD] < 120) {
            out[0] += (5 + (rewards[KILL_REWARD] - 50) / 14);
        } else {
            out[0] += 10;
        }

        //TODO [VISTALL] неверно, фейм дается и ниже, нету выдачи адены
        if (out[0] > 90) {
            out[0] = 90; // badges
            out[1] = 0; //TODO [VISTALL] adena count
            out[2] = 450; // fame
        }

        return out;
    }
    //========================================================================================================================================================================
    //                                                                   Getters/Setters
    //========================================================================================================================================================================

    public void setForSakeQuest(Quest forSakeQuest) {
        _forSakeQuest = forSakeQuest;
    }
}
