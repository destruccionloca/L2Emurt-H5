package l2p.gameserver.utils;

import l2p.gameserver.Config;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.World;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.serverpackets.NpcSay;
import l2p.gameserver.serverpackets.Say2;
import l2p.gameserver.serverpackets.components.ChatType;
import l2p.gameserver.serverpackets.components.NpcString;

public class ChatUtils {

    private static void say(Player activeChar, GameObject activeObject, Iterable<Player> players, int range, Say2 cs) {
        for (Player player : players) {
            if (!player.isBlockAll()) {
                GameObject obj = player.getObservePoint();
                if (obj == null) {
                    obj = player;
                }
                if ((activeObject.isInRangeZ(obj, range)) && (!player.isInBlockList(activeChar)) && (activeChar.canTalkWith(player))) {
                    player.sendPacket(cs);
                }
            }
        }
    }

    public static void say(Player activeChar, Say2 cs) {
        GameObject activeObject = activeChar.getObservePoint();
        if (activeObject == null) {
            activeObject = activeChar;
        }
        say(activeChar, activeObject, World.getAroundObservers(activeObject), Config.CHAT_RANGE, cs);
    }

    public static void say(Player activeChar, Iterable<Player> players, Say2 cs) {
        GameObject activeObject = activeChar.getObservePoint();
        if (activeObject == null) {
            activeObject = activeChar;
        }
        say(activeChar, activeObject, players, Config.CHAT_RANGE, cs);
    }

    public static void say(Player activeChar, int range, Say2 cs) {
        GameObject activeObject = activeChar.getObservePoint();
        if (activeObject == null) {
            activeObject = activeChar;
        }
        say(activeChar, activeObject, World.getAroundObservers(activeObject), range, cs);
    }

    public static void say(NpcInstance activeChar, Iterable<Player> players, int range, NpcSay cs) {
        for (Player player : players) {
            GameObject obj = player.getObservePoint();
            if (obj == null) {
                obj = player;
            }
            if (activeChar.isInRangeZ(obj, range)) {
                player.sendPacket(cs);
            }
        }
    }

    public static void say(NpcInstance activeChar, NpcSay cs) {
        say(activeChar, World.getAroundObservers(activeChar), Config.CHAT_RANGE, cs);
    }

    public static void say(NpcInstance activeChar, Iterable<Player> players, NpcSay cs) {
        say(activeChar, players, Config.CHAT_RANGE, cs);
    }

    public static void say(NpcInstance activeChar, int range, NpcSay cs) {
        say(activeChar, World.getAroundObservers(activeChar), range, cs);
    }

    public static void say(NpcInstance activeChar, int range, NpcString npcString, String... params)
    {
        say(activeChar, range, new NpcSay(activeChar, ChatType.NPC_ALL, npcString, params));
    }

    public static void say(NpcInstance npc, NpcString npcString, String... params)
    {
        say(npc, Config.CHAT_RANGE, npcString, params);
    }

    public static void shout(NpcInstance activeChar, NpcSay cs)
    {
        int rx = MapUtils.regionX(activeChar);
        int ry = MapUtils.regionY(activeChar);

        for(Player player : GameObjectsStorage.getAllPlayersForIterate())
        {
            GameObject obj = player.getObservePoint();
            if(obj == null)
                obj = player;

            if(activeChar.getReflection() != obj.getReflection())
                continue;

            int tx = MapUtils.regionX(obj) - rx;
            int ty = MapUtils.regionY(obj) - ry;

            if (tx*tx + ty*ty <= Config.CHAT_RANGE || activeChar.isInRangeZ(obj, Config.CHAT_RANGE))
                player.sendPacket(cs);
        }
    }

    public static void shout(NpcInstance activeChar, NpcString npcString, String... params)
    {
        shout(activeChar, new NpcSay(activeChar, ChatType.NPC_SHOUT, npcString, params));
    }

    public static void chat(NpcInstance activeChar, ChatType type, NpcString npcString, String... params)
    {
        switch (type)
        {
            case ALL:
            case NPC_ALL:
                say(activeChar, npcString, params);
                break;
            case SHOUT:
            case NPC_SHOUT:
                shout(activeChar, npcString, params);
                break;
        }
    }

}
