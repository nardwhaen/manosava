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

/**
 * 칸마다 숫자를 영구히 기억하는 블롭 (그린 문, 포탈 표시용). 시간이 지나도 줄지 않음.
 */
public abstract class NoaMarkBlob extends Blob {

	@Override
	protected void evolve() {
		int len = Dungeon.level.length();
		for (int cell = 0; cell < len; cell++){
			off[cell] = cur[cell];
			volume += cur[cell];
		}
	}

	public int get( int cell ){
		return (cur == null || volume <= 0) ? 0 : cur[cell];
	}

	public void put( int cell, int value ){
		if (cur == null) seed(Dungeon.level, cell, 0);
		volume += value - cur[cell];
		cur[cell] = value;
		if (off != null) off[cell] = value;
		area.union(cell % Dungeon.level.width(), cell / Dungeon.level.width());
	}
}
