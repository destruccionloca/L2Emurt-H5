package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.List;

import l2p.gameserver.Config;
import l2p.gameserver.model.base.EnchantSkillLearn;
import l2p.gameserver.tables.SkillTreeTable;

public class ExEnchantSkillInfo extends L2GameServerPacket {

    private final List<Integer> _routes;
    private final int _id;
    private int _level;
    private int _canAdd;
    private int canDecrease;

    public ExEnchantSkillInfo(int id, int level) {
        _routes = new ArrayList<>();
        _id = id;
        _level = level;

        // skill already enchanted?
        if (_level > 100) {
            canDecrease = 1;

            /*
             * tmp
             */
            int skillMaxLevel = SkillTreeTable.getSkillEnchant(_id, _level).getMaxLevel();
            int skillBaseLevel = SkillTreeTable.getSkillEnchant(_id, _level).getBaseLevel();
            int root = (_level / 100);
            int realSkillMaxLevel = (root * 100 + skillMaxLevel);
            // get detail for next level
            EnchantSkillLearn esd = SkillTreeTable.getSkillEnchant(_id, Config.ENCHANT_SKILL_TO_MAX ? realSkillMaxLevel : _level + 1);

            // if it exists add it
            if (esd != null) {

                if ((_level == root * 100 + skillBaseLevel) && Config.ENCHANT_SKILL_TO_MAX) {
                    addEnchantSkillDetail(esd.getLevel());
                    _canAdd = 1;
                } else {
                    addEnchantSkillDetail(esd.getLevel());
                    _canAdd = 1;
                }
            }
            if (Config.ENCHANT_SKILL_TO_MAX) {
                _level = realSkillMaxLevel;
            }

            for (EnchantSkillLearn el : SkillTreeTable.getEnchantsForChange(_id, _level)) {
                addEnchantSkillDetail(el.getLevel());
            }
        } else // not already enchanted
        {
            for (EnchantSkillLearn esd : SkillTreeTable.getFirstEnchantsForSkill(_id)) {
                addEnchantSkillDetail(esd.getLevel());
                _canAdd = 1;
            }
        }
    }

    public void addEnchantSkillDetail(int level) {
        _routes.add(level);
    }

    @Override
    protected void writeImpl() {
        writeEx(0x2a);

        writeD(_id);
        writeD(_level);
        writeD(_canAdd); // can add enchant
        writeD(canDecrease); // can decrease enchant

        writeD(_routes.size());
        _routes.forEach(this::writeD);
    }
}
