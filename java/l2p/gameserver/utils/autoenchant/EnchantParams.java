package l2p.gameserver.utils.autoenchant;

import l2p.gameserver.model.items.ItemInstance;

/**
 * Created by Hack
 * Date: 20.05.2016 16:29
 */
public class EnchantParams {
    public ItemInstance targetItem;
    public ItemInstance upgradeItem;
    public boolean isUseCommonScrollWhenSafe = true;
    public int upgradeItemLimit = 10;
    public int maxEnchant = 6;
    public int maxEnchantAtt = 150;
    public boolean isChangingUpgradeItemLimit = false;
    public boolean isChangingMaxEnchant = false;
}
