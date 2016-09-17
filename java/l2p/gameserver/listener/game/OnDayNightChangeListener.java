package l2p.gameserver.listener.game;

import l2p.gameserver.listener.GameListener;

public interface OnDayNightChangeListener extends GameListener {

    void onDay();

    void onNight();
}
