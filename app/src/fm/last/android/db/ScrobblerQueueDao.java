package fm.last.android.db;

import android.content.ContentValues;
import android.database.Cursor;
import fm.last.android.scrobbler.ScrobblerQueueEntry;

public class ScrobblerQueueDao extends AbstractDao<ScrobblerQueueEntry>
{
	/**
	 * The table for the recent stations list.
	 */
	public static final String DB_TABLE_SCROBBLERQUEUE = "t_scrobblerqueue";
		
	private static ScrobblerQueueDao instance = null;

	public static ScrobblerQueueDao getInstance() {
		if(instance != null) {
			return instance;
		} else {
			return new ScrobblerQueueDao();
		}
	}
	/*
	 * (non-Javadoc)
	 * @see fm.last.android.db.AbstractDao#buildObject(android.database.Cursor)
	 */
	@Override
	protected ScrobblerQueueEntry buildObject(Cursor c) 
	{		
		ScrobblerQueueEntry entry = new ScrobblerQueueEntry();
		
		entry.album = c.getString(c.getColumnIndex("Album"));
		entry.artist = c.getString(c.getColumnIndex("Artist"));
		entry.duration = c.getLong(c.getColumnIndex("Duration"));
		entry.loved = c.getInt(c.getColumnIndex("Loved"))==0 ? false : true;
		entry.postedNowPlaying = c.getInt(c.getColumnIndex("PostedNowPlaying"))==0 ? false : true;
		entry.rating = c.getString(c.getColumnIndex("Rating"));
		entry.startTime = c.getLong(c.getColumnIndex("StartTime"));
		entry.title = c.getString(c.getColumnIndex("Title"));
		entry.trackAuth = c.getString(c.getColumnIndex("TrackAuth"));		
		
		return entry;
	}
	
	/*
	 * (non-Javadoc)
	 * @see fm.last.android.db.AbstractDao#getTableName()
	 */
	@Override
	protected String getTableName() 
	{	
		return DB_TABLE_SCROBBLERQUEUE;
	}
	
	/*
	 * (non-Javadoc)
	 * @see fm.last.android.db.AbstractDao#fillContent(android.content.ContentValues, java.lang.Object)
	 */
	@Override
	protected void fillContent(ContentValues content, ScrobblerQueueEntry data) 
	{
		content.put("Album", data.album);
		content.put("Artist", data.artist);
		content.put("Duration", data.duration);
		content.put("Loved", data.loved);
		content.put("PostedNowPlaying", data.postedNowPlaying);
		content.put("Rating", data.rating);
		content.put("StartTime", data.startTime);
		content.put("Title", data.title);
		content.put("TrackAuth", data.trackAuth);
	}

}
