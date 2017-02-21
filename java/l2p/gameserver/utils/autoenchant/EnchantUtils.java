package l2p.gameserver.utils.autoenchant;

import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.Element;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.templates.item.EtcItemTemplate;
import l2p.gameserver.templates.item.ItemTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hack
 * Date: 20.05.2016 16:28
 */
public class EnchantUtils {
    private static EnchantUtils instance;

    public static EnchantUtils getInstance() {
        return instance == null ? instance = new EnchantUtils() : instance;
    }

    //TODO
    public boolean isAttributeStone(ItemInstance item) {
        int[] stones = {
                9547, // Water Stone
                9548, // Earth Stone
                9549, // Wind Stone
                9550, // Dark Stone
                9551, // Divine Stone
                9546
        };
        for(int id : stones)
            if(id == item.getItemId())
                return true;
        return false;
    }

    //TODO
    public boolean isAttributeCrystal(ItemInstance item) {
        int[] crystals = {
                9552, // Fire Crystal
                9553, // Water Crystal
                9554, // Earth Crystal
                9555, // Wind Crystal
                9556, // Dark Crystal
                9557, // Divine Crystal
        };
        for(int id : crystals)
            if(id == item.getItemId())
                return true;
        return false;
    }

    public boolean isAttribute(ItemInstance item) {
        return isAttributeCrystal(item) || isAttributeStone(item);
    }

    public boolean isEnchantScroll(ItemInstance item) {
        return item.getTemplate().getItemType() == EtcItemTemplate.EtcItemType.SCROLL && item.getName().contains("Enchant");
    }

    public boolean isBlessed(ItemInstance item) {
        return item.getName().contains("Blessed");
    }

    public boolean isArmorScroll(ItemInstance item) {
        return item.getName().contains("Armor");
    }

    public List<ItemInstance> getWeapon(Player player) {
        List<ItemInstance> weapon = new ArrayList<>();
        for (ItemInstance item : player.getInventory().getItems())
            if (item.isWeapon()) {
                if (item.getCrystalType() == ItemTemplate.Grade.NONE)
                    continue;
                weapon.add(item);
            }
        return weapon;
    }

    public List<ItemInstance> getArmor(Player player) {
        List<ItemInstance> armor = new ArrayList<>();
        for (ItemInstance item : player.getInventory().getItems())
            if (item.isArmor() || item.getBodyPart() == ItemTemplate.SLOT_UNDERWEAR
                    || (item.getBodyPart() == 25 && Config.ENCHANT_ALLOW_BELTS)) {
                if (item.getCrystalType() == ItemTemplate.Grade.NONE)
                    continue;
                armor.add(item);
            }
        return armor;
    }

    public List<ItemInstance> getJewelry(Player player) {
        List<ItemInstance> jewelry = new ArrayList<>();
        for (ItemInstance item : player.getInventory().getItems())
            if (item.isAccessory()) {
                if (item.getCrystalType() == ItemTemplate.Grade.NONE)
                    continue;
                jewelry.add(item);
            }
        return jewelry;
    }

    public List<ItemInstance> getAtributes(Player player) {
        List<ItemInstance> stones = new ArrayList<>();
        for (ItemInstance item : player.getInventory().getItems())
            if (isAttribute(item))
                stones.add(item);
        return stones;
    }

    public List<ItemInstance> getScrolls(Player player) {
        List<ItemInstance> scrolls = new ArrayList<>();
        for (ItemInstance item : player.getInventory().getItems())
            if (isEnchantScroll(item))
                scrolls.add(item);
        return scrolls;
    }

    public void enchant(Player player) {
        if (player == null)
            return;
        ItemInstance upgradeItem = player.getEnchantParams().upgradeItem;
        if (upgradeItem == null) {
            player.sendMessage("Улучшатель не выбран!");
            return;
        }
        if (player.getEnchantParams().targetItem == null) {
            player.sendMessage("Предмет не выбран!");
            return;
        }

        if (isEnchantScroll(upgradeItem))
            ThreadPoolManager.getInstance().execute(new EnchantByScrollTask(player));
        else if (isAttribute(upgradeItem))
            ThreadPoolManager.getInstance().execute(new EnchantByAttributeTask(player));
    }

    //TODO
    public ItemInstance getUnsafeEnchantScroll(Player player, ItemInstance item) {
        int scrollId = 0;
        ItemInstance scroll = null;
        ItemTemplate.Grade type = item.getCrystalType();
        if (item.isWeapon()) {
            if (type == ItemTemplate.Grade.D) scrollId = 955;
            else if (type == ItemTemplate.Grade.C) scrollId = 951;
            else if (type == ItemTemplate.Grade.B) scrollId = 947;
            else if (type == ItemTemplate.Grade.A) scrollId = 729;
            else if (type == ItemTemplate.Grade.S || type == ItemTemplate.Grade.S80 || type == ItemTemplate.Grade.S84) scrollId = 959;
        }
        else if (item.isArmor() || item.isAccessory()) {
            if (type == ItemTemplate.Grade.D) scrollId = 956;
            else if (type == ItemTemplate.Grade.C) scrollId = 952;
            else if (type == ItemTemplate.Grade.B) scrollId = 948;
            else if (type == ItemTemplate.Grade.A) scrollId = 730;
            else if (type == ItemTemplate.Grade.S || type == ItemTemplate.Grade.S80 || type == ItemTemplate.Grade.S84) scrollId = 960;
        }
        if (scrollId != 0)
            scroll = player.getInventory().getItemByItemId(scrollId);
        return scroll;
    }

    //TODO
    public Element getStoneElement(ItemInstance stone) {
        int id = stone.getItemId();
        if (id == 9546 || id == 9552)
            return Element.FIRE;
        if (id == 9547 || id == 9553)
            return Element.WATER;
        if (id == 9548 || id == 9554)
            return Element.EARTH;
        if (id == 9549 || id == 9555)
            return Element.WIND;
        if (id == 9550 || id == 9556)
            return Element.UNHOLY;
        if (id == 9551 || id == 9557)
            return Element.HOLY;
        return Element.NONE;
    }

}
