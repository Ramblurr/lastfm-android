package fm.last.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.w3c.dom.Element;

import fm.last.EasyElement;

public class Request 
{
	private static int m_idCount = 0;
	private URL m_url;
	private int m_id;
	private int m_wsVersion;
	
	private EventHandler m_requestEventHandler = null;
	
	Request( URL url, int wsVersion, EventHandler handler )
	{
		this( url, wsVersion );
		m_requestEventHandler = handler;
	}
	
	Request( URL url, int wsVersion )
	{
		m_wsVersion = wsVersion;
		m_url = url;
		synchronized (Request.class) {
			m_id = m_idCount++;
		}
	}
	
	Response execute() 
	{
		Response response = null;
		try
		{
			InputStreamReader charStream = new InputStreamReader( m_url.openStream(), "UTF-8" ); 
			response = new Response(this, new BufferedReader( charStream ));
		} catch (UnsupportedEncodingException e) {
			response = new Response( this, null );
			response.setError( "Could not decode response from UTF-8" );
		} catch (IOException e) {
			response = new Response( this, null );
			response.setError( "IOException error: " + e );
		}
		
		String error = errorCheck( response );
		if( error != null )
		{
			response.setError( error );
		}



		
		//If this is an asynchronous request call the event handler
		if( m_requestEventHandler != null )
		{
			if( response.hasError() )
			{
				onError( response.error() );
			}
			else
			{
				onMethodComplete( response );
			}
		}
		
		return response;
	}
	
	String errorCheck( Response response )
	{
		switch( m_wsVersion )
		{
			case 2:
			{
				EasyElement e = new EasyElement(
										response.xmlDocument().getDocumentElement());
				String lfmStatus = e.e("lfm").e().getAttribute( "status" );
				if( lfmStatus.compareTo( "ok" ) != 0 )
				{
					return lfmStatus;
				}
				break;
			}
		}
		return null;
	}
	
	void onMethodComplete( Response response )
	{
		m_requestEventHandler.onMethodComplete( m_id, response);
	}
	
	void onError( String error )
	{
		m_requestEventHandler.onError( m_id, error );
	}
	
	int id()
	{
		return m_id;
	}
	
	int wsVersion()
	{
		return m_wsVersion;
	}
}
