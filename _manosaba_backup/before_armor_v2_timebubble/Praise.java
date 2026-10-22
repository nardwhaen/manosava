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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.sherry;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

//갑옷 능력 1: 셰리짱귀여워사랑해잘했어대단해천재야!
//듣고 싶었던 칭찬을 떠올려 아드레날린을 얻는다
public class Praise extends ArmorAbility {

	{
		baseChargeUse = 50f; //테스트 후 너무 강하면 60으로 (노션 테스트 메모)
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {

		//한나 씨의 칭찬: 20턴 → 25/30/35/40턴
		float duration = 20f + 5f*hero.pointsInTalent(Talent.HANNA_PRAISE);
		Buff.prolong(hero, Adrenaline.class, duration);

		//칭찬받았어요!: 시간 방울 3/5/7/10턴
		int bubble = 0;
		switch (hero.pointsInTalent(Talent.GOT_PRAISED)){
			case 1: bubble = 3; break;
			case 2: bubble = 5; break;
			case 3: bubble = 7; break;
			case 4: bubble = 10; break;
		}
		if (bubble > 0){
			Buff.affect(hero, Swiftthistle.TimeBubble.class).reset(bubble);
		}

		//TODO 이펙트: 칭찬을 떠올리는 연출 (아트 작업 때 추가)
		hero.sprite.operate(hero.pos);

		armor.charge -= chargeUse(hero);
		armor.updateQuickslot();
		Invisibility.dispel();
		hero.spendAndNext(1f);
	}

	@Override
	public int icon() {
		return HeroIcon.ENDURE; //TODO 전용 아이콘
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.HANNA_PRAISE, Talent.GOT_PRAISED, Talent.PRAISE_MORE, Talent.HEROIC_ENERGY};
	}
}
