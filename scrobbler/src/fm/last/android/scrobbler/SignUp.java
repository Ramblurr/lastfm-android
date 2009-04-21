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
package fm.last.android.scrobbler;

import java.io.IOException;

import fm.last.api.LastFmServer;
import fm.last.api.Session;
import fm.last.api.WSError;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
				ScrobblerApplication.the().presentError(SignUp.this, e);
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