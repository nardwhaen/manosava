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
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpiritHawk;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.PaintCan;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ElementalSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SnakeSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

/**
 * 환상의 정원: 노아가 페인트로 그려 낸 동료.
 * 공통: 죽을 때까지 남음, 동시 소환 제한 없음, 층을 옮기면 따라오지 않음, 스탯 고정,
 * 명중·회피는 노아와 같음, 불 면역, 5턴마다 체력 1 감소(보호막부터).
 */
public abstract class NoaSummon extends Mob {

	{
		alignment = Alignment.ALLY;
		WANDERING = new FollowNoa();
		state = WANDERING;
		intelligentAlly = false; //층을 옮길 때 따라오지 않음

		immunities.add(Burning.class);
		immunities.add(Fire.class);

		EXP = 0;
		maxLvl = -2;
	}

	public enum Kind {
		RABBIT(2), SNAKE(4), BIRD(6), DRAGON(10);

		public final int cost;
		Kind(int cost){ this.cost = cost; }

		public String title(){
			return Messages.get(NoaSummon.class, name().toLowerCase() + "_name");
		}
		public String desc(){
			return Messages.get(NoaSummon.class, name().toLowerCase() + "_short");
		}

		public NoaSummon create(){
			switch (this){
				case RABBIT: default: return new Rabbit();
				case SNAKE: return new Snake();
				case BIRD: return new Bird();
				case DRAGON: return new Dragon();
			}
		}
	}

	public abstract Kind kind();

	//싸울 적이 없으면 노아 곁으로 돌아옴
	protected class FollowNoa extends Wandering {
		@Override
		protected int randomDestination() {
			return Dungeon.hero != null ? Dungeon.hero.pos : super.randomDestination();
		}
	}

	protected int dmgMin, dmgMax, armMin, armMax;

	private int decayCounter = 0;
	protected int attacks = 0;

	public static NoaSummon summon( Kind kind, int cell ){
		NoaSummon s = kind.create();
		s.pos = cell;
		s.HP = s.HT;
		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.add(s);
		s.onSummoned();
		CellEmitter.get(cell).burst(com.shatteredpixel.shatteredpixeldungeon.effects.particles.RainbowParticle.BURST, 12);
		return s;
	}

	protected void onSummoned(){}

	protected Hero hero(){
		return Dungeon.hero;
	}

	@Override
	public int attackSkill( Char target ) {
		return hero() != null ? hero().attackSkill(target) : 10;
	}

	@Override
	public int defenseSkill( Char enemy ) {
		return hero() != null ? hero().defenseSkill(enemy) : 5;
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(dmgMin, dmgMax);
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(armMin, armMax);
	}

	@Override
	protected boolean act() {
		//5턴마다 체력 1 감소 (보호막이 있으면 보호막부터)
		decayCounter++;
		if (decayCounter >= 5){
			decayCounter = 0;
			ShieldBuff shield = buff(SummonShield.class);
			if (shield != null && shield.shielding() > 0){
				shield.absorbDamage(1);
			} else {
				HP--;
				if (HP <= 0){
					die(null);
					return true;
				}
			}
		}
		return super.act();
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		attacks++;
		//살아 움직이는 그림 +3: 용과 토끼는 3번 공격할 때마다 불을 뿜음
		if ((this instanceof Rabbit || this instanceof Dragon)
				&& hero() != null && hero().pointsInTalent(Talent.LIVING_PAINTING) >= 3
				&& attacks % 3 == 0){
			breatheFire(enemy.pos);
		}
		return super.attackProc(enemy, damage);
	}

	//공격 방향으로 직선 3칸에 적만 태우는 불
	protected void breatheFire( int towards ){
		Ballistica b = new Ballistica(pos, towards, Ballistica.STOP_SOLID);
		final java.util.ArrayList<Integer> cells = new java.util.ArrayList<>();
		for (int i = 1; i < b.path.size() && cells.size() < 3; i++){
			int c = b.path.get(i);
			if (Dungeon.level.solid[c]) break;
			cells.add(c);
		}
		if (cells.isEmpty()) return;
		int last = cells.get(cells.size()-1);
		if (sprite != null && sprite.visible){
			MagicMissile.boltFromChar(sprite.parent, MagicMissile.FIRE, sprite, last, new Callback() {
				@Override
				public void call() {}
			});
		}
		for (int c : cells){
			CellEmitter.get(c).burst(FlameParticle.FACTORY, 6);
			Char ch = Actor.findChar(c);
			if (ch != null && ch.alignment == Alignment.ENEMY && ch.isAlive() && !ch.isImmune(Burning.class)){
				Buff.affect(ch, Burning.class).reignite(ch);
			}
		}
	}

