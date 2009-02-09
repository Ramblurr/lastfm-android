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

import android.os.Looper;

public class Worker implements Runnable
{

    private final Object mLock = new Object();
    private Looper mLooper;

    /**
     * Creates a worker thread with the given name. The thread then runs a
     * {@link android.os.Looper}.
     * 
     * @param name
     *            A name for the new thread
     */
    public Worker( String name )
    {

        Thread t = new Thread( null, this, name );
        t.setPriority( Thread.MIN_PRIORITY );
        t.start();
        synchronized ( mLock )
        {
            while ( mLooper == null )
            {
                try
                {
                    mLock.wait();
                }
                catch ( InterruptedException ex )
                {
                }
            }
        }
    }

    public Looper getLooper()
    {

        return mLooper;
    }

    public void run()
    {

        synchronized ( mLock )
        {
            Looper.prepare();
            mLooper = Looper.myLooper();
            mLock.notifyAll();
        }
        Looper.loop();
    }

    public void quit()
    {

        mLooper.quit();
    }
}
