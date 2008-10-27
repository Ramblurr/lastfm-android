package fm.last;

import android.net.Uri;
import androidx.util.GUITaskQueue;
import androidx.util.ProgressIndicator;
import androidx.util.ResultReceiver;
import androidx.util.ResultReceiverPair;
import fm.last.api.Session;
import fm.last.api.Station;
import fm.last.tasks.AuthenticationTask;
import fm.last.tasks.TuneRadioTask;

public class LastfmRadio {
	private static LastfmRadio instance;
	
	public static LastfmRadio getInstance() {
		if (instance == null) {
			instance = new LastfmRadio();
		}
		return instance;
	}

	private Session session;
	private Station currentStation;
	
	private ResultReceiver<Session> sessionResult = new ResultReceiver<Session>() {
		public void handle_exception(Throwable t) {
		}

		public void resultObtained(Session result) {
			setSession(result);
		}
	};
	
	private ResultReceiver<Station> stationResult = new ResultReceiver<Station>() {
		public void resultObtained(Station station) {
			setCurrentStation(station);
		}
		
		public void handle_exception(Throwable t) {
		}
	};
	
	private LastfmRadio() {
	}
	
	public void obtainSession(ProgressIndicator progressIndicator, String username, String md5password, ResultReceiver<Session> resultReceiver) {
		// start grabbing a session key in the background
		// let the radio be notified of the session
		GUITaskQueue.getInstance().addTask(progressIndicator,
				new AuthenticationTask(username, md5password, new ResultReceiverPair<Session>(sessionResult, resultReceiver)));
	}
	
	private void setCurrentStation(Station station) {
		currentStation = station;
	}
	
	public Station getCurrentStation() {
		return currentStation;
	}
	
	public void setSession(Session session) {
		this.session = session;
	}
	
	public Session getSession() {
		return session;
	}
	
	public void tuneToSimilarArtist(ProgressIndicator progressIndicator, String artist, ResultReceiver<Station> resultReceiver) {
		String station = "lastfm://artist/" + Uri.encode( artist ) + "/similarartists";
		GUITaskQueue.getInstance().addTask(progressIndicator, new TuneRadioTask(station, new ResultReceiverPair<Station>(stationResult, resultReceiver)));
	}
	
}
