package l2p.gameserver.model.visual;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;

/**
 * Created by Hack
 * Date: 03.02.2017 2:36
 */
public interface IItemCond {
    boolean check(Player player, ItemInstance item);
}
