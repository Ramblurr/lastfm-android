package fm.last.ws;

import java.io.Reader;

public interface RequestEventHandler {
	public void onMethodComplete( int id, Response response );
	public void onError( int id, String error );
}
