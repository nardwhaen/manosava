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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.sherry;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

//마녀화: 받는 피해 30% 감소, 주는 피해 40% 증가. 남은 시간은 최대 20턴
public class WitchForm extends Buff {

	{
		type = buffType.POSITIVE;
		announced = true;
	}

	public static final float MAX_TURNS = 20f;

	private float left = 0;

	public void set( float turns ){
		left = Math.min(MAX_TURNS, Math.max(left, turns));
	}

	//마녀화 중 처치 시 시간 연장 (남은 시간은 20턴을 넘지 않음)
	public void extend( float turns ){
		left = Math.min(MAX_TURNS, left + turns);
		BuffIndicator.refreshHero();
	}

	@Override
	public boolean act() {
		left -= TICK;
		if (left <= 0){
			detach();
		} else {
			spend(TICK);
		}
		return true;
	}

	@Override
	public void detach() {
		super.detach();
		if (target != null && target.buff(WitchPower.class) != null){
			target.buff(WitchPower.class).gain(0, true); //행동 버튼 갱신
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.BERSERK; //TODO 전용 아이콘
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(0.7f, 0.2f, 1f);
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (MAX_TURNS - left) / MAX_TURNS);
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString((int)Math.ceil(left));
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", dispTurns(left));
	}

	private static final String LEFT = "left";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(LEFT, left);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		left = bundle.getFloat(LEFT);
	}
}
