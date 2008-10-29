package fm.last;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import android.media.MediaPlayer;
import android.net.Uri;
import androidx.util.AsyncCallbackPair;
import androidx.util.DoLater;
import androidx.util.GUITaskQueue;
import androidx.util.MediaPlayerX;
import androidx.util.ProgressIndicator;
import fm.last.api.RadioTrack;
import fm.last.api.Session;
import fm.last.api.Station;
import fm.last.tasks.AuthenticationTask;
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
			void onRadioTechnicalProblem();
			void onStationChanged(Station station);
			void onFetchingTrack(RadioTrack track);
			void onTrackFetched(RadioTrack track, boolean started);
			void onTrackFinished(RadioTrack track);
	};
	
	
	private static void remove(List list, Object o) {
		for (int i = 0; i < list.size(); ++i) {
			if (list.get(i) == o) {
				list.remove(i);
				return;
			}
		}
	}
	
	private static class ListenerList implements Listener {
		private List<Listener> listeners;
		
		ListenerList() {
			listeners = new ArrayList<Listener>();
		}
		
		void addListener(Listener listener) {
			removeListener(listener);
			listeners.add(listener);
		}
		
		void removeListener(Listener listener) {
			remove(listeners, listener);
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

		public void onTrackFetched(RadioTrack track, boolean started) {
			for (Listener l : listeners) {
				l.onTrackFetched(track, started);
			}
		}

		public void onFetchingTrack(RadioTrack track) {
			for (Listener l : listeners) {
				l.onFetchingTrack(track);
			}
		}

		public void onRadioTechnicalProblem() {
			for (Listener l : listeners) {
				l.onRadioTechnicalProblem();
			}
		}
	};
	
	private Session session;
	private Station currentStation;
	private MediaPlayerX mediaPlayer;
	private TrackProvider trackProvider;
	private RadioTrack currentTrack;
	private ListenerList listeners;
	private boolean playing;
	
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
		Log.i("radio: got a new station - '" + station.getName() + "'");
		currentStation = station;
		listeners.onStationChanged(station);
	}
		
	public RadioTrack getCurrentTrack() {
		if (playing) {
			return currentTrack;
		} else {
			return null;
		}
	}
		
	public Station getCurrentStation() {
		return currentStation;
	}
	
	public void setSession(Session session) {
		this.session = session;
	}
	
	public boolean isPlaying() {
		return playing;
	}
	
	public void stopPlaying() {
		if (playing) {
			playing = false;
			listeners.onRadioStopped();
		}
	}
	
	private void trackReady(final String url, int where) {
		if (where == MediaPlayerX.TRACK_LOCATION_BEGINNING) {
			Log.i("trackReady() - beginning");
			if (playing) {
				mediaPlayer.play();
				listeners.onTrackFetched(currentTrack, true);
			} else {
				listeners.onTrackFetched(currentTrack, false);
			}
		} else if (where == MediaPlayerX.TRACK_LOCATION_END) {
			Log.i("we are at the end of " + url);
			listeners.onTrackFinished(currentTrack);
			if (playing) {
				playNext();
			}
		}
	}
	
	private void trackReceived(RadioTrack result) {
		Log.i("trackReceived " + result.getCreator() + " - " + result.getTitle());
		currentTrack = result;
		if (currentTrack.getLocationUrl() == null) {
			throw new NullPointerException("No track url!");
		}
		String url = getLocationUrl(currentTrack);
		try {
			mediaPlayer.setDataSource(url);
			listeners.onFetchingTrack(currentTrack);
		} catch (IOException e) {
			Log.e("trouble getting track", e);
		}
	}
	
	private static String getLocationUrl(RadioTrack track) {
		URL url = null;
		try {
			url = new URL(track.getLocationUrl());
			if( url.getHost().equals( "play.last.fm" )) {
				//This url is actually the ticketing url that redirects to a streamer. 
				//Because the MediaPlayer object does not currently support 302 redirects,
				//this has to be done manually.
				//WARNING: if this system or host name changes in the future, this may break!
				url = UrlUtil.getRedirectedUrl(url);
			}
		} catch (Exception e) {
			Log.e(e);
		}
		return (url == null) ? null : url.toExternalForm();
	}
	
	private class TryTrackAgain extends DoLater {
		TryTrackAgain() {
			// 5 seconds
			super(5000);
		}

		@Override
		public void execute() {
			// the next line will cause trackReceived to be called (eventually)
			trackProvider.getNextTrack(trackReceiver);			
		}
	};
	
	private TryTrackAgain tryTrackAgain = new TryTrackAgain();
	
	private void trackFailed(Throwable t) {
		currentTrack = null;
		Log.e("trackFailed", t);
		// notify listeners of the technical glitch
		listeners.onRadioTechnicalProblem();
		if (playing) {
			// if this radio is still on, try again to get the next track
			// in a few seconds
			GUITaskQueue.getInstance().addTask(tryTrackAgain);
		}
	}
	
	public void playNext() {
		Log.i("playNext");
		if (!playing) {
			if (currentStation == null) {
				throw new IllegalStateException("Not tuned to any stations");
			}
			playing = true;
			listeners.onRadioStarted();
		}
		// the next line will cause trackReceived to be called (eventually)
		trackProvider.getNextTrack(trackReceiver);
	}
	
	public Session getSession() {
		return session;
	}
	
	/**
	 * send appropriate events to all registered listeners so that they can
	 * synchronize their GUIs
	 */
	public void sendEventsForListenerCreation() {
		if (this.isPlaying()) {
			listeners.onRadioStarted();
		} else {
			listeners.onRadioStopped();
		}
		Station station = getCurrentStation();
		if (station != null) {
			listeners.onStationChanged(station);
		} else {
			Log.i("No station apparently");
		}
		RadioTrack track = getCurrentTrack();
		if (track != null) {
			Log.i("Faking the fetching of tracks");
			listeners.onFetchingTrack(track);
			listeners.onTrackFetched(track, this.isPlaying());
		}
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
