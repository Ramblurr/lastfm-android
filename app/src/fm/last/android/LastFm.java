package fm.last.android;

import java.util.HashMap;
import java.util.WeakHashMap;

import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.activity.Home;
import fm.last.api.LastFmServer;
import fm.last.api.MD5;
import fm.last.api.Session;
import fm.last.api.WSError;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;

public class LastFm extends Activity
{

    public static final String PREFS = "LoginPrefs";
    public static final String DB_NAME = "lastfm";
    public static final String DB_TABLE_RECENTSTATIONS = "t_recentstations";
    private boolean mLoginShown;

    String authInfo;

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle icicle )
    {

        super.onCreate( icicle );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        SharedPreferences settings = getSharedPreferences( PREFS, 0 );
        String user = settings.getString( "lastfm_user", "" );
        String pass = settings.getString( "lastfm_pass", "" );
        if ( !user.equals( "" ) && !pass.equals( "" ) )
        {
            try
            {
                doLogin( user, pass );
                Intent intent = new Intent( LastFm.this, Home.class );
                startActivity( intent );
                finish();
            }
            catch ( Exception e )
            { // login failed
                Intent data = new Intent();
                data.setAction( e.getMessage() );
                setResult( RESULT_CANCELED, data );
            }
        }

        if ( icicle != null )
        {
			user = icicle.getString( "username" );
			pass = icicle.getString( "pass" );
			if(user != null)
				( ( EditText ) findViewById( R.id.username ) ).setText( user );
			
			if(pass != null)
				( ( EditText ) findViewById( R.id.password ) ).setText( pass );
        }

        setContentView( R.layout.login );
        Button button = ( Button ) findViewById( R.id.sign_in_button );
        button.setOnClickListener( new View.OnClickListener()
        {

            public void onClick( View v )
            {

                String user = ( ( EditText ) findViewById( R.id.username ) )
                        .getText().toString();
                String password = ( ( EditText ) findViewById( R.id.password ) )
                        .getText().toString();

                if(user.length() == 0 || password.length() == 0) {
					LastFMApplication.getInstance().presentError(v.getContext(), getResources().getString(R.string.ERROR_MISSINGINFO_TITLE),
							getResources().getString(R.string.ERROR_MISSINGINFO));
					return;
                }
                
                try
                {
                    doLogin( user, password );
                    SharedPreferences settings = getSharedPreferences( PREFS, 0 );
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString( "lastfm_user", user );
                    editor.putString( "lastfm_pass", password );
                    editor.commit();
                    Intent intent = new Intent( LastFm.this, Home.class );
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
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {

        outState.putBoolean( "loginshown", mLoginShown );
        if ( mLoginShown )
        {
            String user = ( ( EditText ) findViewById( R.id.username ) )
                    .getText().toString();
            String password = ( ( EditText ) findViewById( R.id.password ) )
                    .getText().toString();
            outState.putString( "username", user );
            outState.putString( "passowrd", password );
        }
        super.onSaveInstanceState( outState );
    }

    void doLogin( String user, String pass ) throws Exception, WSError
    {
        LastFmServer server = AndroidLastFmServerFactory.getServer();
        String md5Password = MD5.getInstance().hash(pass);
        String authToken = MD5.getInstance().hash(user + md5Password);
        Session session = server.getMobileSession(user, authToken);
        if(session == null)
        	throw(new WSError("auth.getMobileSession", "auth failure", WSError.ERROR_AuthenticationFailed));
        LastFMApplication.getInstance().map.put( "lastfm_session", session );
    }

}
