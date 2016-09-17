/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package ai.superpoint;

import l2p.gameserver.model.instances.NpcInstance;

/**
 * Created by JunkyFunky on 16.04.2016.
 */
public class FlyingNotAgressiveNpc extends NotAggressiveNpc {


    public FlyingNotAgressiveNpc(NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtSpawn() {
        NpcInstance actor = getActor();
        actor.setFlying(true);
        actor.setHasChatWindow(false);
        super.onEvtSpawn();
    }
}
