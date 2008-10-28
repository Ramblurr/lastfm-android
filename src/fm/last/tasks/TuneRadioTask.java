package fm.last.tasks;

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
	private ResultReceiver<Station> resultReceiver;
	
	private TuneRadioTask() {
		server = AndroidLastFmServerFactory.getServer();
	}
	
	public TuneRadioTask(String station, ResultReceiver<Station> resultReceiver) {
		this();
		this.station = station;
		this.resultReceiver = resultReceiver;
	}
	
	public void executeNonGuiTask() throws Exception {
		Session session = LastfmRadio.getInstance().getSession();
		String sk = session.getKey();
		radioStation = server.tuneToStation(station, sk);
	}

	public void handle_exception(Throwable t) {
		resultReceiver.handle_exception(t);
	}

	public void after_execute() {
		resultReceiver.onSuccess(radioStation);
	}
}
