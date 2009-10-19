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
import android.widget.ImageView;
import android.graphics.Bitmap;

/**
 * @author sam
 *
 */
public class AlbumArt extends ImageView {
	
	private FetchAdTask _fetchTask;
	private String _artURL;
	private int _defaultImageResource = R.drawable.no_artwork;
	
	public AlbumArt(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		
		setImageResource(_defaultImageResource);
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
		
		_artURL = URL;
		_fetchTask = new FetchAdTask();
		_fetchTask.execute((Void)null);
	}
	
    private class FetchAdTask extends UserTask<Void, Void, Boolean> {
    	Bitmap mBitmap = null;
    	
        @Override
    	public void onPreExecute() {
        	setImageResource(_defaultImageResource);
        }
    	
        @Override
        public Boolean doInBackground(Void...params) {
            boolean success = false;
    		try {
   				mBitmap = UrlUtil.getImage(new URL(_artURL));
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
