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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.HealthBar;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

public class IconTitle extends Component {

	private static final float FONT_SIZE = 9;

	private static final float GAP = 2;

	protected Image imIcon;
	protected RenderedTextBlock tfLabel;
	protected HealthBar health;

	private float healthLvl = Float.NaN;

	//[Manosaba] 띄어쓰기 없는 긴 제목이 칸을 넘을 때 글씨 크기를 줄이기 위해 기억해 둠
	private String labelText = "";
	private int labelColor = Window.TITLE_COLOR;
	private int labelSize = (int)FONT_SIZE;

	public IconTitle() {
		super();
	}

	public IconTitle( Item item ) {
		ItemSprite icon = new ItemSprite();
		icon( icon );
		label( Messages.titleCase( item.title() ) );
		icon.view( item );
		layout();
	}
	
	public IconTitle( Heap heap ){
		ItemSprite icon = new ItemSprite();
		icon( icon );
		label( Messages.titleCase( heap.title() ) );
		icon.view( heap );
		layout();
	}

	public IconTitle( Image icon, String label ) {
		icon( icon );
		label( label );
		layout();
	}

	@Override
	protected void createChildren() {
		imIcon = new Image();
		add( imIcon );

		tfLabel = PixelScene.renderTextBlock( (int)FONT_SIZE );
		tfLabel.hardlight( Window.TITLE_COLOR );
		tfLabel.setHightlighting(false);
		add( tfLabel );

		health = new HealthBar();
		add( health );
	}

	@Override
	protected void layout() {

		health.visible = !Float.isNaN( healthLvl );

		imIcon.x = x + (Math.max(0, 8 - imIcon.width()/2));
		imIcon.y = y + (Math.max(0, 8 - imIcon.height()/2));
		PixelScene.align(imIcon);

		int imWidth = (int)Math.max(imIcon.width(), 16);
		int imHeight = (int)Math.max(imIcon.height(), 16);

		int maxW = (int)(width - (imWidth + GAP));
		//[Manosaba] 한 덩어리 제목(예: 셰리짱귀여워사랑해잘했어대단해천재야!)이 칸을 넘으면
		//줄이 넘어가거나 잘리는 대신 글씨를 최소 6까지 줄임
		if (labelSize != (int)FONT_SIZE) setLabelSize((int)FONT_SIZE);
		tfLabel.maxWidth(maxW);
		while (maxW > 0 && tfLabel.width() > maxW && labelSize > 6){
			setLabelSize(labelSize - 1);
			tfLabel.maxWidth(maxW);
		}
		tfLabel.setPos(x + imWidth + GAP,
						imHeight > tfLabel.height() ? y +(imHeight - tfLabel.height()) / 2 : y);
		PixelScene.align(tfLabel);

		if (health.visible) {
			health.setRect( tfLabel.left(), tfLabel.bottom(), tfLabel.maxWidth(), 0 );
			height = Math.max( imHeight, health.bottom() );
		} else {
			height = Math.max( imHeight, tfLabel.height() );
		}
	}

	public float reqWidth(){
		return imIcon.width() + tfLabel.width() + GAP;
	}

	public void icon( Image icon ) {
		if (icon != null) {
			remove(imIcon);
			add(imIcon = icon);
		}
	}

	public void label( String label ) {
		labelText = label;
		tfLabel.text( label );
	}

	public void label( String label, int color ) {
		labelText = label;
		labelColor = color;
		tfLabel.text( label );
		tfLabel.hardlight( color );
	}

	public void color( int color ) {
		labelColor = color;
		tfLabel.hardlight( color );
	}

	//[Manosaba] 같은 내용·색으로 글씨 크기만 바꾼 제목으로 교체
	private void setLabelSize( int size ){
		RenderedTextBlock old = tfLabel;
		tfLabel = PixelScene.renderTextBlock( size );
		tfLabel.hardlight( labelColor );
		tfLabel.setHightlighting( false );
		tfLabel.text( labelText );
		replace( old, tfLabel );
		old.destroy();
		labelSize = size;
	}

	public float alpha(){
		return imIcon.alpha();
	}

	public void alpha( float value ){
		tfLabel.alpha(value);
		imIcon.alpha(value);
	}

	public void health( float value ) {
		health.level( healthLvl = value );
		layout();
	}
}
