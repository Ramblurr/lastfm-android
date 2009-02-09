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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


public class RemoteImageHandler extends Handler
{
    Handler mParentHandler;
    
    public  static final int GET_REMOTE_IMAGE = 3;
    public static final int REMOTE_IMAGE_DECODED = 4;
    
    public RemoteImageHandler( Looper looper, Handler parentHandler )
    {
        super( looper );
        mParentHandler = parentHandler;        
    }

    public void handleMessage( Message msg )
    {

        String url = ( String ) msg.obj;
        if ( msg.what == GET_REMOTE_IMAGE )
        {
            // while decoding the new image, show the default album art
            Message numsg = mParentHandler
                    .obtainMessage( REMOTE_IMAGE_DECODED, null );
            mParentHandler.removeMessages( REMOTE_IMAGE_DECODED );
            mParentHandler.sendMessageDelayed( numsg, 300 );
            Bitmap bm = getArtwork( url );
            if ( bm != null )
            {
                numsg = mParentHandler.obtainMessage( REMOTE_IMAGE_DECODED, bm );
                mParentHandler.removeMessages( REMOTE_IMAGE_DECODED );
                mParentHandler.sendMessage( numsg );
            }
        }
    }
    
    private Bitmap getArtwork( String urlstr )
    {

        try
        {
            URL url;

            url = new URL( urlstr );

            HttpURLConnection c = ( HttpURLConnection ) url.openConnection();
            c.setDoInput( true );
            c.connect();
            InputStream is = c.getInputStream();
            Bitmap img;
            img = BitmapFactory.decodeStream( is );
            return img;
        }
        catch ( MalformedURLException e )
        {
            Log.d( "RemoteImageHandler", "RemoteImageWorker passed invalid URL: " + urlstr );
        }
        catch ( IOException e )
        {
            Log.d( "RemoteImageHandler", "RemoteImageWorker IO exception: " + e );
        }
        return null;
    }
    
}
