package l2p.gameserver.model.instances;

import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.HennaEquipList;
import l2p.gameserver.serverpackets.HennaUnequipList;
import l2p.gameserver.templates.npc.NpcTemplate;

public class SymbolMakerInstance extends NpcInstance {

    public SymbolMakerInstance(int objectID, NpcTemplate template) {
        super(objectID, template);
    }

    @Override
    public void onBypassFeedback(Player player, String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }

        switch (command) {
            case "Draw":
                player.sendPacket(new HennaEquipList(player));
                break;
            case "RemoveList":
                player.sendPacket(new HennaUnequipList(player));
                break;
            default:
                super.onBypassFeedback(player, command);
                break;
        }
    }

    @Override
    public String getHtmlPath(int npcId, int val, Player player) {
        String pom;
        if (val == 0) {
            pom = "SymbolMaker";
        } else {
            pom = "SymbolMaker-" + val;
        }

        return "symbolmaker/" + pom + ".htm";
    }
}