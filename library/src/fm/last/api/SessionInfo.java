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
	private String radio;

	public SessionInfo(String radio) {
		this.radio = radio;
	}

	public String getRadio() {
		return radio;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(radio);
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
		radio = in.readString();
	}

	public int describeContents() {
		// I have no idea what this is for
		return 0;
	}
}
