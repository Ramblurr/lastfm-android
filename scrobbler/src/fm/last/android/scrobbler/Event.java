/***************************************************************************
 *   Copyright 2005-2009 Last.fm Ltd.                                      *
 *   Portions contributed by Casey Link, Lukasz Wisniewski,                *
 *   Mike Jennings, and Michael Novak Jr.                                  *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.         *
 ***************************************************************************/
package fm.last.android.scrobbler;

import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.RadioGroup;
import android.widget.TextView;
import fm.last.api.ImageUrl;
import fm.last.api.LastFmServer;
import fm.last.api.WSError;

public class Event extends Activity {

	private TextView mTitle;
	private TextView mArtists;
	private TextView mVenue;
	private TextView mStreet;
	private TextView mMonth;
	private TextView mDay;
	private RadioGroup mAttendance;
	private Worker mPosterImageWorker;
	private RemoteImageView mPosterImage;
	private RemoteImageHandler mPosterImageHandler;

	public interface EventActivityResult
	{
		public void onEventStatus(int status);
	}
	
    private static int resourceToStatus(int resId) {
    	switch(resId) {
	    	case R.id.attending: return 0;
	    	case R.id.maybe: return 1;
	    	case R.id.notattending: return 2;
		}
    	return 2;
    }

    private static int statusToResource(int status) {
    	switch(status) {
	    	case 0: return R.id.attending;
	    	case 1: return R.id.maybe;
	    	case 2: return R.id.notattending;
		}
    	return R.id.notattending;
    }
	
    @Override
    public void onCreate( Bundle icicle )
    {
        super.onCreate( icicle );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        setContentView( R.layout.event );
        
        mTitle = (TextView)findViewById(R.id.title);
        mTitle.setText(getIntent().getStringExtra("lastfm.event.title"));

        mArtists = (TextView)findViewById(R.id.artists);
        mArtists.setText(getIntent().getStringExtra("lastfm.event.artists"));

        mVenue = (TextView)findViewById(R.id.venue);
        mVenue.setText(getIntent().getStringExtra("lastfm.event.venue"));

        mStreet = (TextView)findViewById(R.id.street);
        mStreet.setText(getIntent().getStringExtra("lastfm.event.street"));

        mMonth = (TextView)findViewById(R.id.month);
        mMonth.setText(getIntent().getStringExtra("lastfm.event.month"));

        mDay = (TextView)findViewById(R.id.day);
        mDay.setText(getIntent().getStringExtra("lastfm.event.day"));
        
        mPosterImage = (RemoteImageView)findViewById(R.id.poster);
        mPosterImageWorker = new Worker( "poster image worker" );
        mPosterImageHandler = new RemoteImageHandler( mPosterImageWorker
                .getLooper(), mHandler );
        mPosterImageHandler.removeMessages( RemoteImageHandler.GET_REMOTE_IMAGE );
        mPosterImageHandler.obtainMessage( RemoteImageHandler.GET_REMOTE_IMAGE,
                getIntent().getStringExtra("lastfm.event.poster") ).sendToTarget();
        
        int statusResource;
        try {
        	statusResource = statusToResource(
        			Integer.parseInt( 
        					getIntent().getStringExtra("lastfm.event.status") ) );
        } catch (Exception e) {
        	statusResource = R.id.notattending;
        }
        mAttendance = (RadioGroup)findViewById(R.id.attend);
        mAttendance.check(statusResource);
        
        findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
        });
        
        findViewById(R.id.showmap).setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		String query = "";
        		String street = getIntent().getStringExtra("lastfm.event.street");
        		String city = getIntent().getStringExtra("lastfm.event.city");
        		String postalcode = getIntent().getStringExtra("lastfm.event.postalcode");
        		String country = getIntent().getStringExtra("lastfm.event.country");
        		
        		if(street != null && street.length() > 0)
        			query += street + ",";
        		if(city != null && city.length() > 0)
        			query += " " + city + ",";
        		if(postalcode != null && postalcode.length() > 0)
        			query += " " + postalcode;
        		if(country != null && country.length() > 0)
        			query += " " + country;
            	final Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/?f=q&q=" + query + "&ie=UTF8&om=1&iwloc=addr")); 
                startActivity(myIntent);
                finish();
        	}
        });
        
        findViewById(R.id.ok).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				LastFmServer server = AndroidLastFmServerFactory.getServer();

				try {
					int status = resourceToStatus(mAttendance.getCheckedRadioButtonId());
					server.attendEvent(
							getIntent().getStringExtra("lastfm.event.id"), 
							String.valueOf(status), 
							ScrobblerApplication.the().getSession().getKey() );
					setResult(RESULT_OK, new Intent().putExtra("status", status));
					finish();
				} catch (WSError e) {
					ScrobblerApplication.the().presentError(Event.this, e);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
        });
    }

    private final Handler mHandler = new Handler()
    {

        public void handleMessage( Message msg )
        {

            switch ( msg.what )
            {
            case RemoteImageHandler.REMOTE_IMAGE_DECODED:
            	if(mPosterImage != null) {
                    mPosterImage.setArtwork( ( Bitmap ) msg.obj );
                    mPosterImage.invalidate();
            	}
                break;

            default:
                break;
            }
        }
    };

	@Override
	protected void onStop() {
		mPosterImageWorker.quit();
		super.onStop();
	}
    
    public static Intent intentFromEvent(Context packageContext, fm.last.api.Event event)
    {
	    Intent intent = new Intent( packageContext, fm.last.android.scrobbler.Event.class );
	    intent.putExtra("lastfm.event.id", Integer.toString(event.getId()));
	    intent.putExtra("lastfm.event.title", event.getTitle());
	    String artists = "";
	    for(String artist : event.getArtists()) {
	        if(artists.length() > 0)
	            artists += ", ";
	        artists += artist;
	    }
	    for(ImageUrl image : event.getImages()) {
	        if(image.getSize().contentEquals("large"))
	            intent.putExtra("lastfm.event.poster", image.getUrl());
	    }
	    intent.putExtra("lastfm.event.artists", artists);
	    intent.putExtra("lastfm.event.venue", event.getVenue().getName());
	    intent.putExtra("lastfm.event.street", event.getVenue().getLocation().getStreet());
	    intent.putExtra("lastfm.event.city", event.getVenue().getLocation().getCity());
	    intent.putExtra("lastfm.event.postalcode", event.getVenue().getLocation().getPostalcode());
	    intent.putExtra("lastfm.event.country", event.getVenue().getLocation().getCountry());
	    intent.putExtra("lastfm.event.month", new SimpleDateFormat("MMM").format(event.getStartDate()));
	    intent.putExtra("lastfm.event.day", new SimpleDateFormat("d").format(event.getStartDate()));
	    intent.putExtra("lastfm.event.status", event.getStatus());
	    return intent;
    }
}
