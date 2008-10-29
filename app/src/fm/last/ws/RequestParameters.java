package fm.last.ws;

import java.util.Map;
import java.util.TreeMap;

public class RequestParameters {
	private TreeMap< String, String > m_paramMap = new TreeMap< String, String >();
	public RequestParameters() {}
	
	public RequestParameters add( String key, String value )
	{
		m_paramMap.put( key, value );
		return this;
	}
	
	public Map<String, String> getMap()
	{
		return m_paramMap;
	}
}
