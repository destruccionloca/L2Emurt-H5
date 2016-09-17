/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package ai.superpoint;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.instances.NpcInstance;

/**
 * @author VISTALL
 * @date 22:38/24.10.2011
 */
public class NotAggressiveNpc extends SuperPointDefaultAI {
	public NotAggressiveNpc(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage) {
	}

	@Override
	protected void onEvtAggression(Creature target, int aggro) {
	}
}
