package npc.model;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.utils.Location;

/**
 * Created by Trick on 19.08.2016, updated Alex 27.09.2016.
 */
public final class h7GatekeeperInstance extends NpcInstance{

    private static final Location TELEPORT_POSITION1 = new Location(-114712, 149576, 447); // 1 Этаж
    private static final Location TELEPORT_POSITION2 = new Location(-114712, 148120, 3552); // 2 Этаж
    private static final Location TELEPORT_POSITION3 = new Location(-114696, 146776, 6616); // 3 Этаж
	private static final Location TELEPORT_POSITION4 = new Location(-84456, 242152, -3728); // Выход
	
    public h7GatekeeperInstance(int objectId, NpcTemplate template) {
        super(objectId, template);
    }
    @Override
    public void onBypassFeedback(Player player, String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }

        if (command.startsWith("one_floor_teltport")) {
            player.teleToLocation(TELEPORT_POSITION1);
        }else if(command.startsWith("two_floor_teltport")){
            player.teleToLocation(TELEPORT_POSITION2);
        }else if(command.startsWith("three_floor_teltport")){
            player.teleToLocation(TELEPORT_POSITION3);
		}else if(command.startsWith("four_floor_teltport")){
            player.teleToLocation(TELEPORT_POSITION4);
        }else {
            super.onBypassFeedback(player, command);
        }
	}
}	