	@Override
	public void die( Object cause ) {
		super.die(cause);
	}

	//다시 그려야 겠다~: 요구 페인트의 30/40/50% 만큼 페인트 통 충전
	public void onDeathRefund(){
		Hero h = hero();
		if (h == null || !h.hasTalent(Talent.REDRAW)) return;
		PaintCan can = Noa.paintCan(h);
		if (can == null) return;
		can.addCharge(kind().cost * (0.2f + 0.1f * h.pointsInTalent(Talent.REDRAW)));
	}

	@Override
	public String description() {
		return Messages.get(NoaSummon.class, kind().name().toLowerCase() + "_desc");
	}

	@Override
	public String name() {
		return kind().title();
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("decay", decayCounter);
		bundle.put("attacks", attacks);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		decayCounter = bundle.getInt("decay");
		attacks = bundle.getInt("attacks");
	}

	//줄어들지 않는 소환 보호막 (5턴마다 줄어드는 체력 규칙에서만 깎임)
	public static class SummonShield extends ShieldBuff {
		{
			type = buffType.POSITIVE;
		}
	}

	protected static void tint( CharSprite s, float r, float g, float b ){
		s.hardlight(r, g, b);
	}

	// ===== 뿔 달린 토끼 (2칸) =====
	public static class Rabbit extends NoaSummon {
		{
			spriteClass = RatSprite.class;
			HT = 1;
			dmgMin = 5; dmgMax = 10; armMin = 0; armMax = 4;
		}
		@Override public Kind kind(){ return Kind.RABBIT; }
		@Override
		protected void onSummoned() {
			int shield = (hero() != null && hero().hasTalent(Talent.LIVING_PAINTING)) ? 20 : 10;
			Buff.affect(this, SummonShield.class).setShield(shield);
		}
		@Override
		public CharSprite sprite() {
			CharSprite s = super.sprite(); tint(s, 1f, 0.95f, 1f); return s;
		}
	}

	// ===== 새하얗고 신성한 뱀 (4칸) =====
	public static class Snake extends NoaSummon {
		{
			spriteClass = SnakeSprite.class;
			HT = 30;
			dmgMin = 7; dmgMax = 10; armMin = 0; armMax = 6;
		}
		@Override public Kind kind(){ return Kind.SNAKE; }
		@Override
		public int attackProc( Char enemy, int damage ) {
			int turns = (hero() != null && hero().pointsInTalent(Talent.LIVING_PAINTING) >= 2) ? 8 : 5;
			Buff.affect(enemy, Poison.class).set(turns);
			return super.attackProc(enemy, damage);
		}
		@Override
		public CharSprite sprite() {
			CharSprite s = super.sprite(); tint(s, 1f, 1f, 1f); return s;
		}
	}

	// ===== 날카로운 발톱을 가진 새 (6칸) =====
	public static class Bird extends NoaSummon {
		{
			spriteClass = SpiritHawk.HawkSprite.class;
			HT = 30;
			dmgMin = 15; dmgMax = 20; armMin = 0; armMax = 6;
			flying = true;
		}
		private boolean struck = false;
		@Override public Kind kind(){ return Kind.BIRD; }
		private boolean ambush(){
			return !struck && hero() != null && hero().pointsInTalent(Talent.LIVING_PAINTING) >= 2;
		}
		@Override
		public float speed() {
			return super.speed() * (ambush() ? 5f : 3f);
		}
		@Override
		public float attackDelay() {
			return super.attackDelay() / 1.5f;
		}
		@Override
		public int damageRoll() {
			if (ambush()) return Random.NormalIntRange(25, 35);
			return super.damageRoll();
		}
		@Override
		public int attackProc( Char enemy, int damage ) {
			struck = true;
			return super.attackProc(enemy, damage);
		}
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put("struck", struck);
		}
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			struck = bundle.getBoolean("struck");
		}
	}

	// ===== 전설의 용 (10칸) =====
	public static class Dragon extends NoaSummon {
		{
			spriteClass = ElementalSprite.Fire.class;
			HT = 90; //[Manosaba] 체력 90, 방어 6~10, 이동·공격 속도 1, 근거리, 비행
			dmgMin = 20; dmgMax = 25; armMin = 6; armMax = 10;
			flying = true;
		}
		@Override public Kind kind(){ return Kind.DRAGON; }
	}
}
