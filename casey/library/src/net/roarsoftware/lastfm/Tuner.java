package net.roarsoftware.lastfm;

import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.os.Parcel;
import android.os.Parcelable;

import net.roarsoftware.xml.DomElement;

/**
 * Provides method to tune in to the new last.fm radio stations using the 2.0
 * api.<br/>
 * This class uses undocumented methods, and is subject to breakage.<br/>
 * To use this class your API Key must have special permissions.
 * 
 * Note that this class does neither play nor scrobble songs. It only changes
 * stations and fetches the playlist which contains the location to the actual
 * sound file. Playing and scrobbling has to be done by the application.<br/>
 * 
 * @author Casey Link <unnamedrambler@gmail.com>
 */
public class Tuner implements Parcelable {
	private String mStationName;
	private Session mSession;
	int mRetryCount;
	private boolean mError;

	Playlist mCurrPlaylist;

	public Tuner(Radio.RadioStation station, Session session) {
		mSession = session;
		mRetryCount = 0;
		mError = false;
		String url = station.getUrl();
		Result result = Caller.getInstance().call("radio.tune", session,
				"station", url);
		if (result.isSuccessful()) {
			try {
				result.getParser().nextTag();
				if (!result.getParser().getName().equals("station"))
					return;
				int event = result.getParser().next();
				boolean loop = true;
				while (loop) {
					String n = result.getParser().getName();
					switch (event) {
					case XmlPullParser.START_TAG:
						if (n.equals("name")) {
							mStationName = result.getParser().nextText();
							loop = false; // hard wire the loop terminator for
										  // now, because all we want is the station name.
						}
						break;
					case XmlPullParser.END_TAG:
						if (n.equals("station"))
							loop = false;
						break;
					case XmlPullParser.TEXT:
					default:
						break;
					}
					event = result.getParser().next();
				}
				// fetchFiveMoreTracks();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}

	/**
	 * Fetches 5 more tracks for the current radio station. Use fetchPlaylist()
	 * to get the new playlist.
	 * 
	 * @post five tracks will be fetched into the playlist, unless the call
	 *       failed in which case isError() will == true, and fetchPlaylist()
	 *       will stay the same.
	 */
	public void fetchFiveMoreTracks() {
		Result result = Caller.getInstance().call("radio.getPlaylist",
				mSession, "rtp", "1");
		if (result.isSuccessful()) {
			try {
				result.getParser().nextTag();
				if (!result.getParser().getName().equals("playlist"))
					return;
				mCurrPlaylist = Playlist.playlistFromXPP(result.getParser(), true);
				DomElement element = result.getLfm().getChild("playlist");

			} catch (Exception e) {
			}
		} else {
			mError = true;
		}
	}

	/**
	 * Get the current playlist for the station. Usually you want to call
	 * fetchFiveMoreTracks() before this.
	 * 
	 * @return the playlist
	 */
	public Playlist fetchPlaylist() {
		return mCurrPlaylist;
	}

	/**
	 * Get the station name.
	 * 
	 * @return the name of the station
	 */
	public String getStationName() {
		return mStationName;
	}

	/**
	 * Detects if there was an error in calling fetchFiveMoreTracks()
	 * 
	 * @return true if there was, false if there was not
	 */
	public boolean isError() {
		return mError;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mStationName);
		dest.writeInt(mError ? 1 : 0);
		dest.writeInt(mRetryCount);
	}
	
	public static final Parcelable.Creator<Tuner> CREATOR = new Parcelable.Creator<Tuner>() {
		public Tuner createFromParcel(Parcel in) {
			return new Tuner(in);
		}

		public Tuner[] newArray(int size) {
			return new Tuner[size];
		}	
	};
	
	private Tuner(Parcel in) {
		mStationName = in.readString();
		mError = in.readInt() == 1;
		mRetryCount = in.readInt();
		
	}

	public int describeContents() {
		// I have no idea what this is for
		return 0;
	}

}
