package fm.last.ws;

import java.io.Reader;

public interface EventHandler
{
	public void onMethodComplete( int id, Response response );
	public void onError( int id, String error );
}
