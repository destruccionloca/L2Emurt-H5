package quests;

import l2p.commons.util.Rnd;
import l2p.gameserver.model.base.Race;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;
import l2p.gameserver.scripts.ScriptFile;

/**
 * Квест Proof Of Valor
 *
 * @author Sergey Ibryaev aka Artful
 */
public class _271_ProofOfValor extends Quest implements ScriptFile {
    //NPC

    private static final int RUKAIN = 30577;
    //Quest Item
    private static final int KASHA_WOLF_FANG_ID = 1473;
    private static final int NECKLACE_OF_VALOR_ID = 1507;
    private static final int NECKLACE_OF_COURAGE_ID = 1506;
    //Drop Cond
    //# [COND, NEWCOND, ID, REQUIRED, ITEM, NEED_COUNT, CHANCE, DROP]
    private static final int[][] DROPLIST_COND = {
        {
            1,
            2,
            20475,
            0,
            KASHA_WOLF_FANG_ID,
            50,
            25,
            2
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

    public _271_ProofOfValor() {
        super(false);

        addStartNpc(RUKAIN);
        addTalkId(RUKAIN);

        //Mob Drop
        for (int[] aDROPLIST_COND : DROPLIST_COND) {
            addKillId(aDROPLIST_COND[2]);
        }

        addQuestItem(KASHA_WOLF_FANG_ID);
    }

    @Override
    public String onEvent(String event, QuestState st, NpcInstance npc) {
        String htmltext = event;
        if (event.equalsIgnoreCase("praetorian_rukain_q0271_03.htm")) {
            st.playSound(SOUND_ACCEPT);
            if (st.getQuestItemsCount(NECKLACE_OF_COURAGE_ID) > 0 || st.getQuestItemsCount(NECKLACE_OF_VALOR_ID) > 0) {
                htmltext = "praetorian_rukain_q0271_07.htm";
            }
            st.setCond(1);
            st.setState(STARTED);
        }
        return htmltext;
    }

    @Override
    public String onTalk(NpcInstance npc, QuestState st) {
        int npcId = npc.getNpcId();
        String htmltext = "noquest";
        int cond = st.getCond();
        if (npcId == RUKAIN) {
            if (cond == 0) {
                if (st.getPlayer().getRace() != Race.orc) {
                    htmltext = "praetorian_rukain_q0271_00.htm";
                    st.exitCurrentQuest(true);
                } else if (st.getPlayer().getLevel() < 4) {
                    htmltext = "praetorian_rukain_q0271_01.htm";
                    st.exitCurrentQuest(true);
                } else if (st.getQuestItemsCount(NECKLACE_OF_COURAGE_ID) > 0 || st.getQuestItemsCount(NECKLACE_OF_VALOR_ID) > 0) {
                    htmltext = "praetorian_rukain_q0271_06.htm";
                    st.exitCurrentQuest(true);
                } else {
                    htmltext = "praetorian_rukain_q0271_02.htm";
                }
            } else if (cond == 1) {
                htmltext = "praetorian_rukain_q0271_04.htm";
            } else if (cond == 2 && st.getQuestItemsCount(KASHA_WOLF_FANG_ID) == 50) {
                st.takeItems(KASHA_WOLF_FANG_ID, -1);
                if (Rnd.chance(14)) {
                    st.takeItems(NECKLACE_OF_VALOR_ID, -1);
                    st.giveItems(NECKLACE_OF_VALOR_ID, 1);
                } else {
                    st.takeItems(NECKLACE_OF_COURAGE_ID, -1);
                    st.giveItems(NECKLACE_OF_COURAGE_ID, 1);
                }
                htmltext = "praetorian_rukain_q0271_05.htm";
                st.exitCurrentQuest(true);
            } else if (cond == 2 && st.getQuestItemsCount(KASHA_WOLF_FANG_ID) < 50) {
                htmltext = "praetorian_rukain_q0271_04.htm";
                st.setCond(1);
                st.setState(STARTED);
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
