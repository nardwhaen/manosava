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
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

/**
 * 낭떠러지 위에 잠시 생긴 물 바닥. 시간이 끝나면 다시 낭떠러지가 되고 그 위의 것은 떨어진다.
 */
public class PaintFloor extends NoaTimedBlob {

	public static void bridge( int cell, int turns ){
		if (Dungeon.level.map[cell] == Terrain.CHASM){
			Level.set(cell, Terrain.WATER);
			Noa.updateCell(cell);
		}
		PaintFloor floor = Noa.seed(cell, PaintFloor.class);
		floor.setTurns(Dungeon.level, cell, turns);
	}

	@Override
	protected void onTick( int cell, int left ) {
		//끝나기 2턴 전 경고
		if (left == 3 && Dungeon.hero != null && Dungeon.hero.pos == cell){
			GLog.w(Messages.get(this, "warning"));
		}
	}

	@Override
	protected void onExpire( int cell ) {
		if (Dungeon.level.map[cell] != Terrain.WATER) return;
		Level.set(cell, Terrain.CHASM);
		Noa.updateCell(cell);

		Heap heap = Dungeon.level.heaps.get(cell);
		if (heap != null){
			ArrayList<Item> items = new ArrayList<>(heap.items);
			heap.destroy();
			for (Item i : items) Dungeon.dropToChasm(i);
		}

		Char ch = Actor.findChar(cell);
		if (ch != null && !ch.flying){
			if (ch == Dungeon.hero){
				Chasm.heroFall(cell);
			} else if (ch instanceof Mob){
				Chasm.mobFall((Mob) ch);
			}
		}
	}

	@Override
	public String tileDesc() {
		return Messages.get(this, "desc");
	}
}
