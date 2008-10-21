package fm.last.tasks;

import fm.last.Utils;
import fm.last.radio.RadioHandshake;
import android.app.ProgressDialog;
import android.widget.EditText;
import androidx.util.GUITask;

public class AuthenticationTask implements GUITask {
  private EditText userField;
  private EditText passwordField;
  private ProgressDialog m_progress;
  private String userName;
  private String userPassword;
  private RadioHandshake m_loginTest;

  public AuthenticationTask(EditText userField, EditText passwordField, 
      ProgressDialog m_progress) {
    this.userField = userField;
    this.passwordField = passwordField;
    this.m_progress = m_progress;
    userName = userField.getText().toString();
    userPassword = Utils.md5(passwordField.getText().toString());
  }
  
  public void executeNonGuiTask() throws Exception {
    m_loginTest = new RadioHandshake(userName, userPassword);
    m_loginTest.connect();
  }

  public void handle_exception(Throwable t) {
    m_progress.dismiss();
  }

  public void after_execute() {
    m_progress.dismiss();
  }

 
}
