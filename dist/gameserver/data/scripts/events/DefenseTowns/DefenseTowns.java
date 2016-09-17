package events.DefenseTowns;

import java.util.ArrayList;
import java.util.Calendar;

import l2p.commons.util.Rnd;
import l2p.gameserver.Announcements;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.listener.actor.OnDeathListener;
import l2p.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Zone;
import l2p.gameserver.model.actor.listener.CharListenerList;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.model.entity.residence.Residence;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.scripts.ScriptFile;
import l2p.gameserver.utils.NpcUtils;
import l2p.gameserver.utils.ReflectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : Ragnarok
 * @modification : 4ipolino
 * @transfer : STIGMATED
 */
public class DefenseTowns extends Functions implements ScriptFile, OnDeathListener, OnPlayerEnterListener {

    private static ArrayList<NpcInstance> mobs = new ArrayList<NpcInstance>();
    private static ArrayList<NpcInstance> allMobs = new ArrayList<NpcInstance>();
    private static boolean playerWin = false;
    private static Zone _zoneShutPiece = ReflectionUtils.getZone("[schuttgart_town_peace]");
    private static final Logger _log = LoggerFactory.getLogger(DefenseTowns.class);
    private static boolean activeEvent = false;

    private enum EventTaskState {

        START,
        END,
        TIME1,
        TIME2,
        TIME3,
        TIME4,
        TIME5,
        TIME6,
        DESPAWN
    }

    private class EventTask implements Runnable {

        EventTaskState state;
        ArrayList<NpcInstance> mb;

        public EventTask(EventTaskState state) {
            this.state = state;
        }

        @SuppressWarnings("unchecked")
        public EventTask(EventTaskState state, Object clone) {
            this.state = state;
            this.mb = (ArrayList<NpcInstance>) clone;
        }

        @Override
        public void run() {
            switch (state) {
                case START:
                    for (Residence c : ResidenceHolder.getInstance().getResidenceList(Castle.class)) {
                        if (c.getSiegeEvent() != null && c.getSiegeEvent().isInProgress()) {
                            ThreadPoolManager.getInstance().schedule(new EventTask(EventTaskState.START), Config.TMEventInterval);
                            _log.info("Event DefenseTowns: no start [Siege]");
                            return;
                        }
                    }
                    activeEvent = true;
                    _zoneShutPiece.setActive(false);
                    playerWin = false;
                    allMobs.clear();
                    Announcements.getInstance().announceToAll("Разведчики монстров замечены около Schuttgart! Нужно защитить жителей !");
                    ThreadPoolManager.getInstance().schedule(new EventTask(EventTaskState.TIME1), Config.TMTime1);
                    _log.info("Event DefenseTowns: Start");
                    break;
                case TIME1:
                    if (!activeEvent)
                        return;
                    mobs.clear();
                    Announcements.getInstance().announceToAll("Монстры атакуют ворота Schuttgart! Нужно защитить жителей !");

                    for (int i = 0; i < Config.TMWave1Count; i++) {
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave1, 87368 + Rnd.get(200), -137176 + Rnd.get(100), -2288));
                    }

