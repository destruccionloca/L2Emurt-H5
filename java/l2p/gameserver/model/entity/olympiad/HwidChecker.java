package l2p.gameserver.model.entity.olympiad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import l2p.gameserver.Config;
import l2p.gameserver.model.Player;
import l2p.gameserver.utils.HWID.HardwareID;
import l2p.gameserver.utils.HWID.HWIDComparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class HwidChecker
{
	private static final Logger _log = LoggerFactory.getLogger(HwidChecker.class);
	private static Map<Long, HardwareID> hwids = new HashMap<Long, HardwareID>();
	private static HWIDComparator comparator = new HWIDComparator();

	public static boolean canRegister(Player player)
	{
		if(player == null)
			return false;

		if(!Config.OLYMPIAD_PLAYER_HWID)
			return true;


		if(comparator.contains(new HardwareID(player.getNetConnection().getHWID()), new ArrayList<HardwareID>(hwids.values())))
			return false;

		return true;
	}

	public static void registerPlayer(Player player)
	{
		if(player == null)
			return;

		if(!Config.OLYMPIAD_PLAYER_HWID)
			return;

		try
		{
			hwids.put(player.getStoredId(), new HardwareID(player.getNetConnection().getHWID()));
		}
		catch(Exception e)
		{
			_log.info("HwidChecker DEBUG: -----------------------------------------------");
			_log.warn("Olympiad register Error: " + e);
			_log.error("", e);
		}
	}

	public static void unregisterPlayer(Player player)
	{
		if(player == null)
			return;

		if(!Config.OLYMPIAD_PLAYER_HWID)
			return;

		try
		{
			hwids.remove(player.getStoredId());
		}
		catch(Exception e)
		{
			_log.info("HwidChecker DEBUG: -----------------------------------------------");
			_log.warn("Olympiad unregister Error: " + e);
			_log.error("", e);
		}
	}
}