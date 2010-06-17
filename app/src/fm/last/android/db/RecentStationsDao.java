package fm.last.android.db;

import java.util.Collections;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import fm.last.api.Station;

public class RecentStationsDao extends AbstractDao<Station>
{
	
	/**
	 * The table for the recent stations list.
	 */
	public static final String DB_TABLE_RECENTSTATIONS = "t_recentstations";
	
	@Override
	protected String getTableName() 
	{	
		return DB_TABLE_RECENTSTATIONS;
	}
	
	
	private static RecentStationsDao instance = null;

	public static RecentStationsDao getInstance() {
		if(instance != null) {
			return instance;
		} else {
			return new RecentStationsDao();
		}
	}

	/**
	 * Append a new station to the list
	 * @param url the station's Last.fm URL
	 * @param name the station's display name
	 */
	public void appendRecentStation(String url, String name) 
	{
		save(Collections.singletonList(new Station(name, null, url, null)));
	}
	
	/**
	 * Read the last added station.
	 * @return the last station that has been added to the list.
	 */
	public Station getLastStation() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Station result = null;
		Cursor c = null;
		try {
			c = db.rawQuery("SELECT * FROM " + DB_TABLE_RECENTSTATIONS + " ORDER BY Timestamp DESC LIMIT 4", null);
			if (c.getCount() > 0) {
				c.moveToFirst();
				result = buildObject(c);
			}
		}
		finally {
			c.close();
			db.close();
		}
		return result;
	}
	
	/**
	 * Get the list of recent stations.
	 * @return all stations in the table.
	 */
	public List<Station> getRecentStations() 
	{
		return loadWithQualification("ORDER BY Timestamp DESC LIMIT 10");
	}
	
	/*
	 * (non-Javadoc)
	 * @see fm.last.android.db.AbstractDao#buildObject(android.database.Cursor)
	 */
	@Override
	protected Station buildObject(Cursor c) 
	{
		int urlColumn = c.getColumnIndex("Url");
		int nameColumn = c.getColumnIndex("Name");
		String name = c.getString(nameColumn);
		String url = c.getString(urlColumn);
		return new Station(name, "", url, "");
	}
	
	/*
	 * (non-Javadoc)
	 * @see fm.last.android.db.AbstractDao#fillContent(android.content.ContentValues, java.lang.Object)
	 */
	@Override
	protected void fillContent(ContentValues content, Station data) 
	{
		content.put("Url", data.getUrl());
		content.put("Name", data.getName());
		content.put("Timestamp", System.currentTimeMillis());
	}
		
}
