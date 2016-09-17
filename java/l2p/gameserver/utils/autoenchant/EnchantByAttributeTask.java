package l2p.gameserver.utils.autoenchant;

import l2p.commons.dao.JdbcEntityState;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.ItemHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.Element;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.serverpackets.ActionFail;
import l2p.gameserver.serverpackets.InventoryUpdate;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.utils.ItemFunctions;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hack
 * Date: 29.05.2016 5:07
 */
@SuppressWarnings("all")
public class EnchantByAttributeTask implements Runnable {
    private Player player;
    public EnchantByAttributeTask(Player player) {
        this.player = player;
    }
    @Override
    public void run() {
        if (!checkPlayer(player))
            return;
        boolean isNeedUpdate = false;
        int stones = 0;
        int crystals = 0;
        int success = 0;
        PcInventory inventory = player.getInventory();
        ItemInstance itemToEnchant = player.getEnchantParams().targetItem;
        ItemInstance stone = player.getEnchantParams().upgradeItem;
        player.setEnchantScroll(null);
        try {
            for (int i = 0; i < player.getEnchantParams().upgradeItemLimit && checkAttributeLvl(player); i++) {
                if (!checkPlayer(player))
                    return;
                if (itemToEnchant == null) {
                    player.sendActionFailed();
                    player.sendMessage("Ошибка: выберите предмет!");
                    return;
                }
                if (stone == null) {
                    player.sendActionFailed();
                    player.sendMessage("Ошибка: выберите камень!");
                    return;
                }

                ItemTemplate item = itemToEnchant.getTemplate();

                if (!itemToEnchant.canBeEnchanted(true) || item.getCrystalType().cry < ItemTemplate.CRYSTAL_S) {
                    player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS, ActionFail.STATIC);
                    return;
                }

                if (itemToEnchant.getLocation() != ItemInstance.ItemLocation.INVENTORY && itemToEnchant.getLocation() != ItemInstance.ItemLocation.PAPERDOLL) {
                    player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS, ActionFail.STATIC);
                    return;
                }

                if (itemToEnchant.isStackable() || (stone = inventory.getItemByObjectId(stone.getObjectId())) == null) {
                    player.sendActionFailed();
                    player.sendMessage("Ошибка: не соблюдены условия!");
                    return;
                }

                Element element = ItemFunctions.getEnchantAttributeStoneElement(stone.getItemId(), itemToEnchant.isArmor());

                if (itemToEnchant.isArmor()) {
                    if (itemToEnchant.getAttributeElementValue(Element.getReverseElement(element), false) != 0) {
                        player.sendPacket(SystemMsg.ANOTHER_ELEMENTAL_POWER_HAS_ALREADY_BEEN_ADDED_THIS_ELEMENTAL_POWER_CANNOT_BE_ADDED, ActionFail.STATIC);
                        return;
                    }
                } else if (itemToEnchant.isWeapon()) {
                    if (itemToEnchant.getAttributeElement() != Element.NONE && itemToEnchant.getAttributeElement() != element) {
                        player.sendPacket(SystemMsg.ANOTHER_ELEMENTAL_POWER_HAS_ALREADY_BEEN_ADDED_THIS_ELEMENTAL_POWER_CANNOT_BE_ADDED, ActionFail.STATIC);
                        return;
                    }
                } else {
                    player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS, ActionFail.STATIC);
                    return;
                }

                if (item.isUnderwear() || item.isCloak() || item.isBracelet() || item.isBelt() || !item.isAttributable()) {
                    player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS, ActionFail.STATIC);
                    return;
                }

