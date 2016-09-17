package l2p.gameserver.scripts;

public interface ScriptFile {

    void onLoad();

    void onReload();

    void onShutdown();
}