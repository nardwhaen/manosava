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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.sherry.CulpritMark;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

//갑옷 능력 3: 범인은 당신이에요!
//보이는 적 하나를 범인으로 지목. 15턴 동안 셰리에게 받는 피해 +30%, 투명·은신 불가.
//범인이 죽으면 충전 15 반환 (Sherry.onMobDeath)
public class Culprit extends ArmorAbility {

	{
		baseChargeUse = 50f;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	public int targetedPos(Char user, int dst) {
		return dst;
	}

	@Override
	public float chargeUse( Hero hero ) {
		float chargeUse = super.chargeUse(hero);
		//어라라? 아니었나 봐요!: 범인 지목이 유지되는 동안 새로 지목하면 30/50/65/75% 적게 소모
		if (hero.hasTalent(Talent.OOPS_WRONG) && CulpritMark.current() != null){
			switch (hero.pointsInTalent(Talent.OOPS_WRONG)){
				case 1: chargeUse *= 0.70f; break;
				case 2: chargeUse *= 0.50f; break;
				case 3: chargeUse *= 0.35f; break;
				case 4: chargeUse *= 0.25f; break;
			}
		}
		return chargeUse;
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		if (target == null){
			return;
		}

		Char ch = Actor.findChar(target);

		if (ch == null || !Dungeon.level.heroFOV[target]){
			GLog.w(Messages.get(this, "no_target"));
			return;
		} else if (ch.alignment != Char.Alignment.ENEMY){
			GLog.w(Messages.get(this, "ally_target"));
			return;
		}

		//비용은 이전 지목을 풀기 전에 계산 (어라라? 아니었나 봐요!)
		float cost = chargeUse(hero);

		CulpritMark.clearOthers(ch);
		CulpritMark mark = Buff.affect(ch, CulpritMark.class);
		mark.reset();

		armor.charge -= cost;
		armor.updateQuickslot();

		//TODO 이펙트: 범인 지목 연출 (아트 작업 때 추가)
		hero.sprite.zap(target);

		//지목은 턴을 소모하지 않음
		hero.next();
	}

	@Override
	public int icon() {
		return HeroIcon.DEATH_MARK; //TODO 전용 아이콘
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.GREAT_DETECTIVE, Talent.ACCOMPLICE, Talent.OOPS_WRONG, Talent.HEROIC_ENERGY};
	}
}
