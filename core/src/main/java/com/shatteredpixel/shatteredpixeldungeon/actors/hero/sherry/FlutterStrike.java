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

//팔랑 팔랑~: 회피 후 다음 공격이 반드시 명중하고 피해 +10/25/40%
public class FlutterStrike extends Buff {

	{
		type = buffType.POSITIVE;
	}

	public int level = 0;

	public void set( int level ){
		this.level = Math.max(this.level, level);
	}

	public float damageFactor(){
		switch (level){
			case 1: return 1.10f;
			case 2: return 1.25f;
			case 3: return 1.40f;
		}
		return 1f;
	}

	@Override
	public int icon() {
		return BuffIndicator.MOMENTUM; //TODO 전용 아이콘
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(0.6f, 0.9f, 1f);
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", Math.round((damageFactor()-1f)*100));
	}

	private static final String LEVEL = "level";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(LEVEL, level);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		level = bundle.getInt(LEVEL);
	}
}
