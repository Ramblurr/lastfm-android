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
package fm.last.api;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a lastfm session
 * 
 * @author Mike Jennings
 */
public class SessionInfo implements Serializable, Parcelable {
	private static final long serialVersionUID = -8500867686679447824L;
	boolean radio;
	boolean freeTrail;
	boolean expired;
	Integer playsLeft;
	Integer playsElapsed;

	public SessionInfo(boolean radio, boolean freeTrail, boolean expired, Integer playsLeft, Integer playsElapsed) {
		this.radio = radio;
		this.freeTrail = freeTrail;
		this.expired = expired;
		this.playsLeft = playsLeft;
		this.playsElapsed = playsElapsed;
	}

	public boolean getRadio() {
		return radio;
	}
	
	public boolean getFreeTrial() {
		return freeTrail;
	}
	
	public boolean getExpired() {
		return expired;
	}
	
	public Integer getPlaysLeft() {
		return playsLeft;
	}
	
	public void setPlaysLeft(Integer playsLeft) {
		this.playsLeft = playsLeft;
	}
	
	public Integer getPlaysElapsed() {
		return playsElapsed;
	}
	
	public void setPlaysElapsed(Integer playsElapsed) {
		this.playsElapsed = playsElapsed;
	}

	public void writeToParcel(Parcel dest, int flags) {
		boolean[] booleanArray = new boolean[3];
		booleanArray[0] = radio;
		booleanArray[0] = freeTrail;             
		booleanArray[0] = expired;          
		dest.writeBooleanArray(booleanArray);
		dest.writeInt(playsLeft);
		dest.writeInt(playsElapsed);
	}

	public static final Parcelable.Creator<SessionInfo> CREATOR = new Parcelable.Creator<SessionInfo>() {
		public SessionInfo createFromParcel(Parcel in) {
			return new SessionInfo(in);
		}

		public SessionInfo[] newArray(int size) {
			return new SessionInfo[size];
		}
	};

	private SessionInfo(Parcel in) {
		boolean[] booleanArray = new boolean[3];
		in.readBooleanArray(booleanArray);
		radio = booleanArray[0];
		freeTrail = booleanArray[1];
		expired = booleanArray[2];
		playsLeft = in.readInt();
		playsElapsed = in.readInt();
	}

	public int describeContents() {
		// I have no idea what this is for
		return 0;
	}
}
