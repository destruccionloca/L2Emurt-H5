package npc.model;

import l2p.gameserver.Config;
import l2p.gameserver.idfactory.IdFactory;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.instances.MonsterInstance;
import l2p.gameserver.templates.npc.NpcTemplate;

public class HellboundMonsterInstance extends MonsterInstance {
    public HellboundMonsterInstance(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public boolean hasRandomWalk() {
        return true;
    }

    @Override
    public boolean canChampion() {
        return false;
    }

    @Override
	public boolean isLethalImmune()	{
		return false;
	}
}
