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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.sherry;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.utils.Random;

/**
 * 타치바나 셰리 전용 규칙을 한곳에 모아 둔 도우미 클래스.
 * (셰리는 원본 전사(WARRIOR) 자리를 대체한다)
 */
public class Sherry {

	// ===== 초과 힘 =====

	//증거는 맛을 봐야죠! 로 얻는 초과 힘 보너스
	public static int tasteBonus( Hero hero ){
		TasteEvidence buff = hero.buff(TasteEvidence.class);
		return buff == null ? 0 : buff.level;
	}

	//초과 힘 = 현재 들고 있는 무기의 요구 힘보다 높은 힘 (+ 증거는 맛을 봐야죠! 보너스)
	public static int excessStr( Hero hero ){
		KindOfWeapon wep = hero.belongings.attackingWeapon();
		int req = (wep instanceof Weapon) ? ((Weapon) wep).STRReq() : 10;
		return Math.max(0, hero.STR() - req + tasteBonus(hero));
	}

	// ===== 한 손으로 던질게요! =====

	public static void onMeleeHit( Hero hero, Char enemy, KindOfWeapon wep ){
		if (!hero.hasTalent(Talent.ONE_HAND_THROW)) return;
		if (wep != null && !(wep instanceof MeleeWeapon)) return; //근접 공격만
		if (enemy == hero || !enemy.isAlive()) return;
		if (Char.hasProp(enemy, Char.Property.BOSS)) return; //보스 제외 (고정형은 throwChar가 제외)

		float chance = 0.06f * excessStr(hero);
		if (Random.Float() >= chance) return;

		knockback(hero, enemy, hero.pointsInTalent(Talent.ONE_HAND_THROW));
	}

