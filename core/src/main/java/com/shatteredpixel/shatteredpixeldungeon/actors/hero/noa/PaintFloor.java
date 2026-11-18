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
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.PointF;
import com.watabou.utils.SparseArray;

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

	//[Manosaba] 무너지기 5턴 전부터 경고: 노아가 그 위에 있으면 하던 행동을 멈추고 빨간 글씨로 알림
	public static final int WARN_TURNS = 5;
	private static int warnedCell = -1;

	@Override
	protected void evolve() {
		//노아가 경고 구역을 벗어나면 다음에 다시 올라섰을 때 또 경고
		if (Dungeon.hero != null && cur != null){
			int h = Dungeon.hero.pos;
			if (h < 0 || h >= cur.length || cur[h] <= 0 || cur[h] > WARN_TURNS + 1) warnedCell = -1;
		}
		super.evolve();
	}

	@Override
	protected void onTick( int cell, int left ) {
		//left는 감소 전 값: 이번 턴이 지나면 left-1턴 남음
		if (left - 1 <= WARN_TURNS && left - 1 > 0
				&& Dungeon.hero != null && Dungeon.hero.pos == cell && warnedCell != cell){
			warnedCell = cell;
			GLog.n(Messages.get(this, "warning", left - 1));
			Dungeon.hero.interrupt();
		}
	}

	//[Manosaba] 남은 턴이 5 이하인 물 바닥에 구멍이 뚫려 가는 모습 (3단계)
	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );
		emitter.add( new HoleOverlay() );
	}

	private class HoleOverlay extends Group {
		private final SparseArray<Image> holes = new SparseArray<>();
		private final TextureFilm film;
		{
			Image probe = new Image("effects/noa_paint_hole.png");
			film = new TextureFilm(probe.texture, 16, 16);
		}

		@Override
		public void update() {
			super.update();
			if (Dungeon.level == null || cur == null) return;
			//사라진 칸 정리
			for (int cell : holes.keyArray()){
				int l = cell < cur.length ? cur[cell] : 0;
				if (l <= 0 || l > WARN_TURNS || Dungeon.level.map[cell] != Terrain.WATER){
					Image im = holes.get(cell);
					holes.remove(cell);
					im.killAndErase();
				}
			}
			if (volume <= 0) return;
			for (int i = area.left; i < area.right; i++){
				for (int j = area.top; j < area.bottom; j++){
					int cell = i + j * Dungeon.level.width();
					if (cell >= cur.length) continue;
					int l = cur[cell];
					if (l <= 0 || l > WARN_TURNS || Dungeon.level.map[cell] != Terrain.WATER) continue;
					int stage = l >= 4 ? 0 : (l >= 2 ? 1 : 2);
					Image im = holes.get(cell);
					if (im == null){
						im = new Image("effects/noa_paint_hole.png");
						PointF p = DungeonTilemap.tileToWorld(cell);
						im.x = p.x; im.y = p.y;
						holes.put(cell, im);
						add(im);
					}
					im.frame(film.get(stage));
					im.visible = Dungeon.level.visited[cell] || Dungeon.level.heroFOV[cell];
				}
			}
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
