package l2p.gameserver.utils.autoenchant;

import l2p.commons.dao.JdbcEntityState;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.ItemHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.actor.instances.player.Bonus;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.premium.PremiumConfig;
import l2p.gameserver.serverpackets.EnchantResult;
import l2p.gameserver.serverpackets.InventoryUpdate;
import l2p.gameserver.serverpackets.MagicSkillUse;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.templates.item.WeaponTemplate;
import l2p.gameserver.utils.ItemFunctions;
import l2p.gameserver.utils.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hack
 * Date: 26.05.2016 18:07
 */
public class EnchantByScrollTask implements Runnable {
    Player player;
    public EnchantByScrollTask(Player player) {
        this.player = player;
    }

    @Override
    @SuppressWarnings("all")
    public void run() {
        if (!checkPlayer(player))
            return;
        boolean isNeedEquip = false;
        boolean isNeedUpdate = false;

        // stat
        int isCrystallized = 0;
        int maxEnchant = 0;
        int commonScrolls = 0;
        int scrolls = 0;
        int success = 0;
        int count = 0;

        ItemInstance item = player.getEnchantParams().targetItem;
        ItemInstance scroll;// = player.getEnchantParams().upgradeItem;
        ItemInstance catalyst = null; // TODO
        PcInventory inventory = player.getInventory();
        if (item.isEquipped()) {
            inventory.unEquipItem(item);
            isNeedEquip = true;
        }
        try {
            inventory.writeLock();
            for (int i = 0; i < player.getEnchantParams().upgradeItemLimit && player.getEnchantParams().targetItem.getEnchantLevel() < player.getEnchantParams().maxEnchant; i++) {
                if (!checkPlayer(player))
                    return;
                if (item == null) {
                    player.sendMessage("Ошибка: выберите предмет!");
                    return;
                }
//                System.out.println("lvl: " + item.getEnchantLevel() + " safe: " + Config.SAFE_ENCHANT_COMMON + "param: " + player.getEnchantParams().isUseCommonScrollWhenSafe);
                if (item.getEnchantLevel() < Config.SAFE_ENCHANT_COMMON && player.getEnchantParams().isUseCommonScrollWhenSafe) {
                    scroll = EnchantUtils.getInstance().getUnsafeEnchantScroll(player, item);
                    if (scroll == null) {
                        player.sendMessage("Ошибка: не соблюдены условия, либо обычные свитки закончились!");
                        return;
                    }
                    commonScrolls++;
                } else {
                    scroll = player.getEnchantParams().upgradeItem;
                    if (scroll == null) {
                        player.sendMessage("Ошибка: закончились свитки!");
                        return;
                    }
                    scrolls++;
                }

                player.setEnchantScroll(scroll);
/*
                if (item == null || scroll == null) {
                    player.sendActionFailed();
                    player.sendMessage("Ошибка: неверное количество предметов!");
                    return;
                }
*/
                // Затычка, ибо клиент криво обрабатывает RequestExTryToPutEnchantSupportItem
                if (!ItemFunctions.checkCatalyst(item, catalyst)) {
                    catalyst = null;
                }

                if (!item.canBeEnchanted(true)) {
                    player.sendPacket(EnchantResult.CANCEL);
                    player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
                    player.sendActionFailed();
                    return;
                }

                int crystalId = ItemFunctions.getEnchantCrystalId(item, scroll, catalyst);

                if (crystalId == -1) {
                    player.sendPacket(EnchantResult.CANCEL);
                    player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
                    player.sendActionFailed();
                    return;
                }

                int scrollId = scroll.getItemId();

                if (scrollId == 13540 && item.getItemId() != 13539) {
                    player.sendPacket(EnchantResult.CANCEL);
                    player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
                    player.sendActionFailed();
                    return;
                }

                // ольф 21580(21581/21582)
                if ((scrollId == 21581 || scrollId == 21582) && item.getItemId() != 21580) {
                    player.sendPacket(EnchantResult.CANCEL);
                    player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
                    player.sendActionFailed();
                    return;
                }

                // ольф 21706(21707)
                if (scrollId == 21707 && item.getItemId() != 21706) {
                    player.sendPacket(EnchantResult.CANCEL);
                    player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
                    player.sendActionFailed();
                    return;
                }

                // ольф 21706(21707)
                if (scrollId != 21707 && item.getItemId() == 21706) {
                    player.sendPacket(EnchantResult.CANCEL);
                    player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
                    player.sendActionFailed();
                    return;
                }

                // ольф 21580(21581/21582)
                if ((scrollId != 21581 || scrollId != 21582) && item.getItemId() == 21580) {
                    player.sendPacket(EnchantResult.CANCEL);
                    player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
                    player.sendActionFailed();
                    return;
                }

                // TODO: [pchayka] временный хардкод до улучения системы описания свитков заточки
                if (ItemFunctions.isDestructionWpnEnchantScroll(scrollId) && item.getEnchantLevel() >= Config.ENCHANT_MAX_DESTRUCTION_WEAPON || ItemFunctions.isDestructionArmEnchantScroll(scrollId) && item.getEnchantLevel() >= Config.ENCHANT_MAX_DESTRUCTION_ARMOR) {
                    player.sendPacket(EnchantResult.CANCEL);
                    player.sendPacket(SystemMsg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
                    player.sendActionFailed();
                    return;
                }

                int itemType = item.getTemplate().getType2();
                boolean fail = false;
                switch (item.getItemId()) {
                    case 13539:
                        if (item.getEnchantLevel() >= Config.ENCHANT_MAX_MASTER_YOGI_STAFF) {
                            fail = true;
                        }
                        break;
                    case 21580:
                        if (item.getEnchantLevel() <= Config.MAX_ENCHANT_OLF) {
                            fail = false;
                        } else {
                            fail = true;
                        }
                        break;
                    default: {
                        if (itemType == ItemTemplate.TYPE2_WEAPON) {
                            if (Config.ENCHANT_MAX_WEAPON > 0 && item.getEnchantLevel() >= Config.ENCHANT_MAX_WEAPON) {
                                fail = true;
                            }
                        } else if (itemType == ItemTemplate.TYPE2_SHIELD_ARMOR) {
                            if (Config.ENCHANT_MAX_ARMOR > 0 && item.getEnchantLevel() >= Config.ENCHANT_MAX_ARMOR) {
                                fail = true;
                            }
                        } else if (itemType == ItemTemplate.TYPE2_ACCESSORY) {
                            if (Config.ENCHANT_MAX_JEWELRY > 0 && item.getEnchantLevel() >= Config.ENCHANT_MAX_JEWELRY) {
                                fail = true;
                            }
                        } else {
                            fail = true;
                        }
                    }
                    break;
                }

                if (!inventory.destroyItem(scroll, 1L) || catalyst != null && !inventory.destroyItem(catalyst, 1L)) {
                    player.sendPacket(EnchantResult.CANCEL);
                    player.sendActionFailed();
                    return;
                }

                if (fail) {
                    player.sendPacket(EnchantResult.CANCEL);
                    player.sendPacket(SystemMsg.INAPPROPRIATE_ENCHANT_CONDITIONS);
                    player.sendActionFailed();
                    return;
                }

                int safeEnchantLevel = item.getTemplate().getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR ? Config.SAFE_ENCHANT_FULL_BODY : Config.SAFE_ENCHANT_COMMON;

                double chance;
                if (item.getEnchantLevel() < safeEnchantLevel) {
                    chance = 100;

                } else if (itemType == ItemTemplate.TYPE2_WEAPON) {

                    //  boolean magewep = (itemType == 0) && (crystaltype.cry >= 1459) && (wepToEnchant.getPDamage() - wepToEnchant.getMDamage() <= wepToEnchant.getPDamage() * 0.4D);
                    boolean mageWeapon = false;
                    if (item.getTemplate().getItemType() == WeaponTemplate.WeaponType.SWORD || item.getTemplate().getItemType() == WeaponTemplate.WeaponType.BIGBLUNT || item.getTemplate().getItemType() == WeaponTemplate.WeaponType.BLUNT) {
                        mageWeapon = true;
                    }

                    if (Config.USE_ALT_ENCHANT) {
                        if (!mageWeapon) {
                            if (ItemFunctions.isCrystallEnchantScroll(scrollId)) {
                                chance = item.getEnchantLevel() > Config.ENCHANT_WEAPON_FIGHT_CRYSTAL.size() ? Config.ENCHANT_WEAPON_FIGHT_CRYSTAL.get(Config.ENCHANT_WEAPON_FIGHT_CRYSTAL.size() - 1) : Config.ENCHANT_WEAPON_FIGHT_CRYSTAL.get(item.getEnchantLevel());
                            } else if (ItemFunctions.isBlessedEnchantScroll(scrollId)) {
                                chance = item.getEnchantLevel() > Config.ENCHANT_WEAPON_FIGHT_BLESSED.size() ? Config.ENCHANT_WEAPON_FIGHT_BLESSED.get(Config.ENCHANT_WEAPON_FIGHT_BLESSED.size() - 1) : Config.ENCHANT_WEAPON_FIGHT_BLESSED.get(item.getEnchantLevel());
                            } else {
                                chance = item.getEnchantLevel() > Config.ENCHANT_WEAPON_FIGHT.size() ? Config.ENCHANT_WEAPON_FIGHT.get(Config.ENCHANT_WEAPON_FIGHT.size() - 1) : Config.ENCHANT_WEAPON_FIGHT.get(item.getEnchantLevel());
                            }
                        } else {
                            if (ItemFunctions.isCrystallEnchantScroll(scrollId)) {
                                chance = item.getEnchantLevel() > Config.ENCHANT_WEAPON_MAGE_CRYSTAL.size() ? Config.ENCHANT_WEAPON_MAGE_CRYSTAL.get(Config.ENCHANT_WEAPON_MAGE_CRYSTAL.size() - 1) : Config.ENCHANT_WEAPON_MAGE_CRYSTAL.get(item.getEnchantLevel());
                            } else if (ItemFunctions.isBlessedEnchantScroll(scrollId)) {
                                chance = item.getEnchantLevel() > Config.ENCHANT_WEAPON_MAGE_BLESSED.size() ? Config.ENCHANT_WEAPON_MAGE_BLESSED.get(Config.ENCHANT_WEAPON_MAGE_BLESSED.size() - 1) : Config.ENCHANT_WEAPON_MAGE_BLESSED.get(item.getEnchantLevel());
                            } else {
                                chance = item.getEnchantLevel() > Config.ENCHANT_WEAPON_MAGE.size() ? Config.ENCHANT_WEAPON_MAGE.get(Config.ENCHANT_WEAPON_MAGE.size() - 1) : Config.ENCHANT_WEAPON_MAGE.get(item.getEnchantLevel());
                            }
                        }
                    } else if ((Config.SERVICES_RATE_TYPE != Bonus.NO_BONUS && player.hasBonus()) && PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).USE_ALT_ENCHANT) {
                        if (!mageWeapon) {
                            if (ItemFunctions.isCrystallEnchantScroll(scrollId)) {
                                chance = item.getEnchantLevel() > PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_WEAPON_FIGHTER_CRYSTALL.size() ? PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_WEAPON_FIGHTER_CRYSTALL.get(PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_WEAPON_FIGHTER_CRYSTALL.size() - 1) : PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_WEAPON_FIGHTER_CRYSTALL.get(item.getEnchantLevel());
                            } else if (ItemFunctions.isBlessedEnchantScroll(scrollId)) {
                                chance = item.getEnchantLevel() > PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_WEAPON_FIGHTER_BLESSED.size() ? PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_WEAPON_FIGHTER_BLESSED.get(PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_WEAPON_FIGHTER_BLESSED.size() - 1) : PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_WEAPON_FIGHTER_BLESSED.get(item.getEnchantLevel());
                            } else {
                                chance = item.getEnchantLevel() > PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_WEAPON_FIGHTER.size() ? PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_WEAPON_FIGHTER.get(PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_WEAPON_FIGHTER.size() - 1) : PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_WEAPON_FIGHTER.get(item.getEnchantLevel());
                            }
                        } else {
                            if (ItemFunctions.isCrystallEnchantScroll(scrollId)) {
                                chance = item.getEnchantLevel() > PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_WEAPON_MAGE_CRYSTALL.size() ? PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_WEAPON_MAGE_CRYSTALL.get(PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_WEAPON_MAGE_CRYSTALL.size() - 1) : PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_WEAPON_MAGE_CRYSTALL.get(item.getEnchantLevel());
                            } else if (ItemFunctions.isBlessedEnchantScroll(scrollId)) {
                                chance = item.getEnchantLevel() > PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_WEAPON_MAGE_BLESSED.size() ? PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_WEAPON_MAGE_BLESSED.get(PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_WEAPON_MAGE_BLESSED.size() - 1) : PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_WEAPON_MAGE_BLESSED.get(item.getEnchantLevel());
                            } else {
                                chance = item.getEnchantLevel() > PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_WEAPON_MAGE.size() ? PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_WEAPON_MAGE.get(PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_WEAPON_MAGE.size() - 1) : PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_WEAPON_MAGE.get(item.getEnchantLevel());
                            }
                        }
                    } else if (Config.SERVICES_RATE_TYPE != Bonus.NO_BONUS && player.hasBonus()) {
                        if (ItemFunctions.isBlessedEnchantScroll(scrollId)) {
                            chance = PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_CHANCE_BLESSED;
                        } else {
                            chance = ItemFunctions.isCrystallEnchantScroll(scrollId) ? PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_CHANCE_CRYSTAL : PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_CHANCE;
                        }
                    } else if (ItemFunctions.isBlessedEnchantScroll(scrollId)) {
                        chance = Config.ENCHANT_CHANCE_WEAPON_BLESS;
                    } else {
                        chance = ItemFunctions.isCrystallEnchantScroll(scrollId) ? Config.ENCHANT_CHANCE_CRYSTAL_WEAPON : Config.ENCHANT_CHANCE_WEAPON;
                    }
                } else if (itemType == ItemTemplate.TYPE2_SHIELD_ARMOR) {
                    if (Config.USE_ALT_ENCHANT) {
                        if (ItemFunctions.isCrystallEnchantScroll(scrollId)) {
                            chance = item.getEnchantLevel() > Config.ENCHANT_ARMOR_CRYSTAL.size() ? Config.ENCHANT_ARMOR_CRYSTAL.get(Config.ENCHANT_ARMOR_CRYSTAL.size() - 1) : Config.ENCHANT_ARMOR_CRYSTAL.get(item.getEnchantLevel());
                        } else if (ItemFunctions.isBlessedEnchantScroll(scrollId)) {
                            chance = item.getEnchantLevel() > Config.ENCHANT_ARMOR_BLESSED.size() ? Config.ENCHANT_ARMOR_BLESSED.get(Config.ENCHANT_ARMOR_BLESSED.size() - 1) : Config.ENCHANT_ARMOR_BLESSED.get(item.getEnchantLevel());
                        } else {
                            chance = item.getEnchantLevel() > Config.ENCHANT_ARMOR.size() ? Config.ENCHANT_ARMOR.get(Config.ENCHANT_ARMOR.size() - 1) : Config.ENCHANT_ARMOR.get(item.getEnchantLevel());
                        }
                    } else if ((Config.SERVICES_RATE_TYPE != Bonus.NO_BONUS && player.hasBonus()) && PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).USE_ALT_ENCHANT) {
                        if (ItemFunctions.isCrystallEnchantScroll(scrollId)) {
                            chance = item.getEnchantLevel() > PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_ARMOR_CRYSTALL.size() ? PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_ARMOR_CRYSTALL.get(PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_ARMOR_CRYSTALL.size() - 1) : PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_ARMOR_CRYSTALL.get(item.getEnchantLevel());
                        } else if (ItemFunctions.isBlessedEnchantScroll(scrollId)) {
                            chance = item.getEnchantLevel() > PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_ARMOR_BLESSED.size() ? PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_ARMOR_BLESSED.get(PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_ARMOR_BLESSED.size() - 1) : PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_ARMOR_BLESSED.get(item.getEnchantLevel());
                        } else {
                            chance = item.getEnchantLevel() > PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_ARMOR.size() ? PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_ARMOR.get(PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_ARMOR.size() - 1) : PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_ARMOR.get(item.getEnchantLevel());
                        }
                    } else if (Config.SERVICES_RATE_TYPE != Bonus.NO_BONUS && player.hasBonus()) {
                        if (ItemFunctions.isBlessedEnchantScroll(scrollId)) {
                            chance = PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_CHANCE_ARMOR_BLESSED;
                        } else {
                            chance = ItemFunctions.isCrystallEnchantScroll(scrollId) ? PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_CHANCE_ARMOR_CRYSTAL : PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_CHANCE_ARMOR;
                        }
                    } else if (ItemFunctions.isBlessedEnchantScroll(scrollId)) {
                        chance = Config.ENCHANT_CHANCE_ARMOR_BLESS;
                    } else {
                        chance = ItemFunctions.isCrystallEnchantScroll(scrollId) ? Config.ENCHANT_CHANCE_CRYSTAL_ARMOR : Config.ENCHANT_CHANCE_ARMOR;
                    }
                } else if (itemType == ItemTemplate.TYPE2_ACCESSORY) {
                    if (Config.USE_ALT_ENCHANT) {
                        if (ItemFunctions.isCrystallEnchantScroll(scrollId)) {
                            chance = item.getEnchantLevel() > Config.ENCHANT_ARMOR_JEWELRY_CRYSTAL.size() ? Config.ENCHANT_ARMOR_JEWELRY_CRYSTAL.get(Config.ENCHANT_ARMOR_JEWELRY_CRYSTAL.size() - 1) : Config.ENCHANT_ARMOR_JEWELRY_CRYSTAL.get(item.getEnchantLevel());
                        } else if (ItemFunctions.isBlessedEnchantScroll(scrollId)) {
                            chance = item.getEnchantLevel() > Config.ENCHANT_ARMOR_JEWELRY_BLESSED.size() ? Config.ENCHANT_ARMOR_JEWELRY_BLESSED.get(Config.ENCHANT_ARMOR_JEWELRY_BLESSED.size() - 1) : Config.ENCHANT_ARMOR_JEWELRY_BLESSED.get(item.getEnchantLevel());
                        } else {
                            chance = item.getEnchantLevel() > Config.ENCHANT_ARMOR_JEWELRY.size() ? Config.ENCHANT_ARMOR_JEWELRY.get(Config.ENCHANT_ARMOR_JEWELRY.size() - 1) : Config.ENCHANT_ARMOR_JEWELRY.get(item.getEnchantLevel());
                        }
                    } else if ((Config.SERVICES_RATE_TYPE != Bonus.NO_BONUS && player.hasBonus()) && PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).USE_ALT_ENCHANT) {
                        if (ItemFunctions.isCrystallEnchantScroll(scrollId)) {
                            chance = item.getEnchantLevel() > PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_ACCESSORY_CRYSTALL.size() ? PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_ACCESSORY_CRYSTALL.get(PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_ACCESSORY_CRYSTALL.size() - 1) : PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_ACCESSORY_CRYSTALL.get(item.getEnchantLevel());
                        } else if (ItemFunctions.isBlessedEnchantScroll(scrollId)) {
                            chance = item.getEnchantLevel() > PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_ACCESSORY_BLESSED.size() ? PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_ACCESSORY_BLESSED.get(PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_ACCESSORY_BLESSED.size() - 1) : PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_ACCESSORY_BLESSED.get(item.getEnchantLevel());
                        } else {
                            chance = item.getEnchantLevel() > PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_ACCESSORY.size() ? PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_ACCESSORY.get(PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_ACCESSORY.size() - 1) : PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_ACCESSORY.get(item.getEnchantLevel());
                        }
                    } else if (Config.SERVICES_RATE_TYPE != Bonus.NO_BONUS && player.hasBonus()) {
                        if (ItemFunctions.isBlessedEnchantScroll(scrollId)) {
                            chance = PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_CHANCE_ACCESSORY_BLESSED;
                        } else {
                            chance = ItemFunctions.isCrystallEnchantScroll(scrollId) ? PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_CHANCE_ACCESSORY_CRYSTAL : PremiumConfig.getPremConfigId(player.getBonus().getBonusId()).ENCHANT_CHANCE_ACCESSORY;
                        }
                    } else if (ItemFunctions.isBlessedEnchantScroll(scrollId)) {
                        chance = Config.ENCHANT_CHANCE_ACCESSORY_BLESS;
                    } else {
                        chance = ItemFunctions.isCrystallEnchantScroll(scrollId) ? Config.ENCHANT_CHANCE_CRYSTAL_ACCESSORY : Config.ENCHANT_CHANCE_ACCESSORY;
                    }
                } else {
                    player.sendPacket(EnchantResult.CANCEL);
                    player.sendActionFailed();
                    return;
                }

                if (ItemFunctions.isDivineEnchantScroll(scrollId)) // Item Mall divine
                {
                    chance = 100;
                } else if (ItemFunctions.isItemMallEnchantScroll(scrollId)) // Item Mall normal/ancient
                {
                    chance += 10;
                }

                if (catalyst != null) {
                    chance += ItemFunctions.getCatalystPower(catalyst.getItemId());
                }

                if (scrollId == 13540) {
                    chance = item.getEnchantLevel() < Config.SAFE_ENCHANT_MASTER_YOGI_STAFF ? 100 : Config.ENCHANT_CHANCE_MASTER_YOGI_STAFF;
                } else if (scrollId == 21581 || scrollId == 21582) {
                    if (item.getEnchantLevel() < 9) {
                        chance = item.getEnchantLevel() < 3 ? 100 : Config.ENCHANT_CHANCE_CRYSTAL_ARMOR_OLF;
                    } else {
                        chance = 0;
                    }
                }

                if (Rnd.chance(chance + Config.ENCHANT_SCROLL_CHANCE_CORRECT)) {
                    if (chance != 100)
                        success++;
                    if (Config.ENCHANT_ONE_CLICK) {
                        if (itemType == ItemTemplate.TYPE2_WEAPON) {
                            item.setEnchantLevel(Config.ENCHANT_ONE_CLICK_WEAPON);
                        }
                        if (itemType == ItemTemplate.TYPE2_SHIELD_ARMOR) {
                            item.setEnchantLevel(Config.ENCHANT_ONE_CLICK_ARMOR);
                        }
                        if (itemType == ItemTemplate.TYPE2_ACCESSORY) {
                            item.setEnchantLevel(Config.ENCHANT_ONE_CLICK_ACCESSORY);
                        }
                        item.setJdbcState(JdbcEntityState.UPDATED);
                        item.update();

//                        player.sendPacket(new InventoryUpdate().addModifiedItem(item)); //TODO

//                        player.sendPacket(EnchantResult.SUCESS); //TODO
                    } else {

                        item.setEnchantLevel(item.getEnchantLevel() + 1);
                        item.setJdbcState(JdbcEntityState.UPDATED);
                        item.update();

//                        player.sendPacket(new InventoryUpdate().addModifiedItem(item)); //TODO

//                        player.sendPacket(EnchantResult.SUCESS); //TODO

//                        broadcastResult(player, item);
                        if (scrollId == 13540 && item.getEnchantLevel() > 3 || Config.SHOW_ENCHANT_EFFECT_RESULT) {
//                            player.broadcastPacket(new MagicSkillUse(player, player, 5965, 1, 500, 1500));
                        }
                    }
                } else if (scrollId == 21582) // фейл, но заточка блесед
                {
                    if (item.getEnchantLevel() >= Config.SAFE_ENCHANT_LVL_OLF) {
                        item.setEnchantLevel(Config.SAFE_ENCHANT_LVL_OLF);
                        item.setJdbcState(JdbcEntityState.UPDATED);
                        item.update();

//                        player.sendPacket(new InventoryUpdate().addModifiedItem(item)); //TODO
//                        player.sendPacket(SystemMsg.THE_BLESSED_ENCHANT_FAILED); //TODO
//                        player.sendPacket(EnchantResult.BLESSED_FAILED); //TODO
                    } else {
                        item.setEnchantLevel(item.getEnchantLevel());
                        item.setJdbcState(JdbcEntityState.UPDATED);
                        item.update();

//                        player.sendPacket(new InventoryUpdate().addModifiedItem(item)); //TODO
//                        player.sendPacket(SystemMsg.THE_BLESSED_ENCHANT_FAILED); //TODO
//                        player.sendPacket(EnchantResult.BLESSED_FAILED); //TODO
                    }

                } else if (ItemFunctions.isCrystallEnchantScroll(scrollId)) // фейл, но заточка кристал
                {
                    item.setEnchantLevel(0);
                    item.setJdbcState(JdbcEntityState.UPDATED);
                    item.update();

//                    player.sendPacket(new InventoryUpdate().addModifiedItem(item)); //TODO
//                    player.sendPacket(SystemMsg.THE_BLESSED_ENCHANT_FAILED); //TODO
//                    player.sendPacket(EnchantResult.BLESSED_FAILED); //TODO
                } else if (ItemFunctions.isBlessedEnchantScroll(scrollId)) // фейл, но заточка блесед
                {
                    item.setEnchantLevel(Config.SAFE_ENCHANT_LVL);
                    item.setJdbcState(JdbcEntityState.UPDATED);
                    item.update();

//                    player.sendPacket(new InventoryUpdate().addModifiedItem(item)); //TODO
//                    player.sendPacket(SystemMsg.THE_BLESSED_ENCHANT_FAILED); //TODO
//                    player.sendPacket(EnchantResult.BLESSED_FAILED); //TODO
                } else if (ItemFunctions.isAncientEnchantScroll(scrollId) || ItemFunctions.isDestructionWpnEnchantScroll(scrollId) || ItemFunctions.isDestructionArmEnchantScroll(scrollId)) // фейл, но заточка ancient или destruction
                {
                    player.sendPacket(EnchantResult.ANCIENT_FAILED); //TODO
                } else // фейл, разбиваем вещь
                {
                    if (item.isEquipped()) {
                        player.sendDisarmMessage(item);
                    }

                    Log.LogItem(player, Log.EnchantFail, item);

                    if (!inventory.destroyItem(item, 1L)) {
                        player.sendPacket(new SystemMessage(SystemMessage.THE_ENCHANTMENT_HAS_FAILED_YOUR_S1_HAS_BEEN_CRYSTALLIZED).addItemName(item.getItemId()));
                        player.sendActionFailed();
                        isCrystallized = 1;
                        return;
                    }

                    if (crystalId > 0 && item.getTemplate().getCrystalCount() > 0) {
                        int crystalAmount = (int) (item.getTemplate().getCrystalCount() * 0.87);
                        if (item.getEnchantLevel() > 3) {
                            crystalAmount += item.getTemplate().getCrystalCount() * 0.25 * (item.getEnchantLevel() - 3);
                        }
                        if (crystalAmount < 1) {
                            crystalAmount = 1;
                        }

//                        player.sendPacket(new EnchantResult(1, crystalId, crystalAmount)); //TODO
                        ItemFunctions.addItem(player, crystalId, crystalAmount, true);
                    } else {
//                        player.sendPacket(EnchantResult.FAILED_NO_CRYSTALS); //TODO
                    }

                    if (scrollId == 13540 || Config.SHOW_ENCHANT_EFFECT_RESULT) {
//                        player.broadcastPacket(new MagicSkillUse(player, player, 5949, 1, 500, 1500));
                    }
                }
                int ench = item.getEnchantLevel();
                if (ench > maxEnchant)
                    maxEnchant = ench;
                isNeedUpdate = true;
                if (ench >= Config.SAFE_ENCHANT_COMMON)
                    count++;
            }
        } finally {
            if (isNeedEquip)
                inventory.equipItem(item);
            inventory.writeUnlock();
            if (isNeedUpdate) {
                if (Config.ENCHANT_CONSUME_ITEM != 0) {
                    player.getInventory().destroyItemByItemId(Config.ENCHANT_CONSUME_ITEM, Config.ENCHANT_CONSUME_ITEM_COUNT);
                    ItemTemplate template = ItemHolder.getInstance().getTemplate(Config.ENCHANT_CONSUME_ITEM);
                    player.sendMessage("Потрачено " + Config.ENCHANT_CONSUME_ITEM_COUNT + " "
                            + (template != null ? template.getName() : "[Ошибка: не найден item id! Пожалуйста сообщение администратору об ошибке]"));
                }
                broadcastResult(player, item);
                player.sendPacket(new InventoryUpdate());
                Map<String, Integer> result = new HashMap<>();
                result.put("crystallized", isCrystallized);
                result.put("enchant", item == null ? 0 : item.getEnchantLevel());
                result.put("maxenchant", maxEnchant);
                result.put("scrolls", scrolls);
                result.put("commonscrolls", commonScrolls);
                if (count == 0) count++; // unreachable in normal situation
                result.put("chance", (int)(((double)success / ((double)count / 100.)) * 100));
                result.put("success", item == null ? 0 : item.getEnchantLevel() == player.getEnchantParams().maxEnchant ? 1 : 0);
                EnchantParser.getInstance().showResultPage(player, EnchantType.Scroll, result);
            }
            player.setEnchantScroll(null);
            player.updateStats();
        }

    }

    @SuppressWarnings("all")
    private static void broadcastResult(Player enchanter, ItemInstance item) {
        if (item.getTemplate().getType2() == 0) {
            if ((item.getEnchantLevel() == 7) || (item.getEnchantLevel() == 15)) {
                enchanter.sendPacket((new SystemMessage(SystemMsg.C1_HAS_SUCCESSFULLY_ENCHANTED_A_S2_S3).addName(enchanter).addNumber(item.getEnchantLevel())), new MagicSkillUse(enchanter, enchanter, 5965, 1, 500, 1500));
            }
        } else if (item.getEnchantLevel() == 6) {
            enchanter.sendPacket((new SystemMessage(SystemMsg.C1_HAS_SUCCESSFULLY_ENCHANTED_A_S2_S3).addName(enchanter).addNumber(item.getEnchantLevel())), new MagicSkillUse(enchanter, enchanter, 5965, 1, 500, 1500));
        }
    }

    private boolean checkPlayer(Player player) {
        if (player == null) {
            return false;
        }

        if (player.isActionsDisabled()) {
            player.setEnchantScroll(null);
            player.sendActionFailed();
            player.sendMessage("Ошибка: действие невозможно!");
            return false;
        }

        if (player.isInTrade()) {
            player.setEnchantScroll(null);
            player.sendActionFailed();
            player.sendMessage("Ошибка: действие невозможно во время торговли!");
            return false;
        }

        if (player.isInStoreMode()) {
            player.setEnchantScroll(null);
            player.sendPacket(EnchantResult.CANCEL);
            player.sendPacket(SystemMsg.YOU_CANNOT_ENCHANT_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
            player.sendActionFailed();
            player.sendMessage("Ошибка: действие невозможно во время торговли!");
            return false;
        }
        return true;
    }
}

