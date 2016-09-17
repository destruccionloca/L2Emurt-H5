package l2p.gameserver.data.xml.parser;

import l2p.commons.data.xml.AbstractDirParser;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.SuperPointHolder;
import l2p.gameserver.serverpackets.components.ChatType;
import l2p.gameserver.serverpackets.components.NpcString;
import l2p.gameserver.templates.superpoint.SuperPointNode;
import l2p.gameserver.templates.superpoint.SuperPointRoute;
import l2p.gameserver.templates.superpoint.SuperPointType;
import org.dom4j.Element;

import java.io.File;

public class SuperPointParser extends AbstractDirParser<SuperPointHolder> {
	private static SuperPointParser _instance;

	public static SuperPointParser getInstance() {
		if (_instance == null) {
			_instance = new SuperPointParser();
		}
		return _instance;
	}

	private SuperPointParser()
	{
		super(SuperPointHolder.getInstance());
	}

	@Override
	public File getXMLDir()
	{
		return new File(Config.DATAPACK_ROOT, "./data/xml/superpoint");
	}

	@Override
	public boolean isIgnored(File f)
	{
		return false;
	}

	@Override
	public String getDTDFileName()
	{
		return "superpoint.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception {
		for(Element e : rootElement.elements()) {
			String name = e.attributeValue("name");
			SuperPointType type = SuperPointType.valueOf(e.attributeValue("type"));
			String running = e.attributeValue("is_running");

			SuperPointRoute superPointRoute = new SuperPointRoute(name, type, running != null && Boolean.parseBoolean(running));
			getHolder().addRoute(superPointRoute);

			for(Element nodeElement : e.elements()) {
				int x = Integer.parseInt(nodeElement.attributeValue("x"));
				int y = Integer.parseInt(nodeElement.attributeValue("y"));
				int z = Integer.parseInt(nodeElement.attributeValue("z"));
				int socialId = Integer.parseInt(nodeElement.attributeValue("social", "0"));
				long delay = Long.parseLong(nodeElement.attributeValue("delay", "0"));
				NpcString npcString = null;
				ChatType chatType = null;
				String npcString0 = nodeElement.attributeValue("npc_string");
				if(npcString0 != null)
				{
					npcString = NpcString.valueOf(npcString0);
					chatType = ChatType.valueOf(nodeElement.attributeValue("chat_type"));
				}

				SuperPointNode node = new SuperPointNode(x, y, z, npcString, socialId, delay, chatType);
				superPointRoute.getNodes().add(node);
			}
		}
	}
}