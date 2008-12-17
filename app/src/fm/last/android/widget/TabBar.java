package fm.last.android.widget;

import java.util.Hashtable;

import fm.last.android.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * Simple tab-like widget managing ViewFlipper instance
 * 
 * @author Lukasz Wisniewski
 */
public class TabBar extends LinearLayout {

	/**
	 * Internal interface defining tabs
	 * 
	 * @author Lukasz Wisniewski
	 */
	private interface Tab {
		void setActive();
		void setInactive();
		View getView();
	}

	/**
	 * Text only tab
	 * 
	 * @author Lukasz Wisniewski
	 */
	private class TabText extends Button implements Tab{

		private void init(){
			this.setTextColor(0xffffffff); //TODO remove hardcoded value white
			this.setGravity(Gravity.CENTER);
		}

		public TabText(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			init();
		}

		public TabText(Context context, AttributeSet attrs) {
			super(context, attrs);
			init();
		}

		public TabText(Context context) {
			super(context);
			init();
		}

		@Override
		public void setActive() {
			setBackgroundColor(mActiveTabColor);
		}

		@Override
		public void setInactive() {
			setBackgroundColor(mInactiveTabColor);
		}

		@Override
		public View getView() {
			return this;
		}

	}
	
	/**
	 * Text & image tab, see /res/layout/tab_image.xml
	 * 
	 * @author Lukasz Wisniewski
	 */
	private class TabImage extends LinearLayout implements Tab{
		
		private ImageButton mImageButton;
		private TextView mTextView;
		
		private int mActiveId;
		private int mInactiveId;

		private void init(){
			LayoutInflater.from(getContext()).inflate(R.layout.tab_image, this);
			mImageButton = (ImageButton) findViewById(R.id.tab_image_button);
			mTextView = (TextView) findViewById (R.id.tab_text_view);
			mTextView.setTextColor(0xffffffff); // TODO remove hardcoded value
		}
		
		public TabImage(Context context, AttributeSet attrs) {
			super(context, attrs);
			init();
		}

		public TabImage(Context context) {
			super(context);
			init();
		}

		public void setData(String text, int active, int inactive){
			mTextView.setText(text);
			mActiveId = active;
			mInactiveId = inactive;
			//requestLayout();
		}

		@Override
		public void setActive() {
			setBackgroundColor(mActiveTabColor);
			mImageButton.setImageResource(mActiveId);
		}

		@Override
		public void setInactive() {
			setBackgroundColor(mInactiveTabColor);
			mImageButton.setImageResource(mInactiveId);
		}
		
		@Override
		public View getView() {
			return this;
		}

		@Override
		public void setOnClickListener(OnClickListener l) {
			mTextView.setOnClickListener(l);
			mImageButton.setOnClickListener(l);
			super.setOnClickListener(l);
		}
		
	}

	private Hashtable<String, Tab> mTabs;

	private ViewFlipper mViewFlipper;
	private int mActiveTabColor;
	private int mInactiveTabColor;
	private int mPadding;
	private String mActiveTab;

	private TabBarListener mListener;

	public TabBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TabBar(Context context) {
		super(context);
		init();
	}

	/**
	 * Sharable code between constructors
	 */
	private void init(){
		mActiveTabColor = 0xff3a3a3c;
		mInactiveTabColor = 0xff1d1d1e;
		mPadding = 4;
	}

	/**
	 * Attaches container that will display the contents attached to tabs
	 * 
	 * @param vf
	 */
	public void setViewFlipper(ViewFlipper vf){
		mViewFlipper = vf;
	}
	
	/**
	 * Sets TabBarListener
	 * 
	 * @param l
	 */
	public void setListener(TabBarListener l){
		mListener = l;
	}

	/**
	 * Adds text only tab
	 * 
	 * @param text Text to be displayed on tab
	 * @param childIndex Child to which ViewFillper should switch when pressed
	 */
	public void addTab(String text, int childIndex){
		TabText tt = new TabText(getContext());
		tt.setText(text);
		tt.setInactive();
		configureTab(tt, text, childIndex);
	}
	
	/**
	 * Adds text & image tab
	 * 
	 * @param text Text to be displayed on tab
	 * @param active Image resId that will be displayed when tab is active
	 * @param inactive Image resId that will be displayed when tab is inactive
	 * @param childIndex Child to which ViewFillper should switch when pressed
	 */
	public void addTab(String text, int active, int inactive, int childIndex){
		TabImage ti = new TabImage(getContext());
		ti.setData(text, active, inactive);
		ti.setInactive();
		configureTab(ti, text, childIndex);
	}
	
	/**
	 * Does internal magic when adding the tab
	 * 
	 * @param v
	 * @param text
	 * @param childIndex
	 */
	private void configureTab(Tab v, final String text, final int childIndex){
		v.getView().setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				setActive(text);
				if(mViewFlipper != null){
					mViewFlipper.setDisplayedChild(childIndex);
				}
				if(mListener != null){
					mListener.tabChanged(text, childIndex);
				}
			}

		});
		getTabs().put(text, v);
		
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		params.weight = 1;
		params.setMargins(mPadding/2, 0, mPadding/2, 0);
		addViewInLayout(v.getView(), -1, params);
	}

	/**
	 * Sets a tab to active and disables the old one
	 * 
	 * @param text
	 */
	public void setActive(String text){
		if(mActiveTab != null){
			// deactivate old tab
			Tab tab = getTab(mActiveTab);
			if(tab != null){
				tab.setInactive();
			}
		}

		// activate new tab
		Tab tab = getTab(text);
		if(tab != null){
			tab.setActive();
		}

		mActiveTab = text;
	}

//	@Override
//	protected void onLayout(boolean changed, int l, int t, int r, int b) {		
//		int n = getChildCount();
//		int w = (getWidth()-mPadding*(n-2))/n;
//		int h = getHeight();
//		for(int i = 0; i<n; i++){
//			int x = i*(w+mPadding);
//			//getChildAt(i).layout(x, 0, x+w, h);
//			ViewGroup.LayoutParams params = getChildAt(i).getLayoutParams();
//			params.width = w;
//			getChildAt(i).setLayoutParams(params);
//			getChildAt(i).requestLayout();
//		}
//		super.onLayout(changed, l, t, r, b);
//		
//	}

	private Tab getTab(String text){
		if(getTabs().containsKey(text)){
			return getTabs().get(text);
		}
		return null;
	}

	private Hashtable<String, Tab> getTabs(){
		if(mTabs == null){
			mTabs = new Hashtable<String, Tab>();
		}
		return mTabs;
	}

}
