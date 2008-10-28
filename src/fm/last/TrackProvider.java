package fm.last;

import androidx.util.AsyncCallbackPair;
import androidx.util.GUITaskQueue;
import androidx.util.ProgressIndicator;

import com.google.gwt.user.client.rpc.AsyncCallback;

import fm.last.api.RadioPlayList;
import fm.last.api.RadioTrack;
import fm.last.tasks.GetRadioPlaylistTask;

class TrackProvider {
	private RadioPlayList currentPlaylist;
	private int currentTrackIndex = -1;

	private AsyncCallback<RadioPlayList> playlistResult = new AsyncCallback<RadioPlayList>() {
		public void onSuccess(RadioPlayList result) {
			setCurrentPlaylist(result);
		}

		public void onFailure(Throwable t) {
		}
	};
	
	private void setCurrentPlaylist(RadioPlayList playlist) {
		currentPlaylist = playlist;
		currentTrackIndex = -1;
	}

	private RadioTrack getNextTrack() {
		++currentTrackIndex;
		if (currentTrackIndex >= currentPlaylist.getTracks().length) {
			currentPlaylist = null;
		}
		if (currentPlaylist == null) {
			return null;
		}
		return currentPlaylist.getTracks()[currentTrackIndex];
	}
	
	
	void getNextTrack(AsyncCallback<RadioTrack> trackReceiver) {
		RadioTrack track = getNextTrack();
		if (track == null) {
			getNextTrackAsync(trackReceiver);
		} else {
			trackReceiver.onSuccess(track);
		}
	}
	
	private class TrackReceiverAdapter implements AsyncCallback<RadioPlayList> {
		private AsyncCallback<RadioTrack> trackReceiver;
		
		TrackReceiverAdapter(AsyncCallback<RadioTrack> _trackReceiver) {
			trackReceiver = _trackReceiver;
		}
		
		public void onFailure(Throwable t) {
			trackReceiver.onFailure(t);
		}

		public void onSuccess(RadioPlayList result) {
			getNextTrack(trackReceiver);
		}
	};

	private void getNextTrackAsync(AsyncCallback<RadioTrack> trackReceiver) {
		AsyncCallback<RadioPlayList> resultReceiver = new TrackReceiverAdapter(trackReceiver);
		resultReceiver = new AsyncCallbackPair<RadioPlayList>(playlistResult, resultReceiver);
	}
	
	
	private void getPlaylist(ProgressIndicator progressIndicator, AsyncCallback<RadioPlayList> resultReceiver) {
		GUITaskQueue.getInstance().addTask(progressIndicator
				, new GetRadioPlaylistTask(new AsyncCallbackPair<RadioPlayList>(playlistResult, resultReceiver)));
		
	}
	
}
