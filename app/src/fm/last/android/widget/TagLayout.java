package fm.last.android.widget;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import fm.last.android.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;

/**
 * Layout/container for TagButtons
 * 
 * @author Lukasz Wisniewski
 */
public class TagLayout extends ViewGroup {

	public static final String TAG = "TagLayout";

	TagLayoutListener mListener;

	Map<String, TagButton> mTagButtons;
	
	/**
	 * Padding between buttons
	 */
	int mPadding;
	
	/**
	 * Animation turned on/off
	 */
	boolean mAnimationEnabled;
	
	/**
	 * Indicator whether an animation 
	 * is ongoing or not
	 */
	boolean mAnimating;

	/**
	 * Container for TagButton animations
	 */
	ArrayList<Animation> mAnimations;

	public TagLayout(Context context) {
		super(context);
		init();
	}

	public TagLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * Sharable code between constructors
	 */
	private void init(){
		mTagButtons = new TreeMap<String, TagButton>();
		mPadding = 5; // TODO get from xml layout
		mAnimationEnabled = false;
		mAnimating = false;

		mAnimations = new ArrayList<Animation>();
	}

	/**
	 * Adds tag by creating button inside TagLayout
	 * 
	 * @param tag
	 */
	public void addTag(final String tag){
		TagButton tagButton = new TagButton(this.getContext());
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		tagButton.setText(tag);

		tagButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				removeTag(tag);				
			}

		});
		tagButton.setVisibility(View.INVISIBLE);

		mTagButtons.put(tag, tagButton);
		this.addView(tagButton, params);

		if(mAnimationEnabled){
			Animation a = AnimationUtils.loadAnimation(this.getContext(), R.anim.tag_fadein);
			tagButton.startAnimation(a);
		}
	}

	/**
	 * Removes TagButton from TagLayout
	 * 
	 * @param tag
	 */
	private void removeTag(final String tag){
		if(!mAnimationEnabled){
			reallyRemoveTag(tag);
			return;
		}

		TagButton tb = mTagButtons.get(tag);
		Animation a = AnimationUtils.loadAnimation(this.getContext(), R.anim.tag_fadeout);
		a.setAnimationListener(new AnimationListener(){

			public void onAnimationEnd(Animation animation) {
				mAnimating = false;
				TagLayout.this.requestLayout();
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
			}

		});
		mAnimating = true;
		tb.startAnimation(a);
		reallyRemoveTag(tag);
	}

	/**
	 * Sharable part of code, which really
	 * removes the tag
	 * 
	 * @param tag
	 */
	private void reallyRemoveTag(String tag){
		this.removeView(mTagButtons.remove(tag));
		if(mListener != null){
			mListener.tagRemoved(tag);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		Log.i(TAG, "onMeasue()");

		int selfw = getMeasuredWidth();
		int selfh = getMeasuredHeight();

		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			if (child.getVisibility() != GONE) {
				//LayoutParams lp = (LayoutParams) child.getLayoutParams();
				child.measure(MeasureSpec.makeMeasureSpec(selfw,
						MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(
								selfh, MeasureSpec.AT_MOST));
			}
		} 
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.i(TAG, "onLayout()");
		if(mAnimating){
			return;
		}

		int selfw = getMeasuredWidth();
		//int selfh = getMeasuredHeight();

		int x = mPadding;
		int y = mPadding;

		for (Map.Entry<String, TagButton> entry : mTagButtons.entrySet()){
			TagButton child = entry.getValue();

			int cw = child.getMeasuredWidth();
			int ch = child.getMeasuredHeight();
			Log.i(TAG, "child("+entry.getKey()+") size - "+cw+","+ch);

			// tag doesn't fit the row, move it to next one
			if(x+cw>selfw){
				x = mPadding;
				y = y + ch +mPadding;
			}

			child.layout(x, y, x+cw, y+ch);
			
			if(mAnimationEnabled){
				Animation a = child.createTranslateAnimation(400);
				if(a != null){
					child.startAnimation(a);
				}
			}

			x = x + cw + mPadding;
		}

	}

	public void setTagLayoutListener(TagLayoutListener l){
		this.mListener = l;
	}

	/**
	 * Enables/disables fancy animations
	 * 
	 * @param value
	 */
	public void setAnimationsEnabled(boolean value){
		this.mAnimationEnabled = value;
	}

}
