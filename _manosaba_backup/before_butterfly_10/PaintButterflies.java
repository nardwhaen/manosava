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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RainbowParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

/**
 * 나비가 날라가~: 이어진 물 칸 전체로 한번에 퍼지는 나비. 그 위의 적에게 매 턴 피해 2/3.
 * 번지는 방식은 원본 전기 함정처럼 이어진 물을 타고 한번에. 노아·아군은 피해 없음.
 */
public class PaintButterflies extends NoaTimedBlob {

	public static final int TURNS = 5;

	//cell에서 시작해 이어진 물 칸 전체에 나비를 퍼뜨림
	public static void spread( int cell ){
		PaintButterflies b = Noa.seed(cell, PaintButterflies.class);
		boolean[] seen = new boolean[Dungeon.level.length()];
		ArrayList<Integer> queue = new ArrayList<>();
		queue.add(cell);
		seen[cell] = true;
		while (!queue.isEmpty()){
			int c = queue.remove(queue.size()-1);
			b.setTurns(Dungeon.level, c, TURNS);
			for (int n : PathFinder.NEIGHBOURS8){
				int nc = c + n;
				if (nc < 0 || nc >= seen.length || seen[nc]) continue;
				if (Dungeon.level.water[nc]){
					seen[nc] = true;
					queue.add(nc);
				}
			}
		}
	}

	@Override
	protected void onTick( int cell, int left ) {
		Char ch = Actor.findChar(cell);
		if (ch != null && ch.alignment == Char.Alignment.ENEMY && ch.isAlive()){
			int dmg = 1 + Dungeon.hero.pointsInTalent(Talent.BUTTERFLIES);
			ch.damage(dmg, this);
		}
	}

	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );
		emitter.pour( RainbowParticle.BURST, 0.6f );
	}

	@Override
	public String tileDesc() {
		return Messages.get(this, "desc");
	}
}
