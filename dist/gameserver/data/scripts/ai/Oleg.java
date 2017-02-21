package ai;

import l2p.commons.util.Rnd;
import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.ai.Guard;
import l2p.gameserver.model.AggroList;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Playable;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.scripts.Functions;
import scriptConfig.ScriptConfig;

public class Oleg extends Guard implements Runnable {

    private boolean _crazyState;
    private long _lastAggroSay;
    private long _lastNormalSay;
    private static final int _crazyChance = ScriptConfig.getInt("TalkGuardChance");
    private static final int _sayNormalChance = ScriptConfig.getInt("TalkNormalChance");
    private static final long _sayNormalPeriod = ScriptConfig.getInt("TalkNormalPeriod") * 6000;
    private static final long _sayAggroPeriod = ScriptConfig.getInt("TalkAggroPeriod") * 6000;
    // Фразы, которые может произнести гвард, когда начинает атаковать пк
    private static final String[] _sayAggroText = {
        "{name}, опа опа опапа -1:))",
        "{name}, ну я же предупреждал, не убивай людей:)",
		"Опачки ещё один -1",
		"Я хороший отмыватель, если что приходи еще.",
		"{name}, ты что не набегался ещё?",
		"{name}, и ты туда же..",
		"Ну я же говорил, *НЕНАДО* ",
		"Всех застрелякаю",
        "{name}, оп оп не пройдешь:)"};
    // Фразы, которые может произнести гвард, адресуя их проходящим мимо игрокам мужского пола
    private static final String[] _sayNormalTextM = {
        "{name}, Харе играть, иди передохни.",
        "{name}, будеш плохо себя вести, по щам надаю, я умею )",
		"Друг стой:) поговори хоть ты со мной...",
		"Раз, два, три, четыре, пять... надо денежки искать, а то прийдут плохие дяди и настучат мне по ...",
		"{name}, если на ПК нарвешся, беги ко мне помогу:)",
		"{name}, у тебя случаем стрел нет? А то у меня на исходе.",
		"А ты голосовал сегодня за сервер:?",
		"Пс... эй... товарищ сгоняй за пивком:) А то меня начальство не отпускает(",
        "{name}, посиди со мной, ппц как скучно тут стоять."};
    // Фразы, которые может произнести гвард, адресуя их проходящим мимо игрокам женского пола
    private static final String[] _sayNormalTextF = {
        "{name}, давай сфоткаемся?",
        "{name}, будеш шалить - накажу по взрослому!",
		"Вот это формы О_о",
		"Девушка... подскажите который час.",
		"{name}, все бегают, бегают, поговори хоть ты со мной:)",
		"{name}, угадай откуда я знаю твое имя?:)",
		"Распрдажа, распродажа... Живое мясо...",
		"Я за пивом, кто со мной:?",
        "{name}, стой! Ты туда не ходи, ты сюда иди:)"};

    public Oleg(NpcInstance actor) {
        super(actor);
        MAX_PURSUE_RANGE = 600;
        _crazyState = false;
        _lastAggroSay = 0;
        _lastNormalSay = 0;
    }

    @Override
    protected void onEvtSpawn() {
        _lastAggroSay = 0;
        _lastNormalSay = 0;
        _crazyState = Rnd.chance(_crazyChance) ? true : false;
        super.onEvtSpawn();
    }

    @Override
    public boolean checkAggression(Creature target) {
        if (_crazyState) {
            NpcInstance actor = getActor();
            Player player = target.getPlayer();
            if (actor == null || actor.isDead() || player == null) {
                return false;
            }
            if (player.isGM()) {
                return false;
            }
            if (Rnd.chance(_sayNormalChance)) {
                if (target.isPlayer() && target.getKarma() <= 0 && (_lastNormalSay + _sayNormalPeriod < System.currentTimeMillis()) && actor.isInRange(target, 250L)) {
                    Functions.npcSay(actor, target.getPlayer().getSex() == 0 ? _sayNormalTextM[Rnd.get(_sayNormalTextM.length)].replace("{name}", target.getName()) : _sayNormalTextF[Rnd.get(_sayNormalTextF.length)].replace("{name}", target.getName()));
                    _lastNormalSay = System.currentTimeMillis();
                }
            }
            if (target.getKarma() <= 0) {
                return false;
            }
            if (getIntention() != CtrlIntention.AI_INTENTION_ACTIVE) {
                return false;
            }
            if (_globalAggro < 0L) {
                return false;
            }
            AggroList.AggroInfo ai = actor.getAggroList().get(target);
            if (ai != null && ai.hate > 0) {
                if (!target.isInRangeZ(actor.getSpawnedLoc(), MAX_PURSUE_RANGE)) {
                    return false;
                }
            } else if (!target.isInRangeZ(actor.getSpawnedLoc(), 600)) {
                return false;
            }
            if (target.isPlayable() && !canSeeInSilentMove((Playable) target)) {
                return false;
            }
            if (!GeoEngine.canSeeTarget(actor, target, false)) {
                return false;
            }
            if (target.isPlayer() && ((Player) target).isInvisible()) {
                return false;
            }
            if ((target.isSummon() || target.isPet()) && target.getPlayer() != null) {
                actor.getAggroList().addDamageHate(target.getPlayer(), 0, 1);
            }
            actor.getAggroList().addDamageHate(target, 0, 2);
            startRunningTask(2000);
            if (_lastAggroSay + _sayAggroPeriod < System.currentTimeMillis()) {
                Functions.npcSay(actor, _sayAggroText[Rnd.get(_sayAggroText.length)].replace("{name}", target.getPlayer().getName()));
                _lastAggroSay = System.currentTimeMillis();
            }

            setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
            return true;
        } else {
            super.checkAggression(target);
        }
        return false;
    }
}