package fm.last.tasks;

import com.google.gwt.user.client.rpc.AsyncCallback;

import fm.last.LastfmRadio;
import fm.last.android.AndroidLastFmServerFactory;
import fm.last.api.LastFmServer;
import fm.last.api.Session;
import fm.last.api.RadioPlayList;
import androidx.util.GUITask;

public class GetRadioPlaylistTask implements GUITask {
	private LastFmServer server;
	private AsyncCallback<RadioPlayList> resultReceiver;
	private RadioPlayList playlist;
	
	private GetRadioPlaylistTask() {
		server = AndroidLastFmServerFactory.getServer();
	}
	
	public GetRadioPlaylistTask(AsyncCallback<RadioPlayList> resultReceiver) {
		this();
		this.resultReceiver = resultReceiver;
	}
	
	public void executeNonGuiTask() throws Exception {
		Session session = LastfmRadio.getInstance().getSession();
		String sk = session.getKey();
		playlist = server.getRadioPlayList(sk);
	}

	public void onFailure(Throwable t) {
		resultReceiver.onFailure(t);
	}

	public void after_execute() {
		resultReceiver.onSuccess(playlist);
	}
}