                    for (int i = 0; i < Config.TMWave1Count; i++) {
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave1, 92040 + Rnd.get(300), -139512 + Rnd.get(100), -2320));
                    }

                    for (int i = 0; i < Config.TMWave1Count; i++) {
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave1, 82712 + Rnd.get(300), -139496 + Rnd.get(100), -2288));
                    }

                    allMobs.addAll(mobs);
                    ThreadPoolManager.getInstance().schedule(new EventTask(EventTaskState.DESPAWN, mobs.clone()), Config.TMMobLife);
                    ThreadPoolManager.getInstance().schedule(new EventTask(EventTaskState.END), Config.TMTime1 + Config.TMTime2 + Config.TMTime3 + Config.TMTime4 + Config.TMTime5 + Config.TMTime6 + Config.BossLifeTime);
                    ThreadPoolManager.getInstance().schedule(new EventTask(EventTaskState.TIME2), Config.TMTime2);
                    break;
                case TIME2:
                    if (!activeEvent)
                        return;
                    mobs.clear();
                    for (int i = 0; i < Config.TMWave2Count; i++) {
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave2, 87586 + Rnd.get(300), -140366, -1541));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave2, 87124 + Rnd.get(300), -140399, -1541));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave2, 87345 + Rnd.get(300), -140634, -1541));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave2, 85309 + Rnd.get(300), -141943, -1495));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave2, 85066 + Rnd.get(300), -141654, -1541));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave2, 84979 + Rnd.get(300), -141423, -1541));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave2, 84951 + Rnd.get(300), -141875, -1541));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave2, 89619 + Rnd.get(300), -141752, -1541));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave2, 89398 + Rnd.get(300), -141956, -1487));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave2, 89677 + Rnd.get(300), -141866, -1541));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave2, 89712 + Rnd.get(300), -141388, -1541));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave2, 87596 + Rnd.get(300), -140366, -1541));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave2, 87134 + Rnd.get(300), -140399, -1541));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave2, 87355 + Rnd.get(300), -140634, -1541));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave2, 85319 + Rnd.get(300), -141943, -1495));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave2, 85076 + Rnd.get(300), -141654, -1541));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave2, 84989 + Rnd.get(300), -141423, -1541));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave2, 84961 + Rnd.get(300), -141875, -1541));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave2, 89629 + Rnd.get(300), -141752, -1541));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave2, 89388 + Rnd.get(300), -141956, -1487));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave2, 89687 + Rnd.get(300), -141866, -1541));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave2, 89722 + Rnd.get(300), -141388, -1541));
                    }
                    for (NpcInstance mob : mobs) {
                        mob.setHeading(40240);
                    }
                    allMobs.addAll(mobs);
                    ThreadPoolManager.getInstance().schedule(new EventTask(EventTaskState.DESPAWN, mobs.clone()), Config.TMMobLife);
                    ThreadPoolManager.getInstance().schedule(new EventTask(EventTaskState.TIME3), Config.TMTime3);
                    break;
                case TIME3:
                    if (!activeEvent)
                        return;
                    Announcements.getInstance().announceToAll("Монстры уже в городе Schuttgart! Нужно защитить жителей !");
                    mobs.clear();
                    for (int i = 0; i < Config.TMWave3Count; i++) {
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave3, 88887 + Rnd.get(300), -142259, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave3, 88780 + Rnd.get(300), -142220, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave3, 88710 + Rnd.get(300), -142575, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave3, 88503 + Rnd.get(300), -142547, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave3, 87168 + Rnd.get(300), -141752, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave3, 87313 + Rnd.get(300), -141630, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave3, 87434 + Rnd.get(300), -141917, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave3, 87204 + Rnd.get(300), -142156, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave3, 86277 + Rnd.get(300), -142634, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave3, 86180 + Rnd.get(300), -142421, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave3, 85908 + Rnd.get(300), -142485, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave3, 85943 + Rnd.get(300), -142266, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave3, 88897 + Rnd.get(300), -142259, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave3, 88790 + Rnd.get(300), -142220, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave3, 88720 + Rnd.get(300), -142575, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave3, 88513 + Rnd.get(300), -142547, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave3, 87178 + Rnd.get(300), -141752, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave3, 87323 + Rnd.get(300), -141630, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave3, 87444 + Rnd.get(300), -141917, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave3, 87214 + Rnd.get(300), -142156, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave3, 86287 + Rnd.get(300), -142634, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave3, 86190 + Rnd.get(300), -142421, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave3, 85918 + Rnd.get(300), -142485, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave3, 85953 + Rnd.get(300), -142266, -1340));
                    }
                    for (NpcInstance mob : mobs) {
                        mob.setHeading(40240);
                    }
                    allMobs.addAll(mobs);
                    ThreadPoolManager.getInstance().schedule(new EventTask(EventTaskState.DESPAWN, mobs.clone()), Config.TMMobLife);
                    ThreadPoolManager.getInstance().schedule(new EventTask(EventTaskState.TIME4), Config.TMTime4);
                    break;
                case TIME4:
                    if (!activeEvent)
                        return;
                    Announcements.getInstance().announceToAll("Монстры захватили главную площaдь Schuttgart! Нужно защитить жителей !");
                    mobs.clear();
                    for (int i = 0; i < Config.TMWave4Count; i++) {
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave4, 87168 + Rnd.get(300), -141752, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave4, 87313 + Rnd.get(300), -141630, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave4, 87434 + Rnd.get(300), -141917, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave4, 87204 + Rnd.get(300), -142156, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave4, 87955 + Rnd.get(300), -142804, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave4, 87956 + Rnd.get(300), -142608, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave4, 87642 + Rnd.get(300), -142589, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave4, 87402 + Rnd.get(300), -142651, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave4, 87261 + Rnd.get(300), -142558, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave4, 87010 + Rnd.get(300), -142625, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave4, 86771 + Rnd.get(300), -142818, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave4, 87178 + Rnd.get(300), -141752, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave4, 87323 + Rnd.get(300), -141630, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave4, 87444 + Rnd.get(300), -141917, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave4, 87214 + Rnd.get(300), -142156, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave4, 87965 + Rnd.get(300), -142804, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave4, 87966 + Rnd.get(300), -142608, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave4, 87652 + Rnd.get(300), -142589, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave4, 87412 + Rnd.get(300), -142651, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave4, 87271 + Rnd.get(300), -142558, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave4, 87020 + Rnd.get(300), -142625, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave4, 86781 + Rnd.get(300), -142818, -1340));
                    }
                    for (NpcInstance mob : mobs) {
                        mob.setHeading(40240);
                    }
                    allMobs.addAll(mobs);
                    ThreadPoolManager.getInstance().schedule(new EventTask(EventTaskState.DESPAWN, mobs.clone()), Config.TMMobLife);
                    ThreadPoolManager.getInstance().schedule(new EventTask(EventTaskState.TIME5), Config.TMTime5);
                    break;
                case TIME5:
                    if (!activeEvent)
                        return;
                    Announcements.getInstance().announceToAll("Монстры прорываются в церковь Schuttgart! Нужно защитить жителей !");
                    mobs.clear();
                    for (int i = 0; i < Config.TMWave5Count; i++) {
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87505 + Rnd.get(300), -143049, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87236 + Rnd.get(300), -142939, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87202 + Rnd.get(300), -143257, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87466 + Rnd.get(300), -143269, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87426 + Rnd.get(300), -143537, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87313 + Rnd.get(300), -143461, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87358 + Rnd.get(300), -143878, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87353 + Rnd.get(300), -144076, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87350 + Rnd.get(300), -144355, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87955 + Rnd.get(300), -142804, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87956 + Rnd.get(300), -142608, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87642 + Rnd.get(300), -142589, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87402 + Rnd.get(300), -142651, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87515 + Rnd.get(300), -143049, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87236 + Rnd.get(300), -142939, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87212 + Rnd.get(300), -143257, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87476 + Rnd.get(300), -143269, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87436 + Rnd.get(300), -143537, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87323 + Rnd.get(300), -143461, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87368 + Rnd.get(300), -143878, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87363 + Rnd.get(300), -144076, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87360 + Rnd.get(300), -144355, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87965 + Rnd.get(300), -142804, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87966 + Rnd.get(300), -142608, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87652 + Rnd.get(300), -142589, -1340));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave5, 87412 + Rnd.get(300), -142651, -1340));
                    }
                    for (NpcInstance mob : mobs) {
                        mob.setHeading(40240);
                    }
                    allMobs.addAll(mobs);
                    ThreadPoolManager.getInstance().schedule(new EventTask(EventTaskState.DESPAWN, mobs.clone()), Config.TMMobLife);
                    ThreadPoolManager.getInstance().schedule(new EventTask(EventTaskState.TIME6), Config.TMTime6);
                    break;
                case TIME6:
                    if (!activeEvent)
                        return;
                    Announcements.getInstance().announceToAll("Предводитель монстров и его свита захватили церковь Schuttgart! Нужно защитить жителей !");
                    mobs.clear();
                    for (int i = 0; i < Config.TMWave6Count; i++) {
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87466 + Rnd.get(100), -143269, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87426 + Rnd.get(100), -143537, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87313 + Rnd.get(100), -143461, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87358 + Rnd.get(100), -143878, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87353 + Rnd.get(100), -144076, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87350 + Rnd.get(100), -144355, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87394 + Rnd.get(100), -144725, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87329 + Rnd.get(100), -144734, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87361 + Rnd.get(100), -144651, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87511 + Rnd.get(100), -144964, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87390 + Rnd.get(100), -144697, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87276 + Rnd.get(100), -145006, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87114 + Rnd.get(100), -145285, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87378 + Rnd.get(100), -145255, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87575 + Rnd.get(100), -145295, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87476 + Rnd.get(100), -143269, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87436 + Rnd.get(100), -143537, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87323 + Rnd.get(100), -143461, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87368 + Rnd.get(100), -143878, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87363 + Rnd.get(100), -144076, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87360 + Rnd.get(100), -144355, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87384 + Rnd.get(100), -144725, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87352 + Rnd.get(100), -144734, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87371 + Rnd.get(100), -144651, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87521 + Rnd.get(100), -144964, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87380 + Rnd.get(100), -144856, -1288));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87286 + Rnd.get(100), -145006, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87124 + Rnd.get(100), -145285, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87388 + Rnd.get(100), -145255, -1292));
                        mobs.add(NpcUtils.spawnSingle(Config.TMWave6, 87585 + Rnd.get(100), -145295, -1292));
                    }
                    for (NpcInstance mob : mobs) {
                        mob.setHeading(40240);
                    }
                    allMobs.addAll(mobs);
                    ThreadPoolManager.getInstance().schedule(new EventTask(EventTaskState.DESPAWN, mobs.clone()), Config.TMMobLife);
                    mobs.clear();
                    mobs.add(NpcUtils.spawnSingle(Config.TMBoss, 87362, -145640, -1292));
                    allMobs.addAll(mobs);
                    ThreadPoolManager.getInstance().schedule(new EventTask(EventTaskState.DESPAWN, mobs.clone()), Config.BossLifeTime);
                    break;
                case DESPAWN:
                    for (NpcInstance npc : mb) {
                        if (npc != null) {
                            npc.deleteMe();
                        }
                    }
                    break;
                case END:
                    if (!playerWin) {
                        Announcements.getInstance().announceToAll("Игроки не сумели защитить город, Schuttgart был разграблен монстрами!");
                    }
                    activeEvent = false;
                    _zoneShutPiece.setActive(true);
                    for (NpcInstance _npc : allMobs) {
                        if (_npc != null) {
                            _npc.deleteMe();
                        }
                    }
                    if (Config.TMEventInterval > 0) {
                        ThreadPoolManager.getInstance().schedule(new EventTask(EventTaskState.START), Config.TMEventInterval);
                    }
            }
        }
    }

    @Override
    public void onDeath(Creature self, Creature killer) {
        if (!activeEvent) {
            return;
        }
        if (self.getNpcId() == Config.TMBoss) {
            Announcements.getInstance().announceToAll("Главарь монстров повержен, игрок " + killer.getName() + " нанес последний удар!");
            Announcements.getInstance().announceToAll("Монстры отступают!");
            for (NpcInstance _npc : allMobs) {
                if (_npc != null) {
                    _npc.deleteMe();
                }
            }
            if (killer.isPlayer()) {
                for (int i = 0; i < Config.TMItem.length; i++) {
                    if (Rnd.get(100) < Config.TMItemChanceBoss[i] && Config.TMItemColBoss[i] > 0) {
                        Player player = (Player) killer;
                        player.getInventory().addItem(Config.TMItem[i], Config.TMItemColBoss[i]);
                        if (Config.TMItem[i] == 57) {
                            player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_OBTAINED_S1_ADENA).addNumber(Config.TMItemColBoss[i]));
                        } else if (Config.TMItemColBoss[i] == 1) {
                            final SystemMessage smsg = new SystemMessage(SystemMessage.YOU_HAVE_OBTAINED_S1);
                            smsg.addItemName(Config.TMItem[i]);
                            player.sendPacket(smsg);
                        } else {
                            final SystemMessage smsg = new SystemMessage(SystemMessage.YOU_HAVE_OBTAINED_S2_S1);
                            smsg.addItemName(Config.TMItem[i]);
                            smsg.addNumber(Config.TMItemColBoss[i]);
                            player.sendPacket(smsg);
                        }
                    }
                }
            }
            playerWin = true;
        } else if (self.getNpcId() == Config.TMWave1
                || self.getNpcId() == Config.TMWave2
                || self.getNpcId() == Config.TMWave3
                || self.getNpcId() == Config.TMWave4
                || self.getNpcId() == Config.TMWave5
                || self.getNpcId() == Config.TMWave6) {
            if (killer.isPlayer()) {
                for (int i = 0; i < Config.TMItem.length; i++) {
                    if (Rnd.get(100) < Config.TMItemChance[i] && Config.TMItemCol[i] > 0) {
                        Player player = (Player) killer;
                        player.getInventory().addItem(Config.TMItem[i], Config.TMItemCol[i]);
                        if (Config.TMItem[i] == 57) {
                            player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_OBTAINED_S1_ADENA).addNumber(Config.TMItemCol[i]));
                        } else if (Config.TMItemCol[i] == 1) {
                            final SystemMessage smsg = new SystemMessage(SystemMessage.YOU_HAVE_OBTAINED_S1);
                            smsg.addItemName(Config.TMItem[i]);
                            player.sendPacket(smsg);
                        } else {
                            final SystemMessage smsg = new SystemMessage(SystemMessage.YOU_HAVE_OBTAINED_S2_S1);
                            smsg.addItemName(Config.TMItem[i]);
                            smsg.addNumber(Config.TMItemCol[i]);
                            player.sendPacket(smsg);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onPlayerEnter(Player player) {
        if (activeEvent) {
            Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.DefenseTowns.AnnounceEventStarted", null);
        }
    }

    /**
     * Запускает эвент
     */
    public void startEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }

        if (SetActive("DefenseTowns", true)) {
            ThreadPoolManager.getInstance().schedule(new EventTask(EventTaskState.START), 1);
        } else {
            player.sendMessage("Event 'Defense Towns' already started.");
        }
        show("admin/events/events.htm", player);
    }

    /**
     * Останавливает эвент
     */
    public void stopEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("DefenseTowns", false)) {
            ThreadPoolManager.getInstance().schedule(new EventTask(EventTaskState.END), 1);
        } else {
            player.sendMessage("Event 'Defense Towns' not started.");
        }
        show("admin/events/events.htm", player);
    }

    public void Start() {
        ThreadPoolManager.getInstance().schedule(new EventTask(EventTaskState.START), 1);
    }

    public void Stop() {
        ThreadPoolManager.getInstance().schedule(new EventTask(EventTaskState.END), 1);
    }

    @Override
    public void onLoad() {
        CharListenerList.addGlobal(this);
        if (Config.TMEnabled) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, Config.TMStartHour);
            cal.set(Calendar.MINUTE, Config.TMStartMin);
            cal.set(Calendar.SECOND, 0);
            while (cal.getTimeInMillis() < System.currentTimeMillis()) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
            ThreadPoolManager.getInstance().schedule(new EventTask(EventTaskState.START), cal.getTimeInMillis() - System.currentTimeMillis());
        }
    }

    @Override
    public void onReload() {
        if (SetActive("DefenseTowns", false)) {
            ThreadPoolManager.getInstance().schedule(new EventTask(EventTaskState.END), 1);
        }
    }

    @Override
    public void onShutdown() {
        if (SetActive("DefenseTowns", false)) {
            ThreadPoolManager.getInstance().schedule(new EventTask(EventTaskState.END), 1);
        }
    }
}
