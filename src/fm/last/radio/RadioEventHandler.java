package fm.last.radio;

import fm.last.TrackInfo;

public interface RadioEventHandler
{
	public void onTrackStarted( TrackInfo track );
	public void onTrackEnded( TrackInfo track );
}
