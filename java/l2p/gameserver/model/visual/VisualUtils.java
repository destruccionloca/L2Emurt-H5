package l2p.gameserver.model.visual;

import l2p.gameserver.Config;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.templates.item.ItemTemplate;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by Hack
 * Date: 03.02.2017 22:27
 */
public class VisualUtils {
    public static boolean isCostume(ItemInstance item) {
        return isCostume(item.getVisualItemId());
    }

    public static boolean isCostume(int itemId) {
        return ArrayUtils.contains(Config.VISUAL_COSTUMES, itemId);
    }

    public static boolean isRejectedConsumable(int itemId) {
        return ArrayUtils.contains(Config.VISUAL_DISALLOWED_ITEMS, itemId);
    }

    public static boolean isRejectedConsumable(ItemInstance item) {
        return isRejectedConsumable(item.getItemId());
    }

    public static boolean isAllowedItem(ItemInstance item) {
        return item.getBodyPart() != ItemTemplate.SLOT_BELT && item.getBodyPart() != ItemTemplate.SLOT_UNDERWEAR;
    }
}