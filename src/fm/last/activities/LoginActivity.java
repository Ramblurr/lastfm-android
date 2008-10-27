package fm.last.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import androidx.util.DialogUtil;
import androidx.util.FinishLaterTask;
import androidx.util.GUITaskQueue;
import androidx.util.ProgressIndicator;
import androidx.util.ResultReceiver;

import fm.last.R;
import fm.last.Log;
import fm.last.LastFmApplication;
import fm.last.LastfmRadio;
import fm.last.api.MD5;
import fm.last.api.Session;
import fm.last.tasks.AuthenticationTask;

public class LoginActivity extends Activity implements
		ResultReceiver<Session>, ProgressIndicator {
	private EditText userField, passwordField;
	// private final Handler m_handler = new Handler();
	ProgressDialog m_progress = null;
	private String md5Password;
	private String username;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.account_settings);

		final Button loginButton = (Button) findViewById(R.id.login_button);
		userField = (EditText) findViewById(R.id.login_username);
		passwordField = (EditText) findViewById(R.id.login_password);

		loginButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				doLogin();
			}
		});
	}

	public void showProgressIndicator() {
		String title = getResources().getString(R.string.authProgressTitle);
		String message = getResources().getString(R.string.authProgressMessage);
		m_progress = ProgressDialog.show(this, title, message, true);
	}
	
	public void hideProgressIndicator() {
		m_progress.dismiss();
	}
	
	private void doLogin() {
		username = userField.getText().toString();
		md5Password = MD5.getInstance().hash(passwordField.getText().toString());
		GUITaskQueue.getInstance().addTask(this,
				new AuthenticationTask(username, md5Password, this));
	}

	public void handle_exception(Throwable t) {
		DialogUtil.showAlertDialog(this, R.string.badAuthTitle, R.string.badAuth, R.drawable.icon, 2000);
		// call finish on this activity after the alert dialog is dismissed
		GUITaskQueue.getInstance().addTask(new FinishLaterTask(this, RESULT_CANCELED, 0));
	}

	public void resultObtained(Session session) {
		Log.i("We've got a session! session.key=" + session.getKey());
		
		// Save our credentials to our SharedPreferences
		LastFmApplication.instance().saveCredentials(username, md5Password);
		// set our session
		LastfmRadio.getInstance().setSession(session);
		setResult(RESULT_OK);
		finish();
	}


}
