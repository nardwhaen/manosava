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
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundle;

/**
 * 어디로든 문: 같은 층의 포탈 (최대 2개). 값은 그린 순서 (작을수록 오래됨).
 */
public class NoaPortals extends NoaMarkBlob {

	private int counter = 0;

	public int count(){
		int n = 0;
		for (int c = 0; c < Dungeon.level.length(); c++) if (get(c) > 0) n++;
		return n;
	}

	public int other( int cell ){
		for (int c = 0; c < Dungeon.level.length(); c++){
			if (c != cell && get(c) > 0) return c;
		}
		return -1;
	}

	public void add( int cell ){
		//2개가 있으면 가장 오래된 포탈을 지움
		while (count() >= 2){
			int oldest = -1;
			for (int c = 0; c < Dungeon.level.length(); c++){
				if (get(c) > 0 && (oldest == -1 || get(c) < get(oldest))) oldest = c;
			}
			if (oldest == -1) break;
			remove(oldest);
		}
		counter++;
		Level.set(cell, Terrain.DOOR);
		Noa.updateCell(cell);
		put(cell, counter);
	}

	public void remove( int cell ){
		put(cell, 0);
		if (Actor.findChar(cell) == null && Dungeon.level.heaps.get(cell) == null){
			Level.set(cell, Terrain.WALL);
			Noa.updateCell(cell);
		}
	}

	@Override
	public String tileDesc() {
		return Messages.get(this, "desc");
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("counter", counter);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		counter = bundle.getInt("counter");
	}
}
