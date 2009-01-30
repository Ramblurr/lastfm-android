package fm.last.android.widget;

import fm.last.android.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;

// TODO usage of NinePath
/**
 * Class representing simple tag widget
 * 
 * @author Lukasz Wisniewski
 */
public class TagButton extends Button {
	
	public static final String TAG = "TagButton";
	
	private String mText;
	
	// animation support
	boolean newPosition = true;
	int old_x;
	int old_y;
	
	public TagButton(Context context) {
		super(context);
		init();
	}

	public TagButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TagButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	/**
	 * Sharable code between constructors
	 */
	private void init(){
		setBackgroundResource(R.drawable.tag);
		setTextColor(0xFFFFFFFF);
		
		mText = "";
		
		this.setFocusable(true);
		this.setOnFocusChangeListener(new OnFocusChangeListener() {

			public void onFocusChange(View v, boolean hasFocus) {
				System.out.print("Focus changed!");
				if(v == TagButton.this && hasFocus) {
					System.out.print("Tag got focused!");
				}
			}
			
		});
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		Log.i(TAG, mText + ": dx = "+ left + " dy = " + top);
		if(newPosition){
			old_x = left;
			old_y = top;
			newPosition = false;
		}
		
		super.onLayout(changed, left, top, right, bottom);
	}

	public void setText(String text) {
		mText = text;
		super.setText(mText);
	}
	
	/**
	 * Generates translate animation on position change, must be executed from parent
	 * within onLayout method 
	 * 
	 * @param durationMillis
	 * @return
	 */
	public Animation createTranslateAnimation(long durationMillis){
		if(old_x == getLeft() && old_y == getTop()){
			return null;
		}
		
		int dx = getLeft() - old_x;
		int dy = getTop() - old_y;
		Animation a = new TranslateAnimation(-dx, 0, -dy, 0);
		a.setFillAfter(true);
		a.setDuration(durationMillis);
		
		old_x = getLeft();
		old_y = getTop();
		
		return a;
	}

}
