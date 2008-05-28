package fm.last.ws;

import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import com.google.common.util.Assert;

public class RequestManager {

	private int m_version;
	private String m_baseHost = "http://ws.audioscrobbler.com/";
	private String m_apiRoot = "";
	private RequestQueue m_requestQueue = new RequestQueue();
	

	/************** Singleton Pattern *************/
	private static Map<Integer, RequestManager> m_webServiceInstances 
												= new TreeMap<Integer, RequestManager>();
	public static RequestManager version1()
	{
		return getInstance( 1 );
	}
	
	public static RequestManager version2()
	{
		return getInstance( 2 );
	}
	
	private static RequestManager getInstance( int version )
	{
		Assert.assertTrue( "Unknown webservices version number: " + version, 
						   ( version > 0 && version < 3 ));
		
		if( !m_webServiceInstances.containsKey(version) )
		{
			m_webServiceInstances.put(version, new RequestManager( version ));
		}
		return m_webServiceInstances.get(version);
	}
	
	private RequestManager( int version )
	{
		m_version = version;
		if( m_version == 2 )
		{
			m_apiRoot = "2.0/?method=";
		}
	}
	/*********************************************/
	
	public void setBaseHost( String baseHost )
	{
		m_baseHost = baseHost;
	}
	
	public Response callMethod( String methodName, RequestParameters methodParams )
	{
		int requestId = callMethod( methodName, methodParams, null );
		return waitForRequestResponse( requestId );
	}
	
	public int callMethod( String methodName, RequestParameters methodParams, RequestEventHandler eventHandler ) 
	{
		String urlString = m_baseHost + 
						   m_apiRoot + 
						   methodName;
		urlString = urlString.trim();
		
		if(!urlString.contains("?"))
			urlString += "?";
		else
			urlString += "&";
		
		Map<String, String> parameterMap = methodParams.getMap();
		
		for( String name : parameterMap.keySet() )
		{
			final String value = parameterMap.get( name );
			urlString += ( name + "=" + value + "&" ); 
		}
		
		URL url = null;
		try {
			url = new URL( urlString );
		} catch (MalformedURLException e) {
			return -1;
		}

		Request request = new Request( url, eventHandler );
		m_requestQueue.sendRequest( request );
		
		return request.id();
	}
	
	public Response waitForRequestResponse( int id )
	{
		return m_requestQueue.waitForRequestResponse( id );
	}
}
