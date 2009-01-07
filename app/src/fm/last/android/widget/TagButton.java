package fm.last.android.widget;

import fm.last.android.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;

// TODO usage of NinePath
/**
 * Class representing simple tag widget
 * 
 * @author Lukasz Wisniewski
 */
public class TagButton extends ImageButton {
	
	public static final String TAG = "TagButton";
	
	private String mText;
	private TextPaint mTextPaint;
	
	// animation support
	boolean newPosition = true;
	int old_x;
	int old_y;
	
	// TODO remove hardcoded values from ImageButton
	private float mTextSize = 17;
	private int mTagButtonHeight;
	private int mTextBottomPadding = 2;
	private int mHeadOffset;
	private int mTailOffset;
	
	private Bitmap mHead_rest;
	private Bitmap mTail_rest;
	private Bitmap mMiddle_rest;
	private Bitmap mHead_focus;
	private Bitmap mTail_focus;
	private Bitmap mMiddle_focus;

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
		setBackgroundDrawable(null);
		
		mText = "";
		
		/* Init painter used to draw the text inside the tag button */
	    mTextPaint = new TextPaint();
	    mTextPaint.setTextSize(mTextSize);
	    mTextPaint.setARGB(255, 255, 255, 255);
	    mTextPaint.setAntiAlias(true);
	    
	    /* Load resources */
	    mHead_rest = BitmapFactory.decodeResource(getResources(), R.drawable.tag_rest_left);
	    mTail_rest = BitmapFactory.decodeResource(getResources(), R.drawable.tag_rest_right);
	    mMiddle_rest = BitmapFactory.decodeResource(getResources(), R.drawable.tag_rest_middle);
	    mHead_focus = BitmapFactory.decodeResource(getResources(), R.drawable.tag_focus_left);
	    mTail_focus = BitmapFactory.decodeResource(getResources(), R.drawable.tag_focus_right);
	    mMiddle_focus = BitmapFactory.decodeResource(getResources(), R.drawable.tag_focus_middle);
	    
	    /* Setting some values*/
	    mTagButtonHeight = mHead_rest.getHeight();
	    mHeadOffset = mHead_rest.getWidth();
	    mTailOffset = mTail_rest.getWidth();

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

	@Override
	protected void onDraw(Canvas canvas) {
		
		if(this.isFocused()) {
			// drawing tag head and tag tail
			canvas.drawBitmap(mHead_focus, 0, 0, null);
			canvas.drawBitmap(mTail_focus, mHeadOffset+getTextWidth(mText), 0, null);
			
			// drawing rectangle behind text
			Rect r = new Rect(mHeadOffset, 
					0, 
					mHeadOffset+getTextWidth(mText), 
					getHeight());
			canvas.drawBitmap(mMiddle_focus, null, r, null);
			
			// drawing tag text
			canvas.drawText(mText, 
					mHeadOffset, 
					(getHeight()+mTextSize)/2-mTextBottomPadding, 
					mTextPaint);
		} else {
			// drawing tag head and tag tail
			canvas.drawBitmap(mHead_rest, 0, 0, null);
			canvas.drawBitmap(mTail_rest, mHeadOffset+getTextWidth(mText), 0, null);
			
			// drawing rectangle behind text
			Rect r = new Rect(mHeadOffset, 
					0, 
					mHeadOffset+getTextWidth(mText), 
					getHeight());
			canvas.drawBitmap(mMiddle_rest, null, r, null);
			
			// drawing tag text
			canvas.drawText(mText, 
					mHeadOffset, 
					(getHeight()+mTextSize)/2-mTextBottomPadding, 
					mTextPaint);
		}
		super.onDraw(canvas);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		heightMeasureSpec = mTagButtonHeight ;
		widthMeasureSpec = mHeadOffset + getTextWidth(mText) + mTailOffset;
		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
		
		//super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	public void setText(String text) {
		mText = text;
	}
	
	/**
	 * Returns text width within mTextPaint
	 * 
	 * @param text
	 * @return
	 */
	private int getTextWidth(String text)
	{
		int count = text.length();
		float[] widths = new float[count];
		mTextPaint.getTextWidths(text, widths);
		int textWidth = 0;
		for (int i = 0; i < count; i++)
			textWidth += widths[i];
		return textWidth;
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
