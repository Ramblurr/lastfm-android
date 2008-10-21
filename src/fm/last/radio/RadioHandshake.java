package fm.last.radio;

import fm.last.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;

public class RadioHandshake {
	private String m_userName, m_md5Password = null;
	private HashMap<String, String> m_parameters = new HashMap<String, String>();

	public RadioHandshake(String username, String md5Password) {
		m_userName = username;
		m_md5Password = md5Password;
	}

	public void connect() throws IOException {
		Log.i("Handshaking with Last.fm radio");
		URL url = null;
		String urlString;
		urlString = "http://ws.audioscrobbler.com/radio/handshake.php?version=0.1&platform=android&platformversion=beta&username=";
		urlString += m_userName;
		urlString += "&passwordmd5=";
		urlString += m_md5Password;
		urlString += "&language=en";
		url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		InputStream inp = conn.getInputStream();
		String content = new String();
		while (inp.available() > 0) {
			content += ((char) inp.read());
		}
		String params[] = content.split("\n");
		for (String param : params) {
			String curParam[] = param.split("=", 2);
			m_parameters.put(curParam[0],
					(curParam.length > 1) ? curParam[1] : "");
			Log.i("parameter[" + curParam[0] + "] = "
						+ ((curParam.length > 1) ? curParam[1] : ""));
		}
	}

	public boolean isValid() {
//		if (!m_parameters.containsKey("session"))
//			return false;
//
//		final String session = (String) m_parameters.get("session");
//		int failed;
//		if (session != null)
//			failed = session.compareTo("FAILED");
//		else
//			failed = 0;
//
//		return failed != 0;
		return true;
	}

	public String getValue(String param) {
		return (String) m_parameters.get(param);
	}
}
