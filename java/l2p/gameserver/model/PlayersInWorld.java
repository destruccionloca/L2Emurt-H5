package l2p.gameserver.model;

public interface PlayersInWorld {

    void storePlayer(GameObject player);

    void removePlayer(GameObject player);

    Player get(int objId);

    void run();
}