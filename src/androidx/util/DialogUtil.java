package androidx.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

public class DialogUtil {
	private DialogUtil() {
	}

	public static void showAlertDialog(Activity activity, int resTitle, int resMessage, int resIcon, long millisToShow) {
		Dialog alert = (new AlertDialog.Builder(activity).setTitle(
				resTitle).setIcon(resIcon)
				.setMessage(resMessage)).create();
		alert.show();
		// dismiss the alert dialog after 2 seconds
		GUITaskQueue.getInstance().addTask(new DismissLaterTask(alert, millisToShow));
		// call finish on this activity after the alert dialog is dismissed
		
		GUITaskQueue.getInstance().addTask(new FinishLaterTask(activity, Activity.RESULT_CANCELED, 0));
	}
}
