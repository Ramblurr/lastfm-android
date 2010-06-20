package fm.last.android.db;

import java.util.Collections;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import fm.last.android.scrobbler.ScrobblerQueueEntry;

/**
 * DAO for table containing scrobbler queue.
 * This DAO can be used to access the queued items and the
 * track currently played. 
 * @author atwupack
 */
public class ScrobblerQueueDao extends AbstractDao<ScrobblerQueueEntry>
{
	/**
	 * The table for the recent stations list.
	 */
	public static final String DB_TABLE_SCROBBLERQUEUE = "t_scrobblerqueue";
	
	/**
	 * The maximum number of entries in the queue.
	 */
	public static final int MAX_QUEUE_SIZE = 1000;
	
	/**
	 * Singleton instance of {@link ScrobblerQueueDao}.
	 */
	private static ScrobblerQueueDao instance = null;

	/** 
	 * @return the {@link ScrobblerQueueDao} singleton.
	 */
	public static ScrobblerQueueDao getInstance() 
	{
		if(instance != null) {
			return instance;
		} 
		else {
			return new ScrobblerQueueDao();
		}
	}
	
	/**
	 * @return the current number of queue entries.
	 */
	public int getQueueSize()
	{
		return countWithQualification("WHERE CurrentTrack=0");
	}
	
	/**
	 * Add e new entry to the queue.
	 * @param entry the {@link ScrobblerQueueEntry} to be added.
	 * @return 	<code>true</code> if the entry has been added
	 * 			<code>false</code> if the queue is full.
	 */
	public boolean addToQueue(ScrobblerQueueEntry entry)
	{
		if (entry==null) return true;
		int currentSize = getQueueSize();
		if (currentSize >= MAX_QUEUE_SIZE) {
			return false;
		}
		entry.currentTrack = false;
		save(Collections.singleton(entry));
		return true;
	}
	
	/**
	 * Remove an entry from the queue.
	 * @param entry the {@link ScrobblerQueueEntry} to be removed.
	 */
	public void removeFromQueue(ScrobblerQueueEntry entry)
	{
		if (entry==null) return;
		removeWithQualification("WHERE StartTime="+entry.startTime+" AND CurrentTrack=0");
	}
	
	/**
	 * Get an entry from the queue.
	 * @return a {@link ScrobblerQueueEntry} from the queue.
	 */
	public ScrobblerQueueEntry nextQueueEntry()
	{
		List<ScrobblerQueueEntry> queue = loadWithQualification("WHERE CurrentTrack=0 LIMIT 1");
		if (queue!=null && queue.size()>0) {
			return queue.get(0);
		}
		return null;
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
