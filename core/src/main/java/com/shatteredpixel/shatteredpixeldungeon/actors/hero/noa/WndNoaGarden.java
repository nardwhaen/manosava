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

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.PaintCan;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

/**
 * 환상의 정원 소환 창 (원본 검투사 연계 창과 같은 방식)
 */
public class WndNoaGarden extends Window {

	private static final int WIDTH_P = 120;
	private static final int WIDTH_L = 180;
	private static final int MARGIN  = 2;

	public WndNoaGarden( final Hero hero ){
		super();

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		float pos = MARGIN;
		RenderedTextBlock title = PixelScene.renderTextBlock(Messages.titleCase(Messages.get(this, "title")), 9);
		title.hardlight(TITLE_COLOR);
		title.setPos((width-title.width())/2, pos);
		title.maxWidth(width - MARGIN * 2);
		add(title);
		pos = title.bottom() + MARGIN;

		RenderedTextBlock sub = PixelScene.renderTextBlock(Messages.get(this, "choose"), 6);
		sub.maxWidth(width - MARGIN * 2);
		sub.setPos(MARGIN, pos);
		add(sub);
		pos = sub.bottom() + 2*MARGIN;

		PaintCan can = Noa.paintCan(hero);

		for (final NoaSummon.Kind kind : NoaSummon.Kind.values()) {
			int have = can == null ? 0 : can.charges();
			int hpCost = can == null ? -1 : can.hpCostFor(hero, kind.cost);
			String cost;
			if (have >= kind.cost || hpCost < 0){
				cost = Messages.get(this, "cost", kind.title(), kind.cost);
			} else {
				cost = Messages.get(this, "cost_hp", kind.title(), have, hpCost);
			}
			String text = "_" + cost + "_ " + kind.desc();
			RedButton btn = new RedButton(text, 6){
				@Override
				protected void onClick() {
					super.onClick();
					hide();
					GardenAction.startSummon(hero, kind);
				}
			};
			btn.leftJustify = true;
			btn.multiline = true;
			btn.setSize(width, btn.reqHeight());
			btn.setRect(0, pos, width, btn.reqHeight());
			btn.enable(can != null && can.canPay(hero, kind.cost));
			add(btn);
			pos = btn.bottom() + MARGIN;
		}

		resize(width, (int)pos);
	}
}
