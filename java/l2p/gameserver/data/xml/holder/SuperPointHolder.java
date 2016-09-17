package l2p.gameserver.data.xml.holder;

import l2p.commons.data.xml.AbstractHolder;
import l2p.gameserver.templates.superpoint.SuperPointRoute;

import java.util.HashMap;
import java.util.Map;

public class SuperPointHolder extends AbstractHolder {
	private Map<String, SuperPointRoute> _routes = new HashMap<>();
	private static SuperPointHolder _instance;

	public static SuperPointHolder getInstance() {
		if (_instance == null) {
			_instance = new SuperPointHolder();
		}
		return _instance;
	}

	private SuperPointHolder() {
	}

	public void addRoute(SuperPointRoute route)
	{
		_routes.put(route.getName(), route);
	}

	public SuperPointRoute getRoute(String name)
	{
		return _routes.get(name);
	}

	@Override
	public int size()
	{
		return _routes.size();
	}

	@Override
	public void clear()
	{
		_routes.clear();
	}
}