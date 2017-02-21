package services.community;

import java.util.StringTokenizer;
import l2p.commons.dao.JdbcEntityState;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.handler.bbs.CommunityBoardManager;
import l2p.gameserver.handler.bbs.ICommunityBoardHandler;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Zone;
import l2p.gameserver.model.actor.instances.player.ShortCut;
import l2p.gameserver.model.base.TeamType;
import l2p.gameserver.model.entity.olympiad.Olympiad;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.scripts.ScriptFile;
import l2p.gameserver.serverpackets.ExShowVariationCancelWindow;
import l2p.gameserver.serverpackets.ExShowVariationMakeWindow;
import l2p.gameserver.serverpackets.InventoryUpdate;
import l2p.gameserver.serverpackets.ShortCutRegister;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.serverpackets.ShowBoard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dislike
 */
public class Augmentation extends Functions implements ScriptFile, ICommunityBoardHandler {

    private static final Logger _log = LoggerFactory.getLogger(Augmentation.class);

    private int PRICE_ID = 24179; // id уплаты
    private int PRICE_COUNT = 1; // кол-во предметов

    @Override
    public void onLoad() {
        _log.info("CommunityBoard: ArgumManager loaded.");
        CommunityBoardManager.getInstance().registerHandler(this);
    }

    @Override
    public void onReload() {
        CommunityBoardManager.getInstance().removeHandler(this);
    }

    @Override
    public void onShutdown() {
    }

    @Override
    public String[] getBypassCommands() {
        return new String[]{"_bbsaugm"};
    }

    @Override
    public void onBypassCommand(Player player, String bypass) {

        if (bypass.startsWith("_bbsaugm")) {

            StringTokenizer st2 = new StringTokenizer(bypass, ";");
            String[] mBypass = st2.nextToken().split(":");

            ItemInstance item = player.getActiveWeaponInstance();
            int id = Integer.parseInt(mBypass[1]);
			
			if (id == 1) {
                player.sendPacket(Msg.SELECT_THE_ITEM_TO_BE_AUGMENTED, ExShowVariationMakeWindow.STATIC);
				player.sendPacket(new ShowBoard());
                return;
            }

            if (id == 0) {
                player.sendPacket(Msg.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION, ExShowVariationCancelWindow.STATIC);
				player.sendPacket(new ShowBoard());
                return;
            }

            if (item == null || item.isAugmented() || !item.canBeAugmented(player, false)) {
				player.sendMessage("Предмет уже зачарован");
				player.sendPacket(new ShowBoard());
                return;
            }
				
            if (!player.getInventory().destroyItemByItemId(PRICE_ID, PRICE_COUNT)) {
                if (PRICE_ID == 4356) {
                        player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                    } else {
                         player.sendMessage("Недостаточно Купонов");
						 player.sendPacket(new ShowBoard());
						 return;
                    }
                }

            boolean equipped = false;
            if (equipped = item.isEquipped()) {
                player.getInventory().unEquipItem(item);
            }
            item.setAugmentationId((id << 16) + 7191); //без прибавки к статам работает неверно
            item.setJdbcState(JdbcEntityState.UPDATED);
            item.update();
            if (equipped) {
                player.getInventory().equipItem(item);
            }
            player.sendPacket(new InventoryUpdate().addModifiedItem(item));
			player.sendMessage("Зачарованно Успешно");
			player.sendPacket(new ShowBoard());

            for (ShortCut sc : player.getAllShortCuts()) {
                if (sc.getId() == item.getObjectId() && sc.getType() == ShortCut.TYPE_ITEM) {
                    player.sendPacket(new ShortCutRegister(player, sc));
                }
            }
            player.sendChanges();

        }
    }

    @Override
    public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5) {
    }

}
