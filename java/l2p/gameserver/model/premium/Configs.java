package l2p.gameserver.model.premium;

import gnu.trove.list.array.TIntArrayList;

import java.util.concurrent.ConcurrentHashMap;

public class Configs implements Comparable<Object> {

    public int ID = 1;
    public long PRICE = 1;
    public int ITEM_ID = 4037;
    public int TIME = 10;

    public double RATE_VALUE = 2.;

    public double RATE_XP = 2.;
    public double RATE_SP = 2.;
    public double RATE_ADENA = 2.;
    public double RATE_ITEM = 2.;
    public double RATE_SPOIL = 2.;
    public double RATE_CHANCE = 2.;
    public double RATE_CRAFT_MW = 2.;
    public double RATE_CRAFT_DOUBLE = 2.;
    public double RATE_QUEST_REWARD = 2.;
    public double RATE_QUEST_DROP = 2.;

    public boolean ALLOW_TRANSFER_ITEM = false;
    public boolean ALLOW_AUTO_LOOT_ITEM = false;
    public boolean ALLOW_HERO_AURA = false;
    public boolean ALLOW_AGATION = false;
    public boolean ALLOW_SOUL_SPIRIT_SHOT_INFINITELY = false;
    public boolean ALLOW_SOUL_SPIRIT_SHOT_INFINITELY_PET = false;
    public boolean ALLOW_ARROW_INFINITELY = false;

    public int ENCHANT_CHANCE = 68;
    public int ENCHANT_CHANCE_ARMOR = 52;
    public int ENCHANT_CHANCE_ACCESSORY = 54;
    public int ENCHANT_CHANCE_BLESSED = 68;
    public int ENCHANT_CHANCE_ARMOR_BLESSED = 52;
    public int ENCHANT_CHANCE_ACCESSORY_BLESSED = 54;
    public int ENCHANT_CHANCE_CRYSTAL = 68;
    public int ENCHANT_CHANCE_ARMOR_CRYSTAL = 52;
    public int ENCHANT_CHANCE_ACCESSORY_CRYSTAL = 54;

    public boolean USE_ALT_ENCHANT = false;
    public TIntArrayList ENCHANT_WEAPON_FIGHTER = new TIntArrayList();
    public TIntArrayList ENCHANT_WEAPON_FIGHTER_CRYSTALL = new TIntArrayList();
    public TIntArrayList ENCHANT_WEAPON_FIGHTER_BLESSED = new TIntArrayList();
    public TIntArrayList ENCHANT_WEAPON_MAGE = new TIntArrayList();
    public TIntArrayList ENCHANT_WEAPON_MAGE_CRYSTALL = new TIntArrayList();
    public TIntArrayList ENCHANT_WEAPON_MAGE_BLESSED = new TIntArrayList();
    public TIntArrayList ENCHANT_ARMOR = new TIntArrayList();
    public TIntArrayList ENCHANT_ARMOR_CRYSTALL = new TIntArrayList();
    public TIntArrayList ENCHANT_ARMOR_BLESSED = new TIntArrayList();
    public TIntArrayList ENCHANT_ACCESSORY = new TIntArrayList();
    public TIntArrayList ENCHANT_ACCESSORY_CRYSTALL = new TIntArrayList();
    public TIntArrayList ENCHANT_ACCESSORY_BLESSED = new TIntArrayList();


    private ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();
/*
    @Override
    public int compareTo(Object obj) {
        Configs tmp = (Configs) obj;
        return START_TIME < tmp.START_TIME ? -1 : START_TIME > tmp.START_TIME ? 1 : 0;
    }

    public int[] getRestictId() {
        return getIntArray(RESTRICT_ITEMS);
    }

    public int[] getRewardId() {
        return getIntArray(ST_REWARD_ITEM_ID);
    }

    public int[] getRewardCount() {
        return getIntArray(ST_REWARD_COUNT);
    }

    private int[] getIntArray(String name) {
        return Util.parseCommaSeparatedIntegerArray(name);
    }
    */

    @Override
    public int compareTo(Object obj) {
        Configs tmp = (Configs) obj;
        return ID < tmp.ID ? -1 : ID > tmp.ID ? 1 : 0;
    }
}
