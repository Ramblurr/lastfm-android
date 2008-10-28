package androidx.util;

import java.net.URL;
import java.util.WeakHashMap;

import android.graphics.Bitmap;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ImageLoader {
	private static ImageLoader instance;
	
	public static ImageLoader getInstance() {
		if (instance == null) {
			instance = new ImageLoader();
		}
		return instance;
	}
	
	private WeakHashMap<String, Bitmap> urlToBitmap;
	
	private ImageLoader() {
		urlToBitmap = new WeakHashMap<String, Bitmap>();
	}

	public void downloadImage(URL url, AsyncCallback<Bitmap> callback) {
		String urlAsString = url.toExternalForm();
		Bitmap cachedBitmap = urlToBitmap.get(urlAsString);
		if (cachedBitmap != null) {
			callback.onSuccess(cachedBitmap);
		} else {
			doDownload(url, callback);
		}
	}

	private void cacheBitmap(URL url, Bitmap bitmap) {
		String urlAsString = url.toExternalForm();
		urlToBitmap.put(urlAsString, bitmap);
	}
	
	private class CachingCallback implements AsyncCallback<Bitmap> {
		URL url;
		CachingCallback(URL _url) {
			url = _url;
		}
		public void onSuccess(Bitmap result) {
			cacheBitmap(url, result);
		}
		public void onFailure(Throwable t) {
		}
	};
	
	private void doDownload(URL url, AsyncCallback<Bitmap> callback) {
		callback = new AsyncCallbackPair<Bitmap>(new CachingCallback(url), callback);
		GUITaskQueue.getInstance().addTask(new DownloadImageTask(url, callback));
	}	
}
