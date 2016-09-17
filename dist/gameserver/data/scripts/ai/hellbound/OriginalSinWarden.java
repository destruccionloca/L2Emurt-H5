package ai.hellbound;

import l2p.commons.util.Rnd;
import l2p.gameserver.ai.Fighter;
import l2p.gameserver.data.xml.holder.NpcHolder;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.SimpleSpawner;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.utils.Location;

public class OriginalSinWarden extends Fighter {

    private static final int[] servants1 = {22424, 22425, 22426, 22427, 22428, 22429, 22430};
    private static final int[] servants2 = {22432, 22433, 22434, 22435, 22436, 22437, 22438};
    private static final int[] DarionsFaithfulServants = {22405, 22406, 22407};

    public OriginalSinWarden(NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtSpawn() {
        super.onEvtSpawn();

        NpcInstance actor = getActor();
        switch (actor.getNpcId()) {
            case 22423: {
                for (int aServants1 : servants1) {
                    try {
                        Location loc = actor.getLoc();
                        SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(aServants1));
                        sp.setLoc(Location.findPointToStay(actor, 150, 350));
                        sp.doSpawn(true);
                        sp.stopRespawn();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            case 22431: {
                for (int aServants2 : servants2) {
                    try {
                        Location loc = actor.getLoc();
                        SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(aServants2));
                        sp.setLoc(Location.findPointToStay(actor, 150, 350));
                        sp.doSpawn(true);
                        sp.stopRespawn();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    protected void onEvtDead(Creature killer) {
        NpcInstance actor = getActor();

        if (Rnd.chance(15)) {
            try {
                Location loc = actor.getLoc();
                SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(DarionsFaithfulServants[Rnd.get(DarionsFaithfulServants.length - 1)]));
                sp.setLoc(Location.findPointToStay(actor, 150, 350));
                sp.doSpawn(true);
                sp.stopRespawn();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onEvtDead(killer);
    }
}