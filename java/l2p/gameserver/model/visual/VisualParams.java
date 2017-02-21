package l2p.gameserver.model.visual;

import l2p.gameserver.model.items.ItemInstance;

/**
 * Created by Hack
 * Date: 03.02.2017 1:54
 */
public class VisualParams {
    private ItemInstance item;
    private ItemInstance consumable;

    public ItemInstance getItem() {
        return item;
    }

    public void setItem(ItemInstance item) {
        this.item = item;
    }

    public ItemInstance getConsumable() {
        return consumable;
    }

    public void setConsumable(ItemInstance consumable) {
        this.consumable = consumable;
    }
}
