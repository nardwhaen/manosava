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
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.PaintCan;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 환상의 정원: 화면 오른쪽 아래 능력 버튼. 누르면 검투사 연계 창처럼 동료 4종을 고르는 창이 뜸.
 * (마녀화 버튼이 떠 있는 동안에는 마녀화 버튼이 우선)
 */
public class GardenAction extends Buff implements ActionIndicator.Action {

	{
		type = buffType.NEUTRAL;
		revivePersists = true;
	}

	@Override
	public boolean attachTo(Char target) {
		if (super.attachTo(target)){
			refresh(target);
			return true;
		}
		return false;
	}

	@Override
	public boolean act() {
		//다른 버튼이 사라졌을 때 다시 나타나도록 가끔 확인
		if (!(target instanceof Hero) || ((Hero) target).subClass != HeroSubClass.NOA_GARDEN){
			detach();
			return true;
		}
		refresh(target);
		spend(TICK);
		return true;
	}

	public static void refresh( Char target ){
		GardenAction g = target == null ? null : target.buff(GardenAction.class);
		if (g == null) return;
		//[Manosaba] 마녀화 버튼은 따로 있으므로 소환 버튼은 항상 표시
		if (ActionIndicator.action != g){
			ActionIndicator.setAction(g);
		}
	}

	@Override
	public void detach() {
		super.detach();
		ActionIndicator.clearAction(this);
	}

	@Override
	public String actionName() {
		return Messages.get(this, "action_name");
	}

	@Override
	public int actionIcon() {
		return HeroIcon.WARDEN; //TODO 환상의 정원 전용 아이콘
	}

	@Override
	public int indicatorColor() {
		return 0x2E7D5B;
	}

	@Override
	public void doAction() {
		GameScene.show(new WndNoaGarden(Dungeon.hero));
	}

	//고른 동료를 시야 안의 빈 칸에 그림
	public static void startSummon( final Hero hero, final NoaSummon.Kind kind ){
		final PaintCan can = Noa.paintCan(hero);
		if (can == null){
			GLog.w(Messages.get(GardenAction.class, "no_can"));
			return;
		}
		GameScene.selectCell(new CellSelector.Listener() {
			@Override
			public void onSelect( Integer cell ) {
				if (cell == null) return;
				if (!Dungeon.level.heroFOV[cell] || Dungeon.level.solid[cell] || !Dungeon.level.passable[cell]
						|| Actor.findChar(cell) != null){
					GLog.w(Messages.get(GardenAction.class, "bad_cell"));
					return;
				}
				if (!can.payCharges(hero, kind.cost)){
					return;
				}
				NoaSummon.summon(kind, cell);
				hero.sprite.zap(cell);
				hero.spendAndNext(1f);
			}
			@Override
			public String prompt() {
				return Messages.get(GardenAction.class, "prompt", kind.title());
			}
		});
	}
}
