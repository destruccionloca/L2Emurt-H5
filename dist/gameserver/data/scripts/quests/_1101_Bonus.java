package quests;

import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.scripts.ScriptFile;

public class _1101_Bonus extends Quest implements ScriptFile
{
	private static final int STARSTONE = 1573;
	private static final int SILVER_SHILEN = 4357;

	public void onLoad() {}

	public void onReload() {}

	public void onShutdown() {}

	public _1101_Bonus()
	{
		super(false);
		addStartNpc(30086);
		addTalkId(new int[] { 30086 });
		addKillId(new int[] { 20045 });
		addQuestItem(new int[] { 1573 });
	}

	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if (event.equalsIgnoreCase("30540-03.htm"))
		{
			st.set("cond", "1");
			st.playSound(SOUND_ACCEPT);
		}
		return htmltext;
	}

	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getInt("cond");
		if (npcId == 30086) {
			if (cond == 0)
			{
				htmltext = "30540-02.htm";
			}
			else if ((cond == 1) && (st.getQuestItemsCount(1573) < 20L))
			{
				htmltext = "30540-04.htm";
			}
			else if ((cond == 2) && (st.getQuestItemsCount(1573) < 20L))
			{
				htmltext = "30540-04.htm";
			}
			else if ((cond == 2) && (st.getQuestItemsCount(1573) >= 20L))
			{
				st.takeItems(STARSTONE, 20);
				st.giveItems(4357, 2);
				st.exitCurrentQuest(true);
				//st.setState(COMPLETED);
			}
		}
		return htmltext;
	}

	public String onKill(NpcInstance npc, QuestState st)
	{
		st.rollAndGive(1573, 1, 1, 20, 33.0D);
		if (st.getQuestItemsCount(1573) >= 20L) {
			st.set("cond", "2");
		}
		return null;
	}
} 