package androidx.util;

import java.io.IOException;

import android.media.MediaPlayer;

public class MediaPlayerX {
	private enum MEDIA_PLAYER_STATE {
		RESET, HAS_DATASOURCE, UNPREPARED, PREPARED
	}
	private MEDIA_PLAYER_STATE state = MEDIA_PLAYER_STATE.RESET;
	private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
		public void onPrepared(MediaPlayer arg0) {
			mediaPlayerIsPrepared();
		}
	};
	
	private MediaPlayer mediaPlayer;
	private String mediaUrl;
	
	public MediaPlayerX(MediaPlayer mediaPlayer) {
		this.mediaPlayer = mediaPlayer;
		mediaPlayer.setOnPreparedListener(onPreparedListener);
	}
	
	private boolean isMediaPlayerReset() {
		return (state == MEDIA_PLAYER_STATE.RESET);
	}
	
	private void reset() {
		if (!isMediaPlayerReset()) {
			mediaPlayer.reset();
			state = MEDIA_PLAYER_STATE.RESET;
		}
	}
	
	
	private void mediaPlayerIsPrepared() {
		state = MEDIA_PLAYER_STATE.PREPARED;
	}	
	
	public void setDataSource(String url) throws IOException {
		if (!isMediaPlayerReset()) {
			reset();
		}
		mediaUrl = url;
		if (mediaUrl != null) {
			mediaPlayer.setDataSource(url);
			mediaPlayer.prepareAsync();
		}
	}
	
}
