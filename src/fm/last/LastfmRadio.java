package fm.last;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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

	private Session session;
	private Station currentStation;
	private RadioPlayList currentPlaylist;
	private MediaPlayerX mediaPlayer;
	private int currentTrackIndex = -1;
	
	private MediaPlayerX.Listener playableListener =
		new MediaPlayerX.Listener() {
			public void onPaused(MediaPlayerX mp, String url, int where) {
				trackReady(url, where);
			}

			public void onPlay(MediaPlayerX mp, String url) {
			}

	};
	
	private LastfmRadio() {
		mediaPlayer = new MediaPlayerX(new MediaPlayer(), playableListener);
	}
	
	
	private AsyncCallback<Session> sessionResult = new AsyncCallback<Session>() {
		public void onFailure(Throwable t) {
		}

		public void onSuccess(Session result) {
			setSession(result);
		}
	};
	
	private AsyncCallback<Station> stationResult = new AsyncCallback<Station>() {
		public void onSuccess(Station station) {
			setCurrentStation(station);
		}
		
		public void onFailure(Throwable t) {
		}
	};
	
	private AsyncCallback<RadioPlayList> playlistResult = new AsyncCallback<RadioPlayList>() {
		public void onSuccess(RadioPlayList result) {
			setCurrentPlaylist(result);
		}

		public void onFailure(Throwable t) {
		}
	};
	
	public void obtainSession(ProgressIndicator progressIndicator, String username, String md5password, AsyncCallback<Session> resultReceiver) {
		// start grabbing a session key in the background
		// let the radio be notified of the session
		GUITaskQueue.getInstance().addTask(progressIndicator,
				new AuthenticationTask(username, md5password, new AsyncCallbackPair<Session>(sessionResult, resultReceiver)));
	}
	
	private void setCurrentStation(Station station) {
		currentStation = station;
	}
	
	private void setCurrentPlaylist(RadioPlayList playlist) {
		currentPlaylist = playlist;
		currentTrackIndex = -1;
	}

	public RadioTrack getCurrentTrack() {
		if (currentPlaylist == null || currentTrackIndex == -1 || currentTrackIndex >= currentPlaylist.getTracks().length) {
			return null;
		}
		return currentPlaylist.getTracks()[currentTrackIndex];
	}
	
	
	private void moveToNextTrack() {
		++currentTrackIndex; 
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
			mediaPlayer.play();
		} else if (where == MediaPlayerX.TRACK_LOCATION_END) {
			Log.i("we are at the end of " + url);
			moveToNextTrack();
			play(null, new AsyncCallback<RadioTrack>() {
				public void onFailure(Throwable t) {
					Log.e(t);
				}

				public void onSuccess(RadioTrack result) {
					Log.i("playing next track '" + result.getTitle() + "' by '" + result.getCreator()+"'");
				}
			});
		}
	}
	
	public void play(ProgressIndicator progressIndicator, AsyncCallback<RadioTrack> trackReceiver) {
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
	private void playNext(ProgressIndicator progressIndicator, AsyncCallback<RadioTrack> trackReceiver) {
		if (currentPlaylist == null) {
			fetchPlaylist(progressIndicator, trackReceiver);
		} else {
			streamNext(progressIndicator, trackReceiver);
		}
	}	
	
	private void fetchPlaylist(final ProgressIndicator progressIndicator, final AsyncCallback<RadioTrack> trackReceiver) {
		getPlaylist(null, new AsyncCallback<RadioPlayList>() {
			public void onFailure(Throwable t) {
				trackReceiver.onFailure(t);
			}
			public void onSuccess(RadioPlayList result) {
				currentPlaylist = result;
				play(progressIndicator, trackReceiver);
			}
		});
	}
	
	private void streamNext(ProgressIndicator progressIndicator, AsyncCallback<RadioTrack> trackReceiver) {
		moveToNextTrack();
		RadioTrack track = getCurrentTrack();
		if (track == null) {
			throw new NullPointerException("no track!");
		}
		if (track.getLocationUrl() == null) {
			throw new NullPointerException("No track url!");
		}
		URL url;
		try {
			url = UrlUtil.getRedirectedUrl(new URL(track.getLocationUrl()));
			mediaPlayer.setDataSource(url.toExternalForm());
			trackReceiver.onSuccess(track);
		} catch (Exception e) {
			Log.e(e);
		}
	}
	
	public Session getSession() {
		return session;
	}
	
	public void tuneToSimilarArtist(ProgressIndicator progressIndicator, String artist, AsyncCallback<Station> resultReceiver) {
		String station = "lastfm://artist/" + Uri.encode( artist ) + "/similarartists";
		GUITaskQueue.getInstance().addTask(progressIndicator, new TuneRadioTask(station, new AsyncCallbackPair<Station>(stationResult, resultReceiver)));
	}
	
	public void getPlaylist(ProgressIndicator progressIndicator, AsyncCallback<RadioPlayList> resultReceiver) {
		GUITaskQueue.getInstance().addTask(progressIndicator
				, new GetRadioPlaylistTask(new AsyncCallbackPair<RadioPlayList>(playlistResult, resultReceiver)));
		
	}
}
