package fm.last.ws;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.util.Assert;

import fm.last.Application;
import fm.last.EasyElement;
import fm.last.Utils;

public class RequestManager {

	private int m_version;
	private String m_baseHost = "http://ws.audioscrobbler.com/";
	private String m_apiRoot = "";
	private RequestQueue m_requestQueue = new RequestQueue();
	
	private static final String API_KEY = "";
	
	private String m_authToken;
	private String m_sessionKey;

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
	/*********************************************/

	
	private RequestManager( int version )
	{
		m_version = version;
		if( m_version == 2 )
		{
			m_apiRoot = "2.0/?method=";
			m_sessionKey = Application.instance().sessionKey();
			if ( m_sessionKey.length() > 0)
			{
				RequestParameters params = new RequestParameters();
				
				m_authToken = Utils.md5( Application.instance().userName() + 
										 Application.instance().password() );
				
				
				params.add( "username", Application.instance().userName() )
					  .add( "authToken", m_authToken )
					  .add( "api_key", API_KEY )
					  .add( "api_sig", methodSignature( "auth.getMobileSession" ) );
				
				Response response = callMethod( "auth.getMobileSession", params );
				EasyElement document = new EasyElement( response.xmlDocument().getDocumentElement() );
				m_sessionKey = document.e("key").value();
				Application.instance().setSessionKey( m_sessionKey );
			}
		}
	}
	
	public void setBaseHost( String baseHost )
	{
		m_baseHost = baseHost;
	}
	
	public Response callMethod( String methodName, RequestParameters methodParams )
	{
		int requestId = callMethod( methodName, methodParams, null );
		return waitForRequestResponse( requestId );
	}
	
	public int callMethod( String methodName, RequestParameters methodParams, EventHandler eventHandler ) 
	{
		String urlString = m_baseHost + 
						   m_apiRoot + 
						   methodName;
		urlString = urlString.trim();
		
		if( !urlString.contains( "?" ) )
			urlString += "?";
		else
			urlString += "&";
		
		Map<String, String> parameterMap = methodParams.getMap();
		
		for( String name : parameterMap.keySet() )
		{
			final String value = parameterMap.get( name ); 
			urlString += ( name + "=" + value + "&" ); 
		}
		
		if( m_version == 2 )
		{
			urlString += "api_key=" + API_KEY + "&" 
					  +  "api_sig=" + methodSignature( methodName ) + "&" 
					  +	 "sk=" + m_sessionKey;
		}
		
		URL url = null;
		try 
		{
			url = new URL( urlString );
		} 
		catch (MalformedURLException e) 
		{
			return -1;
		}
		
		Request request = new Request( url, m_version, eventHandler );
		m_requestQueue.sendRequest( request );
		
		return request.id();
	}
	
	private String methodSignature( String methodName )
	{
		return Utils.md5( "api_key" + API_KEY + 
						  "authToken" + m_authToken + 
						  "method" + methodName );
	}
	
	public Response waitForRequestResponse( int id )
	{
		return m_requestQueue.waitForRequestResponse( id );
	}
}
