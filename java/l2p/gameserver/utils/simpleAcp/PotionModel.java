/**
 * Created by Hack
 * This is a model of Acp potion
 */
package l2p.gameserver.utils.simpleAcp;

import l2p.gameserver.Config;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.serverpackets.ExUseSharedGroupItem;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.skills.TimeStamp;
import l2p.gameserver.stats.Stats;

import java.util.Timer;

public class PotionModel {
    private Timer timer;
    private int minPercent = 0;
    private int maxPercent = 100;
    private boolean isEating = false;
    private boolean isAllow = false;
    private int itemId;
    private Player player;
    private int delay;
    private AcpListnerType potionType;


    public PotionModel(Player player, int itemId, int delay, AcpListnerType type) {
        this.player = player;
        this.itemId = itemId;
        this.delay = delay;
        potionType = type;
    }

    @SuppressWarnings("all")
    public void startEating() {
        if (getCurrentListnerValue() < getMaxListnerValue() && !isEating() && isAllow()) {
            timer = new Timer();
            synchronized (timer) {
                isEating = true;
                timer.schedule(new EatTask(), 0, delay);
            }
        }
    }

    public String getIcon() {
        return getItem().getTemplate().getIcon();
    }

    private double getCurrentListnerValue() {
        if (potionType == AcpListnerType.HP)
            return player.getCurrentHp();
        else if (potionType == AcpListnerType.CP)
            return player.getCurrentCp();
        else if (potionType == AcpListnerType.MP)
            return player.getCurrentMp();
        else if (potionType == AcpListnerType.SOULS)
            return player.getConsumedSouls();
        return player.getCurrentHp(); // unreachable

    }

    private double getMaxListnerValue() {
        if (potionType == AcpListnerType.HP)
            return player.getMaxHp();
        else if (potionType == AcpListnerType.CP)
            return player.getMaxCp();
        else if (potionType == AcpListnerType.MP)
            return player.getMaxMp();
        else if (potionType == AcpListnerType.SOULS)
            return player.calcStat(Stats.SOULS_LIMIT, 0);
        return player.getMaxHp(); // unreachable
    }

    private void eat()
    {
        Player activeChar = player;
        if(activeChar == null)
            return;
        activeChar.setActive();
        ItemInstance item = getItem();
        if(item == null) {
            activeChar.sendActionFailed();
            return;
        }
        if(isUsedDisabled(player))
            return;
        if(activeChar.isInStoreMode()) {
            activeChar.sendPacket(SystemMsg.YOU_MAY_NOT_USE_ITEMS_IN_A_PRIVATE_STORE_OR_PRIVATE_WORK_SHOP);
            return;
        }
        if(activeChar.isFishing()) {
            activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
            return;
        }
        if(activeChar.isSharedGroupDisabled(item.getTemplate().getReuseGroup())) {
            activeChar.sendReuseMessage(item);
            return;
        }
        if(!item.getTemplate().testCondition(activeChar, item))
            return;
        if(activeChar.getInventory().isLockedItem(item))
            return;
        if(item.getTemplate().isForPet()) {
            activeChar.sendPacket(SystemMsg.YOU_MAY_NOT_EQUIP_A_PET_ITEM);
            return;
        }
        if(activeChar.isOutOfControl()) {
            activeChar.sendActionFailed();
            return;
        }
        if(getPlayer().isInvisible() || getPlayer().isInvul())
            return;
        boolean success = item.getTemplate().getHandler().useItem(activeChar, item, false);
        if(success)
        {
            long nextTimeUse = item.getTemplate().getReuseType().next(item);
            if(nextTimeUse > System.currentTimeMillis())
            {
                TimeStamp timeStamp = new TimeStamp(item.getItemId(), nextTimeUse, item.getTemplate().getReuseDelay());
                activeChar.addSharedGroupReuse(item.getTemplate().getReuseGroup(), timeStamp);

                if(item.getTemplate().getReuseDelay() > 0)
                    activeChar.sendPacket(new ExUseSharedGroupItem(item.getTemplate().getDisplayReuseGroup(), timeStamp));
            }
        }
    }

    public boolean isUsedDisabled(Player player) {
        return player.isBlocked() || player.isAlikeDead() || player.isStunned() || player.isSleeping() || player.isParalyzed() || player.isFrozen();
    }

    public void setAllow(boolean allow) {
        isAllow = allow;
    }

    public void setEating(boolean eating) {
        isEating = eating;
    }

    public void setMaxPercent(int maxPercent) {
        this.maxPercent = maxPercent;
    }

    public void setMinPercent(int minPercent) {
        this.minPercent = minPercent;
    }

    public int getMaxPercent() {
        return maxPercent;
    }

    public int getMinPercent() {
        return minPercent;
    }

    public boolean isAllow() {
        return isAllow;
    }

    public boolean isEating() {
        return isEating;
    }

    public void setCond(int min, int max) {
        setMinPercent(min);
        setMaxPercent(max);
    }

    public Player getPlayer() {
        return player;
    }

    public int getItemId() {
        return itemId;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public ItemInstance getItem() {
        return player.getInventory().getItemByItemId(itemId);
    }

    private class EatTask extends java.util.TimerTask {
        @Override
        public void run() {
            if (player == null) {
                timer.cancel();
                return;
            }
            if (!player.hasBonus() && Config.ACP_ONLY_PREMIUM) {
                player.sendMessage("Функция автоматического потребления зелий доступна только для премиумов!");
                for (PotionModel potion : player.getAcp().getPotions()) {
                    potion.setAllow(false);
                    AcpDAO.saveAcpData(player, potion);
                }
            }
            int currentPercent = (int)((int)getCurrentListnerValue() / (getMaxListnerValue() / 100));
            if(getCurrentListnerValue() < getMaxListnerValue() && isEating() && (currentPercent + 1) > getMinPercent()
                    && currentPercent < getMaxPercent() && isAllow())
                eat();
            else {
                timer.cancel();
                isEating = false;
            }
        }
    }
}
