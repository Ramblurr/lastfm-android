package fm.last.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import fm.last.EasyElement;
import fm.last.Log;

public class Request 
{
	private static int s_idCount = 0;
	private URL m_url;
	private int m_id;
	private int m_wsVersion;
	private EventHandler m_requestEventHandler = null;
	private RequestType m_requestType = RequestType.GET_REQUEST;
	
	public enum RequestType {
		GET_REQUEST,
		POST_REQUEST
	};
	
	
	Request( URL url, int wsVersion, EventHandler handler )
	{
		m_wsVersion = wsVersion;
		m_url = url;
		synchronized (Request.class) {
			m_id = s_idCount++;
		}
		m_requestEventHandler = handler;
	}
	
	Request( URL url, int wsVersion, EventHandler handler, RequestType rType )
	{
		this( url, wsVersion, handler );
		m_requestType = rType;
	}
	
	Response getRequest() throws UnsupportedEncodingException, IOException
	{
		InputStream stream = m_url.openStream();
		InputStreamReader charStream = new InputStreamReader( stream, "UTF-8" ); 
		return new Response(this, new BufferedReader( charStream ));
	}
	
	Response postRequest() throws UnsupportedEncodingException, IOException
	{
		URL url = new URL( m_url.getProtocol() + "://" + m_url.getHost() + m_url.getPath() );
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput( true );
		con.setRequestMethod( "POST" );
		con.getOutputStream().write( m_url.getQuery().getBytes() );
		con.connect();
		InputStreamReader charStream = new InputStreamReader( con.getInputStream(), "UTF-8" );
		return new Response(this, new BufferedReader( charStream ));
	}	
	
	Response execute() 
	{
		Response response = null;
		try
		{
			if( m_requestType == RequestType.GET_REQUEST )
			{
				response = getRequest();
			}
			else if( m_requestType == RequestType.POST_REQUEST )
			{
				response = postRequest();
			}
		} 
		catch (UnsupportedEncodingException e) 
		{
			response = new Response( this, null );
			response.setError( "Could not decode response from UTF-8" );
		} 
		catch (IOException e) 
		{
			Log.e( e );
			response = new Response( this, null );
			response.setError( "IOException error: " + e );
		}

		String error = null;
		if( !response.hasError() && 
				(error = errorCheck( response )) != null )
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
