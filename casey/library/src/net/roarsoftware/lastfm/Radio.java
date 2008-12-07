package net.roarsoftware.lastfm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Properties;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.Parcel;
import android.os.Parcelable;

import static net.roarsoftware.util.StringUtilities.encode;

/**
 * Provides method to tune in to last.fm radio stations.<br/>
 * This class uses the <b>old</b> radio APIs since the 2.0 API radio methods are
 * not documented yet.<br/>
 * Note that this class does neither play nor scrobble songs. It only changes
 * stations and fetches the playlist which contains the location to the actual
 * sound file. Playing and scrobbling has to be done by the application.<br/>
 * Also be aware that the last.fm radio service will be closed down so that only
 * special API keys will have access to the radio.<br/>
 * Therefore this API is experimental and may be removed in a later release.
 * 
 * @author Janni Kovacs
 */
public class Radio implements Parcelable {

	private static final String HANDSHAKE_URL = "http://ws.audioscrobbler.com/radio/handshake.php?username=%s&passwordmd5=%s&language=%s&player=%s&platform=%s&version=%s&platformversion=%s";
	private static final String ADJUST_URL = "http://ws.audioscrobbler.com/radio/adjust.php?session=%s&url=%s&lang=%s";
	private static final String PLAYLIST_URL = "http://ws.audioscrobbler.com/radio/xspf.php?sk=%s&discovery=0&desktop=1.5";
	private String lang = "en";

	private String playerName;
	private String playerVersion;

	private String session;
	private boolean subscriber;
	private String stationName;
	private String stationUrl;

	private Radio() {
	}

	public static Radio newRadio(String playerName, String playerVersion) {
		Radio r = new Radio();
		r.playerName = playerName;
		r.playerVersion = playerVersion;
		return r;
	}

	public boolean isSubscriber() {
		return subscriber;
	}

	public String getStationName() {
		return stationName;
	}
	
	public String getStationUrl(){
		return stationUrl;
	}

	public Result handshake(String username, String passwordHash ) {
		String platform = System.getProperty("os.name");
		String platformVersion = System.getProperty("os.version");
		String url = String.format(HANDSHAKE_URL, encode(username),
				passwordHash, lang, encode(playerName), encode(platform),
				encode(playerVersion), encode(platformVersion));
		System.out.println("Radio url: " + url);
		try {
			HttpURLConnection connection = Caller.getInstance().openConnection(
					url);
			
			Properties props = new Properties();
			props.load(connection.getInputStream());
			if ("FAILED".equals(props.getProperty("session"))) {
				return new Result(props.getProperty("msg"));
			}
			this.session = props.getProperty("session");
			this.subscriber = "1".equals(props.getProperty("subscriber"));
//			return Result.createOkResult(null);
			return null;
		} catch (IOException e) {
			return new Result(e.getMessage());
		}
	}

	public Result changeStation(RadioStation station) {
		return changeStation(station.getUrl());
	}

	private Result changeStation(String stationUrl) {
		String url = String.format(ADJUST_URL, session,
				encode(stationUrl), lang);
		try {
			HttpURLConnection connection = Caller.getInstance().openConnection(
					url);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(connection.getInputStream(), "UTF-8"));
			String response = reader.readLine();
			while (null != response) {
				int i = response.indexOf('=');
				if( i == -1 )
					continue;
				String prop = response.substring(0, i);
				String value = response.substring(i + 1);
				if(prop.equals("url"))
					this.stationUrl = value;
				else if(prop.equals("stationname"))
					this.stationName = value;
				else if(prop.equals("response"))
					if( value.equals("FAILED"))
						return Result.createRestErrorResult(-1, null);
				response = reader.readLine();
			}
			return null;
		} catch (IOException e) {
			return new Result(e.getMessage());
		}
	}

	public Playlist fetchPlaylist() {
		String url = String.format(PLAYLIST_URL, session);
		try {
			HttpURLConnection connection = Caller.getInstance().openConnection(
					url);
			
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();
			xpp.setInput(connection.getInputStream(), "utf-8");
			xpp.nextTag();
			xpp.require(XmlPullParser.START_TAG, null, "playlist");
			return Playlist.playlistFromXPP(xpp, true);
		} catch (Exception e) {
			return null;
		}
	}
	
	private void printStream(InputStream httpInput)
	{
		try {
		String all = "";
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(httpInput));
		String response = reader.readLine();
		
		while (null != response) {
			all = all.concat(response + "\n");
			
				response = reader.readLine();
			
		}
		System.out.println(all);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Playlist parsePlayList()
	{
		/*Result result = Caller.getInstance().call("radio.getPlaylist",
				mSession, "rtp", "1");
		if (result.isSuccessful()) {
			try {
				result.getParser().nextTag();
				if (!result.getParser().getName().equals("playlist"))
					return;
				mCurrPlaylist = Playlist.playlistFromResult(result, true);
				DomElement element = result.getLfm().getChild("playlist");

			} catch (Exception e) {
			}
		} else {
			mError = true;
		}*/
		return null;
	}

	public static class RadioStation {
		private String url;
		private String title;

		/**
		 * Creates a radio station from the specified url.
		 * The url must be a lastfm url:
		 * eg. lastfm://user/mxcl/loved
		 * @param url
		 */
		public RadioStation(String url) {
			this.url = url;
		}
		
		public void setTitle(String theTitle) {
			title = theTitle;
		}

		/**
		 * the Last.fm url, eg. lastfm://user/mxcl/loved
		 * 
		 * @return the url of the station
		 */
		public String getUrl() {
			return url;
		}

		/**
		 * eg. "mxcl's Loved Tracks" It is worth noting that the Radio doesn't
		 * set the title of RadioStation object until we have tuned to it, and
		 * then we only set the one we give you back.
		 * 
		 * @return the staton's title
		 */
		public String getTitle() {
			return title;
		}

		public static RadioStation similarArtists(String artist) {
			return new RadioStation("lastfm://artist/" + artist
					+ "/similarartists");
		}

		public static RadioStation artistFans(String artist) {
			return new RadioStation("lastfm://artist/" + artist + "/fans");
		}

		public static RadioStation globalTag(String tag) {
			return new RadioStation("lastfm://globaltags/" + tag);
		}

		public static RadioStation personal(String user) {
			return new RadioStation("lastfm://user/" + user + "/personal");
		}

		public static RadioStation lovedTracks(String user) {
			return new RadioStation("lastfm://user/" + user + "/loved");
		}

		public static RadioStation neighbours(String user) {
			return new RadioStation("lastfm://user/" + user + "/neighbours");
		}

		public static RadioStation recommended(String user) {
			return new RadioStation("lastfm://user/" + user + "/recommended");
		}
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(stationName);
		dest.writeString(stationUrl);
		dest.writeString(playerName);
		dest.writeString(playerVersion);
		dest.writeString(session);
		dest.writeInt(subscriber ? 1 : 0);
	}
	
	public static final Parcelable.Creator<Radio> CREATOR = new Parcelable.Creator<Radio>() {
		public Radio createFromParcel(Parcel in) {
			return new Radio(in);
		}

		public Radio[] newArray(int size) {
			return new Radio[size];
		}	
	};
	
	private Radio(Parcel in) {
		stationName = in.readString();
		stationUrl = in.readString();
		playerName = in.readString();
		playerVersion = in.readString();
		session = in.readString();
		subscriber = in.readInt() == 1;
		
	}

	public int describeContents() {
		// I have no idea what this is for
		return 0;
	}
}
