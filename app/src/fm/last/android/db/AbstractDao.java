package fm.last.android.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Abstract base class for all DAOs.
 * @author atwupack
 */
public abstract class AbstractDao<T>
{
	
	protected AbstractDao() 
	{
		dbHelper = LastFmDbHelper.getInstance();
	}
	
	protected final LastFmDbHelper dbHelper;

	/** 
	 * @return the name of the table accessed by the implementing DAO.
	 */
	abstract protected String getTableName();
	
	/**
	 * Remove all rows from the table.
	 */
	public void clearTable()
	{
		removeWithQualification(null);
		log(Log.DEBUG, "Cleared table "+getTableName());
	}
	
	/**
	 * Helper method for logging.
	 * @param priority the logging severity.
	 * @param msg the message to be logged.
	 */
	protected void log(int priority, String msg)
	{
		Log.println(priority, "lastfm.db."+getTableName(), msg);
	}
	
	/**
	 * Load entries from DB.
	 * @param qual an optional qualification
	 * @return a list with the found objects
	 */
	protected List<T> loadWithQualification(String qual)
	{
		String query = "SELECT * FROM " + getTableName();
		if (qual!=null) {
			query += " " + qual;
		}
		log(Log.DEBUG, query);
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			db = dbHelper.getReadableDatabase();
			c = db.rawQuery(query , null);
			List<T> result = new ArrayList<T>(c.getCount());
			if (c.getCount() > 0) {
				c.moveToFirst();				
				// Loop through all Results
				do {
					result.add(buildObject(c));
				} while (c.moveToNext());
			}
			c.close();
			return result;
		}
		finally {
			close(c,db);
		}
	}
	
	protected void close(Cursor c, SQLiteDatabase db)
	{
		if (c!=null) {
			c.close();
		}
		if (db!=null) {
			db.close();
		}
	}
	
	/**
	 * Load all entries of the table.
	 * @return a list with the entries.
	 */
	public List<T> loadAll()
	{
		return loadWithQualification(null);
	}
	
	/**
	 * Inserts a list of objects as rows into the table.
	 * @param objects the objects to be inserted.
	 */
	public void save(Collection<T> objects)
	{
		SQLiteDatabase db = null;
		ContentValues values = new ContentValues();
		try {
			db =  dbHelper.getWritableDatabase();;
			for (T newObject : objects) {
				if (newObject==null) continue;
				values.clear();
				fillContent(values, newObject);
				db.replace(getTableName(), null, values);
				log(Log.DEBUG,"Inserted/replaced row with values "+values);
			}
		}		
		finally {
			close(null,db);
		}
	}
	
	/**
	 * Removes rows from the table.
	 * @param qual optional qualification
	 */
	protected void removeWithQualification(String qual)
	{		
		String query = "DELETE FROM " + getTableName();
		if (qual!=null) {
			query += " " + qual;
		}
		log(Log.DEBUG, query);
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getWritableDatabase();
			db.execSQL(query);
		}
		finally {
			close(null,db);
		}
	}
	
	protected int countWithQualification(String qual)
	{		
		String query = "SELECT count(*) FROM " + getTableName();
		if (qual!=null) {
			query += " " + qual;
		}
		log(Log.DEBUG, query);
		SQLiteDatabase db = null;
		Cursor c = null;
		try {
			db = dbHelper.getWritableDatabase();
			c = db.rawQuery(query,null);
			if (c.moveToFirst()) {
				int count = c.getInt(0);
				log(Log.DEBUG, "Found "+count+" entries");
				return count;
			}
		}
		finally {
			close(c,db);
		}
		return 0;
	}
	
	/**
	 * Create a new object instance from {@link Cursor} entry
	 * @param c the {@link Cursor} with table data.
	 * @return a new instance of the object filled with table data.
	 */
	abstract protected T buildObject(Cursor c);
	
	/**
	 * Put an object's data into the content for inserts.
	 * @param content a {@link ContentValues} object to be filled.
	 * @param data the object to be stored.
	 */
	abstract protected void fillContent(ContentValues content, T data);
	
}
