package fm.last.tasks;

import fm.last.android.AndroidLastFmServerFactory;
import fm.last.api.LastFmServer;
import fm.last.api.MD5;
import fm.last.api.Session;
//import fm.last.radio.RadioHandshake;
import androidx.util.GUITask;
import androidx.util.ResultReceiver;

public class AuthenticationTask implements GUITask {
  private String username;
  private String md5Password;
//  private RadioHandshake m_loginTest;
  private ResultReceiver<Session> resultReceiver;
  private Session session;

  public AuthenticationTask(String username, String md5Password, ResultReceiver<Session> resultReceiver) {
    this.username = username;
    this.md5Password = md5Password;
    this.resultReceiver = resultReceiver;
  }
  
  public void executeNonGuiTask() throws Exception {
    LastFmServer server = AndroidLastFmServerFactory.getServer();
    String authToken = MD5.getInstance().hash(username + md5Password);
    session = server.getMobileSession(username, authToken);
  }

  public void handle_exception(Throwable t) {
    resultReceiver.handle_exception(t);
  }

  public void after_execute() {
    resultReceiver.resultObtained(session);
  }
}
