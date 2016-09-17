package l2p.gameserver.model;

import java.util.Iterator;

import l2p.commons.collections.EmptyIterator;
import l2p.gameserver.serverpackets.components.IStaticPacket;

public interface PlayerGroup extends Iterable<Player> {

    PlayerGroup EMPTY = new PlayerGroup() {
        @Override
        public void broadCast(IStaticPacket... packet) {
        }

        @Override
        public int getMemberCount() {
            return 0;
        }

        @Override
        public Player getGroupLeader() {
            return null;
        }

        @Override
        public Iterator<Player> iterator() {
            return EmptyIterator.getInstance();
        }
    };

    void broadCast(IStaticPacket... paramVarArgs);

    int getMemberCount();

    Player getGroupLeader();
}
