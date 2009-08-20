/**
 * 
 */
package fm.last.android.widget;

import java.net.URL;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import fm.last.android.LastFMApplication;
import fm.last.android.utils.UserTask;
import fm.last.api.Session;
import fm.last.util.UrlUtil;
import fm.last.util.XMLUtil;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * @author sam
 *
 */
public class AdArea extends ImageButton {
	private static Vector<String> _trackerUrls = new Vector<String>();
	private static Hashtable<Integer,String> _imageTable = new Hashtable<Integer, String>();
	private static String _url = "";
	private static long slastAdCheck = 0;
	private static boolean sCachedAdStatus = false;

	private int _cachedWidth = 0;
	
	public static boolean adsEnabled(Context context) {
		Session session = LastFMApplication.getInstance().map.get( "lastfm_session" );
		
		if( session.getSubscriber().equals("1")) {
			return false;
		}
		
		TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		if(!tm.getNetworkCountryIso().equals("us")) {
			return false;
		}
		
		return getAdInfo();
		
	}
	
	static boolean getAdInfo() {
		
		long currentTime = new Date().getTime();
		//30 minutes - maybe this could be a lot longer..
		if(  currentTime - slastAdCheck < 1800000 ) {
			return sCachedAdStatus;
		}
		
		slastAdCheck = currentTime;
		sCachedAdStatus = retrieveAdInfo();
		return sCachedAdStatus;
	}
	
	static boolean retrieveAdInfo() {
		Document xml = null;
		try {
			String response = UrlUtil.doGet( new URL( "http://cdn.last.fm/mobile_ads/android/android.xml"/*"http://cdn.last.fm/mobile_ads/blackberry/blackberry.xml"*/ ));
			xml = XMLUtil.stringToDocument(response);
		} catch (Exception e) {
			return false;
		}
		
		if( xml != null ) {
	
			//Get tracker URLs
			Node lfm = XMLUtil.findNamedElementNode( xml, "lfm" );
			if( lfm == null ) return false;
			
			//Get Tracker URLs
			{
				List<Node> nodes = XMLUtil.findNamedElementNodes( lfm, "tracker" );
				for( int i = 0; i < nodes.size(); ++i ) {
					Node trackerNode = (Node)nodes.get(i);
					String trackerUrl = XMLUtil.getChildTextNodes(trackerNode);
					_trackerUrls.add(trackerUrl);
				}
			}
			
			//Get Link URLs
			{
				Node urlNode = XMLUtil.findNamedElementNode( lfm, "url" );
				_url = XMLUtil.getChildTextNodes( urlNode );
			}
			
			//Get image URLs
			{
				List<Node> nodes = XMLUtil.findNamedElementNodes( lfm, "img" );
				for( int i = 0; i < nodes.size(); ++i ) {
					Node imageNode = (Node)nodes.get(i);
					String width = XMLUtil.getNodeAttribute( imageNode, "width" );
					String url = XMLUtil.getChildTextNodes( imageNode );
					_imageTable.put(Integer.valueOf(width), url);
				}
			}
		}
		return true;
	}
	
	public AdArea(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		if(!adsEnabled(context))
			setVisibility(View.GONE);
		else
			setVisibility(View.VISIBLE);
		
		setOnClickListener(mClickListener);
	}

	private View.OnClickListener mClickListener = new View.OnClickListener() {

		public void onClick(View v) {
			if(_url.length() > 0) {
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(_url));
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				LastFMApplication.getInstance().getApplicationContext().startActivity(i); 
			}
		}
	};

	
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		
		if(_cachedWidth != getWidth())
			new FetchAdTask().execute((Void)null);
		_cachedWidth = getWidth();
	}
	
    private class FetchAdTask extends UserTask<Void, Void, Boolean> {
    	Bitmap mBitmap = null;
    	
        @Override
    	public void onPreExecute() {
        	setImageBitmap(null);
        }
    	
        @Override
        public Boolean doInBackground(Void...params) {
            boolean success = false;
    		try {
   				mBitmap = UrlUtil.getImage(new URL(_imageTable.get(Integer.valueOf(getWidth() - 10))));
				for( int i = 0; i < _trackerUrls.size(); ++i ) {
					URL url = UrlUtil.getRedirectedUrl( new URL( ((String)_trackerUrls.elementAt( i ) )) );
					UrlUtil.doGet( url );
				}
    			success = true;
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
            return success;
        }

        @Override
        public void onPostExecute(Boolean result) {
        	setImageBitmap(mBitmap);
        }
    }

}
