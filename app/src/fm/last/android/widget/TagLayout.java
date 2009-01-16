package fm.last.android.widget;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import fm.last.android.R;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.TextView;

/**
 * Layout/container for TagButtons
 * 
 * @author Lukasz Wisniewski
 */
public class TagLayout extends ViewGroup {

	public static final String TAG = "TagLayout";

	TagLayoutListener mListener;

	Map<String, TagButton> mTagButtons;
	TextView mAreaHint;
	
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

	/**
	 * Area hint resource
	 */
	private int mAreaTextId = 0;

	public TagLayout(Context context) {
		super(context);
		init(context);
	}

	public TagLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	/**
	 * Sharable code between constructors
	 * 
	 * @param context
	 */
	private void init(Context context){
		mTagButtons = new TreeMap<String, TagButton>();
		mPadding = 5; // TODO get from xml layout
		mAnimationEnabled = false;
		mAnimating = false;

		mAnimations = new ArrayList<Animation>();
		this.setFocusable(true);
		
		// Creating area hint
		mAreaHint = new TextView(context);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mAreaHint.setVisibility(View.GONE);
		mAreaHint.setTextColor(0xff666666);
		mAreaHint.setGravity(Gravity.CENTER);
		mAreaHint.setTextSize(16);
		mAreaHint.setTypeface(mAreaHint.getTypeface(), Typeface.BOLD);
		this.addView(mAreaHint, params);
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
		
		// Area hint (on when count equals 1)
		if(count == 1 && mAreaTextId > 0){
			mAreaHint.setVisibility(View.VISIBLE);
		} else {
			mAreaHint.setVisibility(View.GONE);
		}
		
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
		int selfh = getMeasuredHeight();

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
		
		// positioning AreaHint
		if(mAreaHint.getVisibility() == View.VISIBLE){
			int cw = mAreaHint.getMeasuredWidth();
			int ch = mAreaHint.getMeasuredHeight();
			mAreaHint.layout((selfw-cw)/2, (selfh-ch)/2, (selfw+cw)/2, (selfh+ch)/2);
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
	
	/**
	 * Sets informative text which is displayed in the middle of TagLayout area
	 * before any TagButton has been added
	 * 
	 * @param resid resource Id
	 */
	public void setAreaHint(int resid){
		mAreaTextId = resid;
		mAreaHint.setText(resid);
	}

}
