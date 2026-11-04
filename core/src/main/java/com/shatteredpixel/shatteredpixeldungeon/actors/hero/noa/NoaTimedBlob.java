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
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;

/**
 * 칸마다 남은 턴 수를 담는 노아용 블롭 (퍼지지 않음).
 * 매 턴 1씩 줄고, 0이 되는 순간 onExpire가 불린다.
 */
public abstract class NoaTimedBlob extends Blob {

	@Override
	protected void evolve() {
		int len = Dungeon.level.length();
		for (int cell = 0; cell < len; cell++){
			int v = cur[cell];
			if (v > 0){
				onTick(cell, v);
				v--;
				off[cell] = v;
				if (v == 0) onExpire(cell);
				else volume += v;
			} else {
				off[cell] = 0;
			}
		}
	}

	//남은 턴(감소 전)과 함께 매 턴 호출
	protected void onTick( int cell, int left ){}

	protected void onExpire( int cell ){}

	//남은 턴을 turns로 맞춤 (더 짧아지지는 않음)
	public void setTurns( Level level, int cell, int turns ){
		if (cur == null) seed(level, cell, 0);
		if (cur[cell] < turns){
			volume += turns - cur[cell];
			cur[cell] = turns;
			area.union(cell % level.width(), cell / level.width());
		}
	}

	public int left( int cell ){
		return (cur == null || volume <= 0) ? 0 : cur[cell];
	}
}
