package bosses;

import bosses.EpicBossState.NestState;
import bosses.EpicBossState.State;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.util.Rnd;
import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.listener.actor.OnDeathListener;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Zone;
import l2p.gameserver.model.actor.listener.CharListenerList;
import l2p.gameserver.model.instances.BossInstance;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.scripts.ScriptFile;
import l2p.gameserver.serverpackets.ExShowScreenMessage;
import l2p.gameserver.serverpackets.PlaySound;
import l2p.gameserver.serverpackets.SocialAction;
import l2p.gameserver.serverpackets.components.NpcString;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.Log;
import l2p.gameserver.utils.ReflectionUtils;
import l2p.gameserver.utils.TimeUtils;
import l2p.gameserver.utils.NpcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AntharasManager extends Functions implements ScriptFile, OnDeathListener {

    private static final Logger _log = LoggerFactory.getLogger(AntharasManager.class);
    // Constants
    private static final int _teleportCubeId = 31859;
    private static final int ANTHARAS_STRONG = 29068;
    private static final int PORTAL_STONE = 3865;
    private static final Location TELEPORT_POSITION = new Location(179892, 114915, -7704);
    private static final Location _teleportCubeLocation = new Location(177615, 114941, -7709, 0);
	private static final Location _antharasLocation = new Location(181032, 114840, -7672, 32542);
    // Models
    private static BossInstance _antharas;
    private static NpcInstance _teleCube;
    private static List<NpcInstance> _spawnedMinions = new ArrayList<NpcInstance>();
    // tasks.
    private static ScheduledFuture<?> _monsterSpawnTask = null;
    private static ScheduledFuture<?> _intervalEndTask = null;
    private static ScheduledFuture<?> _socialTask = null;
    private static ScheduledFuture<?> _checkPlayer = null;
    private static ScheduledFuture<?> _moveAtRandomTask = null;
    private static ScheduledFuture<?> _sleepCheckTask = null;
    private static ScheduledFuture<?> _onAnnihilatedTask = null;
    // Vars
    private static EpicBossState _state;
    private static Zone _zone;
    private static long _lastAttackTime = 0;
    private static final int FWA_LIMITUNTILSLEEP = 15 * 60000;
    private static final int FWA_FIXINTERVALOFANTHARAS_DAYS = Config.FIXINTERVALOFANTHARAS_DAYS; // 24 часа
    private static final int FWA_FIXINTERVALOFANTHARAS = FWA_FIXINTERVALOFANTHARAS_DAYS * 24 * 60 * 60000;
    private static final int FWA_APPTIMEOFANTHARAS = 3 * 60000; // 5 минут ожидание перед респом
    private static boolean Dying = false;
    private static boolean _entryLocked = false;

    private static class AntharasSpawn extends RunnableImpl {

        private int _distance = 2550;
        private int _taskId = 0;
        private List<Player> _players = getPlayersInside();

        AntharasSpawn(int taskId) {
            _taskId = taskId;
        }

        @Override
        public void runImpl() {

            switch (_taskId) {
                case 0:
                    _socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(1), 2000);
                    _entryLocked = true;
                    break;
                case 1:
                    _antharas = (BossInstance) NpcUtils.spawnSingle(ANTHARAS_STRONG, _antharasLocation);
                    _antharas.setAggroRange(0);
                    _state.setState(EpicBossState.State.ALIVE);
                    _state.update();
                    _socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(2), 2000);
                    break;
                case 2:
                    // set camera.
                    for (Player pc : _players) {
                        pc.enterMovieMode();
                        pc.specialCamera(_antharas, 700, 13, -19, 0, 20000, 0, 0, 0, 0);
                    }
                    _socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(3), 3000);
                    break;
                case 3:
                    // do social.
                    _antharas.broadcastPacket(new SocialAction(_antharas.getObjectId(), 1));

                    // set camera.
                    for (Player pc : _players) {
                        pc.enterMovieMode();
                        pc.specialCamera(_antharas, 700, 13, 0, 6000, 20000, 0, 0, 0, 0);
                    }
                    _socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(4), 10000);
                    break;
                case 4:
                    _antharas.broadcastPacket(new SocialAction(_antharas.getObjectId(), 2));
                    // set camera.
                    for (Player pc : _players) {
                        pc.enterMovieMode();
                        pc.specialCamera(_antharas, 3700, 0, -3, 0, 10000, 0, 0, 0, 0);
                    }
                    _socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(5), 200);
                    break;
                case 5:
                    // set camera.
                    for (Player pc : _players) {
                        pc.enterMovieMode();
                        pc.specialCamera(_antharas, 1100, 0, -3, 22000, 30000, 0, 0, 0, 0);
                    }
                    _socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(6), 10800);
                    break;
                case 6:
                    // set camera.
                    for (Player pc : _players) {
                        pc.enterMovieMode();
                        pc.specialCamera(_antharas, 1100, 0, -3, 300, 7000, 0, 0, 0, 0);
                    }
                    _socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(7), 7000);
                    break;
                case 7:
                    // reset camera.
                    for (Player pc : _players) {
                        pc.leaveMovieMode();
                    }

                    broadcastScreenMessage(NpcString.ANTHARAS_YOU_CANNOT_HOPE_TO_DEFEAT_ME);
                    _antharas.broadcastPacket(new PlaySound(PlaySound.Type.MUSIC, "BS02_A", 1, _antharas.getObjectId(), _antharas.getLoc()));
                    _antharas.setAggroRange((int)_antharas.getTemplate().aggroRange);
                    _antharas.setRunning();
					_antharas.moveToLocation(new Location(179011, 114871, -7704), 0, false);
					_antharas.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                    _sleepCheckTask = ThreadPoolManager.getInstance().schedule(new CheckLastAttack(), 600000);
                    break;
                case 8:
                    for (Player pc : _players) {
                        pc.enterMovieMode();
                        pc.specialCamera(_antharas, 1200, 20, -10, 0, 13000, 0, 0, 0, 0);
                    }
                    _socialTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(9), 13000);
                    break;
                case 9:
                    for (Player pc : _players) {
                        pc.leaveMovieMode();
                        pc.altOnMagicUseTimer(pc, SkillTable.getInstance().getInfo(23312, 1));
                    }
                    broadcastScreenMessage(NpcString.ANTHARAS_THE_EVIL_LAND_DRAGON_ANTHARAS_DEFEATED);
                    onAntharasDie();
                    break;
            }
        }
    }

    private static class CheckLastAttack extends RunnableImpl {

        @Override
        public void runImpl() {
            if (_state.getState() == EpicBossState.State.ALIVE) {
                if (_lastAttackTime + FWA_LIMITUNTILSLEEP < System.currentTimeMillis()) {
                    sleep();
                } else {
                    _sleepCheckTask = ThreadPoolManager.getInstance().schedule(new CheckLastAttack(), 60000);
                }
            }
        }
    }

    // at end of interval.
    private static class IntervalEnd extends RunnableImpl {

        @Override
        public void runImpl() {
            _state.setState(EpicBossState.State.NOTSPAWN);
            _state.update();
        }
    }

    private static class onAnnihilated extends RunnableImpl {

        @Override
        public void runImpl() {
            sleep();
        }
    }

    private static void banishForeigners() {
        for (Player player : getPlayersInside()) {
            player.teleToClosestTown();
        }
    }

    private synchronized static void checkAnnihilated() {
        if (_onAnnihilatedTask == null && isPlayersAnnihilated()) {
            _onAnnihilatedTask = ThreadPoolManager.getInstance().schedule(new onAnnihilated(), 5000);
        }
    }

    private static List<Player> getPlayersInside() {
        return getZone().getInsidePlayers();
    }

    private static int getRespawnInterval() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, Config.FIXINTERVALOFANTHARAS_DAYS);
        calendar.set(Calendar.HOUR_OF_DAY, Config.RESPAWNHOURANTARAS);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        int interval = (int) (calendar.getTimeInMillis() - System.currentTimeMillis());
        return (int) (Config.ALT_RAID_RESPAWN_MULTIPLIER * interval);
    }

    public static Zone getZone() {
        return _zone;
    }

    private static boolean isPlayersAnnihilated() {
        for (Player pc : getPlayersInside()) {
            if (!pc.isDead()) {
                return false;
            }
        }
        return true;
    }

    private static void onAntharasDie() {
        if (Dying) {
            return;
        }

        Dying = true;
        _state.setRespawnDate(getRespawnInterval());
        _state.setState(EpicBossState.State.INTERVAL);
        _state.update();

        _entryLocked = false;
        _teleCube = (NpcInstance) NpcUtils.spawnSingle(_teleportCubeId, _teleportCubeLocation);
        Log.add("Antharas died", "bosses");
    }

    @Override
    public void onDeath(Creature self, Creature killer) {
        if (self.isPlayer() && _state != null && _state.getState() == State.ALIVE && _zone != null && _zone.checkIfInZone(self.getX(), self.getY())) {
            checkAnnihilated();
        } else if (self.isNpc() && self.getNpcId() == ANTHARAS_STRONG) {
            ThreadPoolManager.getInstance().schedule(new AntharasSpawn(8), 10);
        }
    }

    private static void setIntervalEndTask() {
        setUnspawn();

        if (_state.getState().equals(EpicBossState.State.ALIVE)) {
            _state.setState(EpicBossState.State.NOTSPAWN);
            _state.update();
            return;
        }

        if (!_state.getState().equals(EpicBossState.State.INTERVAL)) {
            _state.setRespawnDate(getRespawnInterval());
            _state.setState(EpicBossState.State.INTERVAL);
            _state.update();
        }

        _intervalEndTask = ThreadPoolManager.getInstance().schedule(new IntervalEnd(), _state.getInterval());
    }

    // clean Antharas's lair.
    private static void setUnspawn() {
        // eliminate players.
        banishForeigners();

        if (_antharas != null) {
            _antharas.deleteMe();
        }
        for (NpcInstance npc : _spawnedMinions) {
            npc.deleteMe();
        }
        if (_teleCube != null) {
            _teleCube.deleteMe();
        }

        _entryLocked = false;

        // not executed tasks is canceled.
        if (_monsterSpawnTask != null) {
            _monsterSpawnTask.cancel(false);
            _monsterSpawnTask = null;
        }
        if (_intervalEndTask != null) {
            _intervalEndTask.cancel(false);
            _intervalEndTask = null;
        }
        if (_socialTask != null) {
            _socialTask.cancel(false);
            _socialTask = null;
        }
	}

    private void init() {
        _state = new EpicBossState(ANTHARAS_STRONG);
        _zone = ReflectionUtils.getZone("[antharas_epic]");

        CharListenerList.addGlobal(this);
        _log.info("AntharasManager: State of Antharas is " + _state.getState() + ".");
        if (!_state.getState().equals(EpicBossState.State.NOTSPAWN)) {
            setIntervalEndTask();
        }

        _log.info("AntharasManager: Next spawn date of Antharas is " + TimeUtils.toSimpleFormat(_state.getRespawnDate()) + ".");
    }

    private static void sleep() {
        setUnspawn();
        if (_state.getState().equals(EpicBossState.State.ALIVE)) {
            _state.setState(EpicBossState.State.NOTSPAWN);
            _state.update();
        }
    }

    public static void setLastAttackTime() {
        _lastAttackTime = System.currentTimeMillis();
    }

    // setting Antharas spawn task.
    public synchronized static void setAntharasSpawnTask() {
        if (_monsterSpawnTask == null) {
            _monsterSpawnTask = ThreadPoolManager.getInstance().schedule(new AntharasSpawn(1), FWA_APPTIMEOFANTHARAS);
        }
    }

    public static void broadcastScreenMessage(NpcString npcs) {
        for (Player p : getPlayersInside()) {
            p.sendPacket(new ExShowScreenMessage(npcs, 8000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false));
        }
    }

    public static void addSpawnedMinion(NpcInstance npc) {
        _spawnedMinions.add(npc);
    }

    public static NestState checkNestEntranceCond(int tryCount) {
        if (_state.getState() == State.INTERVAL) {
            return NestState.NOT_AVAILABLE;
        }
        if (_state.getState() == State.ALIVE) {
            return NestState.ALREADY_ATTACKED;
        }
        if (getZone().getInsidePlayers().size() + tryCount > 200) {
            return NestState.LIMIT_EXCEEDED;
        }
        return NestState.ALLOW;
    }

    public static void notifyEntrance() {
        setAntharasSpawnTask();
    }

    @Override
    public void onLoad() {
        init();
    }

    @Override
    public void onReload() {
        sleep();
    }

    @Override
    public void onShutdown() {
    }
}
