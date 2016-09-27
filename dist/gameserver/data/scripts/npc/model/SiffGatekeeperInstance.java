package npc.model;

import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.utils.Location;

/**
 * Created by Trick on 19.08.2016.
 */
public final class SiffGatekeeperInstance extends NpcInstance{

    private static final int TeletortItem = 40036;
    private static final Location TELEPORT_POSITION1 = new Location(-114712, 150664, 424);

    public SiffGatekeeperInstance(int objectId, NpcTemplate template) {
        super(objectId, template);
    }
    @Override
    public void onBypassFeedback(Player player, String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }

        if (command.startsWith("request_enter_er")) {
            Party party = player.getParty();
            if(party != null && checkPlayer(player) && party.isLeader(player) && player.reduceItem(TeletortItem, 1, true)) {
                boolean allChecked = true;
                for(Player member : party.getPartyMembers()) {
                    if(!checkPlayer(member)) {
                        allChecked = false;
                    }
                }
                if(allChecked) {
                    party.getPartyMembers().forEach(member -> member.teleToLocation(TELEPORT_POSITION1));
                }
            } else {
                showChatWindow(player, "default/30427-1.htm");
            }
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    private boolean checkPlayer(Player player) {
        if(!player.isInRangeZ(this, 100)) {
            return false;
        }
        if(player.isCursedWeaponEquipped()) {
            return false;
        }
        return true;

    }
}
