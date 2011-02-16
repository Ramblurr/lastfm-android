/**
 * Works around Android Bug 6191 by catching IllegalArgumentException after
 * detached from the window.
 *
 * @author Eric Burke (eric@squareup.com)
 */

package fm.last.android.widget;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ViewFlipper;

public class SafeViewFlipper extends ViewFlipper {
  private static final String TAG = "Last.fm";

public SafeViewFlipper(Context context) {
    super(context);
  }

  public SafeViewFlipper(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  /**
   * Workaround for Android Bug 6191:
   * http://code.google.com/p/android/issues/detail?id=6191
   * <p/>
   * ViewFlipper occasionally throws an IllegalArgumentException after
   * screen rotations.
   */
  @Override protected void onDetachedFromWindow() {
    try {
      super.onDetachedFromWindow();
    } catch (IllegalArgumentException e) {
      Log.d(TAG, "SafeViewFlipper ignoring IllegalArgumentException");

      // Call stopFlipping() in order to kick off updateRunning()
      stopFlipping();
    }
  }
}