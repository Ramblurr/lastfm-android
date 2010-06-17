package fm.last.android.db;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
	
	/**
	 * Load the queued entries excluding the current track.
	 * @return list of queue entries.
	 */
	public List<ScrobblerQueueEntry> loadQueue()
	{
		return loadWithQualification("WHERE CurrentTrack=0");
	}
	
	/**
	 * Replace all queue entries.
	 * @param queue a {@link Collection} of queue entries.
	 */
	public void saveQueue(Collection<ScrobblerQueueEntry> queue)
	{
		removeWithQualification("WHERE CurrentTrack=0");
		for (ScrobblerQueueEntry entry : queue) {
			if (entry==null) continue;
			entry.currentTrack = false;
		}
		save(queue);
	}
	
	/**
	 * Load the current track entry from the table.
	 * @return {@link ScrobblerQueueEntry} representing the current track.
	 */
	public ScrobblerQueueEntry loadCurrentTrack()
	{
		List<ScrobblerQueueEntry> entries = loadWithQualification("WHERE CurrentTrack=1");
		if (entries!=null && entries.size()==1) {
			return entries.get(0);
		}
		return null;
	}
	
	/**
	 * Set the current track entry in the table.
	 * @param track the {@link ScrobblerQueueEntry} for the current track.
	 */
	public void saveCurrentTrack(ScrobblerQueueEntry track) 
	{
		removeWithQualification("WHERE CurrentTrack=1");
		if (track!=null) {
			track.currentTrack = true;
			save(Collections.singleton(track));
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
		entry.currentTrack = c.getInt(c.getColumnIndex("CurrentTrack"))==0 ? false : true;
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
		content.put("CurrentTrack", data.currentTrack);
		content.put("Duration", data.duration);
		content.put("Loved", data.loved);
		content.put("PostedNowPlaying", data.postedNowPlaying);
		content.put("Rating", data.rating);
		content.put("StartTime", data.startTime);
		content.put("Title", data.title);
		content.put("TrackAuth", data.trackAuth);
	}

}
