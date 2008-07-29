package fm.last.radio;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;

import android.media.MediaPlayer;

public class RadioStream {
	MediaPlayer m_mediaplayer = new MediaPlayer();
	
	public RadioStream(){}
	
	public void play( String u )
	{
		URL url = null;
		try {
			url = new URL( u );
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		play( url );
	}
	
	public void play( URL url )
	{
		load( url );
		play();
	}
	
	public void play()
	{
		m_mediaplayer.start();
	}
	
	public void stop()
	{
		m_mediaplayer.stop();
		m_mediaplayer.reset();
	}
	
	public void load( URL url )
	{
		if( url.getHost().compareTo( "play.last.fm" ) == 0 )
		{
			//This url is actually the ticketing url that redirects to a streamer. 
			//Because the MediaPlayer object does not currently support 302 redirects,
			//this has to be done manually.
			//WARNING: if this system or host name changes in the future, this may break!
			
			url = getRedirectedURL( url );
		}
		
		try {
			m_mediaplayer.setDataSource( url.toString() );
			m_mediaplayer.prepare();
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private URL getRedirectedURL( final URL url )
	{
		URL streamUrl;
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			httpClient.setRedirectHandler( new org.apache.http.client.RedirectHandler(){
			
				public boolean isRedirectRequested( HttpResponse r, HttpContext c ){ return false; }

				public URI getLocationURI(HttpResponse response,
						HttpContext context) throws ProtocolException {
					return null;
				}
			});
			HttpResponse response = httpClient.execute( new HttpGet( url.toURI()));
			Header[] locationHeaders = response.getHeaders( "Location" ); 
			if( locationHeaders.length > 0 )
			{
				String streamLocation = locationHeaders[0].getValue();
				streamUrl = new URL( streamLocation );
			}
			else
			{
				return url;
			}
			
		} catch (URISyntaxException e) {
			return url;
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			return url;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			return url;
		}
		return streamUrl;
	}
}
