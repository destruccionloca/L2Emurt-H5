package l2p.gameserver.clientpackets;

import java.nio.BufferUnderflowException;
import java.util.List;
import java.util.concurrent.Future;

import l2p.commons.net.nio.impl.ReceivablePacket;
import l2p.gameserver.Config;
import l2p.gameserver.GameServer;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.loginservercon.AuthServerCommunication;
import l2p.gameserver.loginservercon.gspackets.ChangeAccessLevel;
import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Packets received by the game server from clients
 */
public abstract class L2GameClientPacket extends ReceivablePacket<GameClient> {

    private static final Logger _log = LoggerFactory.getLogger(L2GameClientPacket.class);

    @Override
    public final boolean read() {
        try {
            readImpl();
            return true;
        } catch (BufferUnderflowException e) {
            _client.onPacketReadFail();
            _log.error("Client: " + _client + " - Failed reading: " + getType() + " - Server Version: " + GameServer.getInstance().getVersion().getRevisionNumber(), e);
        } catch (Exception e) {
            _log.error("Client: " + _client + " - Failed reading: " + getType() + " - Server Version: " + GameServer.getInstance().getVersion().getRevisionNumber(), e);
        }

        return false;
    }

    protected abstract void readImpl() throws Exception;

    @Override
    public final void run() {
        GameClient client = getClient();
        try {
            Future<?> killTask = ThreadPoolManager.getInstance().schedule(new KillTask(), Config.MAX_PACKET_CALC_TIME);
//            long startTime = System.currentTimeMillis();
            runImpl();
//            _log.info("Client: " + client + " Packet: " + getType() + "Calc time: " + (System.currentTimeMillis() - startTime));
            killTask.cancel(false);
        } catch (Exception e) {
            _log.error("Client: " + client + " - Failed running: " + getType() + " - Server Version: " + GameServer.getInstance().getVersion().getRevisionNumber(), e);
        }
    }

    private class KillTask implements Runnable {
        @Override
        public void run() {
            if (getType().contains("RequestDuelAnswerStart") || getType().contains("RequestItemList"))
                return;
            Player player = _client.getActiveChar();
            if (player != null) {
                if (player.isGM())
                    return;
                player.kick();
            }
            else
                _client.closeNow(true);
            _log.warn("Attention! Too long processing packet! " + _client + "; Packet: " + getType() + ". Connection closed. "
                    + (Config.DO_ACCOUNT_BAN_FOR_LOOP_PACKET ? "Account banned." : ""));
            if (Config.DO_ACCOUNT_BAN_FOR_LOOP_PACKET) {
                int banExpire = (int) (System.currentTimeMillis() / 1000L) + 2880;
                AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(_client.getLogin(), -100, banExpire));
            }
        }
    }

    protected abstract void runImpl() throws Exception;

    protected String readS(int len) {
        String ret = readS();
        return ret.length() > len ? ret.substring(0, len) : ret;
    }

    protected void sendPacket(L2GameServerPacket packet) {
        getClient().sendPacket(packet);
    }

    protected void sendPacket(L2GameServerPacket... packets) {
        getClient().sendPacket(packets);
    }

    protected void sendPackets(List<L2GameServerPacket> packets) {
        getClient().sendPackets(packets);
    }

    public String getType() {
        return "[C] " + getClass().getSimpleName();
    }
}