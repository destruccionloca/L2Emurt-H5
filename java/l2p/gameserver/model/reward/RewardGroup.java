package l2p.gameserver.model.reward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l2p.commons.math.SafeMath;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RewardGroup implements Cloneable {

    private double _chance;
    private boolean _isAdena = false; // Шанс фиксирован, растет только количество
    private boolean _isHerb = false;
    private boolean _isEpolets = false;
    private boolean _rate_rb = false;
    private boolean _isRaid = false;
    private boolean _notRate = false; // Рейты вообще не применяются
    private List<RewardData> _items = new ArrayList<RewardData>();
    private double _chanceSum;
    
    private static final Logger _log = LoggerFactory.getLogger(RewardGroup.class);

    public RewardGroup(double chance) {
        setChance(chance);
    }

    public boolean notRate() {
        return _notRate;
    }

    public void setNotRate(boolean notRate) {
        _notRate = notRate;
    }

    public double getChance() {
        return _chance;
    }

    public void setChance(double chance) {
        _chance = chance;
    }

    public boolean isAdena() {
        return _isAdena;
    }

    public void setIsAdena(boolean isAdena) {
        _isAdena = isAdena;
    }

    public boolean isHerb() {
        return _isHerb;
    }

    public void setIsHerb(boolean isHerb) {
        _isHerb = isHerb;
    }
    
     public boolean isEpolets() {
        return _isEpolets;
    }

    public void setIsEpolets(boolean isEpolets) {
        _isEpolets = isEpolets;
    }

    public void addData(RewardData item) {
        if (item.getItem().isAdena()) {
            setIsAdena(true);
        }
        if (item.getItem().isHerb()) {
            setIsHerb(true);
        }
        if (item.getItem().isEpolets()) {
            setIsEpolets(true);
        }
        _chanceSum += item.getChance();
        item.setChanceInGroup(_chanceSum);
        _items.add(item);
    }

    /**
     * Возвращает список вещей
     */
    public List<RewardData> getItems() {
        return _items;
    }

    /**
     * Возвращает полностью независимую копию группы
     */
    @Override
    public RewardGroup clone() {
        RewardGroup ret = new RewardGroup(_chance);
        for (RewardData i : _items) {
            ret.addData(i.clone());
        }
        return ret;
    }

    /**
     * Функция используется в основном механизме расчета дропа, выбирает
     * одну/несколько вещей из группы, в зависимости от рейтов
     *
     */
    public List<RewardItem> roll(RewardType type, Player player, double mod, boolean isRaid, boolean isSiegeGuard, boolean isChampion) {
        switch (type) {
            case NOT_RATED_GROUPED:
            case NOT_RATED_NOT_GROUPED: {
                _isRaid = false;
                return rollItems(mod, 1.0, 1.0, 1.0);
            }
            case SWEEP: {
                _isRaid = false;
                return rollSpoil(mod, Config.RATE_DROP_SPOIL, player.getRateSpoil(), player.getRateChance());
            }
            case RATED_GROUPED:
                if (_isAdena) {
                    return rollAdena(mod, Config.RATE_DROP_ADENA, player.getRateAdena(), player.getRateChance());
                }
                if (_isHerb) {
                    _isRaid = false;
                    return rollItems(mod, Config.RATE_DROP_HERBS, 1.0, player.getRateChance());
                }
                if (_isEpolets) {
                    _isRaid = false;
                    return rollItems(mod, Config.RATE_DROP_EPOLET, player.getRateItems(), player.getRateChance());
                }
                if (isRaid) {
                    _isRaid = true;
                    return rollItems(mod, Config.RATE_DROP_RAIDBOSS, 1.0, player.getRateChance());
                }
                if (isSiegeGuard) {
                    _isRaid = false;
                    return rollItems(mod, Config.RATE_DROP_SIEGE_GUARD, 1.0, player.getRateChance());
                }
                _isRaid = false;
                return rollItems(mod, Config.RATE_DROP_ITEMS, player.getRateItems(), player.getRateChance());
            default:
                return Collections.emptyList();
        }
    }

    public List<RewardItem> rollItems(double mod, double baseRate, double playerRate, double playerRateChance) {
        if (mod <= 0) {
            return Collections.emptyList();
        }
        double ChanceRate;
        double rate;
        if (_notRate) {
            rate = Math.min(mod, 1.0);
            ChanceRate = Math.min(mod, 1.0);
        } else {
            if (_isRaid) {
                rate = baseRate;
                ChanceRate = 1.;
            } else {
                if (Config.RATE_DROP_ITEMS_PA) {
                    rate = baseRate * playerRate * mod;
                } else {
                    rate = baseRate * mod;
                }
                ChanceRate = playerRateChance;
            }
        }
        double raiteChance = _chance * ChanceRate;
        if (raiteChance > RewardList.MAX_CHANCE) raiteChance = RewardList.MAX_CHANCE;
        double mult = Math.ceil(rate);
        List<RewardItem> ret = new ArrayList<RewardItem>(_items.size() * 3 / 2);
        for (long n = 0; n < mult; n++) {
            double gmult = rate - n;
            if (Rnd.get(1, RewardList.MAX_CHANCE) <= raiteChance * Math.min(gmult, 1.0)) {
                rollFinal(_items, ret, 1.0, ChanceRate);
            }
        }
        return ret;
    }

    public List<RewardItem> rollSpoil(double mod, double baseRate, double playerRate, double playerRateChance) {
        if (mod <= 0) {
            return Collections.emptyList();
        }

        double ChanceRate;
        double rate;
        if (_notRate) {
            rate = Math.min(mod, 1.0);
            ChanceRate = Math.min(mod, 1.0);
        } else {
            if (_isRaid) {
                rate = baseRate;
                ChanceRate = 1.;
            } else {
                if (Config.RATE_SPOIL_ITEMS_PA) {
                    rate = baseRate * playerRate * mod;
                } else {
                    rate = baseRate * mod;
                }
                ChanceRate = playerRateChance;
            }
        }

        double raiteChance = _chance * ChanceRate;
        if (raiteChance > RewardList.MAX_CHANCE) raiteChance = RewardList.MAX_CHANCE;
        double mult = Math.ceil(rate);

        List<RewardItem> ret = new ArrayList<RewardItem>(_items.size() * 3 / 2);
        for (long n = 0; n < mult; n++) {
            double gmult = rate - n;
            if (Rnd.get(1, RewardList.MAX_CHANCE) <= raiteChance * Math.min(gmult, 1.0)) {
                rollFinal(_items, ret, 1.0, ChanceRate);
            }
        }
        return ret;
    }

    private List<RewardItem> rollAdena(double mod, double baseRate, double playerRate, double playerRateChance) {
        double chance = _chance;
        if (mod > 10) {
            mod *= _chance / RewardList.MAX_CHANCE;
            chance = RewardList.MAX_CHANCE;
        }

        if (mod <= 0) {
            return Collections.emptyList();
        }

        double raiteChance = chance * playerRateChance;
        if (raiteChance > RewardList.MAX_CHANCE) raiteChance = RewardList.MAX_CHANCE;
        if (Rnd.get(1, RewardList.MAX_CHANCE) > raiteChance) {
            return Collections.emptyList();
        }

        double rate = baseRate * playerRate * mod;

        List<RewardItem> ret = new ArrayList<RewardItem>(_items.size());
        rollFinal(_items, ret, rate, playerRateChance);
        for (RewardItem i : ret) {
            i.isAdena = true;
        }

        return ret;
    }

    private void rollFinal(List<RewardData> items, List<RewardItem> ret, double mult, double chanceRate) {
        // перебираем все вещи в группе и проверяем шанс
        int chance = Rnd.get(0, RewardList.MAX_CHANCE);
        long count, max;

        for (RewardData i : items) {
            double preChanceInGroup = (chanceRate > 1.)? i.getChanceInGroup() + (i.getChance() * (chanceRate - 1.)): i.getChanceInGroup();
            double ChanceInGroup = (preChanceInGroup > RewardList.MAX_CHANCE)? RewardList.MAX_CHANCE: preChanceInGroup;
            if (chance <= ChanceInGroup && chance >= ChanceInGroup - (i.getChance() * chanceRate)) { // умножаем шансы на шанс райт игрока
                double imult;
                if (Config.ALT_DROP_RATE) {
                    if (i.notRate()) {
                        imult = 1.0;
                    } else {
                        if (isAdena()) {
                            imult = mult;
                        } else {
                            imult = Rnd.get(1, Math.round(mult));
                        }
                    }
                } else {
                    imult = i.notRate() ? 1.0 : mult;
                }

                count = (long)Math.floor(i.getMinDrop() * imult);
                max = (long)Math.ceil(i.getMaxDrop() * imult);
                if (count != max)
                    count = Rnd.get(count, max);

                RewardItem t = null;

                for (RewardItem r : ret) {
                    if (i.getItemId() == r.itemId) {
                        t = r;
                        break;
                    }
                }

                if (t == null) {
                    ret.add(t = new RewardItem(i.getItemId()));
                    t.count = count;
                } else if (!i.notRate()) {
                    t.count = SafeMath.addAndLimit(t.count, count);
                }

                break;
            }
        }
    }
}