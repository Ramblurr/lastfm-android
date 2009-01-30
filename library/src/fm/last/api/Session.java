// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package fm.last.api;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a lastfm session
 *
 * @author Mike Jennings
 */
public class Session implements Serializable, Parcelable {
  private static final long serialVersionUID = -8500867686679447824L;
  private String name, key, subscriber;

  public Session(String name, String key, String subscriber) {
    this.name = name;
    this.key = key;
    this.subscriber = subscriber;
  }
  
  public String getName() {
    return name;
  }

  public String getKey() {
    return key;
  }

  public String getSubscriber() {
    return subscriber;
  }

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(key);
		dest.writeInt(subscriber == "1" ? 1 : 0);
	}
	
	public static final Parcelable.Creator<Session> CREATOR = new Parcelable.Creator<Session>() {
		public Session createFromParcel(Parcel in) {
			return new Session(in);
		}

		public Session[] newArray(int size) {
			return new Session[size];
		}	
	};
	
	private Session(Parcel in) {
		name = in.readString();
		key = in.readString();
		subscriber = in.readInt() == 1 ? "1" : "0";
	}

	public int describeContents() {
		// I have no idea what this is for
		return 0;
	}
}
