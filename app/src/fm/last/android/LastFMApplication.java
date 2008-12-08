package fm.last.android;

import java.util.WeakHashMap;

import android.app.Application;

public class LastFMApplication extends Application
{

    public WeakHashMap map;

    private static LastFMApplication instance;

    public static LastFMApplication getInstance()
    {

        return instance;
    }

    public void onCreate()
    {

        super.onCreate();
        instance = this;

        // construct an 'application global' object
        this.map = new WeakHashMap();
    }

    public void onTerminate()
    {

        // clean up application global
        this.map.clear();
        this.map = null;

        instance = null;
        super.onTerminate();
    }
}
