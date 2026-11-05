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


package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Regeneration;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.noa.CopyDrawingTracker;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.noa.Noa;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.noa.NoaRain;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.noa.NoaWitchPower;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.noa.PaintButterflies;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.noa.WndNoaGarden;
import com.shatteredpixel.shatteredpixeldungeon.effects.Enchanting;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GeyserTrap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

/**
 * [Manosaba] 죠가사키 노아의 페인트 통 (원본 도적 망토처럼 착용하는 유물).
 * 대상에 따라 효과가 달라짐: 적 느려짐 3턴 / 아군 보호막 10 + 회복 10 / 자신 보호막 10 / 빈 땅 물 폭탄.
 * 30턴마다 1칸 충전. 처음 3칸, 망토처럼 쓸수록 경험치가 쌓여 자동으로 레벨업, 레벨마다 +1칸 (최대 10칸).
 */
public class PaintCan extends Artifact {

	{
		image = ItemSpriteSheet.BREW_AQUA; //TODO 페인트 통 전용 그림

		exp = 0;
		levelCap = 7;

		charge = 3;
		partialCharge = 0;
		chargeCap = 3;

		defaultAction = AC_PAINT;

		unique = true;
		bones = false;
	}

	public static final String AC_PAINT = "PAINT";
	public static final String AC_DRAW  = "DRAW";

	public static final float TURNS_PER_CHARGE = 30f;

	//최대 10칸
	public static final int MAX_CAP = 10;

	public int charges(){
		return charge;
	}

	private boolean usable( Hero hero ){
		return (isEquipped(hero) || hero.hasTalent(Talent.STREET_ARTIST))
				&& !cursed && hero.buff(MagicImmune.class) == null;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (usable(hero)) {
			actions.add(AC_PAINT);
			if (hero.subClass == HeroSubClass.NOA_GARDEN){
				actions.add(AC_DRAW);
			}
		}
		return actions;
	}

	@Override
	public String actionName(String action, Hero hero) {
		if (action.equals(AC_PAINT)) return Messages.get(this, "ac_paint");
		if (action.equals(AC_DRAW)) return Messages.get(this, "ac_draw");
		return super.actionName(action, hero);
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute(hero, action);
		if (hero.buff(MagicImmune.class) != null) return;

		if (action.equals(AC_PAINT) || action.equals(AC_DRAW)) {
			if (!isEquipped(hero) && !hero.hasTalent(Talent.STREET_ARTIST)){
				GLog.i( Messages.get(Artifact.class, "need_to_equip") );
				return;
			}
			if (cursed){
				GLog.i( Messages.get(this, "cursed") );
				return;
			}
			if (action.equals(AC_DRAW)){
				GameScene.show(new WndNoaGarden(hero));
				return;
			}
			if (!canPay(hero, 1)){
				GLog.i( Messages.get(this, "no_charge") );
				return;
			}
			curUser = hero;
			GameScene.selectCell(targeter);
		}
	}

	// ===== 충전 =====

	//가짜 예술가: 모자란 칸 1칸당 체력 6/4/2 (이 특성으로 죽지 않음). 불가능하면 -1
	public int hpCostFor( Hero hero, int needed ){
		int missing = Math.max(0, needed - charge);
		if (missing == 0) return 0;
		if (!hero.hasTalent(Talent.FAKE_ARTIST)) return -1;
		int per = 8 - 2 * hero.pointsInTalent(Talent.FAKE_ARTIST);
		return missing * per;
	}

	public boolean canPay( Hero hero, int needed ){
		int hp = hpCostFor(hero, needed);
		return hp == 0 || (hp > 0 && hero.HP > hp);
	}

	public boolean payCharges( Hero hero, int needed ){
		if (!canPay(hero, needed)){
			GLog.w( Messages.get(this, "no_charge") );
			return false;
		}
		int hp = hpCostFor(hero, needed);
		int spent = Math.min(charge, needed); //[Manosaba] 체력으로 낸 몫은 경험치에 넣지 않음
		charge = Math.max(0, charge - needed);
		gainExp(spent);
		if (hp > 0){
			hero.HP -= hp;
			hero.sprite.showStatus(com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.NEGATIVE, Integer.toString(-hp));
		}
		onChargesSpent(hero);
		updateQuickslot();
		return true;
	}

	private void onChargesSpent( Hero hero ){
		//흩뿌려진 페인트: 충전이 0이 되면 보호막 3/5
		if (charge == 0 && hero.hasTalent(Talent.SCATTERED_PAINT)){
			Noa.giveShield(hero, 1 + 2*hero.pointsInTalent(Talent.SCATTERED_PAINT), false);
		}
		//검게 물드는 눈물: 페인트 통을 쓸 때마다 마녀 수치 +5/10/15
		if (hero.subClass == HeroSubClass.NOA_WITCH && hero.hasTalent(Talent.BLACK_TEARS)){
			NoaWitchPower.add(hero, 5 * hero.pointsInTalent(Talent.BLACK_TEARS));
		}
		Talent.onArtifactUsed(hero);
	}

