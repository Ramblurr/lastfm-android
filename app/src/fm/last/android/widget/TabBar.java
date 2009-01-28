package fm.last.android.widget;

import java.util.ArrayList;

import fm.last.android.R;
import fm.last.android.R.id;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
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
	
	private ArrayList<View> mTabs = new ArrayList();
	
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
		
		int i = 0;
		int current = -1;
		int previous = -1;
		for (View v: mTabs)
		{
			if (v.isSelected()) previous = i;
			if (tab == v) current = i;
			i++;
			
			v.setSelected( false );
			v.setFocusable( true );
			v.setClickable( true );
		}
		
		tab.setSelected( true );
		tab.setFocusable( false );
		tab.setClickable( false );
		if (mViewFlipper != null) mViewFlipper.setDisplayedChild( current );		
		
		if (mListener != null)
			mListener.tabChanged( current, previous );
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
