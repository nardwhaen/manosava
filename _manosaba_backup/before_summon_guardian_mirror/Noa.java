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
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.noa.BubbleForm;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.PaintCan;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;

/**
 * 죠가사키 노아 전용 규칙을 한곳에 모아 둔 도우미 클래스.
 * (노아는 원본 마법사(MAGE) 자리를 대체한다)
 */
public class Noa {

	public static boolean isNoa( Char ch ){
		return ch instanceof Hero && ((Hero) ch).heroClass == HeroClass.MAGE;
	}

	public static PaintCan paintCan( Hero hero ){
		return hero.belongings.getItem(PaintCan.class);
	}

	//매 턴 Hero.act에서 호출: 마녀 수치 버프 유지
	public static void ensureBuffs( Hero hero ){
		if (!isNoa(hero)) return;
		if (hero.buff(NoaWitchPower.class) == null){
			Buff.affect(hero, NoaWitchPower.class);
		}
		if (hero.subClass == HeroSubClass.NOA_GARDEN && hero.buff(GardenAction.class) == null){
			Buff.affect(hero, GardenAction.class);
		}
	}

	// ===== 물 / 임시 물 바닥 =====

	//보스층과 마지막 층에서는 낭떠러지를 물로 바꾸지 않음
	public static boolean canBridgeChasm(){
		return !Dungeon.bossLevel() && Dungeon.depth < 26;
	}

	/**
	 * 한 칸을 물로 바꾼다. 원래 물 폭탄이 물을 만드는 칸은 영구히 물이 되고,
	 * 낭떠러지는 chasmTurns 동안만 물 바닥이 된다 (0이면 낭떠러지는 그대로).
	 */
	public static boolean wetCell( int cell, int chasmTurns ){
		if (cell < 0 || cell >= Dungeon.level.length()) return false;
		Fire fire = (Fire) Dungeon.level.blobs.get(Fire.class);
		if (fire != null) fire.clear(cell);
		int terr = Dungeon.level.map[cell];
		if (terr == Terrain.CHASM){
			if (chasmTurns > 0 && canBridgeChasm() && Dungeon.level.insideMap(cell)){
				PaintFloor.bridge(cell, chasmTurns);
				return true;
			}
			return false;
		}
		if (terr == Terrain.WATER){
			PaintFloor floor = (PaintFloor) Dungeon.level.blobs.get(PaintFloor.class);
			//이미 임시 물 바닥이면 남은 시간만 늘려 줌
			if (floor != null && floor.volume > 0 && floor.cur[cell] > 0 && chasmTurns > 0){
				PaintFloor.bridge(cell, chasmTurns);
			}
			return true;
		}
		return Dungeon.level.setCellToWater(true, cell);
	}

	public static boolean onWater( Char ch ){
		return ch != null && ch.isAlive() && !ch.flying && Dungeon.level.water[ch.pos];
	}

	// ===== 페인트 통 효과 (페인트 통, 강화된 마녀화에서 같이 씀) =====

	public static float overpaintFactor( Hero hero ){
		return 1f + 0.5f * hero.pointsInTalent(Talent.OVERPAINT);
	}

	//적: 느려짐 (+달라붙는 페인트), 들키고 싶지 않은 원죄
	public static void paintEnemy( Hero hero, Char ch, float slowTurns ){
		Buff.prolong(ch, Slow.class, slowTurns);
		if (hero.hasTalent(Talent.HIDDEN_SIN)){
			Buff.prolong(ch, PaintBlind.class, slowTurns).level = hero.pointsInTalent(Talent.HIDDEN_SIN);
		}
	}

