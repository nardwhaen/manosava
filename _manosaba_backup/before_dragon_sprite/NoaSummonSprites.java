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

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.TextureFilm;

//[Manosaba] 환상의 정원 동료 전용 스프라이트 (노아가 물감으로 그린 동물)
public class NoaSummonSprites {

	//뿔 달린 토끼: 14x15, 0-1 서기, 2-5 깡충 이동, 6-8 뿔 찌르기, 9-11 물감으로 녹아내림
	public static class RabbitSprite extends MobSprite {
		public RabbitSprite() {
			super();
			texture( "sprites/noa_rabbit.png" );
			TextureFilm frames = new TextureFilm( texture, 14, 15 );

			idle = new Animation( 2, true );
			idle.frames( frames, 0, 0, 0, 1 );

			run = new Animation( 12, true );
			run.frames( frames, 2, 3, 4, 5 );

			attack = new Animation( 15, false );
			attack.frames( frames, 6, 7, 8, 0 );

			die = new Animation( 10, false );
			die.frames( frames, 9, 10, 11 );

			play( idle );
		}

		@Override
		public int blood() {
			return 0xFFF0E8FF; //물감(흰색)
		}
	}

	//새하얗고 신성한 뱀: 12x11, 원본 뱀과 같은 배치 (색만 다름)
	public static class SnakeSprite extends MobSprite {
		public SnakeSprite() {
			super();
			texture( "sprites/noa_snake.png" );
			TextureFilm frames = new TextureFilm( texture, 12, 11 );

			idle = new Animation( 10, true );
			idle.frames( frames, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			                     1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 2, 1, 1);

			run = new Animation( 8, true );
			run.frames( frames, 4, 5, 6, 7 );

			attack = new Animation( 15, false );
			attack.frames( frames, 8, 9, 10, 9, 0 );

			die = new Animation( 10, false );
			die.frames( frames, 11, 12, 13 );

			play( idle );
		}

		@Override
		public int blood() {
			return 0xFFFFF4C8; //물감(흰색·금색)
		}
	}

	//날카로운 발톱을 가진 새(매): 16x15, 0-1 날갯짓, 2-4 비행 이동, 5-7 발톱 공격, 8-10 물감으로 번짐
	public static class BirdSprite extends MobSprite {
		public BirdSprite() {
			super();
			texture( "sprites/noa_bird.png" );
			TextureFilm frames = new TextureFilm( texture, 16, 15 );

			idle = new Animation( 6, true );
			idle.frames( frames, 0, 1 );

			run = new Animation( 10, true );
			run.frames( frames, 2, 3, 4 );

			attack = new Animation( 14, false );
			attack.frames( frames, 5, 6, 6, 7 );

			die = new Animation( 10, false );
			die.frames( frames, 8, 9, 10 );

			play( idle );
		}

		@Override
		public int blood() {
			return 0xFFC49862; //물감(갈색)
		}
	}
}
