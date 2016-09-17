package npc.model.residences.castle;

import java.util.HashSet;
import java.util.Set;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Spawner;
import l2p.gameserver.model.instances.residences.SiegeToggleNpcInstance;
import l2p.gameserver.templates.npc.NpcTemplate;

public class CastleControlTowerInstance extends SiegeToggleNpcInstance {

    private Set<Spawner> _spawnList = new HashSet<>();

    public CastleControlTowerInstance(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onDeathImpl(Creature killer) {
        _spawnList.forEach(Spawner::stopRespawn);
        _spawnList.clear();
    }

    @Override
    public void register(Spawner spawn) {
        _spawnList.add(spawn);
    }
}