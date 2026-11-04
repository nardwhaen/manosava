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


package com.shatteredpixel.shatteredpixeldungeon.actors.hero.noa;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

/**
 * 들키고 싶지 않은 원죄: 페인트 통 효과를 받은 적의 명중 -15/25/35% (느려짐과 같은 시간)
 */
public class PaintBlind extends FlavourBuff {

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	public int level = 1;

	public float accuracyFactor(){
		switch (level){
			case 1: default: return 0.85f;
			case 2: return 0.75f;
			case 3: return 0.65f;
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.BLINDNESS;
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(1f, 0.5f, 0.8f);
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (10f - visualcooldown()) / 10f);
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", Math.round((1f - accuracyFactor())*100), dispTurns());
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("level", level);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		level = bundle.getInt("level");
	}
}
