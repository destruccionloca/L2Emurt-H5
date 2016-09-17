package events.lastHero;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.Announcements;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.instancemanager.ReflectionManager;
import l2p.gameserver.instancemanager.ServerVariables;
import l2p.gameserver.listener.actor.OnDeathListener;
import l2p.gameserver.listener.actor.player.OnPlayerExitListener;
import l2p.gameserver.listener.actor.player.OnTeleportListener;
import l2p.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Zone;
import l2p.gameserver.model.actor.listener.CharListenerList;
import l2p.gameserver.model.base.TeamType;
import l2p.gameserver.model.entity.Hero;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.model.entity.events.impl.DuelEvent;
import l2p.gameserver.model.entity.olympiad.Olympiad;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.model.entity.residence.Residence;
import l2p.gameserver.model.instances.DoorInstance;
import l2p.gameserver.model.items.LockType;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.Revive;
import l2p.gameserver.serverpackets.components.ChatType;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.scripts.ScriptFile;
import l2p.gameserver.serverpackets.SkillList;
import l2p.gameserver.serverpackets.SocialAction;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.templates.DoorTemplate;
import l2p.gameserver.templates.ZoneTemplate;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.PositionUtils;
import l2p.gameserver.utils.ReflectionUtils;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LastHero extends Functions
        implements ScriptFile, OnDeathListener, OnTeleportListener, OnPlayerExitListener {

    private static final Logger _log = LoggerFactory.getLogger(LastHero.class);
    private static final int[] doors = {24190001, 24190002, 24190003, 24190004};
    private static ScheduledFuture<?> _startTask;
    private static List<Long> players_list = new CopyOnWriteArrayList();
    private static List<Long> live_list = new CopyOnWriteArrayList();
    private static int[][] mage_buffs;
    private static int[][] fighter_buffs;
    private static Map<Long, Location> playerRestoreCoord = new LinkedHashMap();
    private static Map<Long, String> boxes = new LinkedHashMap();
    private static boolean _isRegistrationActive = false;
    private static int _status = 0;
    private static int _time_to_start;
    private static int _category;
    private static int _minLevel;
    private static int _maxLevel;
    private static int _autoContinue = 0;
    private static boolean _active = false;
    private static Skill buff;
    private static ScheduledFuture<?> _endTask;
    private static Reflection reflection = ReflectionManager.LAST_HERO;
    private static Map<String, ZoneTemplate> _zones = new HashMap();
    private static IntObjectMap<DoorTemplate> _doors = new HashIntObjectMap();
    private static Zone _zone;
    private static ZoneListener _zoneListener = new ZoneListener();
    private static final Location _enter = new Location(149505, 46719, -3417);

    @Override
    public void onLoad() {
        CharListenerList.addGlobal(this);

        _zones.put("[colosseum_battle]", ReflectionUtils.getZone("[colosseum_battle]").getTemplate());
        for (int doorId : doors) {
            _doors.put(doorId, ReflectionUtils.getDoor(doorId).getTemplate());
        }
        reflection.init(_doors, _zones);
        _zone = reflection.getZone("[colosseum_battle]");
        _zone.addListener(_zoneListener);

        _active = ServerVariables.getString("LastHero", "off").equalsIgnoreCase("on");

        if (isActive()) {
            scheduleEventStart();
        }
        if ((Config.EVENT_LastHeroBuffPlayers) && (Config.EVENT_LastHeroMageBuffs.length > 0)) {
            mage_buffs = new int[Config.EVENT_LastHeroMageBuffs.length][2];
        }
        if ((Config.EVENT_LastHeroBuffPlayers) && (Config.EVENT_LastHeroFighterBuffs.length > 0)) {
            fighter_buffs = new int[Config.EVENT_LastHeroFighterBuffs.length][2];
        }
        int i = 0;

        if ((Config.EVENT_LastHeroBuffPlayers) && (Config.EVENT_LastHeroMageBuffs.length > 0)) {
            for (String skill : Config.EVENT_LastHeroMageBuffs) {
                String[] splitSkill = skill.split(",");
                mage_buffs[i][0] = Integer.parseInt(splitSkill[0]);
                mage_buffs[i][1] = Integer.parseInt(splitSkill[1]);
                i++;
            }
        }
        i = 0;

        if ((Config.EVENT_LastHeroBuffPlayers) && (Config.EVENT_LastHeroMageBuffs.length != 0)) {
            for (String skill : Config.EVENT_LastHeroFighterBuffs) {
                String[] splitSkill = skill.split(",");
                fighter_buffs[i][0] = Integer.parseInt(splitSkill[0]);
                fighter_buffs[i][1] = Integer.parseInt(splitSkill[1]);
                i++;
            }
        }
        _log.info("Loaded Event: Last Hero");
    }

    @Override
    public void onReload() {
        _zone.removeListener(_zoneListener);
        if (_startTask != null) {
            _startTask.cancel(false);
            _startTask = null;
        }
    }

    @Override
    public void onShutdown() {
        onReload();
    }

    private static boolean isActive() {
        return _active;
    }

    public void activateEvent() {
        Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (!isActive()) {
            if (_startTask == null) {
                scheduleEventStart();
            }
            ServerVariables.set("LastHero", "on");
            _log.info("Event 'Last Hero' activated.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.LastHero.AnnounceEventStarted", null);
        } else {
            player.sendMessage("Event 'Last Hero' already active.");
        }
        _active = true;

        show("admin/events/events.htm", player);
    }

    public void teleportPlayers() {
        for (Player player : getPlayers(players_list)) {
            if ((player == null) || (!playerRestoreCoord.containsKey(player.getStoredId()))) {
                continue;
            }
            player.setIsInLastHero(false);
            player.teleToLocation((Location) playerRestoreCoord.get(player.getStoredId()), ReflectionManager.DEFAULT);
        }
        playerRestoreCoord.clear();
    }

    public void deactivateEvent() {
        Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (isActive()) {
            if (_startTask != null) {
                _startTask.cancel(false);
                _startTask = null;
            }
            ServerVariables.unset("LastHero");
            _log.info("Event 'Last Hero' deactivated.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.LastHero.AnnounceEventStoped", null);
        } else {
            player.sendMessage("Event 'LastHero' not active.");
        }
        _active = false;

        show("admin/events/events.htm", player);
    }

    public static boolean isRunned() {
        return (_isRegistrationActive) || (_status > 0);
    }

    public static int getMinLevelForCategory(int category) {
        switch (category) {
            case 1:
                return 20;
            case 2:
                return 30;
            case 3:
                return 40;
            case 4:
                return 52;
            case 5:
                return 62;
            case 6:
                return 76;
        }
        return 0;
    }

    public static int getMaxLevelForCategory(int category) {
        switch (category) {
            case 1:
                return 29;
            case 2:
                return 39;
            case 3:
                return 51;
            case 4:
                return 61;
            case 5:
                return 75;
            case 6:
                return 85;
        }
        return 0;
    }

    public static int getCategory(int level) {
        if ((level >= 20) && (level <= 29)) {
            return 1;
        }
        if ((level >= 30) && (level <= 39)) {
            return 2;
        }
        if ((level >= 40) && (level <= 51)) {
            return 3;
        }
        if ((level >= 52) && (level <= 61)) {
            return 4;
        }
        if ((level >= 62) && (level <= 75)) {
            return 5;
        }
        if (level >= 76) {
            return 6;
        }
        return 0;
    }

    public void start(String[] var) {
        Player player = getSelf();
        if (var.length != 2) {
            show(new CustomMessage("common.Error", player, new Object[0]), player);
            return;
        }
        Integer category;
        Integer autoContinue;
        try {
            category = Integer.valueOf(var[0]);
            autoContinue = Integer.valueOf(var[1]);
        } catch (Exception e) {
            show(new CustomMessage("common.Error", player, new Object[0]), player);
            return;
        }

        _category = category.intValue();
        _autoContinue = autoContinue.intValue();

        if (_category == -1) {
            _minLevel = 1;
            _maxLevel = 85;
        } else {
            _minLevel = getMinLevelForCategory(_category);
            _maxLevel = getMaxLevelForCategory(_category);
        }

        if (_endTask != null) {
            show(new CustomMessage("common.TryLater", player, new Object[0]), player);
            return;
        }

        _status = 0;
        _isRegistrationActive = true;
        _time_to_start = Config.EVENT_LastHeroTime;

        players_list = new CopyOnWriteArrayList();
        live_list = new CopyOnWriteArrayList();
        playerRestoreCoord = new LinkedHashMap();
        String[] param = {String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel)};
        sayToAll("scripts.events.LastHero.AnnouncePreStart", param);

        executeTask("events.lastHero.LastHero", "question", new Object[0], 10000L);
        executeTask("events.lastHero.LastHero", "announce", new Object[0], 60000L);
    }

    public static void sayToAll(String address, String[] replacements) {
        Announcements.getInstance().announceByCustomMessage(address, replacements, ChatType.CRITICAL_ANNOUNCE);
    }

    public static void question() {
        for (Player player : GameObjectsStorage.getAllPlayersForIterate()) {
            if ((player != null) && (!player.isDead()) && (player.getLevel() >= _minLevel) && (player.getLevel() <= _maxLevel) && (player.getReflection().isDefault()) && (!player.isInOlympiadMode()) && (!player.isInObserverMode())) {
                player.scriptRequest(new CustomMessage("scripts.events.LastHero.AskPlayer", player, new Object[0]).toString(), "events.lastHero.LastHero:addPlayer", new Object[0]);
            }
        }
    }

    public static void announce() {
        if (players_list.size() < 2) {
            sayToAll("scripts.events.LastHero.AnnounceEventCancelled", null);
            _isRegistrationActive = false;
            _status = 0;
            executeTask("events.lastHero.LastHero", "autoContinue", new Object[0], 10000L);
            return;
        }

        if (_time_to_start > 1) {
            _time_to_start -= 1;
            String[] param = {String.valueOf(_time_to_start), String.valueOf(_minLevel), String.valueOf(_maxLevel)};
            sayToAll("scripts.events.LastHero.AnnouncePreStart", param);
            executeTask("events.lastHero.LastHero", "announce", new Object[0], 60000L);
        } else {
            _status = 1;
            _isRegistrationActive = false;
            sayToAll("scripts.events.LastHero.AnnounceEventStarting", null);
            executeTask("events.lastHero.LastHero", "prepare", new Object[0], 5000L);
        }
    }

    public void addPlayer() {
        Player player = getSelf();
        if ((player == null) || (!checkPlayer(player, true)) || (!checkDualBox(player))) {
            return;
        }
        players_list.add(player.getStoredId());
        live_list.add(player.getStoredId());

        show(new CustomMessage("scripts.events.LastHero.Registered", player, new Object[0]), player);
    }

    public static boolean checkPlayer(Player player, boolean first) {
        if ((first) && ((!_isRegistrationActive) || (player.isDead()))) {
            show(new CustomMessage("scripts.events.Late", player, new Object[0]), player);
            return false;
        }

        if ((first) && (players_list.contains(player.getStoredId()))) {
            show(new CustomMessage("scripts.events.LastHero.Cancelled", player, new Object[0]), player);
            if (players_list.contains(player.getStoredId())) {
                players_list.remove(player.getStoredId());
            }
            if (live_list.contains(player.getStoredId())) {
                live_list.remove(player.getStoredId());
            }
            if (boxes.containsKey(player.getStoredId())) {
                boxes.remove(player.getStoredId());
            }
            return false;
        }

        if ((player.getLevel() < _minLevel) || (player.getLevel() > _maxLevel)) {
            show(new CustomMessage("scripts.events.LastHero.CancelledLevel", player, new Object[0]), player);
            return false;
        }

        if (player.isMounted()) {
            show(new CustomMessage("scripts.events.LastHero.Cancelled", player, new Object[0]), player);
            return false;
        }

        if (player.isCursedWeaponEquipped()) {
            show(new CustomMessage("scripts.events.CaptureTheFlag.Cancelled", player, new Object[0]), player);
            return false;
        }

        if (player.isInDuel()) {
            show(new CustomMessage("scripts.events.LastHero.CancelledDuel", player, new Object[0]), player);
            return false;
        }

        if ((player.getTeam() != TeamType.NONE) || (player.isInPvPEvent())) {
            show(new CustomMessage("scripts.events.CaptureTheFlag.CancelledOtherEvent", player, new Object[0]), player);
            return false;
        }

        if ((player.getOlympiadGame() != null) || ((first) && (Olympiad.isRegistered(player)))) {
            show(new CustomMessage("scripts.events.LastHero.CancelledOlympiad", player, new Object[0]), player);
            return false;
        }

        if ((player.isInParty()) && (player.getParty().isInDimensionalRift())) {
            show(new CustomMessage("scripts.events.LastHero.CancelledOtherEvent", player, new Object[0]), player);
            return false;
        }

        if (player.isTeleporting()) {
            show(new CustomMessage("scripts.events.LastHero.CancelledTeleport", player, new Object[0]), player);
            return false;
        }

        if (player.isInObserverMode()) {
            show(new CustomMessage("scripts.event.DeathMatch.CancelledObserver", player, new Object[0]), player);
            return false;
        }

        return true;
    }

    public static void prepare() {
        reflection.getDoors().forEach(DoorInstance::closeMe);
        for (Zone z : reflection.getZones()) {
            z.setType(Zone.ZoneType.peace_zone);
        }
        cleanPlayers();
        clearArena();

        executeTask("events.lastHero.LastHero", "ressurectPlayers", new Object[0], 1000L);
        executeTask("events.lastHero.LastHero", "healPlayers", new Object[0], 2000L);
        executeTask("events.lastHero.LastHero", "paralyzePlayers", new Object[0], 3000L);
        executeTask("events.lastHero.LastHero", "teleportPlayersToColiseum", new Object[0], 4000L);
        executeTask("events.lastHero.LastHero", "buffPlayers", new Object[0], 5000L);
        executeTask("events.lastHero.LastHero", "go", new Object[0], 60000L);

        sayToAll("scripts.events.LastHero.AnnounceFinalCountdown", null);
    }

    public static void go() {
        _status = 2;
        blockItems();
        upParalyzePlayers();
        checkLive();
        clearArena();
        sayToAll("scripts.events.LastHero.AnnounceFight", null);
        for (Zone z : reflection.getZones()) {
            z.setType(Zone.ZoneType.battle_zone);
        }
        _endTask = executeTask("events.lastHero.LastHero", "endBattle", new Object[0], 300000L);
    }

    public static void endBattle() {
        _status = 0;
        removeAura();

        reflection.getDoors().forEach(DoorInstance::openMe);

        for (Zone z : reflection.getZones()) {
            z.setType(Zone.ZoneType.peace_zone);
        }
        boxes.clear();
        if (live_list.size() == 1) {
            Iterator i$ = getPlayers(live_list).iterator();
            if (i$.hasNext()) {
                Player player = (Player) i$.next();

                String[] repl = {player.getName()};
                sayToAll("scripts.events.LastHero.AnnounceWiner", repl);
                addItem(player, Config.EVENT_LastHeroItemID, Math.round(Config.EVENT_LastHeroRateFinal ? player.getLevel() * Config.EVENT_LastHeroItemCOUNTFinal : 1.0D * Config.EVENT_LastHeroItemCOUNTFinal));
                
                
                player.setHero(true);
                Hero.addSkills(player);
                player.updatePledgeClass();
                player.sendPacket(new SkillList(player));
                player.broadcastUserInfo(true);
                player.setVar("HeroPeriod", String.valueOf(System.currentTimeMillis() + 60 * 1000 * 60 * 24 * 1), -1);
                ThreadPoolManager.getInstance().schedule(new EndHero(player), System.currentTimeMillis() + (60 * 1000 * 60 * 24 * 1));
                player.broadcastPacket(new SocialAction(player.getObjectId(), 16));
                
                
                
                if (Config.EVENT_LAST_HERO_AURA_ENABLE) {
                    player.setHeroAura(true);
                    player.broadcastCharInfo();
                }
            }
        }
        sayToAll("scripts.events.LastHero.AnnounceEnd", null);
        executeTask("events.lastHero.LastHero", "end", new Object[0], 30000L);
        _isRegistrationActive = false;
        if (_endTask != null) {
            _endTask.cancel(false);
            _endTask = null;
        }
    }

    public static void end() {
        unlockItems();
        executeTask("events.lastHero.LastHero", "ressurectPlayers", new Object[0], 1000L);
        executeTask("events.lastHero.LastHero", "healPlayers", new Object[0], 2000L);
        executeTask("events.lastHero.LastHero", "teleportPlayers", new Object[0], 3000L);
        executeTask("events.lastHero.LastHero", "autoContinue", new Object[0], 10000L);
    }

    public void autoContinue() {
        if (_autoContinue > 0) {
            if (_autoContinue >= 6) {
                _autoContinue = 0;
                return;
            }
            start(new String[]{"" + (_autoContinue + 1), "" + (_autoContinue + 1)});
        } else {
            scheduleEventStart();
        }
    }

    public static void teleportPlayersToColiseum() {
        for (Player player : getPlayers(players_list)) {
            unRide(player);
            if (!Config.EVENT_LastHeroAllowSummons) {
                unSummonPet(player, true);
            }
            DuelEvent duel = (DuelEvent) player.getEvent(DuelEvent.class);
            if (duel != null) {
                duel.abortDuel(player);
            }
            playerRestoreCoord.put(player.getStoredId(), new Location(player.getX(), player.getY(), player.getZ()));
            player.teleToLocation(Location.findPointToStay(_enter, 150, 500, ReflectionManager.DEFAULT.getGeoIndex()), reflection);
            player.setIsInLastHero(true);
        }
    }

    public static void paralyzePlayers() {
        for (Player player : getPlayers(players_list)) {
            if (player == null) {
                continue;
            }
            player.getEffectList().stopEffect(Skill.SKILL_MYSTIC_IMMUNITY);
            if (!player.isParalyzed()) {
                player.startParalyzed();
                if (player.getPet() != null) {
                    player.getPet().startParalyzed();
                }
            }
        }
    }

    public static void upParalyzePlayers() {
        for (Player player : getPlayers(players_list)) {
            if (player == null) {
                continue;
            }
            if (player.isParalyzed()) {
                player.stopParalyzed();
                if (player.getPet() != null) {
                    player.getPet().stopParalyzed();
                }
            }
        }
    }

    public static void blockItems() {
        for (Player player : getPlayers(players_list)) {
            if (player == null) {
                continue;
            }
            player.getInventory().lockItems(LockType.INCLUDE, Config.EVENT_LastHeroEventBlockItems);
        }
    }

    public static void unlockItems() {
        for (Player player : getPlayers(players_list)) {
            if (player == null) {
                continue;
            }
            player.getInventory().unlock();
        }
    }

    public static void ressurectPlayers() {
        getPlayers(players_list).stream().filter(player -> player.isDead()).forEach(player -> {
            player.restoreExp();
            player.setCurrentCp(player.getMaxCp());
            player.setCurrentHp(player.getMaxHp(), true);
            player.setCurrentMp(player.getMaxMp());
            player.broadcastPacket(new L2GameServerPacket[]{new Revive(player)});
        });
    }

    public static void healPlayers() {
        for (Player player : getPlayers(players_list)) {
            player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
            player.setCurrentCp(player.getMaxCp());
        }
    }

    public static void cleanPlayers() {
        getPlayers(players_list).stream().filter(player -> !checkPlayer(player, false)).forEach(LastHero::removePlayer);
    }

    public static void checkLive() {
        List new_live_list = new CopyOnWriteArrayList();

        for (Long storeId : live_list) {
            Player player = GameObjectsStorage.getAsPlayer(storeId.longValue());
            if (player != null) {
                new_live_list.add(storeId);
            }
        }
        live_list = new_live_list;

        for (Player player : getPlayers(live_list)) {
            if ((player.isInZone(_zone)) && (!player.isDead()) && (!player.isLogoutStarted())) {
                player.setTeam(TeamType.BLUE);
            } else {
                loosePlayer(player);
            }
        }
        if (live_list.size() <= 1) {
            endBattle();
        }
    }

    public static void removeAura() {
        for (Player player : getPlayers(live_list)) {
            player.setTeam(TeamType.NONE);
            player.setIsInLastHero(false);
        }
    }

    public static void clearArena() {
        for (GameObject obj : _zone.getObjects()) {
            if (obj == null) {
                continue;
            }
            Player player = obj.getPlayer();
            if ((player != null) && (!live_list.contains(player.getStoredId()))) {
                player.teleToLocation(147451, 46728, -3410, ReflectionManager.DEFAULT);
            }
        }
    }

    @Override
    public void onDeath(Creature self, Creature killer) {
        if ((_status > 1) && (self.isPlayer()) && (self.getTeam() != TeamType.NONE) && (live_list.contains(self.getStoredId()))) {
            Player player = (Player) self;
            loosePlayer(player);
            checkLive();
            if ((killer != null) && (killer.isPlayer()) && (killer.getPlayer().getExpertiseIndex() - player.getExpertiseIndex() > 2)) {
                addItem((Player) killer, Config.EVENT_LastHeroItemID, Math.round(Config.EVENT_LastHeroRate ? player.getLevel() * Config.EVENT_LastHeroItemCOUNT : 1.0D * Config.EVENT_LastHeroItemCOUNT));
            }
            //    self.getPlayer().setIsInLastHero(false);
        }
    }

    @Override
    public void onTeleport(Player player, int x, int y, int z, Reflection reflection) {
        if (_zone.checkIfInZone(x, y, z, reflection)) {
            return;
        }
        if ((_status > 1) && (player.getTeam() != TeamType.NONE) && (live_list.contains(player.getStoredId()))) {
            removePlayer(player);
            checkLive();
        }
    }

    @Override
    public void onPlayerExit(Player player) {
        if (player.getTeam() == TeamType.NONE) {
            return;
        }

        if ((_status == 0) && (_isRegistrationActive) && (live_list.contains(player.getStoredId()))) {
            removePlayer(player);
            return;
        }

        if ((_status == 1) && (live_list.contains(player.getStoredId()))) {
            player.teleToLocation((Location) playerRestoreCoord.get(player.getStoredId()), ReflectionManager.DEFAULT);
            removePlayer(player);

            return;
        }

        if ((_status > 1) && (player.getTeam() != TeamType.NONE) && (live_list.contains(player.getStoredId()))) {
            removePlayer(player);
            checkLive();
        }
    }

    private static void loosePlayer(Player player) {
        if (player != null) {
            live_list.remove(player.getStoredId());
            player.setTeam(TeamType.NONE);
            show(new CustomMessage("scripts.events.LastHero.YouLose", player, new Object[0]), player);
        }
    }

    private static void removePlayer(Player player) {
        if (player != null) {
            live_list.remove(player.getStoredId());
            players_list.remove(player.getStoredId());
            playerRestoreCoord.remove(player.getStoredId());
            player.setIsInLastHero(false);

            if (!Config.EVENT_LastHeroAllowMultiReg) {
                boxes.remove(player.getStoredId());
            }
            player.setTeam(TeamType.NONE);
            player.getInventory().unlock();
        }
    }

    private static List<Player> getPlayers(List<Long> list) {
        List result = new ArrayList(list.size());
        for (Long storeId : list) {
            Player player = GameObjectsStorage.getAsPlayer(storeId.longValue());
            if (player != null) {
                result.add(player);
            }
        }
        return result;
    }

    public static void buffPlayers() {
        getPlayers(players_list).stream().filter(player -> !Config.EVENT_LastHeroAllowBuffs).forEach(player -> {
            player.getEffectList().stopAllEffects();
            if (player.getPet() != null) {
                player.getPet().getEffectList().stopAllEffects();
            }
        });

        if (!Config.EVENT_LastHeroBuffPlayers) {
            return;
        }

        for (Player player : getPlayers(players_list)) {
            if (player.isMageClass()) {
                mageBuff(player);
            } else {
                fighterBuff(player);
            }
        }
        for (Player player : getPlayers(live_list)) {
            if (player.isMageClass()) {
                mageBuff(player);
            } else {
                fighterBuff(player);
            }
        }
    }

    public void scheduleEventStart() {
        try {
            Calendar currentTime = Calendar.getInstance();
            Calendar nextStartTime = null;
            Calendar testStartTime = null;

            for (String timeOfDay : Config.EVENT_LastHeroStartTime) {
                testStartTime = Calendar.getInstance();
                testStartTime.setLenient(true);

                String[] splitTimeOfDay = timeOfDay.split(":");

                testStartTime.set(11, Integer.parseInt(splitTimeOfDay[0]));
                testStartTime.set(12, Integer.parseInt(splitTimeOfDay[1]));

                if (testStartTime.getTimeInMillis() < currentTime.getTimeInMillis()) {
                    testStartTime.add(5, 1);
                }
                if ((nextStartTime == null) || (testStartTime.getTimeInMillis() < nextStartTime.getTimeInMillis())) {
                    nextStartTime = testStartTime;
                }
                if (_startTask != null) {
                    _startTask.cancel(false);
                    _startTask = null;
                }
                _startTask = ThreadPoolManager.getInstance().schedule(new StartTask(), nextStartTime.getTimeInMillis() - System.currentTimeMillis());
            }

            currentTime = null;
            nextStartTime = null;
            testStartTime = null;
        } catch (Exception e) {
            _log.warn("LH: Error figuring out a start time. Check TvTEventInterval in config file.");
        }
    }

    public static void mageBuff(Player player) {
        for (int[] mage_buff : mage_buffs) {
            buff = SkillTable.getInstance().getInfo(mage_buff[0], mage_buff[1]);

            buff.getEffects(player, player, false, false);
        }
    }

    public static void fighterBuff(Player player) {
        for (int[] fighter_buff : fighter_buffs) {
            buff = SkillTable.getInstance().getInfo(fighter_buff[0], fighter_buff[1]);

            buff.getEffects(player, player, false, false);
        }
    }

    private static boolean checkDualBox(Player player) {
        if (!Config.EVENT_LastHeroAllowMultiReg) {
            if ("IP".equalsIgnoreCase(Config.EVENT_LastHeroCheckWindowMethod)) {
                if (boxes.containsValue(player.getIP())) {
                    show(new CustomMessage("scripts.events.LH.CancelledBox", player, new Object[0]), player);
                    return false;
                }

            } else if (("HWid".equalsIgnoreCase(Config.EVENT_LastHeroCheckWindowMethod))
                    && (boxes.containsValue(player.getNetConnection().getHWID()))) {
                show(new CustomMessage("scripts.events.DeathMatch.CancelledBox", player, new Object[0]), player);
                return false;
            }
        }
        return true;
    }

    public class StartTask extends RunnableImpl {

        public StartTask() {
        }

        @Override
        public void runImpl() {
            if (!LastHero._active) {
                return;
            }
            if (Functions.isPvPEventStarted()) {
                _log.info("LH not started: another event is already running");
                return;
            }

            for (Residence c : ResidenceHolder.getInstance().getResidenceList(Castle.class)) {
                if ((c.getSiegeEvent() != null) && (c.getSiegeEvent().isInProgress())) {
                    _log.debug("LH not started: CastleSiege in progress");
                    return;
                }

            }

            if (Config.EVENT_LastHeroCategories) {
                LastHero.this.start(new String[]{"1", "1"});
            } else {
                LastHero.this.start(new String[]{"-1", "-1"});
            }
        }
    }

    private static class ZoneListener
            implements OnZoneEnterLeaveListener {

        @Override
        public void onZoneEnter(Zone zone, Creature cha) {
            if (cha == null) {
                return;
            }
            Player player = cha.getPlayer();
            if ((LastHero._status > 0) && (player != null) && (!LastHero.live_list.contains(player.getStoredId()))) {
                player.teleToLocation(147451, 46728, -3410, ReflectionManager.DEFAULT);
            }
        }

        @Override
        public void onZoneLeave(Zone zone, Creature cha) {
            if (cha == null) {
                return;
            }
            Player player = cha.getPlayer();
            if ((LastHero._status > 1) && (player != null) && (player.getTeam() != TeamType.NONE) && (LastHero.live_list.contains(player.getStoredId()))) {
                double angle = PositionUtils.convertHeadingToDegree(cha.getHeading());
                double radian = Math.toRadians(angle - 90.0D);
                int x = (int) (cha.getX() + 250.0D * Math.sin(radian));
                int y = (int) (cha.getY() - 250.0D * Math.cos(radian));
                int z = cha.getZ();
                player.teleToLocation(x, y, z, LastHero.reflection);
            }
        }
    }
    
    private static class EndHero implements Runnable {
        private Player player;

        public EndHero(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            player.setHero(false);
            Hero.removeSkills(player);
            player.unsetVar("HeroPeriod");
            player.updatePledgeClass();
            player.sendPacket(new SkillList(player));
            player.broadcastUserInfo(true);
        }
    }
}
