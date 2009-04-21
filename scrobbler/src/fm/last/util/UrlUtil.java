/***************************************************************************
 *   Copyright 2005-2009 Last.fm Ltd.                                      *
 *   Portions contributed by Casey Link, Lukasz Wisniewski,                *
 *   Mike Jennings, and Michael Novak Jr.                                  *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.         *
 ***************************************************************************/
package fm.last.util;

import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import android.util.Log;

/**
 * A collection of utility methods to manipulate URLs.
 *
 * @author Mike Jennings
 */
public class UrlUtil {
	private static final int REDIRECT_RESPONSE_CODE = 302;

	private UrlUtil() {
	}
	
	public static String useragent;

	/** mainly sets the User-Agent we need */
	private static void setUserAgent( HttpURLConnection conn )
	{
		if (useragent != null)
			conn.setRequestProperty("User-Agent", useragent );
	}
	
	public static URL getRedirectedUrl(URL url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		setUserAgent( conn );
		conn.setRequestMethod("GET");
		conn.setInstanceFollowRedirects(false);
		int rc = conn.getResponseCode();
		if (rc != REDIRECT_RESPONSE_CODE) {
			throw new IOException("code " + rc + " '" + conn.getResponseMessage() + "'");
		}
		String location = conn.getHeaderField("Location");
		if (location == null) {
			throw new IOException("No 'Location' header found");
		}
		return new URL(location);
	}

	private static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buf = new byte[512];
		int bytesRead = 1;
		while (bytesRead > 0) {
			bytesRead = in.read(buf);
			if (bytesRead > 0) {
				out.write(buf, 0, bytesRead);
			}
		}
	}

	private static int copy(InputStream in, OutputStream out, int maxBytes) throws IOException {
		byte[] buf = new byte[512];
		int bytesRead = 1;
		int totalBytes = 0;
		while (bytesRead > 0) {
			bytesRead = in.read(buf, 0, Math.min(512, maxBytes - totalBytes));
			if (bytesRead > 0) {
				out.write(buf, 0, bytesRead);
				totalBytes += bytesRead;
			}
		}
		return totalBytes;
	}

	public static String doFormPost(URL url, String input) throws IOException {
		return doPost(url, new ByteArrayInputStream(input.getBytes()), "application/x-www-urlencoded");
	}

	public static String doPost(URL url, String input) throws IOException {
		return doPost(url, new ByteArrayInputStream(input.getBytes()));
	}

	public static String doPost(URL url, InputStream stuffToPost) throws IOException {
		return doPost(url, stuffToPost, null);
	}

	public static String doPost(URL url, InputStream stuffToPost, String contentType) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		setUserAgent( conn );
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		if (contentType != null) {
			conn.setRequestProperty("Content-Type", contentType);
		}
		OutputStream ostr = null;
		try {
			ostr = conn.getOutputStream();
			copy(stuffToPost, ostr);
		} finally {
			if( ostr != null )
				ostr.close();
		}

		conn.connect();
		BufferedReader reader = null;
		try {
			int rc = conn.getResponseCode();
			InputStream contentStream = null;
			if (rc == 400)
				contentStream = conn.getErrorStream();
			else if (rc != 200) {
				throw new IOException("code " + rc + " '" + conn.getResponseMessage() + "'");
			}
			else
				contentStream = conn.getInputStream();
			reader = new BufferedReader(new InputStreamReader(contentStream), 512);
			String response = toString(reader);
			return response;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}


	public static String doGet(URL url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		setUserAgent( conn );
		conn.setRequestMethod("GET");
		BufferedReader reader = null;
		try {
			int rc = conn.getResponseCode();
			if (rc != 200) {
				throw new IOException("code " + rc + " '" + conn.getResponseMessage() + "'");
			}
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()), 512);
			return toString(reader);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}


	/**
	 * Do a GET request and retrieve up to maxBytes bytes
	 * @param url
	 * @param maxBytes
	 * @return
	 * @throws IOException
	 */
	public static byte[] doGetAndReturnBytes(URL url, int maxBytes) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		setUserAgent( conn );
		conn.setRequestMethod("GET");
		InputStream istr = null;
		try {
			int rc = conn.getResponseCode();
			if (rc != 200) {
				throw new IOException("code " + rc + " '" + conn.getResponseMessage() + "'");
			}
			istr = new BufferedInputStream(conn.getInputStream(), 512);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			copy(istr, baos, maxBytes);
			return baos.toByteArray();      
		} finally {
			if (istr != null) {
				istr.close();
			}
		}
	}


	private static String buildUrl(String baseurl, Map<String, String> params) throws IOException {
		if (params.isEmpty()) {
			return baseurl;
		} else {
			return baseurl + "?" + buildQuery(params);
		}
	}

	public static String buildQuery(Map<String, String> params) {
		StringBuilder sb = null;
		for (String key : params.keySet()) {
			String value = params.get(key);
			if (sb == null) {
				sb = new StringBuilder();
				sb.append(escape(key)).append('=').append(escape(value));
			} else {
				sb.append("&").append(escape(key)).append('=').append(escape(value));
			}
		}
		return sb.toString();

	}

	public static String doGet(String baseurl, Map<String, String> params) throws IOException {
		return doGet(new URL(buildUrl(baseurl, params)));
	}

	public static String doPost(String baseurl, Map<String, String> params) throws IOException {
		return doPost(new URL(baseurl), buildQuery(params));
	}

	private static String escape(String s) {
		try
		{
			return URLEncoder.encode(s, "UTF-8" );
		} catch( UnsupportedEncodingException e )
		{
			Log.e( "UrlUtil", "Cannot find UTF-8 encoding - this is not very likely!");
			return s;
		}
		
	}

	public static String getXML(URL url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		setUserAgent( conn );
		conn.setRequestMethod("GET");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			return toString(reader);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	private static String toString(BufferedReader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		String line;
		while ( (line = reader.readLine()) != null) {
			sb.append(line).append('\n');
		}
		return sb.toString();
	}

	@SuppressWarnings("unused")
	private static String readString(BufferedReader reader) throws IOException {
		String line;
		StringBuilder sb = new StringBuilder();
		while ( (line = reader.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}


	@SuppressWarnings("unused")
	private static Map<String, String> getParams(BufferedReader reader) throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		String line;
		int eq;
		while ( (line = reader.readLine()) != null) {
			eq = line.indexOf('=');
			if (eq > 0) {
				String key = line.substring(0, eq);
				String value = line.substring(eq + 1);
				params.put(key, value);
			}
		}
		return params;
	}

}

