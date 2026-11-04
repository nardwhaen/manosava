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

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
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

/**
 * 노아의 마녀 수치 (처음부터 있음). 물 위에 있으면 매 턴 +2, 100이 되면 마녀화.
 */
public class NoaWitchPower extends Buff implements ActionIndicator.Action {

	{
		type = buffType.POSITIVE;
		revivePersists = true;
	}

	public static final float MAX = 100f;
	public static final float WATER_GAIN = 2f;

	private float points = 0;

	public float points(){
		return points;
	}

	public boolean isFull(){
		return points >= MAX;
	}

	public void gain( float amount ){
		if (target.buff(NoaWitchForm.class) != null) return; //마녀화 중에는 오르지 않음
		boolean wasFull = isFull();
		points = Math.min(MAX, points + amount);
		if (!wasFull) refreshAction();
		BuffIndicator.refreshHero();
	}

	@Override
	public boolean act() {
		if (Noa.onWater(target)){
			gain(WATER_GAIN);
		}
		if (isFull() && target.buff(NoaWitchForm.class) == null){
			ActionIndicator.setAction(this); //불러온 게임에서도 버튼이 보이도록
		}
		spend(TICK);
		return true;
	}

	public void refreshAction(){
		if (isFull() && target.buff(NoaWitchForm.class) == null){
			ActionIndicator.setAction(this);
		} else {
			ActionIndicator.clearAction(this);
			GardenAction.refresh(target);
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
		points = 0;
		Buff.affect(target, NoaWitchForm.class).set(NoaWitchForm.TURNS);
		target.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(this, "activated"));
		ActionIndicator.clearAction(this);
		GardenAction.refresh(target);
		BuffIndicator.refreshHero();
	}

	@Override
	public String actionName() {
		return Messages.get(this, "action_name");
	}

	@Override
	public int actionIcon() {
		return HeroIcon.WARLOCK; //TODO 노아 전용 마녀화 아이콘
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
		return target.buff(NoaWitchForm.class) == null ? BuffIndicator.WITCH_METER : BuffIndicator.NONE;
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

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("points", points);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		points = bundle.getFloat("points");
	}

	public static void add( Hero hero, float amount ){
		NoaWitchPower p = hero.buff(NoaWitchPower.class);
		if (p != null) p.gain(amount);
	}
}