	//[Manosaba] 자동 강화: 페인트 1칸을 쓸 때마다 경험치 1, (레벨+1)*4 만큼 쌓이면 레벨업
	//0->1: 4칸, 1->2: 8칸 ... 6->7: 28칸 (최대 레벨까지 총 112칸)
	public static final int EXP_PER_LEVEL = 4;

	private void gainExp( int spentCharges ){
		if (spentCharges <= 0 || level() >= levelCap) return;
		exp += spentCharges;
		boolean leveled = false;
		while (level() < levelCap && exp >= (level()+1) * EXP_PER_LEVEL){
			exp -= (level()+1) * EXP_PER_LEVEL;
			upgrade();
			leveled = true;
		}
		if (level() >= levelCap) exp = 0;
		if (leveled) GLog.p( Messages.get(this, "levelup") );
	}

	//칸 단위 충전 (소수점은 부분 충전으로 쌓임)
	public void addCharge( float amount ){
		if (charge >= chargeCap) return;
		partialCharge += amount;
		while (partialCharge >= 1f){
			charge++;
			partialCharge -= 1f;
		}
		if (charge >= chargeCap){
			charge = chargeCap;
			partialCharge = 0;
		}
		updateQuickslot();
	}

	//드리핑: 남은 페인트를 모두 소모
	public int drainAll(){
		int used = charge;
		charge = 0;
		gainExp(used);
		partialCharge = 0;
		if (Dungeon.hero != null) onChargesSpent(Dungeon.hero);
		updateQuickslot();
		return used;
	}

	public float chargeRateMultiplier( Hero hero ){
		float mult = RingOfEnergy.artifactChargeMultiplier(hero);
		if (!isEquipped(hero)){
			mult *= 0.25f * hero.pointsInTalent(Talent.STREET_ARTIST);
		}
		//비로 차오르는 물감: 비가 내리는 곳에 있으면 +50/100/150/200%
		if (hero.hasTalent(Talent.RAIN_PAINT) && NoaRain.inRain(hero.pos)){
			mult *= 1f + 0.5f * hero.pointsInTalent(Talent.RAIN_PAINT);
		}
		return mult;
	}

	@Override
	public void charge( Hero target, float amount ) {
		//유물 충전 효과(와, 밥이다! 등): amount턴만큼 충전
		if (cursed || target.buff(MagicImmune.class) != null) return;
		addCharge(amount / TURNS_PER_CHARGE);
	}

	@Override
	public Item upgrade() {
		chargeCap = Math.min(chargeCap + 1, MAX_CAP);
		return super.upgrade();
	}

	@Override
	public void restoreFromBundle( com.watabou.utils.Bundle bundle ) {
		super.restoreFromBundle(bundle);
		chargeCap = Math.min(3 + level(), MAX_CAP);
	}

