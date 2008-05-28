package fm.last.ws;

import java.io.BufferedReader;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import fm.last.Log;

public class Response {
	private int m_id;
	private BufferedReader m_responseData;
	
	public Response( Request request, BufferedReader responseData )
	{
		m_id = request.id();
		m_responseData = responseData;
	}
	
	public int id()
	{
		return m_id;
	}
	
	public BufferedReader dataReader()
	{
		return m_responseData;
	}
	
	public Document xmlDocument()
	{
		Document xmlDom = null;
		try
		{
			xmlDom = DocumentBuilderFactory.newInstance()
										   .newDocumentBuilder()
										   .parse( new InputSource( m_responseData ) );
		}
		catch (IOException e) 
		{
			Log.e( "IOException: " + e );
		} 
		catch (FactoryConfigurationError e) 
		{
			Log.e( "DocumentBuilder Factory configuration error: " + e );
		} 
		catch (ParserConfigurationException e) 
		{
			Log.e( "Parser Configuration error: " + e );
		}
		catch (org.xml.sax.SAXException e) 
		{
			Log.e( "Sax Error: " + e );
		}
		return xmlDom;
	}
}
