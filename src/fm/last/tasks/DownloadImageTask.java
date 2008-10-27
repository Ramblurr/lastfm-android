package fm.last.tasks;

import java.io.IOException;
import java.net.URL;

import fm.last.util.UrlUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.util.GUITask;
import androidx.util.ResultReceiver;

public class DownloadImageTask implements GUITask {
	private static final int MAX_IMAGE_BYTES = 2000000;
	private URL url;
	private ResultReceiver<Bitmap> resultReceiver;
	private Bitmap bitmap;
	
	public DownloadImageTask(URL url, ResultReceiver<Bitmap> resultReceiver) {
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

	public void handle_exception(Throwable t) {
		resultReceiver.handle_exception(t);
	}

	public void after_execute() {
		resultReceiver.resultObtained(bitmap);
	}
}
