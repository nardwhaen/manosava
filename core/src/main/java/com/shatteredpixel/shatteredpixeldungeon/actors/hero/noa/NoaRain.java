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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * 드리핑: 고른 범위에 비가 내림 (기본 10턴). 매 턴 무작위 칸이 젖고 5턴 안에 모든 칸이 물이 됨.
 * 벽에 막히지 않음. 영역 효과(냉기, 페인트 충전)는 전용 특성으로.
 */
public class NoaRain extends NoaTimedBlob {

	{
		alwaysVisible = true;
	}

	public static final int WET_TURNS = 5;

	//칸마다 처음 비가 내린 시간 (몇 턴째 젖었는지 계산용)
	private int[] start;

	public static boolean inRain( int cell ){
		NoaRain rain = Noa.blob(NoaRain.class);
		return rain != null && rain.left(cell) > 0;
	}

	public void rainAt( int cell, int turns ){
		if (start == null || start.length != Dungeon.level.length()){
			start = new int[Dungeon.level.length()];
		}
		if (left(cell) < turns) start[cell] = turns;
		setTurns(Dungeon.level, cell, turns);
	}

	@Override
	protected void onTick( int cell, int left ) {
		if (start == null || start.length != Dungeon.level.length()){
			start = new int[Dungeon.level.length()];
		}
		if (start[cell] < left) start[cell] = left;

		//5턴 안에 모든 칸이 젖도록: 아직 안 젖은 칸은 남은 시간 비율만큼 확률로 젖음
		int elapsed = start[cell] - left;
		if (elapsed < WET_TURNS && !Dungeon.level.water[cell]){
			int remaining = WET_TURNS - elapsed;
			if (remaining <= 1 || Random.Int(remaining) == 0){
				Noa.wetCell(cell, 10);
			}
		}

		//차가운 비: 영역 안의 적에게 냉기 (일반 1 / 물 2, 특성 레벨당 0/33/66/100% 증가)
		Char ch = Actor.findChar(cell);
		if (ch != null && ch.alignment == Char.Alignment.ENEMY && ch.isAlive()
				&& Dungeon.hero != null && Dungeon.hero.hasTalent(Talent.COLD_RAIN)){
			float[] mult = {1f, 1.33f, 1.66f, 2f};
			float amount = (Dungeon.level.water[cell] && !ch.flying ? 2f : 1f)
					* mult[Dungeon.hero.pointsInTalent(Talent.COLD_RAIN) - 1];
			Noa.chill(ch, amount);
		}
	}

	@Override
	protected void onExpire( int cell ) {
		if (start != null) start[cell] = 0;
	}

	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );
		emitter.pour( Speck.factory( Speck.STORM ), 0.5f );
	}

	@Override
	public String tileDesc() {
		return Messages.get(this, "desc");
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		if (start != null) bundle.put("start", start);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains("start")) start = bundle.getIntArray("start");
	}
}
