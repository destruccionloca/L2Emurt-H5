package npc.model;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.ReflectionUtils;

/**
 * @author pchayka
 */
public class SteelCitadelTeleporterInstance extends NpcInstance {

    public SteelCitadelTeleporterInstance(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(Player player, String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }

        if (!player.isInParty()) {
            showChatWindow(player, "default/32745-1.htm");
            return;
        }
        if (player.getParty().getPartyLeader() != player) {
            showChatWindow(player, "default/32745-2.htm");
            return;
        }
        if (!rangeCheck(player)) {
            showChatWindow(player, "default/32745-2.htm");
            return;
        }

        if (command.equalsIgnoreCase("01_up")) {
            player.getParty().Teleport(new Location(-22208, 277122, -13376));
            return;
        } else if (command.equalsIgnoreCase("02_up")) {
            player.getParty().Teleport(new Location(-22208, 277106, -11648));
            return;
        } else if (command.equalsIgnoreCase("02_down")) {
            player.getParty().Teleport(new Location(-22208, 277074, -15040));
            return;
        } else if (command.equalsIgnoreCase("03_up")) {
            player.getParty().Teleport(new Location(-22208, 277120, -9920));
            return;
        } else if (command.equalsIgnoreCase("03_down")) {
            player.getParty().Teleport(new Location(-22208, 277120, -13376));
            return;
        } else if (command.equalsIgnoreCase("04_up")) {
            player.getParty().Teleport(new Location(-19024, 277126, -8256));
            return;
        } else if (command.equalsIgnoreCase("04_down")) {
            player.getParty().Teleport(new Location(-22208, 277106, -11648));
            return;
        } else if (command.equalsIgnoreCase("06_up")) {
            player.getParty().Teleport(new Location(-19024, 277106, -9920));
            return;
        } else if (command.equalsIgnoreCase("06_down")) {
            player.getParty().Teleport(new Location(-22208, 277122, -9920));
            return;
        } else if (command.equalsIgnoreCase("07_up")) {
            player.getParty().Teleport(new Location(-19008, 277100, -11648));
            return;
        } else if (command.equalsIgnoreCase("07_down")) {
            player.getParty().Teleport(new Location(-19024, 277122, -8256));
            return;
        } else if (command.equalsIgnoreCase("08_up")) {
            player.getParty().Teleport(new Location(-19008, 277100, -13376));
            return;
        } else if (command.equalsIgnoreCase("08_down")) {
            player.getParty().Teleport(new Location(-19008, 277106, -9920));
            return;
        } else if (command.equalsIgnoreCase("09_up")) {
            player.getParty().Teleport(new Location(14602, 283179, -7500));
            return;
        } else if (command.equalsIgnoreCase("09_down")) {
            player.getParty().Teleport(new Location(-19008, 277100, -11648));
            return;
        } else if (command.equalsIgnoreCase("facedemon")) {
            ReflectionUtils.simpleEnterInstancedZone(player, getIz(5));
            return;
        } else if (command.equalsIgnoreCase("faceranku")) {
            ReflectionUtils.simpleEnterInstancedZone(player, getIz(6));
            return;
        } else if (command.equalsIgnoreCase("up_01")) {
            player.getParty().Teleport(new Location(-12808, 273976, -15296));
            return;
        } else if (command.equalsIgnoreCase("up_02")) {
            player.getParty().Teleport(new Location(-12728, 273560, -13600));
            return;
        } else if (command.equalsIgnoreCase("up_03")) {
            player.getParty().Teleport(new Location(-10728, 273160, -11936));
            return;
        } else if (command.equalsIgnoreCase("up_04")) {
            player.getParty().Teleport(new Location(-12664, 273480, -10496));
            return;
        } else if (command.equalsIgnoreCase("up_05")) {
            player.getParty().Teleport(new Location(-13544, 276040, -9032));
            return;
        } else if (command.equalsIgnoreCase("dorian")) {
            player.getParty().Teleport(new Location(-13400, 272827, -15304));
            return;
        } else if (command.equalsIgnoreCase("leave")) {
            player.getReflection().collapse();
            return;
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    private boolean rangeCheck(Player pl) {
        for (Player m : pl.getParty().getPartyMembers()) {
            if (!pl.isInRange(m, 400)) {
                return false;
            }
        }
        return true;
    }

    private int getIz(int floor) {
        if (floor == 5) {
            return 3;
        } else {
            return 4;
        }
    }
}