	public static void knockback( Hero hero, Char enemy, int power ){
		//공격자 → 대상 방향으로 대상 너머까지 궤적을 그림
		Ballistica trajectory = new Ballistica(hero.pos, enemy.pos, Ballistica.STOP_TARGET);
		trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size() - 1), Ballistica.PROJECTILE);

		//WandOfBlastWave.throwChar와 같은 방식으로 실제로 밀려날 거리를 미리 계산
		int dist = Math.min(trajectory.dist, power);
		boolean collided = dist == trajectory.dist;
		if (Char.hasProp(enemy, Char.Property.LARGE)) {
			for (int i = 1; i <= dist; i++) {
				if (!Dungeon.level.openSpace[trajectory.path.get(i)]){
					dist = i-1;
					collided = true;
					break;
				}
			}
		}
		if (dist >= 0 && dist < trajectory.path.size() && Actor.findChar(trajectory.path.get(dist)) != null){
			dist--;
			collided = true;
		}
		//낭떠러지에는 떨어뜨리지 않음
		if (!enemy.flying) {
			while (dist > 0 && Dungeon.level.pit[trajectory.path.get(dist)]) {
				dist--;
				collided = false;
			}
		}
		if (dist <= 0 && !collided) return;

		final int remaining = Math.max(1, power - Math.max(dist, 0));
		final boolean hitObstacle = collided;

		//벽이나 다른 적에 부딪히면 남은 거리만큼 추가 피해 + 1턴 기절
		//(밀려나는 도중에 죽는 경우를 피하려고 피해를 먼저 계산함)
		if (hitObstacle){
			enemy.damage(remaining, hero);
			if (!enemy.isAlive()) return;
			Buff.prolong(enemy, Paralysis.class, 1f);
		}

		if (dist > 0) {
			WandOfBlastWave.throwChar(enemy, trajectory, dist, false, false, hero);
		}
	}

	// ===== 고장 난 위기 경보기 =====

	//점성 마법부여처럼 받는 피해의 일부를 지연 피해로 바꾸는 비율 (+1: 20%, +2: 35%)
	public static float alarmPercent( Hero hero ){
		if (!hero.hasTalent(Talent.BROKEN_ALARM)) return 0f;
		if (hero.buff(Talent.WarriorFoodImmunity.class) != null) return 0f; //식사 중 피해 면역은 건너뜀
		return hero.pointsInTalent(Talent.BROKEN_ALARM) >= 2 ? 0.35f : 0.20f;
	}

	// ===== 피해 배율 =====

	//셰리가 주는 피해 (src == 셰리) 배율: 마녀화 +40%, 범인 지목 +30%(+명탐정의 추리)
	public static float outgoingDamageFactor( Hero hero, Char target ){
		float factor = 1f;
		if (hero.buff(WitchForm.class) != null){
			factor *= 1.4f;
		}
		if (target.buff(CulpritMark.class) != null){
			float bonus = 0.3f;
			if (Char.hasProp(target, Char.Property.BOSS)
					|| Char.hasProp(target, Char.Property.MINIBOSS)
					|| target.buff(ChampionEnemy.class) != null){
				bonus += 0.1f * hero.pointsInTalent(Talent.GREAT_DETECTIVE);
			}
			factor *= 1f + bonus;
		}
		return factor;
	}

	// ===== 그거 칭찬인가요? =====

	public static float debuffDurationFactor( Char target, Buff buff ){
		if (target instanceof Hero && buff.type == Buff.buffType.NEGATIVE){
			switch (((Hero) target).pointsInTalent(Talent.IS_THAT_PRAISE)){
				case 1: return 0.80f;
				case 2: return 0.65f;
				case 3: return 0.50f;
			}
		}
		return 1f;
	}

	// ===== 서브클래스 수치 (팔랑임 / 마녀 수치) =====

	//ignoreActive: 요정이에요!·마녀화 지속 중에도 쌓이는지 (날파리 말고 요정이에요!, 마법의 힘)
	public static void addSubclassMeter( Hero hero, float amount, boolean ignoreActive ){
		if (hero.subClass == HeroSubClass.SHERRY_FAIRY){
			Buff.affect(hero, Flutter.class).gain(amount, ignoreActive);
		} else if (hero.subClass == HeroSubClass.SHERRY_WITCH){
			Buff.affect(hero, WitchPower.class).gain(amount, ignoreActive);
		}
	}

	//매 턴 호출: 서브클래스 버프가 없으면 붙여 줌
	public static void ensureSubclassBuffs( Hero hero ){
		if (hero.subClass == HeroSubClass.SHERRY_FAIRY && hero.buff(Flutter.class) == null){
			Buff.affect(hero, Flutter.class);
		} else if (hero.subClass == HeroSubClass.SHERRY_WITCH && hero.buff(WitchPower.class) == null){
			Buff.affect(hero, WitchPower.class);
		}
	}

	// ===== 회피했을 때 (팔랑 팔랑~, 날파리 말고 요정이에요!) =====

	public static void onDodge( Hero hero, Char attacker ){
		if (hero.hasTalent(Talent.FLUTTER_FLUTTER)){
			Buff.affect(hero, FlutterStrike.class).set(hero.pointsInTalent(Talent.FLUTTER_FLUTTER));
		}
		if (hero.hasTalent(Talent.NOT_A_FLY)){
			addSubclassMeter(hero, hero.pointsInTalent(Talent.NOT_A_FLY), true);
		}
	}

	// ===== 적이 죽었을 때 =====

	public static void onMobDeath( Char ch, Object cause ){
		Hero hero = Dungeon.hero;
		if (hero == null) return;

		//범인은 당신이에요!: 범인이 죽으면 충전 반환 + 당신도 공범인가요?
		if (ch.buff(CulpritMark.class) != null){
			gainArmorCharge(hero, 15f);
			if (hero.hasTalent(Talent.ACCOMPLICE)){
				for (Char other : Actor.chars()){
					if (other != ch && other.alignment == Char.Alignment.ENEMY
							&& Dungeon.level.distance(ch.pos, other.pos) <= 3){
						Buff.prolong(other, Paralysis.class, hero.pointsInTalent(Talent.ACCOMPLICE));
					}
				}
			}
		}

		boolean killedByHero = cause == hero || cause instanceof Weapon || cause instanceof Weapon.Enchantment;
		if (!killedByHero || ch.alignment != Char.Alignment.ENEMY) return;

		//마녀화 중 처치: 지속 시간 +2 (괴물이라 생각하는 마음: 4/6/8), 최대 20턴
		WitchForm form = hero.buff(WitchForm.class);
		if (form != null){
			int extend = 2;
			if (hero.hasTalent(Talent.THINK_MONSTER)){
				extend = 2 + 2*hero.pointsInTalent(Talent.THINK_MONSTER);
			}
			form.extend(extend);
		}

		//더 칭찬해 주세요!: 아드레날린 중 처치 시 갑옷 충전 2/4/6/8
		if (hero.hasTalent(Talent.PRAISE_MORE) && hero.buff(Adrenaline.class) != null){
			gainArmorCharge(hero, 2f*hero.pointsInTalent(Talent.PRAISE_MORE));
		}
	}

	public static void gainArmorCharge( Hero hero, float amount ){
		if (hero.belongings.armor() instanceof ClassArmor){
			ClassArmor armor = (ClassArmor) hero.belongings.armor();
			armor.charge = Math.min(100f, armor.charge + amount);
			armor.updateQuickslot();
		}
	}

	// ===== 증거 발견이에요! =====

	public static void onTypeIdentified(){
		Hero hero = Dungeon.hero;
		if (hero == null || !hero.isAlive() || !hero.hasTalent(Talent.EVIDENCE_FOUND)) return;
		int heal = 2 + 2*hero.pointsInTalent(Talent.EVIDENCE_FOUND); //4/6
		heal = Math.min(heal, hero.HT - hero.HP);
		if (heal > 0){
			hero.HP += heal;
			if (hero.sprite != null) {
				hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(heal), FloatingText.HEALING);
			}
		}
	}

}
