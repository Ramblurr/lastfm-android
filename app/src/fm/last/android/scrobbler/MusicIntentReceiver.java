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
package fm.last.android.scrobbler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;
import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.R;
import fm.last.android.db.ScrobblerQueueDao;
import fm.last.android.player.IRadioPlayer;
import fm.last.android.player.RadioPlayerService;
import fm.last.api.LastFmServer;
import fm.last.api.Session;

/**
 * @author sam
 * 
 */
public class MusicIntentReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Session s = LastFMApplication.getInstance().session;
		if (s != null && s.getKey().length() > 0 && PreferenceManager.getDefaultSharedPreferences(LastFMApplication.getInstance()).getBoolean("scrobble", true)) {
			if (!PreferenceManager.getDefaultSharedPreferences(LastFMApplication.getInstance()).getBoolean("scrobble_music_player", true)
					&& (intent.getAction().startsWith("com.android") || intent.getAction().startsWith("com.htc"))) {
				return;
			}
			if (!PreferenceManager.getDefaultSharedPreferences(LastFMApplication.getInstance()).getBoolean("scrobble_sls", true)
					&& intent.getAction().startsWith("com.adam.")) {
				return;
			}
			if (!PreferenceManager.getDefaultSharedPreferences(LastFMApplication.getInstance()).getBoolean("scrobble_sdroid", true)
					&& intent.getAction().startsWith("net.jjc1138.")) {
				return;
			}
			if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				if (ScrobblerQueueDao.getInstance().getQueueSize()<1) {
					return;
				}
			}
			final Intent out = new Intent(context, ScrobblerService.class);
			out.setAction(intent.getAction());
			out.putExtras(intent);
			context.startService(out);
		} else if (s != null && s.getKey().length() > 0 && intent.getAction().equals("fm.last.android.LOVE")) {
			IBinder service = peekService(context, new Intent(context, RadioPlayerService.class));
			if (service == null) {
				return;
			}
			try {
				IRadioPlayer player = fm.last.android.player.IRadioPlayer.Stub.asInterface(service);
				if (player != null && player.isPlaying()) {
					String track = player.getTrackName();
					String artist = player.getArtistName();
					if (!track.equals(RadioPlayerService.UNKNOWN) && !artist.equals(RadioPlayerService.UNKNOWN)) {
						LastFmServer server = AndroidLastFmServerFactory.getServer();
						server.loveTrack(artist, track, LastFMApplication.getInstance().session.getKey());
						Toast.makeText(context, context.getString(R.string.scrobbler_trackloved), Toast.LENGTH_SHORT).show();
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (s != null && s.getKey().length() > 0 && intent.getAction().equals("fm.last.android.BAN")) {
			IBinder service = peekService(context, new Intent(context, RadioPlayerService.class));
			if (service == null) {
				return;
			}
			try {
				IRadioPlayer player = fm.last.android.player.IRadioPlayer.Stub.asInterface(service);
				if (player != null && player.isPlaying()) {
					String track = player.getTrackName();
					String artist = player.getArtistName();
					if (!track.equals(RadioPlayerService.UNKNOWN) && !artist.equals(RadioPlayerService.UNKNOWN)) {
						LastFmServer server = AndroidLastFmServerFactory.getServer();
						server.banTrack(artist, track, LastFMApplication.getInstance().session.getKey());
						Toast.makeText(context, context.getString(R.string.scrobbler_trackbanned), Toast.LENGTH_SHORT).show();
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
