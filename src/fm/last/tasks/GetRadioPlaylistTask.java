package fm.last.tasks;

import fm.last.LastfmRadio;
import fm.last.android.AndroidLastFmServerFactory;
import fm.last.api.LastFmServer;
import fm.last.api.Session;
import fm.last.api.RadioPlayList;
import androidx.util.GUITask;
import androidx.util.ResultReceiver;

public class GetRadioPlaylistTask implements GUITask {
	private LastFmServer server;
	private ResultReceiver<RadioPlayList> resultReceiver;
	private RadioPlayList playlist;
	
	private GetRadioPlaylistTask() {
		server = AndroidLastFmServerFactory.getServer();
	}
	
	public GetRadioPlaylistTask(ResultReceiver<RadioPlayList> resultReceiver) {
		this();
		this.resultReceiver = resultReceiver;
	}
	
	public void executeNonGuiTask() throws Exception {
		Session session = LastfmRadio.getInstance().getSession();
		String sk = session.getKey();
		playlist = server.getRadioPlayList(sk);
	}

	public void handle_exception(Throwable t) {
		resultReceiver.handle_exception(t);
	}

	public void after_execute() {
		resultReceiver.resultObtained(playlist);
	}
}

