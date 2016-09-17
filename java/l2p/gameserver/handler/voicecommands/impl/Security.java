package l2p.gameserver.handler.voicecommands.impl;

import l2p.gameserver.Config;
import l2p.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2p.gameserver.loginservercon.AuthServerCommunication;
import l2p.gameserver.loginservercon.gspackets.ChangeAllowedHwid;
import l2p.gameserver.loginservercon.gspackets.ChangeAllowedIp;
import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.components.HtmlMessage;

public class Security implements IVoicedCommandHandler {

    private String[] _commandList = {"lock", "unlock", "lockIp", "lockHwid", "unlockIp", "unlockHwid"};

    @Override
    public boolean useVoicedCommand(String command, Player activeChar, String target) {

        if (command.equalsIgnoreCase("lock")) {
            HtmlMessage html = new HtmlMessage(activeChar.getObjectId());
            html.setFile("command/lock/lock.htm");
            html.replace("%ip_block%", IpBlockStatus());
            html.replace("%hwid_block%", HwidBlockStatus());
            html.replace("%hwid_val%", HwidBlockBy());
            html.replace("%curIP%", activeChar.getIP());
            activeChar.sendPacket(html);
            return true;
        } else if (command.equalsIgnoreCase("lockIp")) {

            if (!Config.ALLOW_IP_LOCK) {
                return true;
            }

            AuthServerCommunication.getInstance().sendPacket(new ChangeAllowedIp(activeChar.getAccountName(), activeChar.getIP()));

            HtmlMessage html = new HtmlMessage(activeChar.getObjectId());
            html.setFile("command/lock/lock_ip.htm");
            html.replace("%curIP%", activeChar.getIP());
            activeChar.sendPacket(html);
            return true;
        } else if (command.equalsIgnoreCase("lockHwid")) {

            if (!Config.ALLOW_HWID_LOCK) {
                return true;
            }

            AuthServerCommunication.getInstance().sendPacket(new ChangeAllowedHwid(activeChar.getAccountName(), activeChar.getHwid()));
            HtmlMessage html = new HtmlMessage(activeChar.getObjectId());
            html.setFile("command/lock/lock_hwid.htm");
            activeChar.sendPacket(html);

            return true;
        } else if (command.equalsIgnoreCase("unlockIp")) {

            AuthServerCommunication.getInstance().sendPacket(new ChangeAllowedIp(activeChar.getAccountName(), ""));

            HtmlMessage html = new HtmlMessage(activeChar.getObjectId());
            html.setFile("command/lock/unlock_ip.htm");
            html.replace("%curIP", activeChar.getIP());
            activeChar.sendPacket(html);
            return true;
        } else if (command.equalsIgnoreCase("unlockHwid")) {

            AuthServerCommunication.getInstance().sendPacket(new ChangeAllowedHwid(activeChar.getAccountName(), ""));

            HtmlMessage html = new HtmlMessage(activeChar.getObjectId());
            html.setFile("command/lock/unlock_hwid.htm");
            activeChar.sendPacket(html);

            return true;
        }

        return true;
    }

    private String IpBlockStatus() {
        if (Config.ALLOW_IP_LOCK) {
            return "Разрешено";
        } else {
            return "Запрещено";
        }
    }

    private String HwidBlockStatus() {
        if (Config.ALLOW_HWID_LOCK) {
            return "Разрешено";
        }
        return "Запрещено";
    }

    private String HwidBlockBy() {
        String result = "(CPU/HDD)";

        switch (Config.HWID_LOCK_MASK) {
            case 2:
                result = "(HDD)";
                break;
            case 4:
                result = "(BIOS)";
                break;
            case 6:
                result = "(BIOS/HDD)";
                break;
            case 8:
                result = "(CPU)";
                break;
            case 10:
                result = "(CPU/HDD)";
                break;
            case 12:
                result = "(CPU/BIOS)";
                break;
            case 14:
                result = "(CPU/HDD/BIOS)";
                break;
            case 1:
            case 3:
            case 5:
            case 7:
            case 9:
            case 11:
            case 13:
            default:
                result = "(unknown)";

        }
        return result;
    }

    @Override
    public String[] getVoicedCommandList() {
        return _commandList;
    }
}