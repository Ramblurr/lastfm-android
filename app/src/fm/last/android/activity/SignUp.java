package fm.last.android.activity;

import java.io.IOException;

import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.R;
import fm.last.api.LastFmServer;
import fm.last.api.Session;
import fm.last.api.WSError;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class SignUp extends Activity
{
	protected Button mSignUpButton;
	protected Session mSession;
	protected TextView mUsername;
	protected TextView mPassword;
	protected TextView mEmail;
	
    protected OnClickListener mOnSignUpClickListener = new OnClickListener() 
    {	
		public void onClick(View v) {
			LastFmServer server = AndroidLastFmServerFactory.getServer();
			try {
				String username = mUsername.getText().toString();
				String password = mPassword.getText().toString();
				String email = mEmail.getText().toString();
				
				server.signUp(username, password, email);
				
				setResult( RESULT_OK, new Intent().putExtra("username", username)
												  .putExtra("password", password));
				finish();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WSError e )
			{
				LastFMApplication app = (LastFMApplication)getApplication();
				app.presentError(SignUp.this, e);
			}
		}
    };
	
    @Override
    public void onCreate( Bundle icicle )
    {
        super.onCreate( icicle );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        setContentView( R.layout.signup );
        
        mUsername = (TextView)findViewById( R.id.username );
        mPassword = (TextView)findViewById( R.id.password );
        mEmail = (TextView)findViewById( R.id.email );
        
        mSignUpButton = (Button)findViewById( R.id.create_account_button );
        mSignUpButton.setOnClickListener( mOnSignUpClickListener );
    }
}