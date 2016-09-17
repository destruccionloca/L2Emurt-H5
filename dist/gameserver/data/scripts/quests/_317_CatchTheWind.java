package quests;

import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;
import l2p.gameserver.scripts.ScriptFile;

/**
 * Квест Catch The Wind
 *
 * @author Sergey Ibryaev aka Artful
 */
public class _317_CatchTheWind extends Quest implements ScriptFile {
    //NPCs

    private static int Rizraell = 30361;
    //Quest Items
    private static int WindShard = 1078;
    //Mobs
    private static int Lirein = 20036;
    private static int LireinElder = 20044;
    //Drop Cond
    //# [COND, NEWCOND, ID, REQUIRED, ITEM, NEED_COUNT, CHANCE, DROP]
    public final int[][] DROPLIST_COND = {
        {
            1,
            0,
            Lirein,
            0,
            WindShard,
            0,
            60,
            1
        },
        {
            1,
            0,
            LireinElder,
            0,
            WindShard,
            0,
            60,
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

    public _317_CatchTheWind() {
        super(false);
        addStartNpc(Rizraell);
        //Mob Drop
        for (int[] aDROPLIST_COND : DROPLIST_COND) {
            addKillId(aDROPLIST_COND[2]);
        }
        addQuestItem(WindShard);
    }

    @Override
    public String onEvent(String event, QuestState st, NpcInstance npc) {
        if (event.equalsIgnoreCase("rizraell_q0317_04.htm")) {
            st.setCond(1);
            st.setState(STARTED);
            st.playSound(SOUND_ACCEPT);
        } else if (event.equalsIgnoreCase("rizraell_q0317_08.htm")) {
            st.playSound(SOUND_FINISH);
            st.exitCurrentQuest(true);
        }
        return event;
    }

    @Override
    public String onTalk(NpcInstance npc, QuestState st) {
        int npcId = npc.getNpcId();
        String htmltext = "noquest";
        int cond = st.getCond();
        if (npcId == Rizraell) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 18) {
                    htmltext = "rizraell_q0317_03.htm";
                } else {
                    htmltext = "rizraell_q0317_02.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1) {
                long count = st.getQuestItemsCount(WindShard);
                if (count > 0) {
                    st.takeItems(WindShard, -1);
                    st.giveItems(ADENA_ID, 40 * count);
                    htmltext = "rizraell_q0317_07.htm";
                } else {
                    htmltext = "rizraell_q0317_05.htm";
                }
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