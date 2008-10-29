package androidx.util;

import java.io.IOException;
import java.net.URL;

import fm.last.util.UrlUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.util.GUITask;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class DownloadImageTask implements GUITask {
	private static final int MAX_IMAGE_BYTES = 2000000;
	private URL url;
	private AsyncCallback<Bitmap> resultReceiver;
	private Bitmap bitmap;
	
	public DownloadImageTask(URL url, AsyncCallback<Bitmap> resultReceiver) {
		this.url = url;
		this.resultReceiver = resultReceiver;
	}
	
	public void executeNonGuiTask() throws Exception {
		byte[] imageBytes = UrlUtil.doGetAndReturnBytes(url, MAX_IMAGE_BYTES);
		bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
		if (bitmap == null) {
			throw new IOException(url.toString() + " is not an image");
		}
	}

	public void onFailure(Throwable t) {
		resultReceiver.onFailure(t);
	}

	public void after_execute() {
		resultReceiver.onSuccess(bitmap);
	}
}

