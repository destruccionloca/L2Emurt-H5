package l2p.gameserver.listener.actor.player;

import l2p.gameserver.listener.PlayerListener;
import l2p.gameserver.model.Player;

public interface OnPlayerPartyInviteListener extends PlayerListener {

    void onPartyInvite(Player player);
}