	public static void paintAlly( Hero hero, Char ch, int baseShield, int baseHeal, boolean refillOnly ){
		int shield = Math.round(baseShield * overpaintFactor(hero));
		int heal = Math.round(baseHeal * overpaintFactor(hero));
		giveShield(ch, shield, refillOnly);
		if (heal > 0 && ch.HP < ch.HT){
			int healed = Math.min(heal, ch.HT - ch.HP);
			ch.HP += healed;
			ch.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(healed), FloatingText.HEALING);
		}
	}

	public static void giveShield( Char ch, int shield, boolean refillOnly ){
		Barrier barrier = Buff.affect(ch, Barrier.class);
		if (refillOnly){
			if (barrier.shielding() < shield){
				int gain = shield - barrier.shielding();
				barrier.setShield(shield);
				ch.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(gain), FloatingText.SHIELDING);
			}
		} else {
			barrier.incShield(shield);
			ch.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(shield), FloatingText.SHIELDING);
		}
	}

	// ===== 냉기 (차가운 비) =====

	//원본 냉기 가스와 같은 방식: 냉기가 가득 찬 상태에서 또 받으면 얼어붙음
	public static void chill( Char ch, float amount ){
		if (ch.isImmune(Chill.class)) return;
		if (ch.buff(Frost.class) != null) return;
		Chill chill = ch.buff(Chill.class);
		if (chill != null && chill.cooldown() >= Chill.DURATION - 0.01f){
			if (!ch.isImmune(Frost.class)) Buff.affect(ch, Frost.class, Frost.DURATION);
			return;
		}
		float toCap = Chill.DURATION - (chill == null ? 0 : chill.cooldown());
		Buff.affect(ch, Chill.class, Math.min(amount, toCap));
	}

	// ===== 피해 =====

	//노아가 받는 피해 배율 (마녀화, 물거품)
	public static float incomingDamageFactor( Hero hero ){
		float f = 1f;
		NoaWitchForm form = hero.buff(NoaWitchForm.class);
		if (form != null) f *= form.enhanced() ? 0.8f : 0.9f;
		BubbleForm.BubbleBuff bubble = hero.buff(BubbleForm.BubbleBuff.class);
		if (bubble != null && onWater(hero)){
			f *= 1f - 0.25f * hero.pointsInTalent(Talent.UNCATCHABLE_DROP);
		}
		return f;
	}

	//노아가 주는 피해 배율 (물거품 중 공격: 노아도 싸울 수 있어~)
	public static float outgoingDamageFactor( Hero hero ){
		if (hero.buff(BubbleForm.BubbleBuff.class) != null){
			return 0.25f * hero.pointsInTalent(Talent.NOA_CAN_FIGHT);
		}
		return 1f;
	}

	//물거품 중에는 공격 행동(일반 공격, 마법 막대, 투척 무기)을 할 수 없음
	public static boolean attackBlocked( Hero hero ){
		return hero.buff(BubbleForm.BubbleBuff.class) != null && !hero.hasTalent(Talent.NOA_CAN_FIGHT);
	}

	//보호받는 공주님: 방어력이 적용되기 전 피해의 10/20/30%를 체력이 가장 높은 소환물이 대신 받음
	public static int shareDamage( Hero hero, Char attacker, int damage ){
		if (damage <= 0 || !hero.hasTalent(Talent.PROTECTED_PRINCESS)) return damage;
		NoaSummon best = null;
		for (Char ch : Actor.chars()){
			if (ch instanceof NoaSummon && ch.isAlive() && Dungeon.level.heroFOV[ch.pos]){
				if (best == null || ch.HP + ch.shielding() > best.HP + best.shielding()){
					best = (NoaSummon) ch;
				}
			}
		}
		if (best == null) return damage;
		int moved = Math.round(damage * 0.1f * hero.pointsInTalent(Talent.PROTECTED_PRINCESS));
		if (moved <= 0) return damage;
		int toSummon = Math.max(0, moved - best.drRoll());
		best.damage(toSummon, attacker);
		return damage - moved;
	}

	// ===== 처치 =====

	public static void onMobDeath( Mob mob, Object cause ){
		//소환물 사망: 다시 그려야 겠다~
		if (mob instanceof NoaSummon){
			((NoaSummon) mob).onDeathRefund();
		}
	}

	public static void updateCell( int cell ){
		GameScene.updateMap(cell);
		Dungeon.level.updateCellFlags(cell);
	}

	//블롭을 만들거나 가져옴 (처음 만들 때만 화면에 추가)
	public static <T extends Blob> T seed( int cell, Class<T> cls ){
		boolean existed = Dungeon.level.blobs.get(cls) != null;
		T b = Blob.seed(cell, 0, cls);
		if (!existed) GameScene.add(b);
		return b;
	}

	public static <T extends Blob> T blob( Class<T> cls ){
		return cls.cast(Dungeon.level.blobs.get(cls));
	}
}