	@Override
	public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
		if (super.doUnequip(hero, collect, single)){
			if (collect && hero.hasTalent(Talent.STREET_ARTIST)){
				activate(hero);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean collect( Bag container ) {
		if (super.collect(container)){
			if (container.owner instanceof Hero
					&& passiveBuff == null
					&& ((Hero) container.owner).hasTalent(Talent.STREET_ARTIST)){
				activate((Hero) container.owner);
			}
			return true;
		}
		return false;
	}

	@Override
	protected void onDetach() {
		if (passiveBuff != null){
			passiveBuff.detach();
			passiveBuff = null;
		}
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new PaintRecharge();
	}

	@Override
	public int value() {
		return 0;
	}

	@Override
	public String desc() {
		String desc = super.desc();
		if (isEquipped(Dungeon.hero) || (Dungeon.hero != null && Dungeon.hero.hasTalent(Talent.STREET_ARTIST))){
			if (cursed) desc += "\n\n" + Messages.get(this, "desc_cursed");
		}
		return desc;
	}

	public class PaintRecharge extends ArtifactBuff {
		@Override
		public boolean act() {
			Hero hero = Dungeon.hero;
			if (charge < chargeCap && !cursed && target.buff(MagicImmune.class) == null && Regeneration.regenOn()) {
				partialCharge += chargeRateMultiplier(hero) / TURNS_PER_CHARGE;
				while (partialCharge >= 1) {
					charge++;
					partialCharge -= 1;
					if (charge >= chargeCap){
						charge = chargeCap;
						partialCharge = 0;
					}
				}
			} else if (charge >= chargeCap) {
				partialCharge = 0;
			}
			updateQuickslot();
			spend( TICK );
			return true;
		}
	}

	// ===== 대상 지정 =====

	private static final int[][] BUTTERFLY = {
			{1,1,0,0,0,0,0,1,1},
			{1,1,1,0,0,0,1,1,1},
			{1,1,1,1,0,1,1,1,1},
			{0,1,1,1,1,1,1,1,0},
			{1,1,1,1,0,1,1,1,1},
			{1,1,1,0,0,0,1,1,1},
			{0,0,0,0,1,0,0,0,0}
	};

	private CellSelector.Listener targeter = new CellSelector.Listener() {

		@Override
		public void onSelect( Integer target ) {
			if (target == null) return;
			final Hero hero = curUser;
			if (target == hero.pos){
				if (!payCharges(hero, 1)) return;
				hero.sprite.operate(hero.pos);
				if (paintSelf(hero)){
					hero.spend(1f); //다시 읽는 주문서가 행동을 이어서 처리
				} else {
					hero.spendAndNext(1f);
				}
				return;
			}

			final Ballistica shot = new Ballistica(hero.pos, target, Ballistica.PROJECTILE);
			final int cell = shot.collisionPos;
			if (!payCharges(hero, 1)) return;

			hero.sprite.zap(cell);
			hero.busy();
			Sample.INSTANCE.play(Assets.Sounds.ZAP);
			MagicMissile.boltFromChar(hero.sprite.parent, MagicMissile.RAINBOW, hero.sprite, cell, new Callback() {
				@Override
				public void call() {
					paintCell(hero, cell, shot);
					hero.spendAndNext(1f);
				}
			});
			QuickSlotButton.target(Actor.findChar(cell));
		}

		@Override
		public String prompt() {
			return Messages.get(PaintCan.class, "prompt");
		}
	};

	//주문서를 다시 읽으면 true
	private boolean paintSelf( Hero hero ){
		Noa.giveShield(hero, 10, false);

		//따라 그리기!: 마지막으로 읽은 주문서를 한 번 더 사용 (주문서 한 장당 한 번)
		CopyDrawingTracker tracker = hero.buff(CopyDrawingTracker.class);
		if (tracker != null && tracker.item != null){
			final Item item = Reflection.newInstance(tracker.item);
			tracker.detach();
			if (item instanceof Scroll){
				item.setCurrent(hero);
				Enchanting.show(hero, item);
				((Scroll) item).anonymize();
				((Scroll) item).talentChance = 0;
				ShatteredPixelDungeon.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						((Scroll) item).doRead();
					}
				});
				return true;
			}
		}
		return false;
	}

	private void paintCell( Hero hero, int cell, Ballistica shot ){
		Char ch = Actor.findChar(cell);
		if (ch != null && ch != hero){
			if (ch.alignment == Char.Alignment.ALLY){
				Noa.paintAlly(hero, ch, 10, 10, false);
			} else {
				float turns = 3f + hero.pointsInTalent(Talent.STICKY_PAINT);
				Noa.paintEnemy(hero, ch, turns);
			}
			return;
		}
		waterBomb(hero, cell, shot);
	}

	//빈 땅: 물 폭탄 (상상하는 대로: 5x5 / 큰 나비 모양)
	private void waterBomb( Hero hero, int cell, Ballistica shot ){
		GeyserTrap geyser = new GeyserTrap();
		geyser.pos = cell;
		geyser.source = this;
		if (shot.path.size() > shot.dist+1) {
			geyser.centerKnockBackDirection = shot.path.get(shot.dist + 1);
		}
		geyser.activate();

		int imagine = hero.pointsInTalent(Talent.AS_IMAGINED);
		if (imagine > 0){
			int w = Dungeon.level.width();
			int cx = cell % w, cy = cell / w;
			if (imagine == 1){
				for (int y = cy-2; y <= cy+2; y++) for (int x = cx-2; x <= cx+2; x++){
					wetXY(x, y);
				}
			} else {
				for (int r = 0; r < BUTTERFLY.length; r++){
					for (int c = 0; c < BUTTERFLY[r].length; c++){
						if (BUTTERFLY[r][c] == 1) wetXY(cx - 4 + c, cy - 3 + r);
					}
				}
			}
		}

		//나비가 날라가~: 그 자리에서 이어진 물로 나비가 퍼짐
		if (hero.hasTalent(Talent.BUTTERFLIES) && Dungeon.level.water[cell]){
			PaintButterflies.spread(cell);
		}
	}

	private void wetXY( int x, int y ){
		if (x < 0 || y < 0 || x >= Dungeon.level.width() || y >= Dungeon.level.height()) return;
		Noa.wetCell(x + y*Dungeon.level.width(), 10);
	}
}
