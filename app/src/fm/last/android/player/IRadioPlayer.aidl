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
package fm.last.android.player;

import fm.last.api.Session;
import fm.last.api.WSError;

interface IRadioPlayer {
	void setSession(in Session session);
	boolean tune(in String url, in Session session);

	void pause();
	void stop();
	void startRadio();

	void skip(); 
	
	String getArtistName();
	String getAlbumName();
	String getTrackName();
	String getArtUrl();
	long   getDuration();
	boolean getLoved();
	void setLoved(boolean loved);
	long   getPosition();
	int	   getBufferPercent();
	
	boolean isPlaying();
	int getState();
	String getStationName();
	String getStationUrl();
	
	WSError getError();
	
	boolean getPauseButtonPressed();
	void pauseButtonPressed();
} 
