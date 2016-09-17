package l2p.gameserver.clientpackets;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.matching.MatchingRoom;
import l2p.gameserver.serverpackets.ExMpccPartymasterList;

/**
 * @author VISTALL
 */
public class RequestExMpccPartymasterList extends L2GameClientPacket {

    @Override
    protected void readImpl() {
        //
    }

    @Override
    protected void runImpl() {
        Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }

        MatchingRoom room = player.getMatchingRoom();
        if (room == null || room.getType() != MatchingRoom.CC_MATCHING) {
            return;
        }

        Set<String> set = room.getPlayers().stream().filter($member -> $member.getParty() != null).map($member -> $member.getParty().getPartyLeader().getName()).collect(Collectors.toSet());

        player.sendPacket(new ExMpccPartymasterList(set));
    }
}