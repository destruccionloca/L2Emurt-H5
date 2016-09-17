package l2p.gameserver.templates.superpoint;

import l2p.gameserver.serverpackets.components.ChatType;
import l2p.gameserver.serverpackets.components.NpcString;
import l2p.gameserver.utils.Location;

public class SuperPointNode extends Location
{
	private static final long serialVersionUID = 8291528118019681063L;

	private final NpcString _npcString;
	private final ChatType _chatType;
	private final long _delay;
	private final int _socialId;

	public SuperPointNode(int x, int y, int z, NpcString npcString, int socialId, long delay, ChatType chatType)
	{
		super(x, y, z);
		_npcString = npcString;
		_socialId = socialId;
		_delay = delay * 1000;
		_chatType = chatType;
	}

	public NpcString getNpcString()
	{
		return _npcString;
	}

	public long getDelay()
	{
		return _delay;
	}

	public int getSocialId()
	{
		return _socialId;
	}

	public ChatType getChatType()
	{
		return _chatType;
	}
}
