package fm.last;

import java.math.BigInteger;
import java.security.MessageDigest;

import android.media.MediaPlayer;
import android.media.MediaPlayer.*;

import android.net.*;
import android.content.Intent;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View.*;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;

public class RadioClient extends Activity {
    private Radio m_radio = null;
    private EditText artistField;
    private MediaPlayer m_mediaPlayer;
    private SharedPreferences m_preferences = null;
    
    /** Called when the activity is first created. */
    public void onCreate( Bundle icicle )
    {
        super.onCreate( icicle );
        setContentView( R.layout.main );

        m_preferences = getSharedPreferences( "Last.fm", MODE_PRIVATE );
        
        if( m_preferences.getString( "username", "" ).length() == 0 ||
        	m_preferences.getString( "md5Password", "" ).length() == 0 )
        {
            //show username / password activity
            startActivity( new Intent( "android.intent.action.ACCOUNTSETTINGS" ) );
        }
        
        //userField = (EditText)findViewById( R.id.login_username );
        //passwordField = (EditText)findViewById( R.id.login_password );
        artistField = (EditText)findViewById( R.id.radio_similarartist );
        
        final Button loginButton = (Button)findViewById( R.id.login_button );
        
        loginButton.setOnClickListener( onLoginPush );
    }


    private OnClickListener onLoginPush = new OnClickListener()
    {
        public void onClick(View v)
        {
           String userName = m_preferences.getString( "username", "" );
           String userPassword = m_preferences.getString( "md5Password", "" );
           String artist = artistField.getText().toString();

           m_radio = new Radio( userName, userPassword );
           Log.i( "Last.fm", "Password hash = " + userPassword );
           m_radio.tuneToSimilarArtist(artist);
           TrackInfo[] tracks = m_radio.getPlaylist();
           for( TrackInfo track: tracks )
           {
        	   Log.i( "Last.fm", "Streaming track: " + track );
        	//   Uri myUrl = Uri.parse(track.location());
        	   m_mediaPlayer = new MediaPlayer();
        	   try
        	   {
        		   m_mediaPlayer.setDataSource( track.location() );
        		   m_mediaPlayer.setOnBufferingUpdateListener( onBufferUpdate );
            	  // m_mediaPlayer.prepareAsync();
        	   }
        	   catch( java.io.IOException e )
        	   {
        		   
        	   }

            break;
           }
        }
    };
    
    private OnBufferingUpdateListener onBufferUpdate = new OnBufferingUpdateListener()
    {
		   public void onBufferingUpdate( MediaPlayer mp, int percent )
		   {
			   Log.i( "Last.fm", "BufferUpdate: " + percent + "%" );
		   }
    };
    
    public boolean onCreateOptionsMenu( Menu menu )
    {
       super.onCreateOptionsMenu( menu );
       menu.add( 0, 0, "Account Settings", new Runnable(){ public void run(){ startActivity( new Intent( "android.intent.action.ACCOUNTSETTINGS" ) ); } } );
       return true;
    }

    private String md5( String in )
    {
        try
        {
            MessageDigest m = MessageDigest.getInstance( "MD5" );
            m.update( in.getBytes(), 0, in.length() );
            return (new BigInteger( 1, m.digest())).toString( 16 );
        }
        catch( java.security.NoSuchAlgorithmException e )
        {
            Log.e( "Last.fm", e.toString() );
            return "";
        }
    }
}
