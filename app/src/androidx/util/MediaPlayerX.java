package androidx.util;

import java.io.IOException;

import android.media.MediaPlayer;
import android.util.Log;

public class MediaPlayerX {
	private enum MEDIA_PLAYER_STATE {
		RESET, PREPARING, PREPARED, PLAYING
	}
	public static final int TRACK_LOCATION_BEGINNING = -3;
	public static final int TRACK_LOCATION_END = -1;
	
	private MEDIA_PLAYER_STATE state = MEDIA_PLAYER_STATE.RESET;
	private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
		public void onPrepared(MediaPlayer arg0) {
			mediaPlayerIsPrepared();
		}
	};
	
	private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
		public void onCompletion(MediaPlayer arg0) {
			mediaPlayerHasCompleted();
		}
	};

	public static interface Listener {
		public void onPaused(MediaPlayerX mp, String url, int where);
		public void onPlay(MediaPlayerX mp, String url);
	}
	
	private MediaPlayer mediaPlayer;
	private String mediaUrl;
	private Listener listener;
	private boolean playWhenPrepared;

	public MediaPlayerX(MediaPlayer mediaPlayer, Listener listener) {
		this.mediaPlayer = mediaPlayer;
		this.listener = listener;
		mediaPlayer.setOnPreparedListener(onPreparedListener);
		mediaPlayer.setOnCompletionListener(onCompletionListener);
	}
	
	private synchronized boolean isMediaPlayerReset() {
		return (state == MEDIA_PLAYER_STATE.RESET);
	}

	private synchronized boolean isMediaPlayerPrepared() {
		return (state == MEDIA_PLAYER_STATE.PREPARED);
	}

	private synchronized boolean isMediaPlayerPreparing() {
		return (state == MEDIA_PLAYER_STATE.PREPARING);
	}
	
	private synchronized void reset() {
		if (!isMediaPlayerReset()) {
			mediaPlayer.reset();
			mediaUrl = null;
			state = MEDIA_PLAYER_STATE.RESET;
		}
	}
	
	private synchronized void mediaPlayerIsPrepared() {
		state = MEDIA_PLAYER_STATE.PREPARED;
		listener.onPaused(this, mediaUrl, TRACK_LOCATION_BEGINNING);
		if (playWhenPrepared) {
			playWhenPrepared = false;
			play();
		}
	}	
	
	private synchronized void mediaPlayerHasCompleted() {
		Log.i("androidx", "mediaPlayerHasCompleted()");
		// once a track is finished, we are essentially paused
		// and can restart from the beginning
		state = MEDIA_PLAYER_STATE.PREPARED;
		listener.onPaused(this, mediaUrl, TRACK_LOCATION_END);
	}
	
	public synchronized void play() {
		if (mediaUrl == null || state == MEDIA_PLAYER_STATE.RESET) {
			throw new IllegalStateException("no datasource set");
		}
		if (state == MEDIA_PLAYER_STATE.PLAYING) {
			// calls to play() are idempotent
			return;
		}
		if (state == MEDIA_PLAYER_STATE.PREPARING) {
			playWhenPrepared = true;
			return;
		}
		// the state must be PREPARED
		mediaPlayer.start();
		// notify our listener that the playing has started
		listener.onPlay(this, mediaUrl);
	}
	
	public synchronized void setDataSource(String url) throws IOException {
		if (!isMediaPlayerReset()) {
			reset();
		}
		mediaUrl = url;
		if (mediaUrl != null) {
			mediaPlayer.setDataSource(url);
			state = MEDIA_PLAYER_STATE.PREPARING;
			mediaPlayer.prepareAsync();
		}
	}
}