//            int minValue = 0;
                int maxValue = itemToEnchant.isWeapon() ? 150 : 60;
                int maxValueCrystal = itemToEnchant.isWeapon() ? 300 : 120;

                if (!stone.getTemplate().isAttributeCrystal() && itemToEnchant.getAttributeElementValue(element, false) >= maxValue
                        || stone.getTemplate().isAttributeCrystal() && (itemToEnchant.getAttributeElementValue(element, false) < maxValue
                        || itemToEnchant.getAttributeElementValue(element, false) >= maxValueCrystal)) {
                    player.sendPacket(SystemMsg.ELEMENTAL_POWER_ENHANCER_USAGE_REQUIREMENT_IS_NOT_SUFFICIENT, ActionFail.STATIC);
                    return;
                }

                // Запрет на заточку чужих вещей, баг может вылезти на серверных лагах
                if (itemToEnchant.getOwnerId() != player.getObjectId()) {
                    player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS, ActionFail.STATIC);
                    return;
                }

                if (!inventory.destroyItem(stone, 1)) {
                    player.sendActionFailed();
                    player.sendMessage("Ошибка: нехватает камней!");
                    return;
                }

                if (Rnd.chance((stone.getTemplate().isAttributeCrystal() ? Config.ENCHANT_ATTRIBUTE_CRYSTAL_CHANCE : Config.ENCHANT_ATTRIBUTE_STONE_CHANCE) + Config.ENCHANT_ATTRIBUTE_CHANCE_CORRECT)) {
                    success++;
/*
            if (itemToEnchant.getEnchantLevel() == 0) {
                SystemMessage2 sm = new SystemMessage2(SystemMsg.S2_ELEMENTAL_POWER_HAS_BEEN_ADDED_SUCCESSFULLY_TO_S1);
                sm.addItemName(itemToEnchant.getItemId());
                sm.addItemName(stone.getItemId());
                player.sendPacket(sm);
            } else {
                SystemMessage2 sm = new SystemMessage2(SystemMsg.S3_ELEMENTAL_POWER_HAS_BEEN_ADDED_SUCCESSFULLY_TO_S1_S2);
                sm.addInteger(itemToEnchant.getEnchantLevel());
                sm.addItemName(itemToEnchant.getItemId());
                sm.addItemName(stone.getItemId());
                player.sendPacket(sm);
            }
*/
                    int value;
                    if (Config.ALLOW_ALT_ATT_ENCHANT) {
                        value = itemToEnchant.isWeapon() ? Config.ALT_ATT_ENCHANT_WEAPON_VALUE : Config.ALT_ATT_ENCHANT_ARMOR_VALUE;
                    } else {
                        value = itemToEnchant.isWeapon() ? 5 : 6;
                    }

                    // Для оружия 1й камень дает +20 атрибута
                    if (itemToEnchant.getAttributeElementValue(element, false) == 0 && itemToEnchant.isWeapon() && !Config.ALLOW_ALT_ATT_ENCHANT) {
                        value = 20;
                    }

                    boolean equipped;
                    if (equipped = itemToEnchant.isEquipped()) {
                        player.getInventory().isRefresh = true;
                        player.getInventory().unEquipItem(itemToEnchant);
                    }

                    itemToEnchant.setAttributeElement(element, itemToEnchant.getAttributeElementValue(element, false) + value);
                    itemToEnchant.setJdbcState(JdbcEntityState.UPDATED);
                    itemToEnchant.update();

                    if (equipped) {
                        player.getInventory().equipItem(itemToEnchant);
                        player.getInventory().isRefresh = false;
                    }

//            player.sendPacket(new InventoryUpdate().addModifiedItem(itemToEnchant));
//            player.sendPacket(new ExAttributeEnchantResult(value));
                }
                if (EnchantUtils.getInstance().isAttributeStone(stone))
                    stones++;
                else if (EnchantUtils.getInstance().isAttributeCrystal(stone))
                    crystals++;
                isNeedUpdate = true;
//        else
//            player.sendPacket(SystemMsg.YOU_HAVE_FAILED_TO_ADD_ELEMENTAL_POWER);
            }
        }
        finally {
            if (isNeedUpdate) {
                if (Config.ENCHANT_CONSUME_ITEM != 0) {
                    player.getInventory().destroyItemByItemId(Config.ENCHANT_CONSUME_ITEM, Config.ENCHANT_CONSUME_ITEM_COUNT);
                    ItemTemplate template = ItemHolder.getInstance().getTemplate(Config.ENCHANT_CONSUME_ITEM);
                    player.sendMessage("Потрачено " + Config.ENCHANT_CONSUME_ITEM_COUNT + " "
                            + (template != null ? template.getName() : "[Ошибка: не найден item id! Пожалуйста сообщение администратору об ошибке]"));
                }
                player.sendPacket(new InventoryUpdate().addModifiedItem(itemToEnchant));
                player.sendPacket(new InventoryUpdate());
                player.setEnchantScroll(null);
                player.updateStats();
                Map<String, Integer> result = new HashMap<>();
                result.put("enchant", getAttValue());
                result.put("stones", stones);
                result.put("crystals", crystals);
                int sum = stones + crystals;
                if (sum == 0) sum++; // unreachable in normal situation
                result.put("chance", (int)(((double)success / ((double)sum / 100.)) * 100));
                result.put("success", itemToEnchant == null ? 0 : getAttValue() >= player.getEnchantParams().maxEnchantAtt ? 1 : 0);
                EnchantParser.getInstance().showResultPage(player, EnchantType.Attribute, result);
            }
        }
    }

    private boolean checkPlayer(Player player) {
        if (player == null) {
            return false;
        }

        if (player.isActionsDisabled()) {
            player.sendActionFailed();
            player.sendMessage("Ошибка: действие невозможно!");
            return false;
        }

        if (player.isInStoreMode()) {
            player.sendPacket(SystemMsg.YOU_CANNOT_ADD_ELEMENTAL_POWER_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP, ActionFail.STATIC);
            return false;
        }

        if (player.isInTrade()) {
            player.sendActionFailed();
            player.sendMessage("Ошибка: действие невозможно во время торговли!");
            return false;
        }
        return true;
    }

    private boolean checkAttributeLvl(Player player) {
        if (player == null)
            return false;
        int attValue = getAttValue();
        ItemInstance stone = player.getEnchantParams().upgradeItem;
        int max = player.getEnchantParams().maxEnchantAtt;
//        return EnchantUtils.getInstance().isAttributeStone(stone) && attValue < 150 || EnchantUtils.getInstance().isAttributeCrystal(stone) && attValue < 300; wtf am i doing Oo?
        return attValue < max;
    }

    private int getAttValue() {
        ItemInstance targetItem = player.getEnchantParams().targetItem;
        ItemInstance enchantItem = player.getEnchantParams().upgradeItem;
        if (targetItem == null || enchantItem == null)
            return 0;
        if (targetItem.isWeapon())
            return targetItem.getAttackElementValue();
        else
            return targetItem.getAttributeElementValue(Element.getReverseElement(EnchantUtils.getInstance().getStoneElement(enchantItem)), false);
    }
}

