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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.noa.Noa;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.noa.NoaDoors;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.noa.NoaPortals;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.RainbowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.PaintCan;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

/**
 * 노아의 아틀리에!: 노아 바로 옆(상하좌우) 벽 한 칸에 문을 그려 진짜 문으로 만듦 (영구).
 * 벽 반대편이 벽이 아닌 곳만 가능. 책장·조각상 등 특수 벽은 불가. 충전 소모 50.
 * 어디로든 문: 반대편에 공간이 없는 벽이면 포탈을 그림 (충전 70/60/50/40).
 */
public class NoaAtelier extends ArmorAbility {

	{
		baseChargeUse = 50f;
	}

	public static float portalCost( Hero hero ){
		return 80f - 10f * hero.pointsInTalent(Talent.ANYWHERE_DOOR);
	}

	@Override
	public float chargeUse( Hero hero ) {
		float use = super.chargeUse(hero);
		if (hero.hasTalent(Talent.ANYWHERE_DOOR)){
			//문/포탈 중 더 싼 쪽으로 버튼을 열어 두고, 실제 소모는 activate에서 확인
			use = Math.min(use, portalCost(hero) * (use / baseChargeUse));
		}
		return use;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	public int targetedPos( Char user, int dst ) {
		return dst;
	}

	private static boolean paintableWall( int cell ){
		int t = Dungeon.level.map[cell];
		return t == Terrain.WALL || t == Terrain.WALL_DECO;
	}

	@Override
	protected void activate( ClassArmor armor, Hero hero, Integer target ) {
		if (target == null) return;

		int dir = target - hero.pos;
		boolean cardinal = false;
		for (int n : PathFinder.NEIGHBOURS4) if (n == dir) cardinal = true;
		if (!cardinal || !paintableWall(target)){
			GLog.w(Messages.get(this, "bad_target"));
			return;
		}

		int opposite = target + dir;
		boolean openBeyond = opposite >= 0 && opposite < Dungeon.level.length()
				&& Dungeon.level.insideMap(target)
				&& Math.abs((opposite % Dungeon.level.width()) - (target % Dungeon.level.width())) <= 1
				&& !Dungeon.level.solid[opposite];

		float scale = super.chargeUse(hero) / baseChargeUse; //마법의 갑옷 감소 반영

		if (openBeyond){
			float cost = baseChargeUse * scale;
			if (armor.charge < cost){
				GLog.w(Messages.get(ClassArmor.class, "low_charge"));
				return;
			}
			armor.charge -= cost;
			Level.set(target, Terrain.DOOR);
			Noa.updateCell(target);
			Noa.seed(target, NoaDoors.class).put(target, 1);
		} else {
			if (!hero.hasTalent(Talent.ANYWHERE_DOOR)){
				GLog.w(Messages.get(this, "no_space"));
				return;
			}
			if (!Dungeon.level.insideMap(target)){
				GLog.w(Messages.get(this, "edge"));
				return;
			}
			float cost = portalCost(hero) * scale;
			if (armor.charge < cost){
				GLog.w(Messages.get(ClassArmor.class, "low_charge"));
				return;
			}
			armor.charge -= cost;
			Noa.seed(target, NoaPortals.class).add(target);
			Noa.seed(target, NoaDoors.class).put(target, 1);
		}

		armor.updateQuickslot();
		CellEmitter.get(target).burst(RainbowParticle.BURST, 15);
		Sample.INSTANCE.play(Assets.Sounds.OPEN);
		Dungeon.observe();
		GameScene.updateFog();
		hero.sprite.operate(target);
		Invisibility.dispel();
		hero.spendAndNext(1f);
	}

	//캐릭터가 칸을 밟았을 때 (Char.move에서 호출): 포탈 이동, 어디 갔게~?, 영감이 떠올랐어!
	public static void onStep( final Char ch ){
		if (Dungeon.level == null) return;
		int cell = ch.pos;
		Hero hero = Dungeon.hero;

		NoaDoors doors = Noa.blob(NoaDoors.class);
		if (ch == hero && doors != null && doors.get(cell) > 0){
			//어디 갔게~?: 그린 문 위에 올라가면 은신 3/5/7/10턴
			if (hero.hasTalent(Talent.WHERE_DID_I_GO)){
				int[] turns = {3, 5, 7, 10};
				Buff.affect(hero, Invisibility.class, turns[hero.pointsInTalent(Talent.WHERE_DID_I_GO)-1]);
			}
			//영감이 떠올랐어!: 그린 문을 처음 지날 때 페인트 통 0.5/1/1.5/2칸
			if (doors.get(cell) == 1){
				doors.put(cell, 2);
				if (hero.hasTalent(Talent.INSPIRATION)){
					PaintCan can = Noa.paintCan(hero);
					if (can != null) can.addCharge(0.5f * hero.pointsInTalent(Talent.INSPIRATION));
				}
			}
		}

		NoaPortals portals = Noa.blob(NoaPortals.class);
		if (portals != null && portals.get(cell) > 0
				&& (ch == hero || ch.alignment == Char.Alignment.ALLY)){
			int other = portals.other(cell);
			if (other == -1){
				if (ch == hero) GLog.i(Messages.get(NoaAtelier.class, "one_portal"));
				return;
			}
			int dest = -1;
			for (int n : PathFinder.NEIGHBOURS4){
				int c = other + n;
				if (c >= 0 && c < Dungeon.level.length() && Dungeon.level.passable[c]
						&& !Dungeon.level.solid[c] && Actor.findChar(c) == null
						&& portals.get(c) == 0){
					dest = c;
					break;
				}
			}
			if (dest == -1){
				if (ch == hero) GLog.w(Messages.get(NoaAtelier.class, "portal_blocked"));
				return;
			}
			//이동 연출이 끝난 뒤 순간이동 (다음 행동 전에 처리)
			final int to = dest;
			final int from = cell;
			Actor.addDelayed(new Actor() {
				{
					actPriority = VFX_PRIO;
				}
				@Override
				protected boolean act() {
					Actor.remove(this);
					if (ch.isAlive() && ch.pos == from && Actor.findChar(to) == null){
						ScrollOfTeleportation.appear(ch, to);
						if (ch == Dungeon.hero){
							Dungeon.hero.interrupt();
							Dungeon.observe();
							GameScene.updateFog();
						}
					}
					return true;
				}
			}, 0);
		}
	}

	@Override
	public int icon() {
		return HeroIcon.WARP_BEACON; //TODO 전용 아이콘
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.ANYWHERE_DOOR, Talent.WHERE_DID_I_GO, Talent.INSPIRATION, Talent.HEROIC_ENERGY};
	}
}
