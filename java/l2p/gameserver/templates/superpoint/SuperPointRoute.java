package l2p.gameserver.templates.superpoint;

import java.util.ArrayList;
import java.util.List;

public class SuperPointRoute
{
	private final List<SuperPointNode> _nodes = new ArrayList<>();
	private final String _name;
	private final SuperPointType _type;
	private final boolean _isRunning;

	public SuperPointRoute(String name, SuperPointType type, boolean isRunning)
	{
		_name = name;
		_type = type;
		_isRunning = isRunning;
	}

	public List<SuperPointNode> getNodes()
	{
		return _nodes;
	}

	public String getName()
	{
		return _name;
	}

	public SuperPointType getType()
	{
		return _type;
	}

	public boolean isRunning()
	{
		return _isRunning;
	}
}
