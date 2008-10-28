package fm.last.tasks;

import com.google.gwt.user.client.rpc.AsyncCallback;

import fm.last.android.AndroidLastFmServerFactory;
import fm.last.api.LastFmServer;
import fm.last.api.MD5;
import fm.last.api.Session;
//import fm.last.radio.RadioHandshake;
import androidx.util.GUITask;

public class AuthenticationTask implements GUITask {
  private String username;
  private String md5Password;
//  private RadioHandshake m_loginTest;
  private AsyncCallback<Session> resultReceiver;
  private Session session;

  public AuthenticationTask(String username, String md5Password, AsyncCallback<Session> resultReceiver) {
    this.username = username;
    this.md5Password = md5Password;
    this.resultReceiver = resultReceiver;
  }
  
  public void executeNonGuiTask() throws Exception {
    LastFmServer server = AndroidLastFmServerFactory.getServer();
    String authToken = MD5.getInstance().hash(username + md5Password);
    session = server.getMobileSession(username, authToken);
  }

  public void onFailure(Throwable t) {
    resultReceiver.onFailure(t);
  }

  public void after_execute() {
    resultReceiver.onSuccess(session);
  }
}
