package fm.last.radio;

import java.net.*;
import java.util.*;

import org.kxmlrpc.XmlRpcClient;
	
import android.media.MediaPlayer;
import android.media.MediaPlayer.*;

import android.content.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.*;
import android.view.*;
import android.widget.*;

import fm.last.Application;
import fm.last.ImageLoader;
import fm.last.Log;
import fm.last.R;
import fm.last.TrackInfo;


public class RadioView extends Activity 
{
	private Radio m_radio = null;
	private ImageLoader m_imageLoader;
	
	private enum Requests { Login }
	private enum MenuItems{ Account, Events }

	/** Called when the activity is first created. */
	public void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );
	 
		String user = Application.instance().userName();
		String pass = Application.instance().password();

		m_imageLoader = new ImageLoader(this);
		
		if( user.length() == 0 || pass.length() == 0 ) 
		{
			// show username / password activity
			startSubActivity( new Intent("ACCOUNTSETTINGS"), Requests.Login.ordinal() );
			return;
		}
		else	
			init();
	}
	
	protected void onActivityResult( int requestCode, int resultCode, String data, Bundle extras )
	{
		if( requestCode == Requests.Login.ordinal() )
			switch (resultCode)
			{
				case RESULT_OK:
					init();
				default:
					finish();
			}
	}
	
	RadioEventHandler m_radioEventHandler = new RadioEventHandler()
	{
		public void onTrackEnded( TrackInfo track )
		{
			
		}

		public void onTrackStarted( TrackInfo track )
		{
			setupUi( track );
		}
		
	};
	
	final private void init()
	{

		m_radio = new Radio();	
		m_radio.addRadioHandler( m_radioEventHandler );

		setContentView( R.layout.radio_client );
		
		animate();
		
        ImageButton play = (ImageButton) findViewById( R.id.stop );
        
       
        play.setOnClickListener( new OnClickListener() 
        {
        	EditText edit = new EditText( RadioView.this );
        	
            public void onClick( View v )
            {
            	edit.setLayoutParams( new LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT ) );
            	edit.setHint( "eg. Nirvana" );
            	edit.setSingleLine( true );
            	
                new AlertDialog.Builder( RadioView.this )
                        .setTitle( "Similar Artist Radio" )
                        .setView( edit )
                        .setPositiveButton( "Tune-in", new DialogInterface.OnClickListener() 
                        {
                            public void onClick( DialogInterface dialog, int whichButton ) 
                            {
                                RadioView.this.tuneInSimilarArtists( edit.getText().toString() );
                            }
                        })
                        .setNegativeButton( "Cancel", null )
                        .show();
            }
        });
        
        // check intent for event to begin playback
        ImageButton skip = (ImageButton) findViewById( R.id.skip );
        skip.setOnClickListener( new OnClickListener() 
        {
        	public void onClick( View v )
        	{
        		m_radio.skip();
        	}
        });
	}

	final private void animate()
	{
        AnimationSet set = new AnimationSet( true );
		
        Animation animation = new AlphaAnimation( 0.0f, 1.0f );
        animation.setDuration( 1800 );
        set.addAnimation( animation );

        animation = new TranslateAnimation( Animation.RELATIVE_TO_SELF, 
        									-1.0f,
        									Animation.RELATIVE_TO_SELF, 
        									0.0f,
        									Animation.RELATIVE_TO_SELF, 
        									-1.0f,
        									Animation.RELATIVE_TO_SELF, 
        									0.0f );
        animation.setDuration( 500 );
        set.addAnimation( animation );

        LayoutAnimationController controller = new LayoutAnimationController( set, 0.5f );
        LinearLayout l = (LinearLayout) findViewById( R.id.layout );
        l.setLayoutAnimation( controller );
	}
	
	protected void tuneInSimilarArtists( String artist )
	{
		Log.i( "Tuning-in..." );
		
		String stationName = m_radio.tuneToSimilarArtist( artist );
		
		TextView v = (TextView) findViewById( R.id.station_name );
		v.setText( stationName );
		
		m_radio.play();
	};

	protected void tuneInTag( String tag )
	{
		Log.i( "Tuning-in..." );
		
		String stationName = m_radio.tuneToTag( tag );
		
		TextView v = (TextView) findViewById( R.id.station_name );
		v.setText( stationName );
		
		m_radio.play();
	};
	
	private void setupUi( TrackInfo t )
	{
        TextView tv;
        tv = (TextView) findViewById( R.id.artist );
        tv.setText( t.artist() );
        tv = (TextView) findViewById( R.id.track_title );
        tv.setText( t.title() );
        
		ImageView v = (ImageView) findViewById( R.id.album_art );
		try {
			m_imageLoader.loadImage(v, t.imageUrl());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/** kXMLRPC throws Exception from execute :( */
	private URL albumArtUrl( TrackInfo t ) throws Exception
	{
		XmlRpcClient client = new XmlRpcClient( "http://ws.audioscrobbler.com/1.0/rw/xmlrpc.php" );
	
		Vector<String> v = new Vector<String>( 4 );
		v.add( t.artist() );
		v.add( t.title() );
		v.add( t.album() );
		v.add( "en" );
		
		Map<String, String> m = (Map<String, String>) client.execute( "trackMetadata", v, this );
		return new URL( m.get( "albumCover" ) );
	}

	private OnBufferingUpdateListener onBufferUpdate = new OnBufferingUpdateListener()
	{
		public void onBufferingUpdate( MediaPlayer mp, int percent ) 
		{
			Log.i( "BufferUpdate: " + percent + "%" );
		}
	};
	
	protected void setHeaderRes( int resId )
	{
		LayoutInflater inflater = LayoutInflater.from( this );
		
		View radioPartial = inflater.inflate( resId, null );
		LinearLayout radioLayout = (LinearLayout)findViewById( R.id.layout );
		radioPartial.setLayoutParams( new LinearLayout.LayoutParams( LinearLayout.LayoutParams.FILL_PARENT, 
																	 LinearLayout.LayoutParams.WRAP_CONTENT) );
		radioLayout.addView( radioPartial, 0 );
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		super.onCreateOptionsMenu(menu);
		menu.add( 0, MenuItems.Account.ordinal(), 0, "Account" );

		menu.add(0, MenuItems.Events.ordinal(), 1, "Events");
		
		return true;
	}
	
	public boolean onOptionsItemSelected(Menu.Item item)
	{
		int id = item.getItemId();
		if( id == MenuItems.Account.ordinal() )
		{
			startActivity( new Intent( "ACCOUNTSETTINGS" ) );			
		}
		else if( id == MenuItems.Events.ordinal() )
		{
			startActivity( new Intent( "EVENTSVIEW" ) );
		}
		else
		{
			return false;
		}
		return true;
	}
}
