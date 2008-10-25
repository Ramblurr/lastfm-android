package fm.last.tasks;

import fm.last.LastFmApplication;
import fm.last.android.AndroidLastFmServerFactory;
import fm.last.api.Artist;
import fm.last.api.LastFmServer;
import fm.last.api.Session;
import fm.last.api.Station;
import android.net.Uri;
import androidx.util.GUITask;
import androidx.util.ResultReceiver;

public class TuneRadioTask implements GUITask {
	private String artist;
	private Station radioStation;
	private LastFmServer server;
	private ResultReceiver<Station> resultReceiver;
	
	private TuneRadioTask() {
		server = AndroidLastFmServerFactory.getServer();
	}
	
	public TuneRadioTask(String artist, ResultReceiver<Station> resultReceiver) {
		this();
		this.artist = artist;
		this.resultReceiver = resultReceiver;
	}
	
	public void executeNonGuiTask() throws Exception {
		String station = "lastfm://artist/" + Uri.encode( artist ) + "/similarartists";
		Session session = LastFmApplication.instance().getSession();
		String sk = session.getKey();
		radioStation = server.tuneToSimilarArtist(station, sk);
	}

	public void handle_exception(Throwable t) {
		resultReceiver.handle_exception(t);
	}

	public void after_execute() {
		resultReceiver.resultObtained(radioStation);
	}
}
