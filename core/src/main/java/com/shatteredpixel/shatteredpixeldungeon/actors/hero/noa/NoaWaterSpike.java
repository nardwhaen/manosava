/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.utils.PointF;

//[Manosaba] 노아 마녀화 피해 연출: 발밑의 물에서 날카로운 물줄기가 위로 솟아 찌름
public class NoaWaterSpike extends Image {

	public static final String TEXTURE = "effects/noa_water_spike.png";

	private static final float RISE = 0.08f;  //솟아오르는 시간
	private static final float HOLD = 0.12f;  //멈춰 있는 시간
	private static final float FADE = 0.20f;  //사라지는 시간

	private float time = 0;

	public NoaWaterSpike(){
		super(TEXTURE);
		origin.set(width/2f, height); //아래쪽을 기준으로 늘어남
		scale.set(1, 0);
	}

	public static void hit( Char ch ){
		if (ch == null || ch.sprite == null || ch.sprite.parent == null || !ch.sprite.visible) return;

		NoaWaterSpike spike = new NoaWaterSpike();
		PointF tile = DungeonTilemap.tileToWorld(ch.pos);
		spike.x = tile.x + (DungeonTilemap.SIZE - spike.width)/2f;
		spike.y = tile.y + DungeonTilemap.SIZE - spike.height;
		ch.sprite.parent.add(spike); //캐릭터 앞에 그려짐

		Splash.at(ch.pos, 0x5FB4FA, 4);
	}

	@Override
	public void update() {
		super.update();
		time += Game.elapsed;

		if (time < RISE){
			scale.y = time / RISE;
		} else if (time < RISE + HOLD){
			scale.y = 1;
		} else if (time < RISE + HOLD + FADE){
			float p = (time - RISE - HOLD) / FADE;
			scale.y = 1;
			alpha(1 - p);
			y -= 6 * Game.elapsed; //사라지면서 살짝 위로
		} else {
			killAndErase();
		}
	}
}
