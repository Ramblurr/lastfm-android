package fm.last;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import android.media.MediaPlayer;
import android.net.Uri;
import androidx.util.AsyncCallbackPair;
import androidx.util.GUITaskQueue;
import androidx.util.MediaPlayerX;
import androidx.util.ProgressIndicator;
import fm.last.api.RadioPlayList;
import fm.last.api.RadioTrack;
import fm.last.api.Session;
import fm.last.api.Station;
import fm.last.tasks.AuthenticationTask;
import fm.last.tasks.GetRadioPlaylistTask;
import fm.last.tasks.TuneRadioTask;
import fm.last.util.UrlUtil;

public class LastfmRadio {
	private static LastfmRadio instance;
	
	public static LastfmRadio getInstance() {
		if (instance == null) {
			instance = new LastfmRadio();
		}
		return instance;
	}

	public static interface Listener {
			void onRadioStarted();
			void onRadioStopped();
			void onStationChanged(Station station);
			void onFetchingTrack(RadioTrack track);
			void onTrackStarted(RadioTrack track);
			void onTrackFinished(RadioTrack track);
	};
	
	private static class ListenerList implements Listener {
		private List<Listener> listeners;
		
		ListenerList() {
			listeners = new ArrayList<Listener>();
		}
		
		void addListener(Listener listener) {
			if (!listeners.contains(listener)) {
				listeners.add(listener);
			}
		}
		
		void removeListener(Listener listener) {
			listeners.remove(listener);
		}
		
		public void onRadioStarted() {
			for (Listener l : listeners) {
				l.onRadioStarted();
			}
		}

		public void onRadioStopped() {
			for (Listener l : listeners) {
				l.onRadioStopped();
			}
		}

		public void onStationChanged(Station station) {
			for (Listener l : listeners) {
				l.onStationChanged(station);
			}
		}

		public void onTrackFinished(RadioTrack track) {
			for (Listener l : listeners) {
				l.onTrackFinished(track);
			}
		}

		public void onTrackStarted(RadioTrack track) {
			for (Listener l : listeners) {
				l.onTrackStarted(track);
			}
		}

		public void onFetchingTrack(RadioTrack track) {
			for (Listener l : listeners) {
				l.onFetchingTrack(track);
			}
		}
	};
	
	private Session session;
	private Station currentStation;
	private MediaPlayerX mediaPlayer;
	private TrackProvider trackProvider;
	private RadioTrack currentTrack;
	private ListenerList listeners;
	
	private MediaPlayerX.Listener playableListener =
		new MediaPlayerX.Listener() {
			public void onPaused(MediaPlayerX mp, String url, int where) {
				trackReady(url, where);
			}

			public void onPlay(MediaPlayerX mp, String url) {
			}

	};
	
	private AsyncCallback<RadioTrack> trackReceiver = new AsyncCallback<RadioTrack>() {
		public void onSuccess(RadioTrack result) {
			trackReceived(result);
		}
		
		public void onFailure(Throwable t) {
			trackFailed(t);
		}
	};
	
	private AsyncCallback<Session> sessionResult = new AsyncCallback<Session>() {
		public void onSuccess(Session result) {
			setSession(result);
		}
		public void onFailure(Throwable t) {
			Log.e(t);
		}

	};
	
	private AsyncCallback<Station> stationResult = new AsyncCallback<Station>() {
		public void onSuccess(Station station) {
			setCurrentStation(station);
		}
		
		public void onFailure(Throwable t) {
			Log.e(t);
		}
	};	
	
	private LastfmRadio() {
		mediaPlayer = new MediaPlayerX(new MediaPlayer(), playableListener);
		trackProvider = new TrackProvider();
		listeners = new ListenerList();
	}
	
	public void addListener(Listener listener) {
		listeners.addListener(listener);
	}
	
	public void removeListener(Listener listener) {
		listeners.removeListener(listener);
	}
	
	public void obtainSession(ProgressIndicator progressIndicator, String username, String md5password, AsyncCallback<Session> resultReceiver) {
		// start grabbing a session key in the background
		// let the radio be notified of the session
		GUITaskQueue.getInstance().addTask(progressIndicator,
				new AuthenticationTask(username, md5password, new AsyncCallbackPair<Session>(sessionResult, resultReceiver)));
	}
	
	/**
	 * This is called when a new station is tuned into
	 * @param station
	 */
	private void setCurrentStation(Station station) {
		currentStation = station;
		listeners.onStationChanged(station);
	}
		
	public RadioTrack getCurrentTrack() {
		return currentTrack;
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

	private void trackReady(final String url, int where) {
		if (where == MediaPlayerX.TRACK_LOCATION_BEGINNING) {
			Log.i("trackReady() - beginning");
			mediaPlayer.play();
			listeners.onTrackStarted(currentTrack);
		} else if (where == MediaPlayerX.TRACK_LOCATION_END) {
			Log.i("we are at the end of " + url);
			listeners.onTrackFinished(currentTrack);
			playNext();
		}
	}
	
	private void trackReceived(RadioTrack result) {
		Log.i("trackReceived " + result.getCreator() + " - " + result.getTitle());
		currentTrack = result;
		if (currentTrack.getLocationUrl() == null) {
			throw new NullPointerException("No track url!");
		}
		URL url;
		try {
			url = UrlUtil.getRedirectedUrl(new URL(currentTrack.getLocationUrl()));
			mediaPlayer.setDataSource(url.toExternalForm());
			listeners.onFetchingTrack(currentTrack);
		} catch (Exception e) {
			Log.e(e);
		}
	}
	
	private void trackFailed(Throwable t) {
		currentTrack = null;
		Log.e(t);
	}
	
	
	public void playNext() {
		Log.i("playNext");
		trackProvider.getNextTrack(trackReceiver);
	}
	
	public Session getSession() {
		return session;
	}
	
	/**
	 * Tune to a station which plays music similar to the given artist.
	 * 
	 * @param progressIndicator
	 * @param artist
	 */
	public void tuneToSimilarArtist(ProgressIndicator progressIndicator, String artist) {
		String station = "lastfm://artist/" + Uri.encode( artist ) + "/similarartists";
		GUITaskQueue.getInstance().addTask(progressIndicator, new TuneRadioTask(station, stationResult));
	}
	
}
