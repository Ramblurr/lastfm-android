package fm.last.tasks;

import fm.last.Utils;
import fm.last.android.AndroidLastFmServerFactory;
import fm.last.api.LastFmServer;
import fm.last.api.MD5;
import fm.last.api.Session;
import fm.last.radio.RadioHandshake;
import android.app.ProgressDialog;
import android.view.View.OnClickListener;
import android.widget.EditText;
import androidx.util.GUITask;
import androidx.util.ResultReceiver;

public class AuthenticationTask implements GUITask {
  private String username;
  private String md5Password;
  private ProgressDialog m_progress;
  private RadioHandshake m_loginTest;
  private ResultReceiver<RadioHandshake> resultReceiver;
  private Session session;

  public AuthenticationTask(String username, String md5Password, 
      ProgressDialog m_progress, ResultReceiver<RadioHandshake> resultReceiver) {
    this.username = username;
    this.md5Password = md5Password;
    this.m_progress = m_progress;
    this.resultReceiver = resultReceiver;
  }
  
  public void executeNonGuiTask() throws Exception {
    LastFmServer server = AndroidLastFmServerFactory.getServer();
    String authToken = MD5.getInstance().hash(username + md5Password);
    session = server.getMobileSession(username, authToken);
    m_loginTest = new RadioHandshake(username, md5Password);
    m_loginTest.connect();
  }

  public void handle_exception(Throwable t) {
    m_progress.dismiss();
    resultReceiver.handle_exception(t);
  }

  public void after_execute() {
    m_progress.dismiss();
    resultReceiver.resultObtained(m_loginTest);
  }

 
}
