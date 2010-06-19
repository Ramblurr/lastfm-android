package fm.last.android.db;

import fm.last.android.LastFMApplication;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LastFmDbHelper extends SQLiteOpenHelper 
{
	/**
	 * The name of the Last.fm database.
	 */
	public static final String DB_NAME = "lastfm";
	
	/**
	 * The DB's version number.
	 * This needs to be increased on schema changes.
	 */
	public static final int DB_VERSION = 5;
	
	/**
	 * Singleton instance of {@link ScrobblerQueueDao}.
	 */
	private static LastFmDbHelper instance = null;

	/** 
	 * @return the {@link ScrobblerQueueDao} singleton.
	 */
	public static LastFmDbHelper getInstance() 
	{
		if(instance != null) {
			return instance;
		} 
		else {
			return new LastFmDbHelper();
		}
	}
	
	public void clearDatabase()
	{
		ScrobblerQueueDao.getInstance().clearTable();
		RecentStationsDao.getInstance().clearTable();
	}

	
	private LastFmDbHelper() 
	{
		super(LastFMApplication.getInstance().getApplicationContext(), DB_NAME, null, DB_VERSION);
	}

	/*
	 * (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		// create the table for the recent stations
		db.execSQL("CREATE TABLE IF NOT EXISTS " + RecentStationsDao.DB_TABLE_RECENTSTATIONS +
				" (Url VARCHAR UNIQUE NOT NULL PRIMARY KEY, " +
				"Name VARCHAR NOT NULL, " +
				"Timestamp INTEGER NOT NULL)");
		
		// create table for scrobbling queue
		// the start time is used as PK because there can be only one track at a time
		db.execSQL("CREATE TABLE IF NOT EXISTS " + ScrobblerQueueDao.DB_TABLE_SCROBBLERQUEUE +
				" (Artist VARCHAR NOT NULL," +
				" Title VARCHAR NOT NULL," +
				" Album VARCHAR NOT NULL," +
				" TrackAuth VARCHAR NOT NULL," +
				" Rating VARCHAR NOT NULL," +
				" StartTime INTEGER NOT NULL PRIMARY KEY," +
				" Duration INTEGER NOT NULL," +
				" PostedNowPlaying INTEGER NOT NULL," +
				" Loved INTEGER NOT NULL," +
				" CurrentTrack INTEGER NOT NULL)");

	}

	/*
	 * (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		// for now we just drop everything and create it again
		db.execSQL("DROP TABLE IF EXISTS " + RecentStationsDao.DB_TABLE_RECENTSTATIONS);
		db.execSQL("DROP TABLE IF EXISTS " + ScrobblerQueueDao.DB_TABLE_SCROBBLERQUEUE);

		onCreate(db);
	}

}
