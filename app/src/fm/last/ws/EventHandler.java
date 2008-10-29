package fm.last.ws;

public interface EventHandler
{
	public void onMethodComplete( int id, Response response );
	public void onError( int id, String error );
}
