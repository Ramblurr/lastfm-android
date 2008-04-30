package fm.last;

import java.io.*;
import java.net.*;
import java.util.*;

import org.kxmlrpc.XmlRpcClient;

import android.media.MediaPlayer;
import android.media.MediaPlayer.*;

import android.net.*;
import android.content.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.*;
import android.view.*;
import android.widget.*;

import fm.last.Log;


public class RadioClient extends Activity 
{
	private Radio m_radio = null;
	private EditText artistField;
	private MediaPlayer m_mediaPlayer;
	private SharedPreferences m_preferences = null;

	/** Called when the activity is first created. */
	public void onCreate( Bundle icicle )
	{
		super.onCreate( icicle );
		setContentView( R.layout.main );
		animate();
		
		m_preferences = getSharedPreferences("Last.fm", MODE_PRIVATE);

		String user = m_preferences.getString("username", "");
		String pass = m_preferences.getString("md5Password", "");
		
		if( user.length() == 0 || pass.length() == 0 ) {
			// show username / password activity
			startActivity(new Intent("ACCOUNTSETTINGS"));
		}

        Button play = (Button) findViewById( R.id.stop );
        play.setOnClickListener( new OnClickListener() 
        {
        	EditText edit = new EditText( RadioClient.this );
        	
            public void onClick( View v )
            {
            	edit.setLayoutParams( new LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT ) );
            	edit.setHint( "eg. Nirvana" );
            	edit.setSingleLine( true );
            	
                new AlertDialog.Builder( RadioClient.this )
                        .setTitle( "Similar Artist Radio" )
                        .setView( edit )
                        .setPositiveButton( "Tune-in", new DialogInterface.OnClickListener() 
                        {
                            public void onClick( DialogInterface dialog, int whichButton ) 
                            {
                                RadioClient.this.tuneIn( edit.getText().toString() );
                            }
                        })
                        .setNegativeButton( "Cancel", null )
                        .show();
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
	
	private void tuneIn( String artist )
	{
		Log.i( "Tuning-in..." );
		
		String user = m_preferences.getString("username", "");
		String pass = m_preferences.getString("md5Password", "");

		m_radio = new Radio( user, pass );
		String stationName = m_radio.tuneToSimilarArtist( artist );
		TrackInfo[] tracks = m_radio.getPlaylist();
		
		TextView v = (TextView) findViewById( R.id.station_name );
		v.setText( stationName );
		
		for (TrackInfo track : tracks)
		{
			Log.i( "Streaming track: " + track );

			setupUi( track );
			
			// Uri myUrl = Uri.parse(track.location());
			m_mediaPlayer = new MediaPlayer();
			
			try
			{
				m_mediaPlayer.setDataSource( track.location() );
				m_mediaPlayer.setOnBufferingUpdateListener( onBufferUpdate );
				// m_mediaPlayer.prepareAsync();
			}
			catch (java.io.IOException e) 
			{}

			break;
		}
	};
	
	private void setupUi( TrackInfo t )
	{
        TextView tv;
        tv = (TextView) findViewById( R.id.artist );
        tv.setText( t.artist() );
        tv = (TextView) findViewById( R.id.track_title );
        tv.setText( t.title() );
        
		ImageView v = (ImageView) findViewById( R.id.album_art );
		v.setImageBitmap( downloadAlbumArt( t ) );
	}
	
	private Bitmap downloadAlbumArt( TrackInfo t )
	{
		try 
		{ 
            URL url = albumArtUrl( t ); 
            URLConnection conn = url.openConnection(); 
            conn.connect(); 
            InputStream is = conn.getInputStream(); 
            BufferedInputStream bis = new BufferedInputStream( is ); 

            Bitmap bm = BitmapFactory.decodeStream(bis); 
            bis.close(); 
            is.close(); 
             
            return bm; 
		}
		catch (Exception e)
		{
            Log.e( e ); 
		}
		
		return null;
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
		public void onBufferingUpdate(MediaPlayer mp, int percent) 
		{
			Log.i( "BufferUpdate: " + percent + "%" );
		}
	};

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		super.onCreateOptionsMenu(menu);
		menu.add(0, 0, "Account", new Runnable() {
			public void run() {
				startActivity( new Intent( "ACCOUNTSETTINGS" ) );
			}
		});

		menu.add(0, 1, "Events", new Runnable() {
			public void run() {
				startActivity( new Intent( "EVENTSVIEW" ) );
			}
		});
		
		return true;
	}
}
