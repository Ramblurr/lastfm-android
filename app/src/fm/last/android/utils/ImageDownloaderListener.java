package fm.last.android.utils;

/**
 * Listener to asynchronous download of images
 * 
 * @author Lukasz Wisniewski
 */
public interface ImageDownloaderListener extends DownloaderListener{
	
	/**
	 * Notifies on images total download progress
	 * 
	 * @param imageDownloaded
	 * @param imageCount
	 */
	public void imageDownloadProgress(int imageDownloaded, int imageCount);
}
