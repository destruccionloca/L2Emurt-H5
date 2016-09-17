package l2p.gameserver.clientpackets;

import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.base.EnchantSkillLearn;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.serverpackets.ExEnchantSkillInfo;
import l2p.gameserver.serverpackets.ExEnchantSkillResult;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.skills.TimeStamp;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.tables.SkillTreeTable;
import l2p.gameserver.utils.Log;

public final class RequestExEnchantSkillRouteChange extends L2GameClientPacket {

    private int _skillId;
    private int _skillLvl;

    @Override
    protected void readImpl() {
        _skillId = readD();
        _skillLvl = readD();
    }

    @Override
    protected void runImpl() {
        Player activeChar = getClient().getActiveChar();
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

        EnchantSkillLearn sl = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl);
        if (sl == null) {
            return;
        }

        int slevel = activeChar.getSkillDisplayLevel(_skillId);
        if (slevel == -1) {
            return;
        }

        if ((slevel <= sl.getBaseLevel() || slevel % 100 != _skillLvl % 100) && !Config.ENCHANT_SKILL_TO_MAX) {
            return;
        }

        int[] cost = sl.getCost();
        int requiredSp = cost[1] * sl.getCostMult() / SkillTreeTable.SAFE_ENCHANT_COST_MULTIPLIER;
        int requiredAdena = cost[0] * sl.getCostMult() / SkillTreeTable.SAFE_ENCHANT_COST_MULTIPLIER;

        if (!Config.ENCHANT_SKILL_TO_MAX && activeChar.getSp() < requiredSp) {
            activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
            return;
        }

        if (!Config.ENCHANT_SKILL_TO_MAX && activeChar.getAdena() < requiredAdena) {
            activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
            return;
        }

        if (!Config.ENCHANT_SKILL_TO_MAX && Functions.getItemCount(activeChar, SkillTreeTable.CHANGE_ENCHANT_BOOK) == 0) {
            activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
            return;
        }

        if (!Config.ENCHANT_SKILL_TO_MAX) {
            Functions.removeItem(activeChar, SkillTreeTable.CHANGE_ENCHANT_BOOK, 1);
        }
        if (!Config.ENCHANT_SKILL_TO_MAX) {
            Functions.removeItem(activeChar, 57, requiredAdena);
        }
        if (!Config.ENCHANT_SKILL_TO_MAX) {
            activeChar.addExpAndSp(0, -1 * requiredSp);
        }
        TimeStamp ts;
        ts = activeChar.getSkillReuse(activeChar.getKnownSkill(_skillId));
		int levelPenalty = 9;
        if (!Config.ENCHANT_SKILL_TO_MAX) {
			levelPenalty = Rnd.get(Math.min(4, _skillLvl % 100));
		
			_skillLvl -= levelPenalty;
		}
        if (_skillLvl % 100 == 0) {
            _skillLvl = sl.getBaseLevel();
        }

        Skill skill = SkillTable.getInstance().getInfo(_skillId, SkillTreeTable.convertEnchantLevel(sl.getBaseLevel(), !Config.ENCHANT_SKILL_TO_MAX ? _skillLvl : _skillLvl + 1, sl.getMaxLevel()));

        if (skill != null) {
            activeChar.addSkill(skill, true);
            if (ts != null && ts.hasNotPassed() && Config.ALT_ENCHANT_SKILL_REMOVE_COOLDOWN) {
                activeChar.disableSkill(skill, ts.getReuseCurrent());
            }
        }

        if (levelPenalty == 0) {
            SystemMessage2 sm = new SystemMessage2(SystemMsg.S1S_AUCTION_HAS_ENDED);
            sm.addSkillName(_skillId, _skillLvl);
            activeChar.sendPacket(sm);
        } else {
            SystemMessage2 sm = new SystemMessage2(SystemMsg.S1S2S_AUCTION_HAS_ENDED);
            sm.addSkillName(_skillId, _skillLvl);
            sm.addInteger(levelPenalty);
            activeChar.sendPacket(sm);
        }

        Log.add(activeChar.getName() + "|Successfully changed route|" + _skillId + "|" + slevel + "|to+" + _skillLvl + "|" + levelPenalty, "enchant_skills");

        activeChar.sendPacket(new ExEnchantSkillInfo(_skillId, activeChar.getSkillDisplayLevel(_skillId)), new ExEnchantSkillResult(1));
        RequestExEnchantSkill.updateSkillShortcuts(activeChar, _skillId, _skillLvl);
    }
}