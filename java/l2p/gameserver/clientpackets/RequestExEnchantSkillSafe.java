package l2p.gameserver.clientpackets;

import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.base.EnchantSkillLearn;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.serverpackets.ExEnchantSkillInfo;
import l2p.gameserver.serverpackets.ExEnchantSkillResult;
import l2p.gameserver.serverpackets.SkillList;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.skills.TimeStamp;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.tables.SkillTreeTable;
import l2p.gameserver.utils.Log;

/**
 * Format (ch) dd
 */
public final class RequestExEnchantSkillSafe extends L2GameClientPacket {

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
        int root = (_skillLvl / 100);
        int realSkillMaxLevel = (root * 100 + sl.getMaxLevel());

        int slevel = activeChar.getSkillLevel(_skillId);
        if (slevel == -1) {
            return;
        }

        int enchantLevel = SkillTreeTable.convertEnchantLevel(sl.getBaseLevel(), Config.ENCHANT_SKILL_TO_MAX ? realSkillMaxLevel : _skillLvl, sl.getMaxLevel());

        // already knows the skill with this level
        if (slevel >= enchantLevel) {
            return;
        }

        // Можем ли мы перейти с текущего уровня скилла на данную заточку
        if ((slevel == sl.getBaseLevel() ? _skillLvl % 100 != 1 : slevel != enchantLevel - 1) && !Config.ENCHANT_SKILL_TO_MAX) {
            activeChar.sendMessage("Incorrect enchant level.");
            return;
        }

        Skill skill = SkillTable.getInstance().getInfo(_skillId, enchantLevel);
        if (skill == null) {
            return;
        }

        int[] cost = sl.getCost();
        int requiredSp = cost[1] * SkillTreeTable.SAFE_ENCHANT_COST_MULTIPLIER * sl.getCostMult();
        int requiredAdena = cost[0] * SkillTreeTable.SAFE_ENCHANT_COST_MULTIPLIER * sl.getCostMult();

        int rate = sl.getRate(activeChar);

        if (!Config.ENCHANT_SKILL_TO_MAX && activeChar.getSp() < requiredSp) {
            activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
            return;
        }

        if (!Config.ENCHANT_SKILL_TO_MAX && activeChar.getAdena() < requiredAdena) {
            activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
            return;
        }

        if (!Config.ENCHANT_SKILL_TO_MAX && Functions.getItemCount(activeChar, SkillTreeTable.SAFE_ENCHANT_BOOK) == 0) {
            activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
            return;
        }

        Functions.removeItem(activeChar, SkillTreeTable.SAFE_ENCHANT_BOOK, 1);

        if (Rnd.chance(rate)) {
            TimeStamp ts;
            ts = activeChar.getSkillReuse(activeChar.getKnownSkill(_skillId));
            activeChar.addSkill(skill, true);
            if (ts != null && ts.hasNotPassed() && Config.ALT_ENCHANT_SKILL_REMOVE_COOLDOWN) {
                activeChar.disableSkill(skill, ts.getReuseCurrent());
            }
            if (!Config.ENCHANT_SKILL_TO_MAX) {
                activeChar.addExpAndSp(0, -1 * requiredSp);
            }
            if (!Config.ENCHANT_SKILL_TO_MAX) {
                Functions.removeItem(activeChar, 57, requiredAdena);
            }
            activeChar.sendPacket(new SystemMessage2(SystemMsg.YOUR_SP_HAS_DECREASED_BY_S1).addInteger(requiredSp), new SystemMessage2(SystemMsg.SKILL_ENCHANT_WAS_SUCCESSFUL_S1_HAS_BEEN_ENCHANTED).addSkillName(_skillId, _skillLvl), new ExEnchantSkillResult(1));
            activeChar.sendPacket(new SkillList(activeChar));
            RequestExEnchantSkill.updateSkillShortcuts(activeChar, _skillId, _skillLvl);
            Log.add(activeChar.getName() + "|Successfully safe enchanted|" + _skillId + "|to+" + _skillLvl + "|" + rate, "enchant_skills");
        } else {
            activeChar.sendPacket(new SystemMessage(SystemMessage.Skill_enchant_failed_Current_level_of_enchant_skill_S1_will_remain_unchanged).addSkillName(_skillId, _skillLvl), new ExEnchantSkillResult(0));
            Log.add(activeChar.getName() + "|Failed to safe enchant|" + _skillId + "|to+" + _skillLvl + "|" + rate, "enchant_skills");
        }

        activeChar.sendPacket(new ExEnchantSkillInfo(_skillId, activeChar.getSkillDisplayLevel(_skillId)));
    }
}