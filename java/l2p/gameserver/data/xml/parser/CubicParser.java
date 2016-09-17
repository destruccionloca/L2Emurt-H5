package l2p.gameserver.data.xml.parser;

import gnu.trove.map.hash.TIntIntHashMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import l2p.commons.data.xml.AbstractFileParser;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.CubicHolder;
import l2p.gameserver.model.Skill;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.templates.CubicTemplate;

public final class CubicParser extends AbstractFileParser<CubicHolder> {

    private static CubicParser _instance = new CubicParser();

    public static CubicParser getInstance() {
        return _instance;
    }

    CubicParser() {
        super(CubicHolder.getInstance());
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/other/cubics.xml");
    }

    @Override
    public String getDTDFileName() {
        return "cubics.dtd";
    }

    @Override
    protected void readData(Element rootElement) throws Exception {
        for (Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();) {
            Element cubicElement = iterator.next();
            int id = Integer.parseInt(cubicElement.attributeValue("id"));
            int level = Integer.parseInt(cubicElement.attributeValue("level"));
            int delay = Integer.parseInt(cubicElement.attributeValue("delay"));
            int maxCount = Integer.parseInt(cubicElement.attributeValue("max_count"));
            CubicTemplate template = new CubicTemplate(id, level, delay);
            getHolder().addCubicTemplate(template);

            // skills
            for (Iterator<Element> skillsIterator = cubicElement.elementIterator(); skillsIterator.hasNext();) {
                Element skillsElement = skillsIterator.next();
                int chance = Integer.parseInt(skillsElement.attributeValue("chance"));
                List<CubicTemplate.SkillInfo> skills = new ArrayList<>(1);
                // skill
                for (Iterator<Element> skillIterator = skillsElement.elementIterator(); skillIterator.hasNext();) {
                    Element skillElement = skillIterator.next();
                    int id2 = Integer.parseInt(skillElement.attributeValue("id"));
                    int level2 = Integer.parseInt(skillElement.attributeValue("level"));
                    String val = skillElement.attributeValue("chance");
                    int chance2 = val == null ? 0 : Integer.parseInt(val);
                    boolean canAttackDoor = Boolean.parseBoolean(skillElement.attributeValue("can_attack_door"));
                    val = skillElement.attributeValue("min_hp");
                    int minHp = val == null ? 0 : Integer.parseInt(val);
                    val = skillElement.attributeValue("min_hp_per");
                    int minHpPer = val == null ? 0 : Integer.parseInt(val);
                    CubicTemplate.ActionType type = CubicTemplate.ActionType.valueOf(skillElement.attributeValue("action_type"));

                    TIntIntHashMap set = new TIntIntHashMap();
                    for (Iterator<Element> chanceIterator = skillElement.elementIterator(); chanceIterator.hasNext();) {
                        Element chanceElement = chanceIterator.next();
                        int min = Integer.parseInt(chanceElement.attributeValue("min"));
                        int max = Integer.parseInt(chanceElement.attributeValue("max"));
                        int value = Integer.parseInt(chanceElement.attributeValue("value"));
                        for (int i = min; i <= max; i++) {
                            set.put(i, value);
                        }
                    }

                    if (chance2 == 0 && set.isEmpty()) {
                        warn("Wrong skill chance. Cubic: " + id + "/" + level);
                    }
                    Skill skill = SkillTable.getInstance().getInfo(id2, level2);
                    if (skill != null) {
                        skill.setCubicSkill(true);
                        skills.add(new CubicTemplate.SkillInfo(skill, chance2, type, canAttackDoor, minHp, minHpPer, maxCount, set));
                    }
                }

                template.putSkills(chance, skills);
            }
        }
    }
}
