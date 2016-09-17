package l2p.gameserver.data.xml.parser;

import l2p.commons.collections.MultiValueSet;
import l2p.commons.data.xml.AbstractFileParser;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.FishDataHolder;
import l2p.gameserver.templates.item.support.FishGroup;
import l2p.gameserver.templates.item.support.FishTemplate;
import l2p.gameserver.templates.item.support.LureTemplate;
import l2p.gameserver.templates.item.support.LureType;
import org.dom4j.Attribute;
import org.dom4j.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @date 8:34/19.07.2011
 */
public class FishDataParser extends AbstractFileParser<FishDataHolder> {

    private static final FishDataParser _instance = new FishDataParser();

    public static FishDataParser getInstance() {
        return _instance;
    }

    private FishDataParser() {
        super(FishDataHolder.getInstance());
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/other/fishdata.xml");
    }

    @Override
    public String getDTDFileName() {
        return "fishdata.dtd";
    }

    @Override
    protected void readData(Element rootElement) throws Exception {
        for (Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();) {
            Element e = iterator.next();
            if ("fish".equals(e.getName())) {
                MultiValueSet<String> fish = new MultiValueSet<>();
                List<MultiValueSet<String>> rewards = new ArrayList<>();

                for (Iterator<Attribute> attributeIterator = e.attributeIterator(); attributeIterator.hasNext();) {
                    Attribute attribute = attributeIterator.next();
                    fish.put(attribute.getName(), attribute.getValue());
                }

                for (Iterator<Element> iteratorFish = e.elementIterator(); iteratorFish.hasNext();) {
                    Element elFish = iteratorFish.next();

                    if ("set".equals(elFish.getName())) {
                        fish.put(elFish.attributeValue("name"), elFish.attributeValue("value"));
                    } else if ("reward".equals(elFish.getName())) {
                        MultiValueSet<String> reward = new MultiValueSet<>();
                        for (Iterator<Attribute> attributeIterator = elFish.attributeIterator(); attributeIterator.hasNext();) {
                            Attribute attribute = attributeIterator.next();
                            reward.put(attribute.getName(), attribute.getValue());
                        }
                        rewards.add(reward);
                    }
                }

                getHolder().addFish(new FishTemplate(fish, rewards));
            } else if ("lure".equals(e.getName())) {
                MultiValueSet<String> map = new MultiValueSet<>();
                for (Iterator<Attribute> attributeIterator = e.attributeIterator(); attributeIterator.hasNext();) {
                    Attribute attribute = attributeIterator.next();
                    map.put(attribute.getName(), attribute.getValue());
                }

                Map<FishGroup, Integer> chances = new HashMap<>();
                for (Iterator<Element> elementIterator = e.elementIterator(); elementIterator.hasNext();) {
                    Element chanceElement = elementIterator.next();
                    chances.put(FishGroup.valueOf(chanceElement.attributeValue("type")), Integer.parseInt(chanceElement.attributeValue("value")));
                }
                map.put("chances", chances);
                getHolder().addLure(new LureTemplate(map));
            } else if ("distribution".equals(e.getName())) {
                int id = Integer.parseInt(e.attributeValue("id"));

                for (Iterator<Element> forLureIterator = e.elementIterator(); forLureIterator.hasNext();) {
                    Element forLureElement = forLureIterator.next();

                    LureType lureType = LureType.valueOf(forLureElement.attributeValue("type"));
                    Map<FishGroup, Integer> chances = new HashMap<>();

                    for (Iterator<Element> chanceIterator = forLureElement.elementIterator(); chanceIterator.hasNext();) {
                        Element chanceElement = chanceIterator.next();
                        chances.put(FishGroup.valueOf(chanceElement.attributeValue("type")), Integer.parseInt(chanceElement.attributeValue("value")));
                    }
                    getHolder().addDistribution(id, lureType, chances);
                }
            }
        }
    }
}