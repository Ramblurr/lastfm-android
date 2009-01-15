package fm.last.android.activity;

import java.io.IOException;

import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.R;
import fm.last.android.RemoteImageHandler;
import fm.last.android.RemoteImageView;
import fm.last.android.Worker;
import fm.last.api.LastFmServer;
import fm.last.api.Session;
import fm.last.api.WSError;
import android.app.Activity;
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
        mPosterImageWorker = new Worker( "profile image worker" );
        mPosterImageHandler = new RemoteImageHandler( mPosterImageWorker
                .getLooper(), mHandler );
        mPosterImageHandler.removeMessages( RemoteImageHandler.GET_REMOTE_IMAGE );
        mPosterImageHandler.obtainMessage( RemoteImageHandler.GET_REMOTE_IMAGE,
                getIntent().getStringExtra("lastfm.event.poster") ).sendToTarget();
        
        mAttendance = (RadioGroup)findViewById(R.id.attend);
        String status = getIntent().getStringExtra("lastfm.event.status");
        System.out.printf("Incoming status: %s\n", status);
        if(status != null) {
	        if(status.contentEquals("0"))
	        	mAttendance.check(R.id.attending);
	        else if(status.contentEquals("1"))
	        	mAttendance.check(R.id.maybe);
	        else
	        	mAttendance.check(R.id.notattending);
        } else {
        	mAttendance.check(R.id.notattending);
        }
        
        findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
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
					if(mAttendance.getCheckedRadioButtonId() == R.id.attending) {
						server.attendEvent(getIntent().getStringExtra("lastfm.event.id"), "0", ((Session)LastFMApplication.getInstance().map.get("lastfm_session")).getKey());
					} else if(mAttendance.getCheckedRadioButtonId() == R.id.maybe) {
						server.attendEvent(getIntent().getStringExtra("lastfm.event.id"), "1", ((Session)LastFMApplication.getInstance().map.get("lastfm_session")).getKey());
					} else {
						server.attendEvent(getIntent().getStringExtra("lastfm.event.id"), "2", ((Session)LastFMApplication.getInstance().map.get("lastfm_session")).getKey());
					}
					finish();
				} catch (WSError e) {
					LastFMApplication.getInstance().presentError(Event.this, e);
				} catch (IOException e) {
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
}
