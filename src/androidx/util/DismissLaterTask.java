package androidx.util;

import android.app.Dialog;
import android.util.Log;

public class DismissLaterTask implements GUITask {
	private Dialog dialog;
	private long millisBeforeDismiss;
	
	public DismissLaterTask(Dialog dialog, long millisBeforeDismiss) {
		this.dialog = dialog;
		this.millisBeforeDismiss = millisBeforeDismiss;
	}
	
	public void executeNonGuiTask() throws Exception {
		Thread.sleep(millisBeforeDismiss);
	}

	public void after_execute() {
		dialog.dismiss();
	}

	public void handle_exception(Throwable t) {
		Log.e("androidx", "Exception!", t);
	}
}
