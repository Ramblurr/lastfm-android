package fm.last;

import android.net.Uri;
import androidx.util.ExceptionHandler;
import androidx.util.GUITaskQueue;
import androidx.util.ProgressIndicator;
import androidx.util.ResultReceiver;
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
	private ExceptionHandler sessionExceptionHandler;
	
	private ResultReceiver<Session> sessionResult = new ResultReceiver<Session>() {
		public void handle_exception(Throwable t) {
			if (sessionExceptionHandler != null) {
				sessionExceptionHandler.handle_exception(t);
			}
		}

		public void resultObtained(Session result) {
			setSession(result);
			sessionExceptionHandler = null;
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
	
	public void obtainSession(ProgressIndicator progressIndicator, String username, String md5password, ExceptionHandler handler) {
		sessionExceptionHandler = handler;
		// start grabbing a session key in the background
		// let the radio be notified of the session
		GUITaskQueue.getInstance().addTask(progressIndicator,
				new AuthenticationTask(username, md5password, sessionResult));
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
	
}
