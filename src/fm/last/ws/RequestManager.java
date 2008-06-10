package fm.last.ws;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import android.net.Uri;

import com.google.common.util.Assert;

import fm.last.Application;
import fm.last.EasyElement;
import fm.last.Utils;
import fm.last.ws.Request.RequestType;

public class RequestManager {

	private int m_version;
	private String m_baseHost = "http://ws.audioscrobbler.com/";
	private String m_apiRoot = "";
	private RequestQueue m_requestQueue = new RequestQueue();
	
	private static final String API_KEY = "";
	private static final String API_SECRET = "";
	
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
			m_apiRoot = "2.0/?";

			m_sessionKey = Application.instance().sessionKey();
			m_authToken = Utils.md5( Application.instance().userName() + 
						  Application.instance().password() );
			
			if ( m_sessionKey.length() == 0)
			{
				RequestParameters params = new RequestParameters();
				
				params.add( "username", Application.instance().userName() )
					  .add( "authToken", m_authToken )
					  .add( "api_key", API_KEY )
					  .add( "api_sig", methodSignature( new RequestParameters() ) );
				
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
		return callMethod( methodName, methodParams, RequestType.GET_REQUEST );
	}
	
	public Response callMethod( String methodName, RequestParameters methodParams, RequestType rType )
	{
		int requestId = callMethod( methodName, methodParams, null, rType );
		return waitForRequestResponse( requestId );
	}
	
	public int callMethod( String methodName, RequestParameters methodParams, EventHandler eventHandler ) 
	{
		return callMethod( methodName, methodParams, eventHandler, RequestType.GET_REQUEST);
	}
	
	public int callMethod( String methodName, RequestParameters methodParams, EventHandler eventHandler, RequestType requestType ) 
	{
		String urlString = m_baseHost + 
						   m_apiRoot;
		
		if( m_version == 1 )
		{
			urlString += methodName + "?";
		}
		else if( m_version == 2 )
		{
			methodParams.add( "method", methodName )
						.add( "api_key", API_KEY )
						.add( "sk", m_sessionKey );
			methodParams.add( "api_sig", methodSignature( methodParams ) );
		}
		
		Map<String, String> parameterMap = methodParams.getMap();
		
		for( String name : parameterMap.keySet() )
		{
			final String value = parameterMap.get( name ); 
			urlString += ( name + "=" + value + "&" ); 
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
		
		Request request = new Request( url, m_version, eventHandler, requestType );
		m_requestQueue.sendRequest( request );
		
		return request.id();
	}
	
	private String methodSignature( RequestParameters params )
	{
		Map< String, String > paramMap = params.getMap();
		Set< Map.Entry<String, String> > set = paramMap.entrySet();
		
		String paramString = "";
		for( Map.Entry<String, String> entry: set )
		{
			paramString += entry.getKey() + Uri.decode( entry.getValue() );
		}
		
		paramString += API_SECRET;
		return Utils.md5( paramString );
	}
	
	public Response waitForRequestResponse( int id )
	{
		return m_requestQueue.waitForRequestResponse( id );
	}
}
