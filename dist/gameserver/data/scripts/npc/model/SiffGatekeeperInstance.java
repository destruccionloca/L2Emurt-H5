package npc.model;

import l2p.commons.util.Rnd;
import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.utils.Location;

/**
 * Created by Trick on 19.08.2016, updated Alex 27.09.2016.
 */
public final class SiffGatekeeperInstance extends NpcInstance{

    private static final int TeletortItem = 40036;
    private static final Location TELEPORT_POSITION1 = new Location(-114696, 149576, 456);

    public SiffGatekeeperInstance(int objectId, NpcTemplate template) {
        super(objectId, template);
    }
    @Override
    public void onBypassFeedback(Player player, String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }

        if (command.startsWith("request_enter_er")) {
            if(player.isInParty() && !player.getParty().isLeader(player)) {
                player.sendMessage("Доступно только для лидера группы");
                return;
            }
            Party party = player.getParty();
            if(party != null) {
                if(checkParty(party) && player.reduceItem(TeletortItem, 1, true)) {
                    party.getPartyMembers().forEach(member -> member.teleToLocation(getTeleportLoc()));
                }
            } else {
                showChatWindow(player, "default/30427-1.htm");
            }
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    private boolean checkParty(Party party) {
        for(Player member : party.getPartyMembers()) {
            if(!member.isInRangeZ(this, 200)) {
                party.broadcastMessageToPartyMembers("Член группы: "+ member.getName()+" находится слишком далеко.");
                return false;
            }
            if(member.isCursedWeaponEquipped()) {
                party.broadcastMessageToPartyMembers("Член группы: "+ member.getName()+" владелец проклятого оружия");
                return false;
            }

        }
        return true;
    }

    private Location getTeleportLoc() {

        int i0, i1, i2, i3;
        i0 = Rnd.get(3);
        if(i0 == 0)
        {
            i1 = -114712 + Rnd.get(100);
            i2 = 150664 + Rnd.get(100);
            i3 = 424;
        }
        else if(i0 == 1)
        {
            i1 = -114752 + Rnd.get(100);
            i2 = 150674 + Rnd.get(100);
            i3 = 424;
        }
        else
        {
            i1 = -114732 + Rnd.get(100);
            i2 = 150644 + Rnd.get(100);
            i3 = 424;
        }
        return new Location(i1, i2, i3);
    }
}