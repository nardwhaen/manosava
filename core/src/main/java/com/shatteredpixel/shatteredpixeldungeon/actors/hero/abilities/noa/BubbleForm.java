/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */


package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.noa;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.noa.Noa;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;

/**
 * 물거품이 되어: 물 위에서만 사용. 켜져 있는 동안 물 위에 있으면 해로운 상태이상 면역,
 * 켤 때 걸려 있던 해로운 상태이상 해제. 켤 때 20, 유지 매 턴 4.
 * 물을 벗어나거나, 다시 쓰거나, 충전이 모자라면 꺼짐. 사용 중 일반 공격·마법 막대·투척 무기 불가.
 */
public class BubbleForm extends ArmorAbility {

	{
		baseChargeUse = 20f;
	}

	public static final float UPKEEP = 4f;

	@Override
	public float chargeUse( Hero hero ) {
		if (hero.buff(BubbleBuff.class) != null) return 0; //끄기는 충전 없이
		return super.chargeUse(hero);
	}

	@Override
	protected void activate( ClassArmor armor, Hero hero, Integer target ) {
		BubbleBuff active = hero.buff(BubbleBuff.class);
		if (active != null){
			active.detach();
			hero.sprite.operate(hero.pos);
			hero.spendAndNext(1f);
			return;
		}
		if (!Noa.onWater(hero)){
			GLog.w(Messages.get(this, "need_water"));
			return;
		}
		armor.charge -= chargeUse(hero);
		armor.updateQuickslot();

		PotionOfCleansing.cleanse(hero, 0f);
		Buff.affect(hero, BubbleBuff.class);
		hero.sprite.emitter().burst(Speck.factory(Speck.BUBBLE), 12);
		hero.sprite.operate(hero.pos);
		hero.spendAndNext(1f);
	}

	@Override
	public int icon() {
		return HeroIcon.ENDURE; //TODO 전용 아이콘
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.UNCATCHABLE_DROP, Talent.NOA_CAN_FIGHT, Talent.WATER_PROTECTS, Talent.HEROIC_ENERGY};
	}

	//물 위에서 물거품 상태인지 (해로운 상태이상 면역 판정용)
	public static boolean blocksDebuffs( Char ch ){
		return ch.buff(BubbleBuff.class) != null && Noa.onWater(ch);
	}

	public static class BubbleBuff extends Buff {

		{
			type = buffType.POSITIVE;
			announced = true;
		}

		@Override
		public boolean act() {
			Hero hero = (Hero) target;
			ClassArmor armor = hero.belongings.armor() instanceof ClassArmor ? (ClassArmor) hero.belongings.armor() : null;
			if (!Noa.onWater(hero) || armor == null || armor.charge < UPKEEP){
				detach();
				return true;
			}
			armor.charge -= UPKEEP;
			armor.updateQuickslot();

			//물이 노아를 지켜줘~: +1 신속, +2 축복, +3 투명, +4 매 턴 체력 1 (매 턴 1턴짜리로 다시 걸어서 꺼지면 바로 사라짐)
			int lvl = hero.pointsInTalent(Talent.WATER_PROTECTS);
			if (lvl >= 1) Buff.prolong(hero, Haste.class, 1.01f);
			if (lvl >= 2) Buff.prolong(hero, Bless.class, 1.01f);
			if (lvl >= 3) Buff.prolong(hero, Invisibility.class, 1.01f);
			if (lvl >= 4 && hero.HP < hero.HT){
				hero.HP++;
				hero.sprite.showStatus(CharSprite.POSITIVE, "1");
			}

			if (target.sprite != null) target.sprite.emitter().burst(Speck.factory(Speck.BUBBLE), 2);
			spend(TICK);
			return true;
		}

		@Override
		public void detach() {
			super.detach();
			//1턴짜리 버프 정리
			if (target.buff(Haste.class) != null && target.buff(Haste.class).cooldown() <= 1.01f) target.buff(Haste.class).detach();
			if (target.buff(Bless.class) != null && target.buff(Bless.class).cooldown() <= 1.01f) target.buff(Bless.class).detach();
			if (target.buff(Invisibility.class) != null && target.buff(Invisibility.class).cooldown() <= 1.01f) target.buff(Invisibility.class).detach();
			if (target instanceof Hero && Dungeon.hero == target) GLog.i(Messages.get(BubbleForm.class, "ended"));
		}

		@Override
		public int icon() {
			return BuffIndicator.IMMUNITY;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.4f, 0.8f, 1f);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc");
		}
	}
}
