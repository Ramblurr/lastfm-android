package fm.last.tasks;

import com.google.gwt.user.client.rpc.AsyncCallback;

import fm.last.LastfmRadio;
import fm.last.android.AndroidLastFmServerFactory;
import fm.last.api.LastFmServer;
import fm.last.api.Session;
import fm.last.api.Station;
import androidx.util.GUITask;
import androidx.util.ResultReceiver;

public class TuneRadioTask implements GUITask {
	private String station;
	private Station radioStation;
	private LastFmServer server;
	private AsyncCallback<Station> resultReceiver;
	
	private TuneRadioTask() {
		server = AndroidLastFmServerFactory.getServer();
	}
	
	public TuneRadioTask(String station, AsyncCallback<Station> resultReceiver) {
		this();
		this.station = station;
		this.resultReceiver = resultReceiver;
	}
	
	public void executeNonGuiTask() throws Exception {
		Session session = LastfmRadio.getInstance().getSession();
		String sk = session.getKey();
		radioStation = server.tuneToStation(station, sk);
	}

	public void onFailure(Throwable t) {
		resultReceiver.onFailure(t);
	}

	public void after_execute() {
		resultReceiver.onSuccess(radioStation);
	}
}
