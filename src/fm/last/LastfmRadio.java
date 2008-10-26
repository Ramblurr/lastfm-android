package fm.last;

import android.net.Uri;
import androidx.util.GUITaskQueue;
import androidx.util.ResultReceiver;
import fm.last.api.Session;
import fm.last.api.Station;
import fm.last.tasks.TuneRadioTask;

public class LastfmRadio implements ResultReceiver<Session> {
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
	
	public void tuneToSimilarArtist(String artist) {
		String station = "lastfm://artist/" + Uri.encode( artist ) + "/similarartists";
		GUITaskQueue.getInstance().addTask(new TuneRadioTask(station, stationResult));
	}

	
	public void resultObtained(Session session) {
		this.session = session;
	}	
	
	// called if there was a problem authenticating
	public void handle_exception(Throwable t) {
		Log.e(t);
	}

}
