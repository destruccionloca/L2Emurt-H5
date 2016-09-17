package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import l2p.gameserver.instancemanager.MatchingRoomManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.matching.MatchingRoom;

/**
 * @author VISTALL
 */
public class ExMpccRoomMember extends L2GameServerPacket {

    private int _type;
    private List<MpccRoomMemberInfo> _members = Collections.emptyList();

    public ExMpccRoomMember(MatchingRoom room, Player player) {
        _type = room.getMemberType(player);
        _members = new ArrayList<>(room.getPlayers().size());

        _members.addAll(room.getPlayers().stream().map(member -> new MpccRoomMemberInfo(member, room.getMemberType(member))).collect(Collectors.toList()));
    }

    @Override
    public void writeImpl() {
        writeEx(0x9F);
        writeD(_type);
        writeD(_members.size());
        for (MpccRoomMemberInfo member : _members) {
            writeD(member.objectId);
            writeS(member.name);
            writeD(member.level);
            writeD(member.classId);
            writeD(member.location);
            writeD(member.memberType);
        }
    }

    static class MpccRoomMemberInfo {

        public final int objectId;
        public final int classId;
        public final int level;
        public final int location;
        public final int memberType;
        public final String name;

        public MpccRoomMemberInfo(Player member, int type) {
            this.objectId = member.getObjectId();
            this.name = member.getName();
            this.classId = member.getClassId().ordinal();
            this.level = member.getLevel();
            this.location = MatchingRoomManager.getInstance().getLocation(member);
            this.memberType = type;
        }
    }
}