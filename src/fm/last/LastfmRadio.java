package fm.last;

import androidx.util.ResultReceiver;
import fm.last.api.Session;

public class LastfmRadio implements ResultReceiver<Session> {
	private static LastfmRadio instance;
	
	public static LastfmRadio getInstance() {
		if (instance == null) {
			instance = new LastfmRadio();
		}
		return instance;
	}

	private Session session;
	
	private LastfmRadio() {
	}
	
	public void setSession(Session session) {
		this.session = session;
	}
	
	public Session getSession() {
		return session;
	}
	
	public void resultObtained(Session session) {
		this.session = session;
	}	
	
	// called if there was a problem authenticating
	public void handle_exception(Throwable t) {
		Log.e(t);
	}

}
