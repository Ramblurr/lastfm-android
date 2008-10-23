package androidx.util;

import android.app.Activity;
import android.content.Intent;

public class FinishLaterTask implements GUITask {
	private Activity activity;
	private int resultCode;
	private Intent data;
	private long millisBeforeDismiss;

	public FinishLaterTask(Activity activity, int resultCode, Intent data, long millisBeforeDismiss) {
		this.activity = activity;
		this.resultCode = resultCode;
		this.data = data;
		this.millisBeforeDismiss = millisBeforeDismiss;
	}

	public FinishLaterTask(Activity activity, int resultCode, long millisBeforeDismiss) {
		this(activity, resultCode, null, millisBeforeDismiss);
	}
	
	public FinishLaterTask(Activity activity, long millisBeforeDismiss) {
		this(activity, Activity.RESULT_OK, millisBeforeDismiss);
	}
	
	public void executeNonGuiTask() throws Exception {
		if (millisBeforeDismiss > 0) {
			Thread.sleep(millisBeforeDismiss);
		}
	}

	public void after_execute() {
		if (data != null) {
			activity.setResult(resultCode, data);
		} else {
			activity.setResult(resultCode);
		}
	}

	public void handle_exception(Throwable t) {
		// TODO Auto-generated method stub
		activity.setResult(Activity.RESULT_CANCELED);
	}

}
