package l2p.gameserver.data.xml.holder;

import l2p.commons.data.xml.AbstractHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.events.EventType;
import l2p.gameserver.model.entity.events.GlobalEvent;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.TreeIntObjectMap;

public final class EventHolder extends AbstractHolder {

    private static final EventHolder _instance = new EventHolder();
    private final IntObjectMap<GlobalEvent> _events = new TreeIntObjectMap<>();

    public static EventHolder getInstance() {
        return _instance;
    }

    public void addEvent(EventType type, GlobalEvent event) {
        _events.put(type.step() + event.getId(), event);
    }

    @SuppressWarnings("unchecked")
    public <E extends GlobalEvent> E getEvent(EventType type, int id) {
        return (E) _events.get(type.step() + id);
    }

    public void findEvent(Player player) {
        _events.values().stream().filter(event -> event.isParticle(player)).forEach(player::addEvent);
    }

    public void callInit() {
        _events.values().forEach(GlobalEvent::initEvent);
    }

    @Override
    public int size() {
        return _events.size();
    }

    @Override
    public void clear() {
        _events.clear();
    }
}
