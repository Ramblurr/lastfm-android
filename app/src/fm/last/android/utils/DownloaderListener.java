package fm.last.android.utils;

/**
 * Listener describing basic asynchronous download operation
 * 
 * @author Lukasz Wisniewski
 */
public interface DownloaderListener {
	/**
	 * Notifies of asynchronous operation started, can be used
	 * to start indeterminate progressBar in UI thread 
	 */
	public void asynOperationStarted();
	
	/**
	 * Notifies of asynchronous operation ended, can be used
	 * to end indeterminate progressBar in UI thread
	 */
	public void asynOperationEnded();
}
