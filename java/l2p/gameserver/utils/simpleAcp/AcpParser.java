/**
 * Created by Hack
 * This is parser for .acp page
 */
package l2p.gameserver.utils.simpleAcp;

import l2p.gameserver.data.htm.HtmCache;
import l2p.gameserver.model.Player;
import l2p.gameserver.scripts.Functions;

import java.util.regex.Matcher;

public class AcpParser {
    public static final String OFF = "<font color=\"ff0000\">Off</font>";
    public static final String ON = "<font color=\"00ff00\">On</font>";


    public static void showMenu(Player player) {
        String dialog = HtmCache.getInstance().getNotNull("command/acp.htm", player);
        dialog = dialog.replaceFirst("%page_content%", Matcher.quoteReplacement(getContent(player)));
        Functions.show(dialog, player, null);
    }

    public static String getContent(Player player) {
        int idx = 0;
        String content = "";
        for (PotionModel model : player.getAcp().getPotions()) {
            if (model.getItem() == null)
                continue;
            String potion = "<img src=L2UI.SquareGray width=270 height=1><table><tr><td><img src=\"" + model.getIcon()
                    + "\" width=32 height=32></td><td><table><tr><td width=95><font color=F2C202>" + getFixedName(model.getItem().getName())
                    + "</font></td><td width=50>(" + model.getMinPercent() + " - " + model.getMaxPercent() + ")</td><td width=25>"
                    + (model.isAllow() ? ON : OFF) + "</td><td><button width=30 height=22 back=\"L2UI_CT1.Button_DF_Down\" "
                    + "fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h user_acpset " + model.getItemId() + " " + "1\" value=\"On\"></td><td><button width=30 "
                    + "height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h user_acpset " + model.getItemId() + " " + "0\""
                    + " value=\"Off\"></td>" + "</tr></table><table><tr>" + "<td width=31>Мин:</td><td width = 40><edit var=\"box" + idx
                    + "\" width=30 height=12></td><td width=36>Макс:</td><td width=45><edit var=\"box0" + idx
                    + "\" width=30 height=12></td><td><button width=75 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" "
                    + "action=\"bypass -h user_acpcond " + model.getItemId() + " $box" + idx + " $box0" + idx + "\" value=\"Установить\"></td>"
                    + "</tr></table></td></tr></table><br>";
            content = content + potion;
            idx++;
        }
        return content.equals("") ? "У Вас нет доступных зелий" : content;
    }

    private static String getFixedName(String defName) {
        String[] words = defName.split(" ");
        String fixed = "";
        for (String word : words)
            if (fixed.length() < 16)
                fixed = fixed + word + " ";
            else
                break;
        return fixed.substring(0, fixed.length() - 1);
    }
}
