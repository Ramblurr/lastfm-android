package androidx.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;

public class DialogUtil {
	private DialogUtil() {
	}

	public static void showAlertDialog(Context context, int resTitle, int resMessage, int resIcon, long millisToShow) {
		Dialog alert = (new AlertDialog.Builder(context).setTitle(
				resTitle).setIcon(resIcon)
				.setMessage(resMessage)).create();
		alert.show();
		// dismiss the alert dialog after 2 seconds
		GUITaskQueue.getInstance().addTask(new DismissLaterTask(alert, millisToShow));
	}
}
