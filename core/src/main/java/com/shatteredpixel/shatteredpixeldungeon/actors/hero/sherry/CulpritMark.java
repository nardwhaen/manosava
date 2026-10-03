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

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

//범인은 당신이에요!: 지목된 적. 15턴 동안 셰리에게 받는 피해 증가, 투명·은신 불가
public class CulpritMark extends Buff {

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	public static final float DURATION = 15f;

	private float left = DURATION;

	public void reset(){
		left = DURATION;
	}

	//현재 범인으로 지목된 적이 있는지 (어라라? 아니었나 봐요!)
	public static Char current(){
		for (Char ch : Actor.chars()){
			if (ch.buff(CulpritMark.class) != null){
				return ch;
			}
		}
		return null;
	}

	//범인은 한 번에 하나만: 다른 지목은 풀기
	public static void clearOthers( Char except ){
		for (Char ch : Actor.chars().toArray(new Char[0])){
			if (ch != except && ch.buff(CulpritMark.class) != null){
				ch.buff(CulpritMark.class).detach();
			}
		}
	}

	@Override
	public boolean attachTo(Char target) {
		if (super.attachTo(target)){
			Buff.detach(target, Invisibility.class);
			return true;
		}
		return false;
	}

	@Override
	public boolean act() {
		//숨을 수 없음
		if (target.buff(Invisibility.class) != null){
			Buff.detach(target, Invisibility.class);
		}
		left -= TICK;
		if (left <= 0){
			detach();
		} else {
			spend(TICK);
		}
		return true;
	}

	@Override
	public int icon() {
		return BuffIndicator.MARK; //TODO 전용 아이콘
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(1f, 0.8f, 0.2f);
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - left) / DURATION);
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
