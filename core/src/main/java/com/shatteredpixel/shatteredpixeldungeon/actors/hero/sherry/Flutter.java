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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation;
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

//서브클래스 A 요정님이에요!: 팔랑임 수치와 요정이에요! 발동
public class Flutter extends Buff implements ActionIndicator.Action {

	{
		type = buffType.POSITIVE;
		revivePersists = true;
	}

	public static final float MAX = 100f;
	public static final float MIN_TO_USE = 20f;

	private float points = 0;

	public float points(){
		return points;
	}

	public void gain( float amount, boolean ignoreActive ){
		if (!ignoreActive && target.buff(FairyForm.class) != null) return;
		points = Math.min(MAX, points + amount);
		refreshAction();
		BuffIndicator.refreshHero();
	}

	//팔랑임 20당 회피 +5% (최대 25%). 회피 수치에 곱해지는 배율
	public float evasionFactor(){
		return 1f + 0.05f * (int)(points / 20f);
	}

	@Override
	public boolean act() {
		//그거 칭찬인가요?: 해로운 상태이상에 걸려 있는 동안 매 턴 팔랑임 1/2/4
		if (target instanceof Hero && ((Hero) target).hasTalent(Talent.IS_THAT_PRAISE)){
			boolean debuffed = false;
			for (Buff b : target.buffs()){
				if (b.type == buffType.NEGATIVE){
					debuffed = true;
					break;
				}
			}
			if (debuffed){
				int pts = ((Hero) target).pointsInTalent(Talent.IS_THAT_PRAISE);
				gain(pts == 3 ? 4 : pts, false);
			}
		}
		refreshAction();
		spend(TICK);
		return true;
	}

	private void refreshAction(){
		if (points >= MIN_TO_USE && target.buff(FairyForm.class) == null){
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
		if (points < MIN_TO_USE){
			GLog.w(Messages.get(this, "not_enough"));
			return;
		}
		float spent = points;
		points = 0;

		int duration = (int)(spent * 0.2f); //소모한 팔랑임 1당 0.2턴, 최대 20턴
		FairyForm form = Buff.affect(target, FairyForm.class, duration);
		form.setBoost(Math.min(0.5f, spent * 0.005f)); //소모한 팔랑임 1당 회피 +0.5%, 최대 50%
		Buff.prolong(target, Haste.class, duration);
		Buff.prolong(target, Levitation.class, duration);

		//TODO 이펙트: 요정이에요! 발동 연출 (아트 작업 때 추가)
		target.sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "activated"));

		ActionIndicator.clearAction(this);
		BuffIndicator.refreshHero();
	}

	@Override
	public String actionName() {
		return Messages.get(this, "action_name");
	}

	@Override
	public int actionIcon() {
		return HeroIcon.GLADIATOR; //TODO 전용 아이콘
	}

	@Override
	public Visual secondaryVisual() {
		BitmapText txt = new BitmapText(PixelScene.pixelFont);
		txt.text(Integer.toString((int)points));
		txt.hardlight(CharSprite.POSITIVE);
		txt.measure();
		return txt;
	}

	@Override
	public int indicatorColor() {
		return 0x4488CC;
	}

	@Override
	public void doAction() {
		activate();
	}

	@Override
	public int icon() {
		return points > 0 ? BuffIndicator.MOMENTUM : BuffIndicator.NONE; //TODO 전용 아이콘
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(0.5f, 0.8f, 1f);
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
		return Messages.get(this, "desc", (int)points, Math.round((evasionFactor()-1f)*100));
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

	//요정이에요! 지속 효과
	public static class FairyForm extends com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff {

		{
			type = buffType.POSITIVE;
			announced = true;
		}

		private float boost = 0;

		public void setBoost( float boost ){
			this.boost = Math.max(this.boost, boost);
		}

		public float evasionFactor(){
			return 1f + boost;
		}

		@Override
		public int icon() {
			return BuffIndicator.LEVITATION; //TODO 전용 아이콘
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.6f, 1f, 1f);
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (20f - visualcooldown()) / 20f);
		}

		@Override
		public void detach() {
			super.detach();
			if (target != null && target.buff(Flutter.class) != null){
				target.buff(Flutter.class).refreshAction();
			}
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", Math.round(boost*100), dispTurns());
		}

		private static final String BOOST = "boost";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(BOOST, boost);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			boost = bundle.getFloat(BOOST);
		}
	}
}
