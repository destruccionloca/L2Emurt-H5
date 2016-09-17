package l2p.gameserver.model.actor.listener;

import l2p.commons.listener.Listener;
import l2p.gameserver.listener.actor.npc.OnDecayListener;
import l2p.gameserver.listener.actor.npc.OnSpawnListener;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.instances.NpcInstance;

public class NpcListenerList extends CharListenerList {

    public NpcListenerList(NpcInstance actor) {
        super(actor);
    }

    @Override
    public NpcInstance getActor() {
        return (NpcInstance) actor;
    }

    public void onSpawn() {
        if (!global.getListeners().isEmpty()) {
            global.getListeners().stream().filter(listener -> OnSpawnListener.class.isInstance(listener)).forEach(listener -> {
                ((OnSpawnListener) listener).onSpawn(getActor());
            });
        }

        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(listener -> OnSpawnListener.class.isInstance(listener)).forEach(listener -> {
                ((OnSpawnListener) listener).onSpawn(getActor());
            });
        }
    }

    public void onDecay() {
        if (!global.getListeners().isEmpty()) {
            global.getListeners().stream().filter(listener -> OnDecayListener.class.isInstance(listener)).forEach(listener -> {
                ((OnDecayListener) listener).onDecay(getActor());
            });
        }

        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(listener -> OnDecayListener.class.isInstance(listener)).forEach(listener -> {
                ((OnDecayListener) listener).onDecay(getActor());
            });
        }
    }
}
