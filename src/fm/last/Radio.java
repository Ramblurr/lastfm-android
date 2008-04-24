/* request example:
   ws.audioscrobbler.com/radio/handshake.php?version=0.1&platform=android&platformversion=beta&username=jonocole&passwordmd5=myhashedpassword&language=en
   */
package fm.last;

import android.util.Log;
import java.net.URL;
import java.io.InputStream;
import java.util.Vector;


import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.InputStream;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Radio
{
    private String m_sessionId = null;
    private String m_streamUrl = null;
    private boolean m_subscriber = false;
    private String m_baseUrl = null;
    private String m_basePath = null;

    Radio( String username, String md5password )
    {
        Log.i( "Last.fm", "Starting last.fm radio" );
        RadioHandshake handshake = new RadioHandshake(username, md5password);
        handshake.connect();
        m_sessionId = handshake.getValue( "session" );
        m_streamUrl = handshake.getValue( "stream_url" );
        m_subscriber = Integer.valueOf( handshake.getValue( "subscriber" ) ).intValue() == 1;
        m_baseUrl = handshake.getValue( "base_url" );
        m_basePath = handshake.getValue( "base_path" );
    }

    public void tuneToSimilarArtist( String artistName )
    {
        String urlString;
        urlString  = "http://";
        urlString += m_baseUrl;
        urlString += "/radio/adjust.php?";
        urlString += "session=" + m_sessionId + "&";
        urlString += "url=lastfm://artist/" + artistName + "/similarartists&";
        urlString += "lang=en";
        URL radioAdjust;
        try
        {
           radioAdjust = new URL(urlString);        
        }
        catch( java.net.MalformedURLException e )
        {
            Log.e("Last.fm", "Error: malformed URL: " + e.toString());
            return;
        }

        String content = new String();
        try
        {
            InputStream inp = radioAdjust.openStream();
            while (inp.available() > 0)
            {
                content += ((char)inp.read());
            }
            Log.i("Last.fm", "Radio adjust complete:\n" + content);
        }
        catch( java.io.IOException e )
        {
        }
    }

    public TrackInfo[] getPlaylist()
    {
        String urlString;
        urlString = "http://";
        urlString += m_baseUrl;
        urlString += "/radio/xspf.php?";
        urlString += "sk=" + m_sessionId + "&";
        urlString += "discovery=0&";
        urlString += "&desktop=0.1";
        URL xspfRequest;
        Vector trackVector = new Vector();
        try
        {
            xspfRequest = new URL(urlString);
            try
            {
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = db.parse(new InputSource(xspfRequest.openStream()));
                Element rootElement = doc.getDocumentElement();
                Element trackList = (Element)rootElement.getElementsByTagName("trackList").item(0);
                NodeList tracks = trackList.getElementsByTagName("track");
                Log.i("Last.fm", "getPlaylist complete");

                for (int i = 0; i < tracks.getLength(); i++)
                {
                    TrackInfo ti = new TrackInfo();
                    ti.read( (Element)tracks.item(i) );
                    trackVector.add(ti);
                }
            }
            catch (org.xml.sax.SAXException e)
            {
            }
            catch (java.io.IOException e)
            {
            }
            catch (javax.xml.parsers.ParserConfigurationException e)
            {
            }
        }
        catch (java.net.MalformedURLException e)
        {
            Log.e("Last.fm", "Error: malformed URL: " + e.toString());
            return new TrackInfo[] { };
        }
        TrackInfo[] trackInfoArray = new TrackInfo[ trackVector.size() ];
        trackVector.toArray(trackInfoArray);
        return trackInfoArray;
    }
}
