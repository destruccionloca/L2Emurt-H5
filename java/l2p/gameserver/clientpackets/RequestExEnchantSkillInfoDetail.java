package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.SkillAcquireHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.SkillLearn;
import l2p.gameserver.model.base.AcquireType;
import l2p.gameserver.model.base.EnchantSkillLearn;
import l2p.gameserver.serverpackets.ExEnchantSkillInfoDetail;
import l2p.gameserver.tables.SkillTreeTable;

public final class RequestExEnchantSkillInfoDetail extends L2GameClientPacket {

    private static final int TYPE_NORMAL_ENCHANT = 0;
    private static final int TYPE_SAFE_ENCHANT = 1;
    private static final int TYPE_UNTRAIN_ENCHANT = 2;
    private static final int TYPE_CHANGE_ENCHANT = 3;
    private int _type;
    private int _skillId;
    private int _skillLvl;
	private SkillLearn _skillbase;

    @Override
    protected void readImpl() {
        _type = readD();
        _skillId = readD();
        _skillLvl = readD();
    }

    @Override
    protected void runImpl() {
        Player activeChar = getClient().getActiveChar();
		if(Config.ENCHANT_SKILL_TO_MAX) {
			_skillbase = SkillAcquireHolder.getInstance().getSkillLearn(activeChar, _skillId, 1, AcquireType.NORMAL);
		}

        if (activeChar == null) {
            return;
        }

        if (activeChar.getTransformation() != 0) {
            activeChar.sendMessage("You must leave transformation mode first.");
            return;
        }

        if (activeChar.getLevel() < 76 || activeChar.getClassId().getLevel() < 4) {
            activeChar.sendMessage("You must have 3rd class change quest completed.");
            return;
        }
        int bookId = 0;
        int sp;
        int adenaCount = 0;
        double spMult = SkillTreeTable.NORMAL_ENCHANT_COST_MULTIPLIER;

        EnchantSkillLearn esd = null;

        switch (_type) {
            case TYPE_NORMAL_ENCHANT:
                if (_skillLvl % 100 == 1) {
                    bookId = SkillTreeTable.NORMAL_ENCHANT_BOOK;
                }
                esd = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl);
                break;
            case TYPE_SAFE_ENCHANT:
                bookId = SkillTreeTable.SAFE_ENCHANT_BOOK;
                esd = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl);
                spMult = SkillTreeTable.SAFE_ENCHANT_COST_MULTIPLIER;
                break;
            case TYPE_UNTRAIN_ENCHANT:
                bookId = SkillTreeTable.UNTRAIN_ENCHANT_BOOK;
                esd = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl + 1);
                break;
            case TYPE_CHANGE_ENCHANT:
                bookId = SkillTreeTable.CHANGE_ENCHANT_BOOK;
                esd = SkillTreeTable.getEnchantsForChange(_skillId, _skillLvl).get(0);
                spMult = 1f / SkillTreeTable.SAFE_ENCHANT_COST_MULTIPLIER;
                break;
        }

        if (esd == null) {
            return;
        }

        spMult *= esd.getCostMult();
        int[] cost = esd.getCost();

        sp = (int) (cost[1] * spMult);

        if (_type != TYPE_UNTRAIN_ENCHANT) {
            adenaCount = (int) (cost[0] * spMult);
        }

        // send skill enchantment detail
		if(Config.ENCHANT_SKILL_TO_MAX)
		{
			if(_type == TYPE_NORMAL_ENCHANT)
			{
				int reenchantLevel = 0;
				if(_skillbase != null)
					reenchantLevel = _skillbase.getMinLevel() < 76 ? 29 : 14;
				int _skillLvlMax = _skillLvl + reenchantLevel;
				
				activeChar.sendPacket(new ExEnchantSkillInfoDetail(_skillId, _skillLvlMax, 0, 100, 0, 0));
				return;
			}
			if(_type == TYPE_SAFE_ENCHANT)
			{
				int reenchantLevel = 0;
				if(_skillbase != null)
					reenchantLevel = _skillbase.getMinLevel() < 76 ? 29 : 14;
				int _skillLvlMax = _skillLvl + reenchantLevel;
				
				activeChar.sendPacket(new ExEnchantSkillInfoDetail(_skillId, _skillLvlMax, 0, 100, 0, 0));
				return;
			}
			else if(_type == TYPE_CHANGE_ENCHANT)
			{
				activeChar.sendPacket(new ExEnchantSkillInfoDetail(_skillId, _skillLvl, sp, esd.getRate(activeChar), bookId, 0));
				return;
			}
		}
        activeChar.sendPacket(new ExEnchantSkillInfoDetail(_skillId, _skillLvl, sp, esd.getRate(activeChar), bookId, adenaCount));
    }
}
