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


package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.noa;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.noa.Noa;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.noa.NoaRain;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.PaintCan;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

/**
 * 드리핑: 페인트 통 충전을 모두 소모해 고른 칸 중심으로 비를 내림 (10턴, 최대 15x15).
 * 범위 반경 = 1 + 소모한 칸 x 0.6 (내림). 벽에 막히지 않음. 페인트 통이 0칸이면 사용 불가. 충전 20.
 */
public class Dripping extends ArmorAbility {

	{
		baseChargeUse = 20f;
	}

	public static final int BASE_TURNS = 10;

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	public int targetedPos( Char user, int dst ) {
		return dst;
	}

	public static int rainTurns( Hero hero ){
		int[] turns = {BASE_TURNS, 13, 16, 20, 25};
		return turns[hero.pointsInTalent(Talent.ENDLESS_RAIN)];
	}

	@Override
	protected void activate( ClassArmor armor, Hero hero, Integer target ) {
		if (target == null) return;
		PaintCan can = Noa.paintCan(hero);
		if (can == null || can.charges() <= 0){
			GLog.w(Messages.get(this, "no_paint"));
			return;
		}
		if (!Dungeon.level.insideMap(target) || !Dungeon.level.heroFOV[target]){
			GLog.w(Messages.get(this, "bad_target"));
			return;
		}

		armor.charge -= chargeUse(hero);
		armor.updateQuickslot();

		int used = can.drainAll();
		int radius = 1 + (int)(used * 0.6f);
		int turns = rainTurns(hero);

		NoaRain rain = Noa.seed(target, NoaRain.class);
		int w = Dungeon.level.width();
		int cx = target % w, cy = target / w;
		for (int y = cy - radius; y <= cy + radius; y++){
			for (int x = cx - radius; x <= cx + radius; x++){
				if (x < 0 || y < 0 || x >= w || y >= Dungeon.level.height()) continue;
				int cell = x + y*w;
				if (Dungeon.level.solid[cell] && Dungeon.level.map[cell] != com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.CHASM) continue;
				rain.rainAt(cell, turns);
			}
		}

		Sample.INSTANCE.play(Assets.Sounds.GAS);
		hero.sprite.zap(target);
		Invisibility.dispel();
		hero.spendAndNext(1f);
	}

	@Override
	public int icon() {
		return HeroIcon.NATURES_POWER; //TODO 전용 아이콘
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.ENDLESS_RAIN, Talent.RAIN_PAINT, Talent.COLD_RAIN, Talent.HEROIC_ENERGY};
	}
}
