package l2p.gameserver.network.telnet.commands;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.Player;
import l2p.gameserver.network.telnet.TelnetCommand;
import l2p.gameserver.network.telnet.TelnetCommandHolder;
import l2p.gameserver.tables.GmListTable;

public class TelnetWorld implements TelnetCommandHolder {

    private Set<TelnetCommand> _commands = new LinkedHashSet<>();

    public TelnetWorld() {
        _commands.add(new TelnetCommand("find") {
            @Override
            public String getUsage() {
                return "find <name>";
            }

            @Override
            public String handle(String[] args) {
                if (args.length == 0) {
                    return null;
                }

                Iterable<Player> players = GameObjectsStorage.getAllPlayersForIterate();
                Iterator<Player> itr = players.iterator();
                StringBuilder sb = new StringBuilder();
                int count = 0;
                Player player;
                Pattern pattern = Pattern.compile(args[0] + "\\S+", Pattern.CASE_INSENSITIVE);
                while (itr.hasNext()) {
                    player = itr.next();

                    if (pattern.matcher(player.getName()).matches()) {
                        count++;
                        sb.append(player).append("\n\r");
                    }
                }

                if (count == 0) {
                    sb.append("Player not found.").append("\n\r");
                } else {
                    sb.append("=================================================\n\r");
                    sb.append("Found: ").append(count).append(" players.").append("\n\r");
                }

                return sb.toString();
            }
        });
        _commands.add(new TelnetCommand("whois", "who") {
            @Override
            public String getUsage() {
                return "whois <name>";
            }

            @Override
            public String handle(String[] args) {
                if (args.length == 0) {
                    return null;
                }

                Player player = GameObjectsStorage.getPlayer(args[0]);
                if (player == null) {
                    return "Player not found.\n\r";
                }

                StringBuilder sb = new StringBuilder();

                sb.append("Name: .................... ").append(player.getName()).append("\n\r");
                sb.append("ID: ...................... ").append(player.getObjectId()).append("\n\r");
                sb.append("Account Name: ............ ").append(player.getAccountName()).append("\n\r");
                sb.append("IP: ...................... ").append(player.getIP()).append("\n\r");
                sb.append("Level: ................... ").append(player.getLevel()).append("\n\r");
                sb.append("Location: ................ ").append(player.getLoc()).append("\n\r");
                if (player.getClan() != null) {
                    sb.append("Clan: .................... ").append(player.getClan().getName()).append("\n\r");
                    if (player.getAlliance() != null) {
                        sb.append("Ally: .................... ").append(player.getAlliance().getAllyName()).append("\n\r");
                    }
                }
                sb.append("Offline: ................. ").append(player.isInOfflineMode()).append("\n\r");

                sb.append(player.toString()).append("\n\r");

                return sb.toString();
            }
        });
        _commands.add(new TelnetCommand("gmlist", "gms") {
            @Override
            public String getUsage() {
                return "gmlist";
            }

            @Override
            public String handle(String[] args) {
                List<Player> gms = GmListTable.getAllGMs();
                int count = gms.size();

                if (count == 0) {
                    return "GMs not found.\n\r";
                }

                StringBuilder sb = new StringBuilder();
                for (Player gm : gms) {
                    sb.append(gm).append("\n\r");
                }

                sb.append("Found: ").append(count).append(" GMs.").append("\n\r");

                return sb.toString();
            }
        });
    }

    @Override
    public Set<TelnetCommand> getCommands() {
        return _commands;
    }
}