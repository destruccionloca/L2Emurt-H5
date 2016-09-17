package quests;

import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;
import l2p.gameserver.scripts.ScriptFile;

/**
 * Квест A Looteranda Railroad Man
 *
 * @author Sergey Ibryaev aka Artful
 */
public class _649_ALooterandaRailroadMan extends Quest implements ScriptFile {
    //NPC

    private static final int OBI = 32052;
    //Quest Item
    private static final int THIEF_GUILD_MARK = 8099;
    //Main
    //Drop Cond
    //# [COND, NEWCOND, ID, REQUIRED, ITEM, NEED_COUNT, CHANCE, DROP]
    private static final int[][] DROPLIST_COND = {
        {
            1,
            2,
            22017,
            0,
            THIEF_GUILD_MARK,
            200,
            50,
            1
        },
        {
            1,
            2,
            22018,
            0,
            THIEF_GUILD_MARK,
            200,
            50,
            1
        },
        {
            1,
            2,
            22019,
            0,
            THIEF_GUILD_MARK,
            200,
            50,
            1
        },
        {
            1,
            2,
            22021,
            0,
            THIEF_GUILD_MARK,
            200,
            50,
            1
        },
        {
            1,
            2,
            22022,
            0,
            THIEF_GUILD_MARK,
            200,
            50,
            1
        },
        {
            1,
            2,
            22023,
            0,
            THIEF_GUILD_MARK,
            200,
            50,
            1
        },
        {
            1,
            2,
            22024,
            0,
            THIEF_GUILD_MARK,
            200,
            50,
            1
        },
        {
            1,
            2,
            22026,
            0,
            THIEF_GUILD_MARK,
            200,
            50,
            1
        }
    };

    @Override
    public void onLoad() {
    }

    @Override
    public void onReload() {
    }

    @Override
    public void onShutdown() {
    }

    public _649_ALooterandaRailroadMan() {
        super(true);

        addStartNpc(OBI);

        //Mob Drop
        for (int[] aDROPLIST_COND : DROPLIST_COND) {
            addKillId(aDROPLIST_COND[2]);
        }
        addQuestItem(THIEF_GUILD_MARK);
    }

    @Override
    public String onEvent(String event, QuestState st, NpcInstance npc) {
        String htmltext = event;
        if (event.equalsIgnoreCase("quest_accept")) {
            htmltext = "railman_obi_q0649_0103.htm";
            st.setCond(1);
            st.setState(STARTED);
            st.playSound(SOUND_ACCEPT);
        } else if (event.equalsIgnoreCase("649_3")) {
            if (st.getQuestItemsCount(THIEF_GUILD_MARK) == 200) {
                htmltext = "railman_obi_q0649_0201.htm";
                st.takeItems(THIEF_GUILD_MARK, -1);
                st.giveItems(ADENA_ID, 21698, true);
                st.playSound(SOUND_FINISH);
                st.exitCurrentQuest(true);
            } else //Проверка сработает если игрок во время диалога удалит марки
            {
                st.setCond(1);
                htmltext = "railman_obi_q0649_0202.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(NpcInstance npc, QuestState st) {
        int npcId = npc.getNpcId();
        String htmltext = "noquest";
        int id = st.getState();
        int cond = 0;
        if (id != CREATED) {
            cond = st.getCond();
        }
        if (npcId == OBI) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() < 30) {
                    htmltext = "railman_obi_q0649_0102.htm";
                    st.exitCurrentQuest(true);
                } else {
                    htmltext = "railman_obi_q0649_0101.htm";
                }
            } else if (cond == 1) {
                htmltext = "railman_obi_q0649_0106.htm";
            } else if (cond == 2 && st.getQuestItemsCount(THIEF_GUILD_MARK) == 200) {
                htmltext = "railman_obi_q0649_0105.htm";
            } else {
                htmltext = "railman_obi_q0649_0106.htm";
                st.setCond(1);
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(NpcInstance npc, QuestState st) {
        int npcId = npc.getNpcId();
        int cond = st.getCond();
        for (int[] aDROPLIST_COND : DROPLIST_COND) {
            if (cond == aDROPLIST_COND[0] && npcId == aDROPLIST_COND[2]) {
                if (aDROPLIST_COND[3] == 0 || st.getQuestItemsCount(aDROPLIST_COND[3]) > 0) {
                    if (aDROPLIST_COND[5] == 0) {
                        st.rollAndGive(aDROPLIST_COND[4], aDROPLIST_COND[7], aDROPLIST_COND[6]);
                    } else if (st.rollAndGive(aDROPLIST_COND[4], aDROPLIST_COND[7], aDROPLIST_COND[7], aDROPLIST_COND[5], aDROPLIST_COND[6])) {
                        if (aDROPLIST_COND[1] != cond && aDROPLIST_COND[1] != 0) {
                            st.setCond(aDROPLIST_COND[1]);
                            st.setState(STARTED);
                        }
                    }
                }
            }
        }
        return null;
    }
}
