/**
 * 
 */
package fm.last.android.widget;

import java.net.URL;
import fm.last.android.R;
import fm.last.android.utils.UserTask;
import fm.last.util.UrlUtil;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.graphics.Bitmap;

/**
 * @author sam
 *
 */
public class AlbumArt extends ImageView {
	
	private FetchArtTask _fetchTask;
	private int _defaultImageResource = R.drawable.no_artwork;
	private Bitmap _bitmap;
	
	public AlbumArt(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		
		setImageResource(_defaultImageResource);
	}
	
	public Bitmap getBitmap() {
		return _bitmap;
	}
	
	@Override
	public void setImageBitmap(Bitmap bmp) {
		super.setImageBitmap(bmp);
		_bitmap = bmp;
	}
	
	public void cancel() {
		if(_fetchTask != null)
			_fetchTask.cancel(true);
	}
	
	public void setDefaultImageResource(int res) {
		_defaultImageResource = res;
		setImageResource(_defaultImageResource);
	}
	
	public void fetch(String URL) {
		if(_fetchTask != null)
			_fetchTask.cancel(true);
		
		_fetchTask = new FetchArtTask(URL);
		_fetchTask.execute((Void)null);
	}
	
    private class FetchArtTask extends UserTask<Void, Void, Boolean> {
    	Bitmap mBitmap = null;
    	String mURL = null;
    	
    	public FetchArtTask(String url) {
    		super();
    		
    		mURL = url;
    		Log.i("LastFm", "Fetching: " + mURL);
    	}
    	
        @Override
    	public void onPreExecute() {
        	setImageResource(_defaultImageResource);
        }
    	
        @Override
        public Boolean doInBackground(Void...params) {
            boolean success = false;
    		try {
   				mBitmap = UrlUtil.getImage(new URL(mURL));
    			success = true;
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
            return success;
        }

        @Override
        public void onPostExecute(Boolean result) {
        	if(result && !isCancelled()) {
        		setImageBitmap(mBitmap);
        		_fetchTask = null;
        	}
        }
    }

}
