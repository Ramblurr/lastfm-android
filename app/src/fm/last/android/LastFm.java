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
package fm.last.android;

import java.net.URL;

import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.activity.Profile;
import fm.last.android.activity.SignUp;
import fm.last.android.utils.UserTask;
import fm.last.api.LastFmServer;
import fm.last.api.MD5;
import fm.last.api.Session;
import fm.last.api.WSError;
import fm.last.util.UrlUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

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
        String pass;
        
        new CheckUpdatesTask().execute((Void)null);
        
        if ( !user.equals( "" ) && !session_key.equals( "" ) )
        {
        	if(getIntent().getAction().equals("android.appwidget.action.APPWIDGET_CONFIGURE")) {
        		Intent intent = getIntent();
        		Bundle extras = intent.getExtras();
        		if (extras != null) {
        			int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 
        		            AppWidgetManager.INVALID_APPWIDGET_ID);
            		Intent resultValue = new Intent();
            		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            		setResult(RESULT_OK, resultValue);
        		}
        	} else {
	            Intent intent = getIntent();
	            intent = new Intent( LastFm.this, Profile.class );
	            startActivity( intent );
        	}
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
            	if (mLoginTask != null) return; 
            	
            	String user = mUserField.getText().toString();
                String password = mPassField.getText().toString();

                if(user.length() == 0 || password.length() == 0) {
					LastFMApplication.getInstance().presentError(v.getContext(), getResources().getString(R.string.ERROR_MISSINGINFO_TITLE),
							getResources().getString(R.string.ERROR_MISSINGINFO));
					return;
                }
            
                mLoginTask = new LoginTask( v.getContext() );
                mLoginTask.execute( user, password );
            }
        });
        
        mSignupButton.setOnClickListener( new OnClickListener() 
        {
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


    /** In a task because it can take a while, and Android has a tendency to 
      * panic and show the force quit/wait dialog quickly. And this blocks. 
      */
    private class LoginTask extends UserTask<String, Void, Session>
    {
    	Context context;
    	ProgressDialog mDialog;
    	
    	Exception e;
    	WSError wse;
    	
    	LoginTask( Context c )
    	{
    		this.context = c;
    		mLoginButton.setEnabled( false );
    		
			mDialog = ProgressDialog.show( c , "", "Authenticating", true, false );
			mDialog.setCancelable( true );
    	}
    	    	
        public Session doInBackground(String...params) 
        {
        	String user = params[0];
        	String pass = params[1];
        	
            try
            {
            	return login( user, pass );
            }
            catch ( WSError e )
            {
                wse = e;
            }
            catch ( Exception e )
            {
            	this.e = e;
            }        	

            return null;
        }         
        
        Session login( String user, String pass ) throws Exception, WSError
        {
            LastFmServer server = AndroidLastFmServerFactory.getServer();
            String md5Password = MD5.getInstance().hash(pass);
            String authToken = MD5.getInstance().hash(user + md5Password);
            Session session = server.getMobileSession(user, authToken);
            if(session == null)
            	throw(new WSError("auth.getMobileSession", "auth failure", WSError.ERROR_AuthenticationFailed));
            return session;
        }
        
        @Override
        public void onPostExecute( Session session ) 
        {
        	mLoginButton.setEnabled( true );
        	mLoginTask = null;
        	
        	if (session != null)
        	{
	            SharedPreferences.Editor editor = getSharedPreferences( PREFS, 0 ).edit();
	            editor.putString( "lastfm_user", session.getName() );
	            editor.putString( "lastfm_session_key", session.getKey());
	            editor.putString( "lastfm_subscriber", session.getSubscriber());
	            editor.commit();

	        	if(getIntent().getAction().equals("android.appwidget.action.APPWIDGET_CONFIGURE")) {
	        		Intent intent = getIntent();
	        		Bundle extras = intent.getExtras();
	        		if (extras != null) {
	        			int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 
	        		            AppWidgetManager.INVALID_APPWIDGET_ID);
	            		Intent resultValue = new Intent();
	            		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
	            		setResult(RESULT_OK, resultValue);
	        		}
	        	} else {
		            LastFMApplication.getInstance().map.put( "lastfm_session", session );
		            Intent intent = new Intent( LastFm.this, Profile.class );
		            intent.putExtra("lastfm.profile.new_user", mNewUser );
		            startActivity( intent );
	        	}
	            finish();
        	}
        	else if (wse != null)
        	{
                LastFMApplication.getInstance().presentError( context, wse );
            }
        	else if (e != null)
            {           	
        		AlertDialog.Builder d = new AlertDialog.Builder(LastFm.this);
        		d.setIcon(android.R.drawable.ic_dialog_alert);
        		d.setNeutralButton("OK",
        				new DialogInterface.OnClickListener() {
        					public void onClick(DialogInterface dialog, int whichButton)
        					{
        					}
        				});
            	if(e.getMessage().contains("code 403")) {
            		d.setTitle(getResources().getString(R.string.ERROR_AUTH_TITLE));
            		d.setMessage(getResources().getString(R.string.ERROR_AUTH));
    				((EditText)findViewById( R.id.password )).setText( "" );
            		d.setNegativeButton("Forgot Password",
            				new DialogInterface.OnClickListener() {
            					public void onClick(DialogInterface dialog, int whichButton)
            					{
            		            	final Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://www.last.fm/settings/lostpassword")); 
            		                startActivity(myIntent);
            					}
            				});
            	} else {
            		d.setTitle(getResources().getString(R.string.ERROR_SERVER_UNAVAILABLE_TITLE));
            		d.setMessage(getResources().getString(R.string.ERROR_SERVER_UNAVAILABLE));
            	}
        		d.show();
            }
            
            mDialog.dismiss();
        }
    }
    
    private LoginTask mLoginTask;
    

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
