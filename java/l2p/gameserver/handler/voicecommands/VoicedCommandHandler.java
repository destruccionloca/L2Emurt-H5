package l2p.gameserver.handler.voicecommands;

import java.util.HashMap;
import java.util.Map;

import l2p.gameserver.handler.voicecommands.impl.*;
import l2p.commons.data.xml.AbstractHolder;
import l2p.gameserver.Config;

public class VoicedCommandHandler extends AbstractHolder {

    private static final VoicedCommandHandler _instance = new VoicedCommandHandler();

    public static VoicedCommandHandler getInstance() {
        return _instance;
    }
    private Map<String, IVoicedCommandHandler> _datatable = new HashMap<String, IVoicedCommandHandler>();

    private VoicedCommandHandler() {
        registerVoicedCommandHandler(new Help());
        registerVoicedCommandHandler(new Hellbound());
        registerVoicedCommandHandler(new Cfg());
        registerVoicedCommandHandler(new CWHPrivileges());
        registerVoicedCommandHandler(new Inform());
        registerVoicedCommandHandler(new Offline());
        registerVoicedCommandHandler(new Password());
        registerVoicedCommandHandler(new Relocate());
        registerVoicedCommandHandler(new Repair());
        registerVoicedCommandHandler(new ServerInfo());
        registerVoicedCommandHandler(new Talismans());
        registerVoicedCommandHandler(new Wedding());
        registerVoicedCommandHandler(new WhoAmI());
        registerVoicedCommandHandler(new Debug());
        registerVoicedCommandHandler(new Security());
		if (Config.ALLOW_ACP)
            registerVoicedCommandHandler(new Acp());
        if (Config.ALLOW_ENCHANT_SERVICE)
            registerVoicedCommandHandler(new Enchant());
    }

    public void registerVoicedCommandHandler(IVoicedCommandHandler handler) {
        String[] ids = handler.getVoicedCommandList();
        for (String element : ids) {
            _datatable.put(element, handler);
        }
    }

    public IVoicedCommandHandler getVoicedCommandHandler(String voicedCommand) {
        String command = voicedCommand;
        if (voicedCommand.contains(" ")) {
            command = voicedCommand.substring(0, voicedCommand.indexOf(" "));
        }

        return _datatable.get(command);
    }

    @Override
    public int size() {
        return _datatable.size();
    }

    @Override
    public void clear() {
        _datatable.clear();
    }
}
