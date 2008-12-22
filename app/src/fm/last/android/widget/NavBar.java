package fm.last.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Extends LinearLayout providing default set of attributes
 * e.g. background color, layout gravity etc.
 * 
 * Attaches onClickListener to each child and notifies back the
 * owner of the class through unified NavBarListener
 * 
 * @author Lukasz Wisniewski
 */
public class NavBar extends LinearLayout {
	
	private NavBarListener mListener;

	public NavBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public NavBar(Context context) {
		super(context);
		init();
	}
	
	/**
	 * Attaches OnClickListener to the given child that
	 * will call NavBarListener on click event
	 * 
	 * @param child
	 * @param index
	 */
	private void attachListener(View child, final int index){
		child.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				if(mListener!=null){
					if(index == 0){
						mListener.backClicked(v);
						return;
					}
					if(index == getChildCount()-1){
						mListener.forwardClicked(v);
						return;
					}
					if((index > 0) && (index < getChildCount()-1)){
						mListener.middleClicked(v, index-1);
					}
				}
			}
			
		});
	}

	/**
	 * Sharable code between constructors
	 */
	private void init() {
		//LayoutInflater.from(getContext()).inflate(R.layout.nav_bar, this);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int n = this.getChildCount();
		
		// iterate through all children and attach OnClickListener to each
		for(int i=0; i<n; i++){
			attachListener(getChildAt(i), i);
		}
		
		super.onLayout(changed, l, t, r, b);
	}
	
	/**
	 * Sets listener of NavBar events
	 * 
	 * @param l NavBarListener instance
	 */
	public void setListener(NavBarListener l){
		mListener = l;
	}

}
