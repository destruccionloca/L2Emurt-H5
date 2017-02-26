package l2p.gameserver.model.entity.olympiad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.Config;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.serverpackets.ExOlympiadUserInfo;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.templates.StatsSet;
import l2p.gameserver.ThreadPoolManager;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OlympiadTeam {

    private static final Logger _log = LoggerFactory.getLogger(OlympiadManager.class);
    private OlympiadGame _game;
    private IntObjectMap<TeamMember> _members;
    private String _name = "";
    private int _side;
    private double _damage;

    public OlympiadTeam(OlympiadGame game, int side) {
        _game = game;
        _side = side;
        _members = new CHashIntObjectMap<>();
    }

    public void addMember(int obj_id) {
        String player_name = "";
        Player player = GameObjectsStorage.getPlayer(obj_id);
        if (player != null) {
            player_name = player.getName();
        } else {
            StatsSet noble = Olympiad._nobles.get(obj_id);
            if (noble != null) {
                player_name = noble.getString(Olympiad.CHAR_NAME, "");
            }
        }

        _members.put(obj_id, new TeamMember(obj_id, player_name, player, _game, _side));

        _name = player_name;
    }

    public void addDamage(Player player, double damage) {
        _damage += damage;

        TeamMember member = _members.get(player.getObjectId());
        member.addDamage(damage);
    }

    public double getDamage() {
        return _damage;
    }

    public String getName() {
        return _name;
    }

    public void portPlayersToArena() {
        _members.values().forEach(TeamMember::portPlayerToArena);
    }

    public void portPlayersBack() {
        _members.values().forEach(TeamMember::portPlayerBack);
    }

    public void preparePlayers() {
        _members.values().forEach(TeamMember::preparePlayer);

        if (_members.size() <= 1) {
            return;
        }

        List<Player> list = new ArrayList<>();
        for (TeamMember member : _members.values()) {
            Player player = member.getPlayer();
            if (player != null) {
                list.add(player);
                player.leaveParty();
            }
        }

        if (list.size() <= 1) {
            return;
        }

        Player leader = list.get(0);
        if (leader == null) {
            return;
        }

        Party party = new Party(leader, 0);
        leader.setParty(party);

        list.stream().filter(player -> player != leader).forEach(player -> {
            player.joinParty(party);
        });
    }

    public void takePointsForCrash() {
        _members.values().forEach(TeamMember::takePointsForCrash);
    }

    public boolean checkPlayers() {
        for (TeamMember member : _members.values()) {
            if (member.checkPlayer()) {
                return true;
            }
        }
        return false;
    }

    public boolean isAllDead() {
        for (TeamMember member : _members.values()) {
            if (!member.isDead() && member.checkPlayer()) {
                return false;
            }
        }
        return true;
    }

    public boolean contains(int objId) {
        return _members.containsKey(objId);
    }

    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>(_members.size());
        for (TeamMember member : _members.values()) {
            Player player = member.getPlayer();
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }

    public Collection<TeamMember> getMembers() {
        return _members.values();
    }

    public void broadcast(L2GameServerPacket p) {
        for (TeamMember member : _members.values()) {
            Player player = member.getPlayer();
            if (player != null) {
                player.sendPacket(p);
            }
        }
    }

    public void broadcast(IStaticPacket p) {
        for (TeamMember member : _members.values()) {
            Player player = member.getPlayer();
            if (player != null) {
                player.sendPacket(p);
            }
        }
    }

    public void broadcastInfo() {
        for (TeamMember member : _members.values()) {
            Player player = member.getPlayer();
            if (player != null) {
                player.broadcastPacket(new ExOlympiadUserInfo(player, player.getOlympiadSide()));
            }
        }
    }

    public boolean logout(Player player) {
        if (player != null) {
            for (TeamMember member : _members.values()) {
                Player pl = member.getPlayer();
                if (pl != null && pl == player) {
                    member.logout();
                }
            }
            Olympiad._playersIp.remove(player.getIP());
            HwidChecker.unregisterPlayer(player);
        }
        return checkPlayers();
    }

    public boolean doDie(Player player) {
        if (player != null) {
            for (TeamMember member : _members.values()) {
                Player pl = member.getPlayer();
                if (pl != null && pl == player) {
                    member.doDie();
                }
            }
        }
        return isAllDead();
    }

    public void restorePreparePlayer() {
        for (TeamMember member : _members.values()) {
            Player player = member.getPlayer();
            if (player != null) {
                player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
                player.setCurrentCp(player.getMaxCp());
                player.broadcastUserInfo(true);
            }
        }
    }

    public void scheduleCurrentHpMpCp(long time) {
        ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
            @Override
            public void runImpl() throws Exception {
                restorePreparePlayer();
            }
        }, time);
    }

    public void saveNobleData() {
        _members.values().forEach(TeamMember::saveNobleData);
    }
}