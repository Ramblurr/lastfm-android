package fm.last.android;

import java.util.HashMap;
import java.util.WeakHashMap;

import fm.last.android.AndroidLastFmServerFactory;
import fm.last.api.LastFmServer;
import fm.last.api.MD5;
import fm.last.api.Session;

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
                Intent intent = new Intent( LastFm.this, LastFMHome.class );
                startActivity( intent );
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
            mLoginShown = icicle.getBoolean( "loginshown" );
            if ( mLoginShown )
            {
                user = icicle.getString( "username" );
                pass = icicle.getString( "pass" );
                showLogin( user, pass );
                return;
            }
        }

        setContentView( R.layout.main );
        Button b = ( Button ) findViewById( R.id.buttonSetup );
        b.setOnClickListener( mGoListener );

        ImageView i = ( ImageView ) findViewById( R.id.main_logo );
        i.setAdjustViewBounds( true );
        i.setBackgroundResource( R.drawable.lastfm_revolution );

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

    private void showLogin( String user, String pass )
    {

        setContentView( R.layout.login );
        mLoginShown = true;
        if ( user != null && pass != null )
        {
            ( ( EditText ) findViewById( R.id.username ) ).setText( user );
            ( ( EditText ) findViewById( R.id.password ) ).setText( pass );
        }
        LastFm.this.setTitle( "LastFM: Login" );
        Button button = ( Button ) findViewById( R.id.cancel );

        button.setOnClickListener( new View.OnClickListener()
        {

            public void onClick( View v )
            {

                setResult( RESULT_CANCELED );
                finish();
            }
        } );

        button = ( Button ) findViewById( R.id.login );
        button.setOnClickListener( new View.OnClickListener()
        {

            public void onClick( View v )
            {

                String user = ( ( EditText ) findViewById( R.id.username ) )
                        .getText().toString();
                String password = ( ( EditText ) findViewById( R.id.password ) )
                        .getText().toString();

                try
                {
                    doLogin( user, password );
                    SharedPreferences settings = getSharedPreferences( PREFS, 0 );
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString( "lastfm_user", user );
                    editor.putString( "lastfm_pass", password );
                    editor.commit();
                    Intent intent = new Intent( LastFm.this, LastFMHome.class );
                    startActivity( intent );
                    finish();
                }
                catch ( Exception e )
                { // login failed
                    TextView tv = new TextView( LastFm.this );
                    tv.setText( "Error Logging in: " + e.getMessage() );
                    LinearLayout l = ( LinearLayout ) findViewById( R.id.loginLayout );
                    l.addView( tv );
                }
            }
        } );
    }

    private OnClickListener mGoListener = new OnClickListener()
    {

        public void onClick( View v )
        {

            showLogin( null, null );
        }
    };

    void doLogin( String user, String pass ) throws Exception
    {
        LastFmServer server = AndroidLastFmServerFactory.getServer();
        String md5Password = MD5.getInstance().hash(pass);
        String authToken = MD5.getInstance().hash(user + md5Password);
        Session session = server.getMobileSession(user, authToken);
        if ( session == null )
            throw ( new Exception( "Could not log in." ) );
        LastFMApplication.getInstance().map.put( "lastfm_session", session );

    }

}
