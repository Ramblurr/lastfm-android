package fm.last.android;

import java.io.IOException;
import java.net.URL;

import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.activity.AddToPlaylist;
import fm.last.android.activity.Player;
import fm.last.android.activity.Profile;
import fm.last.android.activity.SignUp;
import fm.last.android.utils.UserTask;
import fm.last.api.LastFmServer;
import fm.last.api.MD5;
import fm.last.api.Session;
import fm.last.api.WSError;
import fm.last.util.UrlUtil;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class LastFm extends Activity
{

    public static final String PREFS = "LoginPrefs";
    public static final String DB_NAME = "lastfm";
    public static final String DB_TABLE_RECENTSTATIONS = "t_recentstations";
    private boolean mLoginShown;
    private EditText mPassField;
    private EditText mUserField;
    private Button mLoginButton;
    private Button mSignupButton;
    
    /** Specifies if the user has just signed up */
    private boolean mNewUser = false;

    String authInfo;

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle icicle )
    {

        super.onCreate( icicle );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        SharedPreferences settings = getSharedPreferences( PREFS, 0 );
        String user = settings.getString( "lastfm_user", "" );
        String session_key = settings.getString( "lastfm_session_key", "" );
        String subscriber = settings.getString( "lastfm_subscriber", "0" );
        String pass;
        
        new CheckUpdatesTask().execute((Void)null);
        
        if ( !user.equals( "" ) && !session_key.equals( "" ) )
        {
        	Session session = new Session(user, session_key, subscriber);
            LastFMApplication.getInstance().map.put( "lastfm_session", session );
            Intent intent = new Intent( LastFm.this, Profile.class );
            startActivity( intent );
            finish();
            return;
        }
        setContentView( R.layout.login );
        mPassField = ( EditText ) findViewById( R.id.password );
        mUserField = ( EditText ) findViewById( R.id.username );
        if(!user.equals(""))
        	mUserField.setText(user);
        mLoginButton = ( Button ) findViewById( R.id.sign_in_button );
        mSignupButton = ( Button ) findViewById( R.id.sign_up_button );
        mUserField.setNextFocusDownId( R.id.password );
        
        mPassField.setOnKeyListener( new View.OnKeyListener()
        {

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch ( event.getKeyCode() ) {
                case KeyEvent.KEYCODE_ENTER:
                    mLoginButton.setPressed(true);
                    mLoginButton.performClick();
                    return true;
                }
                return false;
            }
        });

        if ( icicle != null )
        {
			user = icicle.getString( "username" );
			pass = icicle.getString( "pass" );
			if(user != null)
			    mUserField.setText( user );
			
			if(pass != null)
			    mPassField.setText( pass );
        }
        mLoginButton.setOnClickListener( new View.OnClickListener()
        {

            public void onClick( View v )
            {

                String user = mUserField.getText().toString();
                String password = mPassField.getText().toString();

                if(user.length() == 0 || password.length() == 0) {
					LastFMApplication.getInstance().presentError(v.getContext(), getResources().getString(R.string.ERROR_MISSINGINFO_TITLE),
							getResources().getString(R.string.ERROR_MISSINGINFO));
					return;
                }
                
                try
                {
                	Session session = doLogin( user, password );
                    SharedPreferences settings = getSharedPreferences( PREFS, 0 );
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString( "lastfm_user", user );
                    editor.putString( "lastfm_session_key", session.getKey());
                    editor.putString( "lastfm_subscriber", session.getSubscriber());
                    editor.commit();
                    LastFMApplication.getInstance().map.put( "lastfm_session", session );
                    Intent intent = new Intent( LastFm.this, Profile.class );
                    intent.putExtra("lastfm.profile.new_user", mNewUser );
                    startActivity( intent );
                    finish();
                }
                catch ( WSError e )
                {
                    LastFMApplication.getInstance().presentError(v.getContext(), e);
                }
                catch ( Exception e )
                {
                	if(e.getMessage().contains("code 403")) {
    					LastFMApplication.getInstance().presentError(v.getContext(), getResources().getString(R.string.ERROR_AUTH_TITLE),
    							getResources().getString(R.string.ERROR_AUTH));
    					( ( EditText ) findViewById( R.id.password ) ).setText( "" );
                	} else {
    					LastFMApplication.getInstance().presentError(v.getContext(), getResources().getString(R.string.ERROR_SERVER_UNAVAILABLE_TITLE),
    							getResources().getString(R.string.ERROR_SERVER_UNAVAILABLE));
                	}
                }
            }
        } );
        
        mSignupButton.setOnClickListener( new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent( LastFm.this, SignUp.class );
				startActivityForResult(intent, 0);
			}
        	
        });
    }
    
    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data)
    {
    	if( requestCode != 0 || resultCode != RESULT_OK )
    		return;
    	
    	mUserField.setText( data.getExtras().getString("username") );
    	mPassField.setText( data.getExtras().getString("password") );
    	mNewUser = true;
    	mLoginButton.requestFocus();
    	mLoginButton.performClick();
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {

        outState.putBoolean( "loginshown", mLoginShown );
        if ( mLoginShown )
        {
            String user = mUserField.getText().toString();
            String password = mPassField.getText().toString();
            outState.putString( "username", user );
            outState.putString( "passowrd", password );
        }
        super.onSaveInstanceState( outState );
    }

    Session doLogin( String user, String pass ) throws Exception, WSError
    {
        LastFmServer server = AndroidLastFmServerFactory.getServer();
        String md5Password = MD5.getInstance().hash(pass);
        String authToken = MD5.getInstance().hash(user + md5Password);
        Session session = server.getMobileSession(user, authToken);
        if(session == null)
        	throw(new WSError("auth.getMobileSession", "auth failure", WSError.ERROR_AuthenticationFailed));
        return session;
    }

    private class CheckUpdatesTask extends UserTask<Void, Void, Boolean> {
    	private String mUpdateURL = "";
    	
        @Override
        public Boolean doInBackground(Void...params) {
            boolean success = false;

            try {
            	URL url = new URL("http://cdn.last.fm/client/android/"+getPackageManager().getPackageInfo("fm.last.android", 0).versionName+".txt" );
            	mUpdateURL = UrlUtil.doGet(url);
            	success = true;
            } catch (Exception e) {
            	// No updates available! Yay!
            }
            return success;
        }

        @Override
        public void onPostExecute(Boolean result) {
        	if(result) {
        		NotificationManager nm = ( NotificationManager ) getSystemService( NOTIFICATION_SERVICE );
        		Notification notification = new Notification(
        				R.drawable.as_statusbar, "A new version of Last.fm is available", System.currentTimeMillis() );
        		PendingIntent contentIntent = PendingIntent.getActivity( LastFm.this, 0,
        				new Intent( Intent.ACTION_VIEW, Uri.parse(mUpdateURL)), 0 );
        		notification.setLatestEventInfo( LastFm.this, "New version available",
        				"Click here to download the update", contentIntent );

        		nm.notify( 12345, notification );
        	}
        }
    }
}
