package androidx.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.WeakHashMap;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

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
	
	private static class ImageViewCallback implements AsyncCallback<Bitmap> {
		private ImageView imageView;
		private int errorImageResource;
		
		ImageViewCallback(ImageView _imageView, int _errorImageResource) {
			imageView = _imageView;
			errorImageResource = _errorImageResource;
		}
		
		public void onSuccess(Bitmap result) {
			imageView.setImageBitmap(result);
		}

		public void onFailure(Throwable t) {
			imageView.setImageResource(errorImageResource);
		}

	};
	
	public void loadImage(String urlAsString, ImageView view ) {
		loadImage(urlAsString, view, android.R.drawable.screen_background_dark);
	}

	public void loadImage(String urlAsString, ImageView view, int errorImageResource ) {
		URL url = null;
		if (urlAsString != null) {
			try {
				url = new URL(urlAsString);
			} catch (MalformedURLException e) {
				Log.e("androidx", "", e);
			}
		}
		downloadImage(url, new ImageViewCallback(view, errorImageResource));
	}
	

	public void downloadImage(URL url, AsyncCallback<Bitmap> callback) {
		if (url == null) {
			callback.onFailure(new IOException("Bad URL"));
			return;
		}
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
