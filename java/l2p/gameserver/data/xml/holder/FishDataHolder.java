package l2p.gameserver.data.xml.holder;

import l2p.commons.data.xml.AbstractHolder;
import l2p.gameserver.model.reward.RewardData;
import l2p.gameserver.templates.item.support.FishGrade;
import l2p.gameserver.templates.item.support.FishGroup;
import l2p.gameserver.templates.item.support.FishTemplate;
import l2p.gameserver.templates.item.support.LureTemplate;
import l2p.gameserver.templates.item.support.LureType;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author VISTALL
 * @date 8:31/19.07.2011
 */
public class FishDataHolder extends AbstractHolder {

    private static final FishDataHolder _instance = new FishDataHolder();
    private IntObjectMap<FishTemplate> _fishes = new HashIntObjectMap<>();
    private IntObjectMap<LureTemplate> _lures = new HashIntObjectMap<>();
    private IntObjectMap<Map<LureType, Map<FishGroup, Integer>>> _distributionsForZones = new HashIntObjectMap<>();

    public static FishDataHolder getInstance() {
        return _instance;
    }

    public void addFish(FishTemplate fishTemplate) {
        _fishes.put(fishTemplate.getItemId(), fishTemplate);
    }

    public void addLure(LureTemplate template) {
        _lures.put(template.getItemId(), template);
    }

    public void addDistribution(int id, LureType lureType, Map<FishGroup, Integer> map) {
        Map<LureType, Map<FishGroup, Integer>> byLureType = _distributionsForZones.get(id);
        if (byLureType == null) {
            _distributionsForZones.put(id, (byLureType = new HashMap<>()));
        }

        byLureType.put(lureType, map);
    }

    public int[] getFishIds() {
        return _fishes.keySet().toArray();
    }

    public List<FishTemplate> getFish(FishGroup group, FishGrade grade, int level) {
        List<FishTemplate> ret = _fishes.values().stream().filter(fish -> fish.getGroup() == group && fish.getGrade() == grade && fish.getLevel() == level).collect(Collectors.toList());

        if (ret.isEmpty()) {
            warn("Not found fish with group: " + group + ", grade: " + grade + ", level: " + level);
        }

        return ret;
    }

    public Collection<RewardData> getFishReward(int fishid) {
        FishTemplate fish = _fishes.get(fishid);

        return fish != null ? fish.getRewards() : new ArrayList<>();
    }

    public LureTemplate getLure(int id) {
        return _lures.get(id);
    }

    public Map<FishGroup, Integer> getDistributionChances(int destributionId, LureType lureType) {
        if (_distributionsForZones.containsKey(destributionId)) {
            if (_distributionsForZones.get(destributionId).containsKey(lureType)) {
                return _distributionsForZones.get(destributionId).get(lureType);
            } else {
                warn("Not found distribution chances for lureType: " + lureType);
            }
        } else {
            warn("Not found destribution chance for destributionId: " + destributionId);
        }

        return new HashMap<>(0);
    }

    @Override
    public void log() {
        info("load " + _fishes.size() + " fish(es).");
        info("load " + _lures.size() + " lure(s).");
        info("load " + _distributionsForZones.size() + " distribution(s).");
    }

    @Deprecated
    @Override
    public int size() {
        return 0;
    }

    @Override
    public void clear() {
        _fishes.clear();
        _lures.clear();
        _distributionsForZones.clear();
    }
}
