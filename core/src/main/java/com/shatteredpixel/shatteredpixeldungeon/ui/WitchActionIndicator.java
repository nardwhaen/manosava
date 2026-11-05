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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Visual;

//[Manosaba] 두 번째 능력 버튼: 마녀화 전용 (원본 능력 버튼과 따로 표시되어 소환·연계 버튼을 가리지 않음)
public class WitchActionIndicator extends Tag {

	Visual primaryVis;
	Visual secondVis;

	public static ActionIndicator.Action action;
	public static WitchActionIndicator instance;

	public WitchActionIndicator() {
		super( 0 );

		instance = this;

		setSize( SIZE, SIZE );
		visible = false;
	}
	
	@Override
	public void destroy() {
		super.destroy();
		instance = null;
	}
	
	@Override
	protected synchronized void layout() {
		super.layout();
		
		if (primaryVis != null){
			if (!flipped)   primaryVis.x = x + (SIZE - primaryVis.width()) / 2f + 1;
			else            primaryVis.x = x + width - (SIZE + primaryVis.width()) / 2f - 1;
			primaryVis.y = y + (height - primaryVis.height()) / 2f;
			PixelScene.align(primaryVis);
			if (secondVis != null){
				if (secondVis.width() > 16) secondVis.x = primaryVis.center().x - secondVis.width()/2f;
				else                        secondVis.x = primaryVis.center().x + 8 - secondVis.width();
				if (secondVis instanceof BitmapText){
					//need a special case here for text unfortunately
					secondVis.y = primaryVis.center().y + 8 - ((BitmapText) secondVis).baseLine();
				} else {
					secondVis.y = primaryVis.center().y + 8 - secondVis.height();
				}
				PixelScene.align(secondVis);
			}
		}
	}
	
	private boolean needsRefresh = false;
	
	@Override
	public void update() {
		super.update();

		synchronized (WitchActionIndicator.class) {
			if (!visible && action != null) {
				visible = true;
				needsRefresh = true;
				flash();
			} else {
				visible = action != null;
			}

			if (needsRefresh) {
				if (primaryVis != null) {
					primaryVis.destroy();
					primaryVis.killAndErase();
					primaryVis = null;
				}
				if (secondVis != null) {
					secondVis.destroy();
					secondVis.killAndErase();
					secondVis = null;
				}
				if (action != null) {
					primaryVis = action.primaryVisual();
					add(primaryVis);

					secondVis = action.secondaryVisual();
					if (secondVis != null) {
						add(secondVis);
					}

					setColor(action.indicatorColor());
				}

				layout();
				needsRefresh = false;
			}

			if (!Dungeon.hero.ready) {
				if (primaryVis != null) primaryVis.alpha(0.5f);
				if (secondVis != null) secondVis.alpha(0.5f);
			} else {
				if (primaryVis != null) primaryVis.alpha(1f);
				if (secondVis != null) secondVis.alpha(1f);
			}
		}

	}

	@Override
	protected void onClick() {
		super.onClick();
		if (action != null && Dungeon.hero.ready) {
			action.doAction();
		}
	}

	@Override
	protected String hoverText() {
		String text = (action == null ? null : action.actionName());
		if (text != null){
			return Messages.titleCase(text);
		} else {
			return null;
		}
	}

	public static void setAction(ActionIndicator.Action action){
		synchronized (WitchActionIndicator.class) {
			WitchActionIndicator.action = action;
			refresh();
		}
	}

	public static void clearAction(){
		clearAction(null);
	}

	public static void clearAction(ActionIndicator.Action action){
		synchronized (WitchActionIndicator.class) {
			if (action == null || WitchActionIndicator.action == action) {
				WitchActionIndicator.action = null;
			}
		}
	}

	public static void refresh(){
		synchronized (WitchActionIndicator.class) {
			if (instance != null) {
				instance.needsRefresh = true;
			}
		}
	}
}
