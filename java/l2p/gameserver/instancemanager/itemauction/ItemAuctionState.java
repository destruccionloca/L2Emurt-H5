package l2p.gameserver.instancemanager.itemauction;

import l2p.commons.lang.ArrayUtils;

public enum ItemAuctionState {

    CREATED,
    STARTED,
    FINISHED;

    public static ItemAuctionState stateForStateId(int stateId) {
        return ArrayUtils.valid(values(), stateId);
    }
}