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

import android.os.Parcel;
import android.os.Parcelable;

public class WSError extends Error implements Parcelable {
	private static final long serialVersionUID = 4424446450329878122L;
	private String method;
	private String message;
	private Integer code;

	public static final int ERROR_InvalidService = 2;
	public static final int ERROR_InvalidMethod = 3;
	public static final int ERROR_AuthenticationFailed = 4;
	public static final int ERROR_InvalidFormat = 5;
	public static final int ERROR_InvalidParameters = 6;
	public static final int ERROR_InvalidResource = 7;
	public static final int ERROR_OperationFailed = 8;
	public static final int ERROR_InvalidSession = 9;
	public static final int ERROR_InvalidAPIKey = 10;
	public static final int ERROR_ServiceOffline = 11;
	public static final int ERROR_SubscribersOnly = 12;
	public static final int ERROR_InvalidAPISignature = 13;

	public static final int ERROR_NotEnoughContent = 20;
	public static final int ERROR_NotEnoughMembers = 21;
	public static final int ERROR_NotEnoughFans = 22;
	public static final int ERROR_NotEnoughNeighbours = 23;
	
	public static final int ERROR_RadioUnavailable = 99;

	public WSError(String method, String message, Integer code) {
		this.method = method;
		this.message = message;
		this.code = code;
	}

	public String getMethod() {
		return method;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public Integer getCode() {
		return code;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(method);
		dest.writeString(message);
		dest.writeInt(code);
	}

	public static final Parcelable.Creator<WSError> CREATOR = new Parcelable.Creator<WSError>() {
		public WSError createFromParcel(Parcel in) {
			return new WSError(in);
		}

		public WSError[] newArray(int size) {
			return new WSError[size];
		}
	};

	private WSError(Parcel in) {
		method = in.readString();
		message = in.readString();
		code = in.readInt();
	}

	public int describeContents() {
		// I have no idea what this is for
		return 0;
	}
}
