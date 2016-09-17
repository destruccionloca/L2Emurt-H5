package l2p.gameserver.handler.voicecommands.impl;

import l2p.gameserver.Config;
import l2p.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2p.gameserver.model.Player;
import l2p.gameserver.utils.simpleAcp.AcpDAO;
import l2p.gameserver.utils.simpleAcp.AcpParser;
import l2p.gameserver.utils.simpleAcp.PotionModel;

public class Acp implements IVoicedCommandHandler {
    private static final String[] commands = { "acp", "acpon", "acpoff", "acpcond", "acpset" };
    @Override
    public boolean useVoicedCommand(String command, Player player, String args) {
        if (Config.ACP_ONLY_PREMIUM && !player.hasBonus()) {
            player.sendMessage("Только для премиумов!");
            return false;
        }
        if (command.equalsIgnoreCase("acp"))
            AcpParser.showMenu(player);
        else if (command.equalsIgnoreCase("acpset") && args != null && !args.equals("")) {
            String[] arr = args.split(" ");
            if (arr.length != 2)
                return false;
            int id = Integer.parseInt(arr[0]);
            int value = Integer.parseInt(arr[1]);
            PotionModel potion = player.getAcp().getPotionByItemId(id);
            potion.setAllow(value == 1);
            AcpDAO.saveAcpData(player, potion);
            AcpParser.showMenu(player);
        } else if (command.equalsIgnoreCase("acpcond") && args != null && !args.equals("")) {
            String[] arr = args.split(" ");
            if (arr.length != 3) {
                player.sendMessage("Задайте оба параметра!");
                return false;
            }
            int id = Integer.parseInt(arr[0]);
            PotionModel potion = player.getAcp().getPotionByItemId(id);
            potion.setCond(Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
            AcpDAO.saveAcpData(player, potion);
            AcpParser.showMenu(player);
        } else if (command.equalsIgnoreCase("acpon")) {
            for (PotionModel potion : player.getAcp().getPotions()) {
                potion.setAllow(true);
                AcpDAO.saveAcpData(player, potion);
            }
        } else if (command.equalsIgnoreCase("acpoff")) {
            for (PotionModel potion : player.getAcp().getPotions()) {
                potion.setAllow(false);
                AcpDAO.saveAcpData(player, potion);
            }
        }
        return true;
    }

    @Override
    public String[] getVoicedCommandList() {
        return commands;
    }
}
