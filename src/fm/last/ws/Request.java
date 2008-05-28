package fm.last.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

public class Request 
{
	private static int m_idCount = 0;
	private URL m_url;
	private int m_id;
	
	private RequestEventHandler m_requestEventHandler = null;
	
	public Request( URL url, RequestEventHandler handler )
	{
		this( url );
		m_requestEventHandler = handler;
	}
	
	public Request( URL url )
	{
		m_url = url;
		synchronized (Request.class) {
			m_id = m_idCount++;
		}
	}
	
	public Response execute() 
	{
		Response response = null;
		try
		{
			InputStreamReader charStream = new InputStreamReader( m_url.openStream(), "UTF-8" ); 
			response = new Response(this, new BufferedReader( charStream ));
		} catch (UnsupportedEncodingException e) {
			onError( "Could not decode response from UTF-8" );
		} catch (IOException e) {
			onError( "IOException error: " + e );
		}
		
		//If this is a synchronous / fire and forget request
		//OR there was an error.
		if( m_requestEventHandler == null ||
			response == null )
			return response;

		onMethodComplete( response );
		return response;
	}
	
	private void onMethodComplete( Response response )
	{
		m_requestEventHandler.onMethodComplete( m_id, response);
	}
	
	private void onError( String error )
	{
		m_requestEventHandler.onError( m_id, error );
	}
	
	public int id()
	{
		return m_id;
	}
}
