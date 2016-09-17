package l2p.gameserver.handler.voicecommands.impl;

import java.util.ArrayList;
import java.util.List;

import l2p.commons.dao.JdbcEntityState;
import l2p.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.serverpackets.InventoryUpdate;
import l2p.gameserver.scripts.Functions;

public class Talismans extends Functions implements IVoicedCommandHandler {

    private static final String[] COMMANDS = new String[]{ "talisman" };

    @Override
    public boolean useVoicedCommand(String command, Player activeChar, String args) {
        combineTalismans(activeChar);
        return true;
    }

    public static void combineTalismans(Player activeChar) {
        List<int[]> sameIds = new ArrayList<>();

        for (ItemInstance item : activeChar.getInventory().getItems()) {
            if (item.getLifeTime() > 0 && item.getName().contains("Talisman")) {
                talismanAddToCurrent(sameIds, item.getItemId());
            }
        }

        int allCount = 0;
        int newCount = 0;
        for (int[] idCount : sameIds) {
            if (idCount[1] > 1) {
                int lifeTime = 0;
                List<ItemInstance> existingTalismans = activeChar.getInventory().getItemsByItemId(idCount[0]);
                for (ItemInstance existingTalisman : existingTalismans) {
                    lifeTime += existingTalisman.getLifeTime();
                    activeChar.getInventory().destroyItem(existingTalisman);
                }

                ItemInstance newTalisman = activeChar.getInventory().addItem(idCount[0], 1L);
                newTalisman.setLifeTime(lifeTime);
                newTalisman.setJdbcState(JdbcEntityState.UPDATED);
                newTalisman.update();
                activeChar.sendPacket(new InventoryUpdate().addModifiedItem(newTalisman));

                allCount += idCount[0];
                newCount++;
            }
        }
		String talisman = activeChar.isLangRus() ? " Талисманов комбинировано в " : " Talismans were combined into ";
        if (allCount > 0) {
            activeChar.sendMessage(allCount + talisman + newCount);
        } else {
            activeChar.sendMessage(activeChar.isLangRus() ? "У вас нету талисманов для комбинирования" : "You don't have Talismans to combine!");
        }
    }

    private static void talismanAddToCurrent(List<int[]> sameIds, int itemId) {
        for (int[] sameId : sameIds) {
            if (sameId[0] == itemId) {
                sameId[1] = sameId[1] + 1;
                return;
            }
        }
        sameIds.add(new int[]{
            itemId, 1
        });
    }

    @Override
    public String[] getVoicedCommandList() {
        return COMMANDS;
    }
}
