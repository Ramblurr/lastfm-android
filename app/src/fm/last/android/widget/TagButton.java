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
	private Paint mRectPaint;
	private TextPaint mTextPaint;
	
	// animation support
	boolean newPosition = true;
	int old_x;
	int old_y;
	
	// TODO remove hardcoded values from ImageButton
	private float mTextSize = 17;
	private int mTagButtonHeight;
	private int mTextBottomPadding = 2;
	private int mBgColor = 0xff719ef6;
	private int mHeadOffset;
	private int mTailOffset;
	
	private Bitmap mHead;
	private Bitmap mTail;

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
		
		/* Init painter used to draw the rectangle behind the text */
		mRectPaint = new Paint();
		mRectPaint.setColor(mBgColor);
		
		/* Init painter used to draw the text inside the tag button */
	    mTextPaint = new TextPaint();
	    mTextPaint.setTextSize(mTextSize);
	    mTextPaint.setARGB(255, 255, 255, 255);
	    mTextPaint.setAntiAlias(true);
	    
	    /* Load head and tail .png */
	    mHead = BitmapFactory.decodeResource(getResources(), R.drawable.tag_button_head);
	    mTail = BitmapFactory.decodeResource(getResources(), R.drawable.tag_button_tail);
	    
	    /* Setting some values*/
	    mTagButtonHeight = mHead.getHeight();
	    mHeadOffset = mHead.getWidth();
	    mTailOffset = mTail.getWidth();

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
		
		// drawing tag head and tag tail
		canvas.drawBitmap(mHead, 0, 0, null);
		canvas.drawBitmap(mTail, mHeadOffset+getTextWidth(mText), 0, null);
		
		// drawing rectangle behind text
		Rect r = new Rect(mHeadOffset, 
				0, 
				mHeadOffset+getTextWidth(mText), 
				getHeight());
		canvas.drawRect(r, mRectPaint);
		
		// drawing tag text
		canvas.drawText(mText, 
				mHeadOffset, 
				(getHeight()+mTextSize)/2-mTextBottomPadding, 
				mTextPaint);
		
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
