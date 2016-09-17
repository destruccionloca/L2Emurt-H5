package handler.items;

import l2p.gameserver.handler.items.IItemHandler;
import l2p.gameserver.model.Playable;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.Log;

public abstract class ScriptItemHandler implements IItemHandler {

    @Override
    public boolean pickupItem(Playable playable, ItemInstance item) {
        return true;
    }

    @Override
    public void dropItem(Player player, ItemInstance item, long count, Location loc) {
        if (item.isEquipped()) {
            player.getInventory().unEquipItem(item);
            player.sendUserInfo(true);
        }

        item = player.getInventory().removeItemByObjectId(item.getObjectId(), count);
        if (item == null) {
            player.sendActionFailed();
            return;
        }

        Log.LogItem(player, Log.Drop, item);

        item.dropToTheGround(player, loc);
        player.disableDrop(1000);

        player.sendChanges();
    }
}
