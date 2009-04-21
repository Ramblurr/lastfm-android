/**
 * 
 */
package fm.last.android.scrobbler;

import java.io.IOException;

import fm.last.android.utils.UserTask;
import fm.last.api.LastFmServer;
import fm.last.api.MD5;
import fm.last.api.Session;
import fm.last.api.User;
import fm.last.api.WSError;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author sam
 *
 */
public class Login extends Activity {
    private EditText mPassField;
    private EditText mUserField;
    private Button mLoginButton;
    private Button mSignupButton;
    
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        requestWindowFeature( Window.FEATURE_NO_TITLE );
        setContentView( R.layout.login );
        mPassField = ( EditText ) findViewById( R.id.password );
        mUserField = ( EditText ) findViewById( R.id.username );
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
			String user = icicle.getString( "username" );
			String pass = icicle.getString( "pass" );
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
					ScrobblerApplication.the().presentError(v.getContext(), getResources().getString(R.string.ERROR_MISSINGINFO_TITLE),
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
				Intent intent = new Intent( Login.this, SignUp.class );
				startActivityForResult(intent, 0);
			}
        });

        // Find the widget id from the intent. 
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
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
	            SharedPreferences.Editor editor = getSharedPreferences( ScrobblerApplication.PREFS, 0 ).edit();
	            editor.putString( "lastfm_user", session.getName() );
	            editor.putString( "lastfm_session_key", session.getKey());
	            editor.putString( "lastfm_subscriber", session.getSubscriber());
	            editor.commit();
	            // Make sure we pass back the original appWidgetId
	            Intent resultValue = new Intent();
	            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
	            setResult(RESULT_OK, resultValue);
	            finish();
       	}
       	else if (wse != null)
       	{
       		ScrobblerApplication.the().presentError( context, wse );
        }
       	else if (e != null)
        {           	
       		AlertDialog.Builder d = new AlertDialog.Builder(Login.this);
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
}
