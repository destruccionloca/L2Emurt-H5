package zones;

import l2p.gameserver.instancemanager.ReflectionManager;
import l2p.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Zone;
import l2p.gameserver.scripts.ScriptFile;
import l2p.gameserver.utils.ReflectionUtils;

public class PvPZoneListener implements ScriptFile {
	private static ZoneListener _zoneListener;
	private static String[] ZONES = { "[hard_pvp]", "[baium_pvp]", "[valakas_pvp]", "[antharas_pvp]", "[queen_ant_epic]", "[baium_epic]", "[antharas_epic]", "[valakas_epic]" };;

	@Override
	public void onLoad() {
		_zoneListener = new ZoneListener();
		for (int i = 0; i < ZONES.length; i++) {
			Zone zone = ReflectionUtils.getZone(ZONES[i]);
			zone.addListener(_zoneListener);
		}
	}

	@Override
	public void onReload() {

	}

	@Override
	public void onShutdown() {

	}

	public class ZoneListener implements OnZoneEnterLeaveListener {
		public ZoneListener() {
		}

		public void onZoneEnter(Zone zone, Creature cha) {
			if (!cha.isPlayable()) {
				return;
			}
			Player player = cha.getPlayer();
			if (player != null) {
				player.updatePvPFlag(1);
			}
		}

		public void onZoneLeave(Zone zone, Creature cha) {
			Player player = cha.getPlayer();
			if (player != null) {
				player.updatePvPFlag(0);
			}
		}
	}
}
