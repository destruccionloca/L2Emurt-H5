package l2p.gameserver.listener.actor.player;

import l2p.gameserver.listener.PlayerListener;
import l2p.gameserver.model.Player;

public interface OnPlayerUpLvlListener extends PlayerListener {

    void onLvlUp(Player player);
}
