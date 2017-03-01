package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Zone;
import l2p.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.network.GameClient.GameClientState;
import l2p.gameserver.serverpackets.ActionFail;
import l2p.gameserver.serverpackets.CharacterSelectionInfo;
import l2p.gameserver.serverpackets.RestartResponse;

public class RequestRestart extends L2GameClientPacket {

    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        Player activeChar = getClient().getActiveChar();

        if (activeChar == null) {
            return;
        }

        if (activeChar.isInObserverMode()) {
            activeChar.sendPacket(SystemMsg.OBSERVERS_CANNOT_PARTICIPATE, RestartResponse.FAIL, ActionFail.STATIC);
            return;
        }

        if (activeChar.isInCombat()) {
            activeChar.sendPacket(SystemMsg.YOU_CANNOT_RESTART_WHILE_IN_COMBAT, RestartResponse.FAIL, ActionFail.STATIC);
            return;
        }

        if (activeChar.isFishing()) {
            activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2, RestartResponse.FAIL, ActionFail.STATIC);
            return;
        }

        if (activeChar.isBlocked() && !activeChar.isFlying()) // Разрешаем выходить из игры если используется сервис HireWyvern. Вернет в начальную точку.
        {
            activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestRestart.OutOfControl", activeChar));
            activeChar.sendPacket(RestartResponse.FAIL, ActionFail.STATIC);
            return;
        }
		
        if (activeChar._event != null) {
            activeChar.sendMessage(activeChar.isLangRus() ? "Вы не можите выйти во время участия в ивенте!" : "You can follow any responses did not leave while participating in the event!");
            activeChar.sendPacket(RestartResponse.FAIL, ActionFail.STATIC);
            return;
        }

        if (activeChar.isInOlympiadMode()) {
            activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.Logout.Olympiad", activeChar));
            activeChar.sendPacket(RestartResponse.FAIL, ActionFail.STATIC);
            return;
        }

        if (activeChar.isInStoreMode() && !activeChar.isInZone(Zone.ZoneType.offshore) && Config.SERVICES_OFFLINE_TRADE_ALLOW_OFFSHORE) {
            activeChar.sendMessage(new CustomMessage("trade.OfflineNoTradeZoneOnlyOffshore", activeChar));
            activeChar.sendPacket(RestartResponse.FAIL, ActionFail.STATIC);
            return;
        }
        // Prevent player from restarting if they are a festival participant
        // and it is in progress, otherwise notify party members that the player
        // is not longer a participant.
        if (activeChar.isFestivalParticipant()) {
            if (SevenSignsFestival.getInstance().isFestivalInitialized()) {
                activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestRestart.Festival", activeChar));
                activeChar.sendPacket(RestartResponse.FAIL, ActionFail.STATIC);
                return;
            }
        }

        if (activeChar.isInLastHero()) {
            activeChar.sendMessage(activeChar.isLangRus() ? "Вы не можете выйти во время Last Hero!" : "You can't logout until participating in Last Hero!");
            activeChar.sendPacket(RestartResponse.FAIL, ActionFail.STATIC);
            return;
        }


        if (getClient() != null) {
            getClient().setState(GameClientState.AUTHED);
        }
        activeChar.restart();
        // send char list
        CharacterSelectionInfo cl = new CharacterSelectionInfo(getClient().getLogin(), getClient().getSessionKey().playOkID1);
        sendPacket(RestartResponse.OK, cl);
        getClient().setCharSelection(cl.getCharInfo());
    }
}