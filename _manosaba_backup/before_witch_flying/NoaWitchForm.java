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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

/**
 * 노아의 마녀화 (20턴).
 * 기본: 받는 피해 10% 감소, 시야 안의 물 위의 적에게 매 턴 고정 피해.
 * 마녀화: 액체 조종 마법(서브클래스 B): 받는 피해 20% 감소, 고정 피해 증가,
 * 물 위의 모든 캐릭터에게 매 턴 페인트 통 효과, 매 턴 주위를 물로 적심.
 */
public class NoaWitchForm extends Buff {

	{
		type = buffType.POSITIVE;
		announced = true;
	}

	public static final float TURNS = 20f;

	private float left = 0;

	public void set( float turns ){
		left = Math.max(left, turns);
	}

	public boolean enhanced(){
		return target instanceof Hero && ((Hero) target).subClass == HeroSubClass.NOA_WITCH;
	}

	//기본 마녀화 피해: 1 + (레벨-1)/4
	public static int baseDamage( Hero hero ){
		return 1 + (hero.lvl - 1) / 4;
	}

	//서브클래스 B 추가 피해: max(1, 1 + (레벨-15)/2)
	public static int bonusDamage( Hero hero ){
		return Math.max(1, 1 + (hero.lvl - 15) / 2);
	}

	public int damage(){
		Hero hero = (Hero) target;
		return baseDamage(hero) + (enhanced() ? bonusDamage(hero) : 0);
	}

	@Override
	public boolean act() {
		Hero hero = (Hero) target;

		if (enhanced()){
			//매 턴 주위 3x3(넘쳐흐르는 물감: 5x5/7x7/9x9)을 물로 (낭떠러지는 20턴)
			int r = 1 + hero.pointsInTalent(Talent.OVERFLOWING_PAINT);
			int w = Dungeon.level.width();
			int px = hero.pos % w, py = hero.pos / w;
			for (int y = py - r; y <= py + r; y++){
				for (int x = px - r; x <= px + r; x++){
					if (x < 0 || y < 0 || x >= w || y >= Dungeon.level.height()) continue;
					Noa.wetCell(x + y*w, 20);
				}
			}
		}

		int dmg = damage();
		ArrayList<Char> chars = new ArrayList<>(Actor.chars());
		for (Char ch : chars){
			if (!ch.isAlive() || !Noa.onWater(ch)) continue;
			if (ch.alignment == Char.Alignment.ENEMY && Dungeon.level.heroFOV[ch.pos]){
				NoaWaterSpike.hit(ch); //[Manosaba] 물줄기가 아래에서 찌르는 연출
				ch.damage(dmg, this);
				if (enhanced() && ch.isAlive()){
					Noa.paintEnemy(hero, ch, 2f);
				}
			} else if (enhanced()){
				if (ch == hero){
					Noa.giveShield(hero, 10, true);
				} else if (ch.alignment == Char.Alignment.ALLY){
					Noa.paintAlly(hero, ch, 10, 10, true);
				}
			}
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
	public void detach() {
		super.detach();
		if (target != null && target.buff(NoaWitchPower.class) != null){
			target.buff(NoaWitchPower.class).refreshAction();
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.BERSERK; //TODO 노아 전용 아이콘
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(0.3f, 0.6f, 1f);
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (TURNS - left) / TURNS);
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString((int)Math.ceil(left));
	}

	@Override
	public String name() {
		return Messages.get(this, enhanced() ? "name_enhanced" : "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, enhanced() ? "desc_enhanced" : "desc", damage(), dispTurns(left));
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("left", left);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		left = bundle.getFloat("left");
	}
}
