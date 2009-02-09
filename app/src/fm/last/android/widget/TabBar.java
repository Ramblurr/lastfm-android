/***************************************************************************
 *   Copyright 2005-2009 Last.fm Ltd.                                      *
 *   Portions contributed by Casey Link, Lukasz Wisniewski,                *
 *   Mike Jennings, and Michael Novak Jr.                                  *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.         *
 ***************************************************************************/
package fm.last.android.widget;

import java.util.ArrayList;

import fm.last.android.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * Simple tab-like widget managing ViewFlipper instance
 * 
 * @author Lukasz Wisniewski
 */
public class TabBar extends LinearLayout 
{
	private ViewFlipper mViewFlipper = null;
	private TabBarListener mListener = null;
	
	public TabBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public TabBar(Context context) {
		super(context);
	}

	/**
	 * Attaches container that will display the contents attached to tabs
	 * 
	 * @param vf
	 */
	public void setViewFlipper(ViewFlipper vf) {
		mViewFlipper = vf;
	}

	/**
	 * @param text Text to be displayed on tab
	 */
	public View addTab( String text ) {
		return addTab( text, -1 );
	}
	
	private ArrayList<View> mTabs = new ArrayList<View>();
	
	/**
	 * Set the id of the @returned view if you want to observe it via the TabListener
	 * by default and for your convenience the id is set to the image_id */
	public View addTab( String text, int image_id )
	{
		TextView tab = (TextView) LayoutInflater.from(getContext()).inflate( R.layout.tab_image, null );
		tab.setText( text );
		tab.setOnClickListener( mOnClickListener );
		
		if( image_id != -1) {
			tab.setId( image_id );
			tab.setCompoundDrawablesWithIntrinsicBounds( null, getContext().getResources().getDrawable( image_id ), null, null );
		}
		
		if (mTabs.size() == 0) {
			tab.setSelected( true );
			tab.setClickable( false );
		} else {
			tab.setFocusable( true );
			((LinearLayout.LayoutParams)mTabs.get( mTabs.size()-1 ).getLayoutParams()).rightMargin = 6;
		}
		mTabs.add( tab );

		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
		params.weight = 1;
		addViewInLayout( tab, -1, params );
	
		return tab;
	}
	
	public void setActive( int tab_id )
	{
		for (View tab: mTabs)
			if (tab.getId() == tab_id)
				setActive( tab );
	}

	private void setActive( View tab )
	{
		if (tab == null) return;
		
		int previous = -1;
		int index = -1;
		int i = 0;
		for (View v: mTabs)
		{			
			if (v.isSelected()) previous = v.getId();
			if (tab == v) index = i;
			i++;
			
			v.setSelected( false );
			v.setFocusable( true );
			v.setClickable( true );
		}
		
		tab.setSelected( true );
		tab.setFocusable( false );
		tab.setClickable( false );
		
		if (mViewFlipper != null) mViewFlipper.setDisplayedChild( index );		
		if (mListener != null) mListener.tabChanged( tab.getId(), previous );
	}
	
	public int getActive()
	{
		for (View tab: mTabs)
			if (tab.isSelected())
				return tab.getId();
		return -1;	
	}

	OnClickListener mOnClickListener = new OnClickListener()
	{
		public void onClick( View v )
		{
			setActive( v );
		}
	};

	public void setListener(TabBarListener l)
	{
		mListener = l;
	}
}
