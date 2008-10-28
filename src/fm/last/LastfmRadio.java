package fm.last;

import android.net.Uri;
import androidx.util.GUITaskQueue;
import androidx.util.ProgressIndicator;
import androidx.util.ResultReceiver;
import androidx.util.ResultReceiverPair;
import fm.last.api.RadioPlayList;
import fm.last.api.RadioTrack;
import fm.last.api.Session;
import fm.last.api.Station;
import fm.last.tasks.AuthenticationTask;
import fm.last.tasks.GetRadioPlaylistTask;
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
	private RadioPlayList currentPlaylist;
	
	private ResultReceiver<Session> sessionResult = new ResultReceiver<Session>() {
		public void handle_exception(Throwable t) {
		}

		public void onSuccess(Session result) {
			setSession(result);
		}
	};
	
	private ResultReceiver<Station> stationResult = new ResultReceiver<Station>() {
		public void onSuccess(Station station) {
			setCurrentStation(station);
		}
		
		public void handle_exception(Throwable t) {
		}
	};
	
	private ResultReceiver<RadioPlayList> playlistResult = new ResultReceiver<RadioPlayList>() {
		public void onSuccess(RadioPlayList result) {
			setCurrentPlaylist(result);
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
	
	private void setCurrentPlaylist(RadioPlayList playlist) {
		currentPlaylist = playlist;
	}

	public RadioTrack getCurrentTrack() {
		if (currentPlaylist == null) {
			return null;
		}
		RadioTrack[] tracks = currentPlaylist.getTracks();
		if (tracks.length == 0) {
			Log.d("LastfmRadio.getCurrentTrack(): 0 tracks returned");
			return null;
		}
		return currentPlaylist.getTracks()[0];
	}
	
	private void moveToNextTrack() {
		// no-op for now
	}
	
	public Station getCurrentStation() {
		return currentStation;
	}
	
	public void setSession(Session session) {
		this.session = session;
	}
	
	public boolean isPlaying() {
		return (getCurrentTrack() != null);
	}
	
	public void play(ProgressIndicator progressIndicator, ResultReceiver<RadioTrack> trackReceiver) {
		RadioTrack track = getCurrentTrack();
		if (track == null) {
			playNext(progressIndicator, trackReceiver);
		} else {
			trackReceiver.onSuccess(track);
		}
	}

	/**
	 * Play the next track of the current playlist.
	 * 
	 * @param progressIndicator
	 * @param trackReceiver
	 */
	private void playNext(ProgressIndicator progressIndicator, ResultReceiver<RadioTrack> trackReceiver) {
		if (currentPlaylist == null) {
			fetchPlaylist(progressIndicator, trackReceiver);
		} else {
			streamNext(progressIndicator, trackReceiver);
		}
	}	
	
	private void fetchPlaylist(final ProgressIndicator progressIndicator, final ResultReceiver<RadioTrack> trackReceiver) {
		getPlaylist(null, new ResultReceiver<RadioPlayList>() {
			public void handle_exception(Throwable t) {
				trackReceiver.handle_exception(t);
			}
			public void onSuccess(RadioPlayList result) {
				currentPlaylist = result;
				play(progressIndicator, trackReceiver);
			}
		});
	}
	
	private void streamNext(ProgressIndicator progressIndicator, ResultReceiver<RadioTrack> trackReceiver) {
		moveToNextTrack();
		RadioTrack track = getCurrentTrack();
		trackReceiver.onSuccess(track);
	}
	
	public Session getSession() {
		return session;
	}
	
	public void tuneToSimilarArtist(ProgressIndicator progressIndicator, String artist, ResultReceiver<Station> resultReceiver) {
		String station = "lastfm://artist/" + Uri.encode( artist ) + "/similarartists";
		GUITaskQueue.getInstance().addTask(progressIndicator, new TuneRadioTask(station, new ResultReceiverPair<Station>(stationResult, resultReceiver)));
	}
	
	public void getPlaylist(ProgressIndicator progressIndicator, ResultReceiver<RadioPlayList> resultReceiver) {
		GUITaskQueue.getInstance().addTask(progressIndicator
				, new GetRadioPlaylistTask(new ResultReceiverPair<RadioPlayList>(playlistResult, resultReceiver)));
		
	}
}
