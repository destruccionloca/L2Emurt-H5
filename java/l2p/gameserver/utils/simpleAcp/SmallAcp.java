/**
 * Created by Hack
 * New simple Acp service (command .acp) based on startRegeneration(). Provided additional user params
 */

package l2p.gameserver.utils.simpleAcp;

import l2p.gameserver.Config;
import l2p.gameserver.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SmallAcp {
    private static final Logger _log = LoggerFactory.getLogger(SmallAcp.class);
    private Player player;
    private List<PotionModel> potions;
    public SmallAcp(Player player) {
        this.player = player;
        init();
    }

    private void init() {
        potions = new ArrayList<>();
        int[] ids = Config.ACP_POTIONS_IDS;
        int[] delays = Config.ACP_POTIONS_DELAY;
        String[] types = Config.ACP_POTIONS_TYPE;
        try {
            for (int i = 0; i < ids.length; i++)
                potions.add(new PotionModel(player, ids[i], delays[i], convertTypes(types)[i]));
        }
        catch (Exception e) {
            _log.warn("SmallAcp: Cant initalize acp for player " + player.getName() + "!");
        }
    }

    private AcpListnerType[] convertTypes(String[] types) {
        AcpListnerType[] result = new AcpListnerType[types.length];
        for(int i = 0; i < types.length; i++)
            if (types[i].equalsIgnoreCase("hp"))
                result[i] = AcpListnerType.HP;
            else if (types[i].equalsIgnoreCase("cp"))
                result[i] = AcpListnerType.CP;
            else if (types[i].equalsIgnoreCase("mp"))
                result[i] = AcpListnerType.MP;
            else if (types[i].equalsIgnoreCase("souls"))
                result[i] = AcpListnerType.SOULS;
        return result;
    }

    public void startAcp() {
        potions.forEach(PotionModel::startEating);
    }

    public List<PotionModel> getPotions() {
        return potions;
    }

    public PotionModel getPotionByItemId(int itemId) {
        for (PotionModel potion : getPotions())
            if (potion.getItemId() == itemId)
                return potion;
        return null;
    }
}
