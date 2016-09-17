package l2p.gameserver.data.xml;

import l2p.gameserver.data.StringHolder;
import l2p.gameserver.data.htm.HtmCache;
import l2p.gameserver.data.xml.holder.BuyListHolder;
import l2p.gameserver.data.xml.holder.MultiSellHolder;
import l2p.gameserver.data.xml.holder.ProductHolder;
import l2p.gameserver.data.xml.holder.RecipeHolder;
import l2p.gameserver.data.xml.parser.*;
import l2p.gameserver.instancemanager.ReflectionManager;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.tables.SpawnTable;

public abstract class Parsers {

    public static void parseAll() {
        HtmCache.getInstance().reload();
        StringHolder.getInstance().load();
        //
        SkillTable.getInstance().load(); // - SkillParser.getInstance();
        OptionDataParser.getInstance().load();
        ItemParser.getInstance().load();
        //
        ExtractableItems.getInstance();
        NpcParser.getInstance().load();
        
        PetDataTemplateParser.getInstance().load();
        SuperPointParser.getInstance().load();

        DomainParser.getInstance().load();
        RestartPointParser.getInstance().load();

        StaticObjectParser.getInstance().load();
        DoorParser.getInstance().load();
        ZoneParser.getInstance().load();
        SpawnTable.getInstance();
        SpawnParser.getInstance().load();
        InstantZoneParser.getInstance().load();

        ReflectionManager.getInstance();
        //
        AirshipDockParser.getInstance().load();
        SkillAcquireParser.getInstance().load();
        
        //
        CharTemplateParser.getInstance().load();
        //
        ResidenceParser.getInstance().load();
        EventParser.getInstance().load();
        // support(cubic & agathion)
        CubicParser.getInstance().load();
        //
        BuyListHolder.getInstance();
        RecipeHolder.getInstance();
        MultiSellHolder.getInstance();
        ProductHolder.getInstance();
        // AgathionParser.getInstance();
        // item support
        HennaParser.getInstance().load();
        EnchantItemParser.getInstance().load();
        SoulCrystalParser.getInstance().load();
        ArmorSetsParser.getInstance().load();

        // etc
        PetitionGroupParser.getInstance().load();
    }
}
