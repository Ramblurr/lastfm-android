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
	
	private final LastFmDbHelper dbHelper;
	
	private ScrobblerQueueDao() 
	{
		dbHelper = new LastFmDbHelper();
	}
	
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
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected String getTableName() 
	{	
		return DB_TABLE_SCROBBLERQUEUE;
	}
	@Override
	protected void fillContent(ContentValues content, ScrobblerQueueEntry data) {
		// TODO Auto-generated method stub
		
	}

}
