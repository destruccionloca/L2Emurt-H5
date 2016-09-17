package npc.model;

import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.utils.ItemFunctions;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.ReflectionUtils;

/**
 * Created by Trick on 19.08.2016.
 */
public final class SiffGatekeeperInstance extends NpcInstance{

    private static final int TeletortItem = 40036;
    private static final Location TELEPORT_POSITION1 = new Location(48765, 248461, -6190);

    public SiffGatekeeperInstance(int objectId, NpcTemplate template) {
        super(objectId, template);
    }
    @Override
    public void onBypassFeedback(Player player, String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }

        if (command.startsWith("request_enter_er")) {
            if (player.getParty() == null || player.getParty().getMemberCount() < 2) {
                showChatWindow(player, "default/30427-2.htm");
            } else if (ItemFunctions.getItemCount(player, TeletortItem) > 0) {
                Party party_tp = player.getParty();
                ItemFunctions.removeItem(player, TeletortItem, 1, true);
                for (Player member : party_tp) {
                    if (member.isInRange(this, 500) && !member.isCursedWeaponEquipped()) {
                        member.teleToLocation(TELEPORT_POSITION1);
                    }
                }
                } else {
                showChatWindow(player, "default/30427-1.htm");
            }
        } else {
            super.onBypassFeedback(player, command);
        }
    }
}
