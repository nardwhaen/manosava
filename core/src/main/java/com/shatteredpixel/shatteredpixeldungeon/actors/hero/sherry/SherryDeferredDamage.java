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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

//고장 난 위기 경보기: 셰리 전용 지연 피해. 매 턴 남은 양의 20%(최소 1)씩 들어옴
//원본 점성 상형문자(10%씩)와 풀이 섞이지 않도록 별도 버프로 둠
public class SherryDeferredDamage extends Buff implements Buff.DOTbuff {

	{
		type = buffType.NEGATIVE;
	}

	protected int damage = 0;

	public void extend( int amount ){
		if (this.damage == 0){
			//처음 붙었을 때는 1턴 뒤부터 피해
			postpone(TICK);
		}
		this.damage += amount;
		if (target != null) target.needsIncomingDOTUpdate = true;
	}

	@Override
	public int icon() {
		return BuffIndicator.DEFERRED;
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(1f, 0.4f, 0.4f);
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString(damage);
	}

	@Override
	public boolean act() {
		if (target.isAlive()) {

			int tick = Math.max(1, (int)(damage * 0.2f));
			damage -= tick;

			//마녀화 중이면 틱 피해 30% 감소 (풀에서는 원래 틱만큼 빠짐)
			int dealt = tick;
			if (target.buff(WitchForm.class) != null){
				dealt = Math.round(tick * 0.7f);
			}
			if (dealt > 0) {
				target.damage(dealt, this);
			}

			if (target == Dungeon.hero && !target.isAlive()) {
				Dungeon.fail( this );
				GLog.n( Messages.get(this, "ondeath") );
			}
			spend( TICK );

			if (damage <= 0) {
				detach();
			}

		} else {
			detach();
		}

		if (target != null) target.needsIncomingDOTUpdate = true;
		return true;
	}

	@Override
	public void detach() {
		if (target != null) target.needsIncomingDOTUpdate = true;
		super.detach();
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", damage);
	}

	@Override
	public int totalIncomingDMG() {
		return damage;
	}

	private static final String DAMAGE = "damage";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( DAMAGE, damage );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		damage = bundle.getInt( DAMAGE );
	}
}
