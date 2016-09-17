package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import l2p.gameserver.model.pledge.Alliance;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2p.gameserver.model.entity.events.impl.SiegeEvent;
import l2p.gameserver.model.entity.events.objects.SiegeClanObject;
import l2p.gameserver.model.entity.residence.Castle;

public class CastleSiegeDefenderList extends L2GameServerPacket {

    public static int OWNER = 1;
    public static int WAITING = 2;
    public static int ACCEPTED = 3;
    public static int REFUSE = 4;
    private int _id, _registrationValid;
    private List<DefenderClan> _defenderClans = Collections.emptyList();

    public CastleSiegeDefenderList(Castle castle) {
        _id = castle.getId();
        _registrationValid = !castle.getSiegeEvent().isRegistrationOver() && castle.getOwner() != null ? 1 : 0;

        List<SiegeClanObject> defenders = castle.getSiegeEvent().getObjects(SiegeEvent.DEFENDERS);
        List<SiegeClanObject> defendersWaiting = castle.getSiegeEvent().getObjects(CastleSiegeEvent.DEFENDERS_WAITING);
        List<SiegeClanObject> defendersRefused = castle.getSiegeEvent().getObjects(CastleSiegeEvent.DEFENDERS_REFUSED);
        _defenderClans = new ArrayList<>(defenders.size() + defendersWaiting.size() + defendersRefused.size());
        if (castle.getOwner() != null) {
            _defenderClans.add(new DefenderClan(castle.getOwner(), OWNER, 0));
        }
        _defenderClans.addAll(defenders.stream().map(siegeClan -> new DefenderClan(siegeClan.getClan(), ACCEPTED, (int) (siegeClan.getDate() / 1000L))).collect(Collectors.toList()));
        _defenderClans.addAll(defendersWaiting.stream().map(siegeClan -> new DefenderClan(siegeClan.getClan(), WAITING, (int) (siegeClan.getDate() / 1000L))).collect(Collectors.toList()));
        _defenderClans.addAll(defendersRefused.stream().map(siegeClan -> new DefenderClan(siegeClan.getClan(), REFUSE, (int) (siegeClan.getDate() / 1000L))).collect(Collectors.toList()));
    }

    @Override
    protected final void writeImpl() {
        writeC(0xCB);
        writeD(_id);
        writeD(0x00);
        writeD(_registrationValid);
        writeD(0x00);

        writeD(_defenderClans.size());
        writeD(_defenderClans.size());
        for (DefenderClan defenderClan : _defenderClans) {
            Clan clan = defenderClan._clan;

            writeD(clan.getClanId());
            writeS(clan.getName());
            writeS(clan.getLeaderName());
            writeD(clan.getCrestId());
            writeD(defenderClan._time);
            writeD(defenderClan._type);
            writeD(clan.getAllyId());
            Alliance alliance = clan.getAlliance();
            if (alliance != null) {
                writeS(alliance.getAllyName());
                writeS(alliance.getAllyLeaderName());
                writeD(alliance.getAllyCrestId());
            } else {
                writeS(StringUtils.EMPTY);
                writeS(StringUtils.EMPTY);
                writeD(0x00);
            }
        }
    }

    private static class DefenderClan {

        private Clan _clan;
        private int _type;
        private int _time;

        public DefenderClan(Clan clan, int type, int time) {
            _clan = clan;
            _type = type;
            _time = time;
        }
    }
}