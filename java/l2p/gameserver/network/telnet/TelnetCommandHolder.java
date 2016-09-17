package l2p.gameserver.network.telnet;

import java.util.Set;

public interface TelnetCommandHolder {

    Set<TelnetCommand> getCommands();
}
