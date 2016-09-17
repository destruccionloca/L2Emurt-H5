package l2p.gameserver.model.entity.events.actions;

import l2p.gameserver.Config;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.World;
import l2p.gameserver.model.entity.events.EventAction;
import l2p.gameserver.model.entity.events.GlobalEvent;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.serverpackets.components.ChatType;
import l2p.gameserver.serverpackets.components.NpcString;
import l2p.gameserver.serverpackets.NpcSay;
import l2p.gameserver.utils.MapUtils;

public class NpcSayAction implements EventAction {

    private int _npcId;
    private int _range;
    private ChatType _chatType;
    private NpcString _text;

    public NpcSayAction(int npcId, int range, ChatType type, NpcString string) {
        _npcId = npcId;
        _range = range;
        _chatType = type;
        _text = string;
    }

    @Override
    public void call(GlobalEvent event) {
        NpcInstance npc = GameObjectsStorage.getByNpcId(_npcId);
        if (npc == null) {
            return;
        }

        if (_range <= 0) {
            int rx = MapUtils.regionX(npc);
            int ry = MapUtils.regionY(npc);
            int offset = Config.SHOUT_OFFSET;

            for (Player player : World.getAroundObservers(npc)) {
                if (npc.getReflection() != player.getReflection()) {
                    continue;
                }

                if (player.isInRangeZ(npc, _range)) {
                    packet(npc, player);
                }
            }
        } else {
            World.getAroundPlayers(npc, _range, Math.max(_range / 2, 200)).stream().filter(player -> npc.getReflection() == player.getReflection()).forEach(player -> {
                packet(npc, player);
            });
        }
    }

    private void packet(NpcInstance npc, Player player) {
        player.sendPacket(new NpcSay(npc, _chatType, _text));
    }
}
