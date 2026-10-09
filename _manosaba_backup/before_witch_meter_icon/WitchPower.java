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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.utils.Bundle;

//서브클래스 B 마녀화라는 건 [이런] 거군요.: 마녀 수치와 마녀화 발동
public class WitchPower extends Buff implements ActionIndicator.Action {

	{
		type = buffType.POSITIVE;
		revivePersists = true;
	}

	public static final float MAX = 100f;

	private float points = 0;

	public float points(){
		return points;
	}

	public boolean isFull(){
		return points >= MAX;
	}

	public void gain( float amount, boolean ignoreActive ){
		if (!ignoreActive && target.buff(WitchForm.class) != null) return;
		points = Math.min(MAX, points + amount);
		refreshAction();
		BuffIndicator.refreshHero();
	}

	@Override
	public boolean act() {
		//매 턴 마녀 수치 +1 (마녀화 중에는 오르지 않음)
		//테스트 후 너무 강하면 "적이 보일 때만 +1"로 바꾸기로 함 (노션 테스트 메모)
		gain(1f, false);
		spend(TICK);
		return true;
	}

	private void refreshAction(){
		if (isFull() && target.buff(WitchForm.class) == null){
			ActionIndicator.setAction(this);
		} else {
			ActionIndicator.clearAction(this);
		}
	}

	@Override
	public void detach() {
		super.detach();
		ActionIndicator.clearAction(this);
	}

	public void activate(){
		if (!isFull()){
			GLog.w(Messages.get(this, "not_enough"));
			return;
		}
		points = 0; //..그래도 너는 인간이야 의 힘 보너스도 여기서 사라짐 (의도된 양자택일)

		Buff.affect(target, WitchForm.class).set(WitchForm.MAX_TURNS);

		//역시 인간이 아니네요 저..: 보이는 모든 적에게 취약 3/6/9턴
		if (target instanceof Hero && ((Hero) target).hasTalent(Talent.NOT_HUMAN)){
			int turns = 3 * ((Hero) target).pointsInTalent(Talent.NOT_HUMAN);
			for (Char ch : Actor.chars()){
				if (ch.alignment == Char.Alignment.ENEMY && Dungeon.level.heroFOV[ch.pos]){
					Buff.prolong(ch, Vulnerable.class, turns);
				}
			}
		}

		//TODO 이펙트: 마녀화 발동 연출 (아트 작업 때 추가)
		target.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(this, "activated"));

		ActionIndicator.clearAction(this);
		BuffIndicator.refreshHero();
	}

	@Override
	public String actionName() {
		return Messages.get(this, "action_name");
	}

	@Override
	public int actionIcon() {
		return HeroIcon.SHERRY_WITCH; //[Manosaba] 마녀화 버튼: 마녀화한 셰리의 눈 (보조직업 아이콘과 같은 그림)
	}

	@Override
	public Visual secondaryVisual() {
		BitmapText txt = new BitmapText(PixelScene.pixelFont);
		txt.text((int)points + "%");
		txt.hardlight(CharSprite.NEGATIVE);
		txt.measure();
		return txt;
	}

	@Override
	public int indicatorColor() {
		return 0x550066;
	}

	@Override
	public void doAction() {
		activate();
	}

	@Override
	public int icon() {
		return target.buff(WitchForm.class) == null ? BuffIndicator.RAGE : BuffIndicator.NONE; //TODO 전용 아이콘
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(0.7f, 0.2f, 1f);
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, 1f - points / MAX);
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString((int)points);
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", (int)points);
	}

	private static final String POINTS = "points";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(POINTS, points);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		points = bundle.getFloat(POINTS);
	}
}
