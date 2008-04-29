package fm.last;

import android.media.MediaPlayer;
import android.media.MediaPlayer.*;

import android.net.*;
import android.content.DialogInterface;
import android.content.Intent;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
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
		super.onCreate(icicle);
		setContentView(R.layout.main);
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
		Log.i( "Tune-in " + artist );
		
		String user = m_preferences.getString("username", "");
		String pass = m_preferences.getString("md5Password", "");

		m_radio = new Radio( user, pass );
		Log.i( "Password hash = " + pass );
		
		m_radio.tuneToSimilarArtist( artist );
		TrackInfo[] tracks = m_radio.getPlaylist();
		
		for (TrackInfo track : tracks)
		{
			Log.i( "Streaming track: " + track );
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

	private OnBufferingUpdateListener onBufferUpdate = new OnBufferingUpdateListener() {
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
