package events.TVTArena;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import l2p.commons.dbutils.DbUtils;
import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.Announcements;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.data.htm.HtmCache;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.instancemanager.ServerVariables;
import l2p.gameserver.listener.actor.OnDeathListener;
import l2p.gameserver.listener.actor.player.OnPlayerExitListener;
import l2p.gameserver.listener.actor.player.OnTeleportListener;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.SimpleSpawner;
import l2p.gameserver.model.actor.listener.CharListenerList;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.scripts.ScriptFile;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TVTArena1 extends Functions
        implements ScriptFile, OnDeathListener, OnTeleportListener, OnPlayerExitListener {

    private static final Logger _log = LoggerFactory.getLogger(TVTArena1.class);
    private static List<SimpleSpawner> _spawns = new ArrayList();
    private static int EVENT_MANAGER_ID = 36612;
    private static ScheduledFuture<?> _startTask;
    private static ScheduledFuture<?> _stopTask;
    private static TVTArenaTemplate _instance;

    private void spawnEventManagers() {
        int[][] EVENT_MANAGERS = {{82232, 148936, -3492, 0}};

        SpawnNPCs(EVENT_MANAGER_ID, EVENT_MANAGERS, _spawns);
    }

    private void unSpawnEventManagers() {
        deSpawnNPCs(_spawns);
    }

    public static TVTArenaTemplate getInstance() {
        if (_instance == null) {
            _instance = new TVTArena1Impl();
        }
        return _instance;
    }

    @Override
    public void onLoad() {
        CharListenerList.addGlobal(this);
        getInstance().onLoad();
        if ((Config.EVENT_TVT_ARENA_ENABLED) && (!isActive())) {
            scheduleEventStart();
            scheduleEventStop();
            _log.info("Loaded Event: TVT Arena 1 [state: activated]");
        } else if ((Config.EVENT_TVT_ARENA_ENABLED) && (isActive())) {
            spawnEventManagers();
            _log.info("Loaded Event: TVT Arena 1 [state: started]");
        } else {
            _log.info("Loaded Event: TVT Arena 1 [state: deactivated]");
        }
    }

    public boolean isActive() {
        return IsActive("TVTArena1");
    }

    public void scheduleEventStart() {
        try {
            Calendar currentTime = Calendar.getInstance();
            Calendar nextStartTime = null;
            Calendar testStartTime = null;

            for (String timeOfDay : Config.EVENT_TVT_ARENA_START_TIME) {
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
            _log.warn("TVT Arena 1: Error figuring out a start time. Check EventStartTime in config file.");
        }
    }

    public void scheduleEventStop() {
        try {
            Calendar currentTime = Calendar.getInstance();
            Calendar nextStartTime = null;
            Calendar testStartTime = null;

            for (String timeOfDay : Config.EVENT_TVT_ARENA_STOP_TIME) {
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
                if (_stopTask != null) {
                    _stopTask.cancel(false);
                    _stopTask = null;
                }
                _stopTask = ThreadPoolManager.getInstance().schedule(new StopTask(), nextStartTime.getTimeInMillis() - System.currentTimeMillis());
            }

            currentTime = null;
            nextStartTime = null;
            testStartTime = null;
        } catch (Exception e) {
            _log.warn("TVT Arena 1: Error figuring out a stop time. Check EventStopTime in config file.");
        }
    }

    @Override
    public void onReload() {
        getInstance().onReload();
        unSpawnEventManagers();
        _instance = null;
        if (_startTask != null) {
            _startTask.cancel(false);
            _startTask = null;
        }
    }

    @Override
    public void onShutdown() {
        onReload();
    }

    @Override
    public void onDeath(Creature cha, Creature killer) {
        getInstance().onDeath(cha, killer);
    }

    @Override
    public void onPlayerExit(Player player) {
        getInstance().onPlayerExit(player);
    }

    @Override
    public void onTeleport(Player player, int x, int y, int z, Reflection reflection) {
        getInstance().onTeleport(player);
    }

    public String DialogAppend_36612(Integer val) {
        Player player = getSelf();
        if (val.intValue() == 0) {
            return HtmCache.getInstance().getHtml("scripts/events/TVTArena/36612.htm", player);
        }
        return "";
    }

    public void showRules() {
        Player player = getSelf();
        show("scripts/events/TVTArena/rules.htm", player);
    }

    public void create1() {
        getInstance().template_create1(getSelf());
    }

    public void register() {
        getInstance().template_register(getSelf());
    }

    public void check1() {
        getInstance().template_check1(getSelf(), getNpc());
    }

    public void register_check_blue() {
        getInstance().template_register_check_blue(getSelf());
    }

    public void register_check_red() {
        getInstance().template_register_check_red(getSelf());
    }

    public void stop() {
        getInstance().template_stop();
    }

    public void announce() {
        getInstance().template_announce();
    }

    public void prepare() {
        getInstance().template_prepare();
    }

    public void start() {
        getInstance().template_start();
    }

    public void timeOut() {
        getInstance().template_timeOut();
    }

    public void showStat() {
        Player player = getSelf();
        String html = HtmCache.getInstance().getHtml("scripts/events/TVTArena/stat.htm", player);
        String playersTopList = "";

        playersTopList = playersTopList + "<table width=285>";
        playersTopList = playersTopList + "<tr>";
        playersTopList = playersTopList + "<td valign=\"top\" width=10></td>";
        playersTopList = playersTopList + "<td valign=\"top\" width=165><center>Игрок</center></td>";
        playersTopList = playersTopList + "<td valign=\"top\" width=50><center>Уровень</center></td>";
        playersTopList = playersTopList + "<td valign=\"top\" width=50><center>Рейтинг</center></td>";
        playersTopList = playersTopList + "<td valign=\"top\" width=10></td>";
        playersTopList = playersTopList + "</tr>";

        Connection conSelect = null;
        PreparedStatement statementSelect = null;
        ResultSet rsetSelect = null;
        try {
            conSelect = DatabaseFactory.getInstance().getConnection();
            statementSelect = conSelect.prepareStatement("SELECT char_name, char_level, char_points FROM event_tvt_arena WHERE arena_id=? ORDER BY char_points DESC LIMIT 0,9");
            statementSelect.setInt(1, 1);
            rsetSelect = statementSelect.executeQuery();
            while (rsetSelect.next()) {
                playersTopList = playersTopList + "<tr>";
                playersTopList = playersTopList + "<td valign=\"top\" width=10></td>";
                playersTopList = playersTopList + "<td valign=\"top\" width=165>" + rsetSelect.getString("char_name") + "</td>";
                playersTopList = playersTopList + "<td valign=\"top\" width=50><center>" + rsetSelect.getInt("char_level") + "</center></td>";
                playersTopList = playersTopList + "<td valign=\"top\" width=50><center>" + rsetSelect.getInt("char_points") + "</center></td>";
                playersTopList = playersTopList + "<td valign=\"top\" width=10></td>";
                playersTopList = playersTopList + "</tr>";
            }
        } catch (Exception e) {
        } finally {
            DbUtils.closeQuietly(conSelect, statementSelect, rsetSelect);
        }

        playersTopList = playersTopList + "</table>";
        html = html.replace("%top_list%", playersTopList);
        show(html, player);
    }

    public void startEvent() {
        Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("TVTArena1", true)) {
            spawnEventManagers();
            _log.info("Event: TVT Arena 1 started");
            Announcements.getInstance().announceToAll("Начался евент TVT Arena 1");
        } else {
            player.sendMessage("Event: TVT Arena 1 already started");
        }
        show("admin/events/events.htm", player);
    }

    public void stopEvent() {
        Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("TVTArena1", false)) {
            ServerVariables.unset("TVTArena1");
            unSpawnEventManagers();
            stop();
            _log.info("Event: TVT Arena 1 stopped");
            Announcements.getInstance().announceToAll("Окончен евент TVT Arena 1");
        } else {
            player.sendMessage("Event: TVT Arena 1 not started");
        }
        show("admin/events/events.htm", player);
    }

    public class StopTask extends RunnableImpl {

        public StopTask() {
        }

        @Override
        public void runImpl() {
            unSpawnEventManagers();
            Announcements.getInstance().announceToAll("Окончен евент TVT Arena 1");
            _log.info("Event TVT Arena 1 stopped");
        }
    }

    public class StartTask extends RunnableImpl {

        public StartTask() {
        }

        @Override
        public void runImpl() {
            spawnEventManagers();
            Announcements.getInstance().announceToAll("Начался евент TVT Arena 1");
            _log.info("Event TVT Arena 1 started");
        }
    }

    private static class TVTArena1Impl extends TVTArenaTemplate {

        @Override
        protected void onLoad() {
            _managerId = 36612;
            _className = "TVTArena1";
            _status = 0;

            _team1list = new CopyOnWriteArrayList();
            _team2list = new CopyOnWriteArrayList();
            _team1live = new CopyOnWriteArrayList();
            _team2live = new CopyOnWriteArrayList();

            _zoneListener = new TVTArenaTemplate.ZoneListener();
            _zone = ReflectionUtils.getZone("[UnderGroundPvPKandins]");
            _zone.addListener(_zoneListener);

            _team1points = new ArrayList();
            _team2points = new ArrayList();

            _team1points.add(new Location(-82488, -51704, -11529));
            _team1points.add(new Location(-82264, -51400, -11529));
            _team1points.add(new Location(-82408, -51032, -11529));
            _team1points.add(new Location(-82824, -51016, -11529));
            _team1points.add(new Location(-82856, -51672, -11529));
            _team1points.add(new Location(-86120, -52408, -11529));
            _team1points.add(new Location(-85912, -52136, -11529));
            _team1points.add(new Location(-85976, -52744, -11529));
            _team1points.add(new Location(-85608, -52776, -11529));

            _team2points.add(new Location(-83432, -50712, -11529));
            _team2points.add(new Location(-83400, -50408, -11529));
            _team2points.add(new Location(-83656, -50232, -11529));
            _team2points.add(new Location(-83928, -50360, -11529));
            _team2points.add(new Location(-84232, -53912, -11529));
            _team2points.add(new Location(-83944, -54104, -11529));
            _team2points.add(new Location(-83608, -53976, -11529));
            _team2points.add(new Location(-83576, -53624, -11529));
            _team2points.add(new Location(-84200, -53560, -11529));
        }

        @Override
        protected void onReload() {
            if (_status > 0) {
                template_stop();
            }
            _zone.removeListener(_zoneListener);
        }
    }
}