/**
 * 
 */
package fm.last.android.sync;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.LastFMApplication;
import fm.last.android.R;
import fm.last.android.player.RadioPlayerService;
import fm.last.api.Friends;
import fm.last.api.LastFmServer;
import fm.last.api.Tasteometer;
import fm.last.api.Track;
import fm.last.api.User;
import fm.last.util.UrlUtil;

import android.accounts.Account;
import android.accounts.OperationCanceledException;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContacts.Entity;

/**
 * @author sam
 * 
 */
public class ContactsSyncAdapterService extends Service {
	private static SyncAdapterImpl sSyncAdapter = null;
	private static ContentResolver mContentResolver = null;
	private static String UsernameColumn = ContactsContract.RawContacts.SYNC1;
	private static String PhotoUrlColumn = ContactsContract.RawContacts.SYNC2;
	private static String PhotoTimestampColumn = ContactsContract.RawContacts.SYNC3;
	private static String TasteTimestampColumn = ContactsContract.RawContacts.SYNC4;
	private static Integer syncSchema = 1;

	public ContactsSyncAdapterService() {
		super();
	}

	private static class SyncAdapterImpl extends AbstractThreadedSyncAdapter {
		private Context mContext;

		public SyncAdapterImpl(Context context) {
			super(context, true);
			mContext = context;
		}

		@Override
		public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
			try {
				ContactsSyncAdapterService.performSync(mContext, account, extras, authority, provider, syncResult);
			} catch (OperationCanceledException e) {
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		IBinder ret = null;
		ret = getSyncAdapter().getSyncAdapterBinder();
		return ret;
	}

	private SyncAdapterImpl getSyncAdapter() {
		if (sSyncAdapter == null)
			sSyncAdapter = new SyncAdapterImpl(this);
		return sSyncAdapter;
	}

	private static long addContact(Account account, String name, String username) {
		ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();

		ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI);
		builder.withValue(RawContacts.ACCOUNT_NAME, account.name);
		builder.withValue(RawContacts.ACCOUNT_TYPE, account.type);
		builder.withValue(UsernameColumn, username);
		operationList.add(builder.build());

		if(name.length() > 0 && PreferenceManager.getDefaultSharedPreferences(LastFMApplication.getInstance()).getBoolean("sync_names", true)) {
			builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, 0);
			builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
			builder.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name);
			operationList.add(builder.build());
		} else {
			builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(ContactsContract.CommonDataKinds.Nickname.RAW_CONTACT_ID, 0);
			builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE);
			builder.withValue(ContactsContract.CommonDataKinds.Nickname.NAME, username);
			operationList.add(builder.build());
		}

		builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
		builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
		builder.withValue(ContactsContract.Data.MIMETYPE, "vnd.android.cursor.item/vnd.fm.last.android.profile");
		builder.withValue(ContactsContract.Data.DATA1, username);
		builder.withValue(ContactsContract.Data.DATA2, "Last.fm Profile");
		builder.withValue(ContactsContract.Data.DATA3, "View profile");
		operationList.add(builder.build());

		if(!PreferenceManager.getDefaultSharedPreferences(LastFMApplication.getInstance()).getBoolean("sync_website", true)) {
			builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
			builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
			builder.withValue(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_PROFILE);
			builder.withValue(ContactsContract.CommonDataKinds.Website.URL, "http://www.last.fm/user/" + username);
			operationList.add(builder.build());
		}
		
		try {
			mContentResolver.applyBatch(ContactsContract.AUTHORITY, operationList);
			// Load the local Last.fm contacts
			Uri rawContactUri = RawContacts.CONTENT_URI.buildUpon().appendQueryParameter(RawContacts.ACCOUNT_NAME, account.name).appendQueryParameter(
					RawContacts.ACCOUNT_TYPE, account.type).build();
			Cursor c1 = mContentResolver.query(rawContactUri, new String[] { BaseColumns._ID, UsernameColumn }, UsernameColumn + " = '" + username + "'", null, null);
			if (c1.moveToNext()) {
				return c1.getLong(0);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	private static void updateContactStatus(ArrayList<ContentProviderOperation> operationList, long rawContactId, Track track) {
		Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
		Uri entityUri = Uri.withAppendedPath(rawContactUri, Entity.CONTENT_DIRECTORY);
		Cursor c = mContentResolver.query(entityUri, new String[] { Entity.DATA_ID }, Entity.MIMETYPE + " = 'vnd.android.cursor.item/vnd.fm.last.android.profile'", null, null);
		try {
			if (c.moveToNext()) {
				if (!c.isNull(0)) {
					String status = "";
					Boolean gotTrack = false;
					if (track.getNowPlaying() != null && track.getNowPlaying().equals("true")) {
						status = "Listening to ";
					} else {
						status = "Listened to ";
					}

					if(track.getName() != null && !track.getName().equals(RadioPlayerService.UNKNOWN)) {
						status += track.getName();
						gotTrack = true;
					}

					if(track.getArtist() != null && track.getArtist().getName() != null && !track.getArtist().getName().equals(RadioPlayerService.UNKNOWN)) {
						if(gotTrack)
							status += " by ";
						status += track.getArtist().getName();
					}
					
					ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(ContactsContract.StatusUpdates.CONTENT_URI);
					builder.withValue(ContactsContract.StatusUpdates.DATA_ID, c.getLong(0));
					builder.withValue(ContactsContract.StatusUpdates.STATUS, status);
					builder.withValue(ContactsContract.StatusUpdates.STATUS_RES_PACKAGE, "fm.last.android");
					builder.withValue(ContactsContract.StatusUpdates.STATUS_LABEL, R.string.app_name);
					builder.withValue(ContactsContract.StatusUpdates.STATUS_ICON, R.drawable.icon);
					if (track.getDate() != null) {
						long date = Long.parseLong(track.getDate()) * 1000;
						builder.withValue(ContactsContract.StatusUpdates.STATUS_TIMESTAMP, date);
					}
					operationList.add(builder.build());

					builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
					builder.withSelection(BaseColumns._ID + " = '" + c.getLong(0) + "'", null);
					builder.withValue(ContactsContract.Data.DATA3, status);
					operationList.add(builder.build());
				}
			}
		} finally {
			c.close();
		}
	}

	private static void updateContactName(ArrayList<ContentProviderOperation> operationList, long rawContactId, String name, String username) {
		ContentProviderOperation.Builder builder = ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI);
		builder.withSelection(ContactsContract.Data.RAW_CONTACT_ID + " = '" + rawContactId 
				+ "' AND (" + ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE + "'"
				+ " OR " + ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE + "')", null);
		operationList.add(builder.build());

		if(name.length() > 0 && PreferenceManager.getDefaultSharedPreferences(LastFMApplication.getInstance()).getBoolean("sync_names", true)) {
			builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValue(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, rawContactId);
			builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
			builder.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name);
			operationList.add(builder.build());
		} else {
			builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValue(ContactsContract.CommonDataKinds.Nickname.RAW_CONTACT_ID, rawContactId);
			builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE);
			builder.withValue(ContactsContract.CommonDataKinds.Nickname.NAME, username);
			operationList.add(builder.build());
		}
	}

	private static void updateContactPhoto(ArrayList<ContentProviderOperation> operationList, long rawContactId, String url) {
		byte[] image;
		ContentProviderOperation.Builder builder = ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI);
		builder.withSelection(ContactsContract.Data.RAW_CONTACT_ID + " = '" + rawContactId 
				+ "' AND " + ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'", null);
		operationList.add(builder.build());

		if(!PreferenceManager.getDefaultSharedPreferences(LastFMApplication.getInstance()).getBoolean("sync_icons", true)) {
			return;
		}
		
		try {
			if(url != null && url.length() > 0) {
				image = UrlUtil.doGetAndReturnBytes(new URL(url), 65535);
				if(image.length > 0) {
					builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
					builder.withValue(ContactsContract.CommonDataKinds.Photo.RAW_CONTACT_ID, rawContactId);
					builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
					builder.withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, image);
					operationList.add(builder.build());
	
					builder = ContentProviderOperation.newUpdate(ContactsContract.RawContacts.CONTENT_URI);
					builder.withSelection(ContactsContract.RawContacts.CONTACT_ID + " = '" + rawContactId + "'", null);
					builder.withValue(PhotoUrlColumn, url);
					builder.withValue(PhotoTimestampColumn, String.valueOf(System.currentTimeMillis()));
					operationList.add(builder.build());
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void updateTasteometer(ArrayList<ContentProviderOperation> operationList, long rawContactId, String username, Tasteometer taste) {
		ContentProviderOperation.Builder builder = ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI);
		builder.withSelection(ContactsContract.Data.RAW_CONTACT_ID + " = '" + rawContactId 
				+ "' AND " + ContactsContract.Data.MIMETYPE + " = 'vnd.android.cursor.item/vnd.fm.last.android.tasteometer'", null);
		operationList.add(builder.build());

		if(!PreferenceManager.getDefaultSharedPreferences(LastFMApplication.getInstance()).getBoolean("sync_taste", true)) {
			return;
		}
		
		String tastes[] = { "very low", "low", "medium", "high", "super" };
		String artists = "";
		Integer tasteIdx = (int)(taste.getScore() * 5);
		if(tasteIdx > 4)
			tasteIdx = 4;
		
		for(String artist : taste.getResults()) {
			if(artists.length() > 0)
				artists += ", ";
			artists += artist;
		}
		
		try {
			builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
			builder.withValue(ContactsContract.Data.MIMETYPE, "vnd.android.cursor.item/vnd.fm.last.android.tasteometer");
			builder.withValue(ContactsContract.Data.DATA1, username );
			builder.withValue(ContactsContract.Data.DATA2, "Musical Compatibility" );
			builder.withValue(ContactsContract.Data.DATA3, "Your musical compatibility is " + tastes[tasteIdx]);
			operationList.add(builder.build());

			builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
			builder.withValue(ContactsContract.Data.MIMETYPE, "vnd.android.cursor.item/vnd.fm.last.android.tasteometer");
			builder.withValue(ContactsContract.Data.DATA1, username );
			builder.withValue(ContactsContract.Data.DATA2, "Common Artists" );
			builder.withValue(ContactsContract.Data.DATA3, artists);
			operationList.add(builder.build());
			
			builder = ContentProviderOperation.newUpdate(ContactsContract.RawContacts.CONTENT_URI);
			builder.withSelection(ContactsContract.RawContacts.CONTACT_ID + " = '" + rawContactId + "'", null);
			builder.withValue(TasteTimestampColumn, String.valueOf(System.currentTimeMillis()));
			operationList.add(builder.build());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void deleteContact(Context context, long rawContactId) {
		Uri uri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId).buildUpon().appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();
		ContentProviderClient client = context.getContentResolver().acquireContentProviderClient(ContactsContract.AUTHORITY_URI);
		try {
			client.delete(uri, null, null);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.release();
	}

	private static class SyncEntry {
		public Long raw_id = 0L;
		public String photo_url = null;
		public Long photo_timestamp = null;
		public Long taste_timestamp = null;
	}
	
	private static void performSync(Context context, Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult)
			throws OperationCanceledException {
		HashMap<String, SyncEntry> localContacts = new HashMap<String, SyncEntry>();
		ArrayList<String> lastfmFriends = new ArrayList<String>();
		mContentResolver = context.getContentResolver();

		//If our app has requested a full sync, we're going to delete all our local contacts and start over
		boolean is_full_sync = PreferenceManager.getDefaultSharedPreferences(LastFMApplication.getInstance()).getBoolean("do_full_sync", false);
		
		//If our schema is out-of-date, do a fresh sync
		if(PreferenceManager.getDefaultSharedPreferences(LastFMApplication.getInstance()).getInt("sync_schema", 0) < syncSchema)
			is_full_sync = true;
		
		// Load the local Last.fm contacts
		Uri rawContactUri = RawContacts.CONTENT_URI.buildUpon().appendQueryParameter(RawContacts.ACCOUNT_NAME, account.name).appendQueryParameter(
				RawContacts.ACCOUNT_TYPE, account.type).build();
		Cursor c1 = mContentResolver.query(rawContactUri, new String[] { BaseColumns._ID, UsernameColumn, PhotoUrlColumn, PhotoTimestampColumn, TasteTimestampColumn }, null, null, null);
		while (c1.moveToNext()) {
			if(is_full_sync) {
				deleteContact(context, c1.getLong(0));
			} else {
				SyncEntry entry = new SyncEntry();
				entry.raw_id = c1.getLong(0);
				entry.photo_url = c1.getString(2);
				entry.photo_timestamp = c1.getLong(3);
				entry.taste_timestamp = c1.getLong(4);
				localContacts.put(c1.getString(1), entry);
			}
		}

		Editor editor = PreferenceManager.getDefaultSharedPreferences(LastFMApplication.getInstance()).edit();
		editor.remove("do_full_sync");
		editor.putInt("sync_schema", syncSchema);
		editor.commit();
		
		LastFmServer server = AndroidLastFmServerFactory.getServer();
		ArrayList<User> friends = null;
		try {
			friends = new ArrayList<User>();
			
			Friends f = server.getFriends(account.name, null, "1024");
			for (User user : f.getFriends()) {
				friends.add(user);
			}
			
			User self = server.getUserInfo(account.name, LastFMApplication.getInstance().session.getKey());
			friends.add(self);
			
			for (User user : friends) {
				if (!localContacts.containsKey(user.getName())) {
					long id = addContact(account, user.getRealName(), user.getName());
					if(id != -1) {
						SyncEntry entry = new SyncEntry();
						entry.raw_id = id;
						localContacts.put(user.getName(), entry);
					}
				}
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}

		ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
		for (User user : friends) {
			String username = user.getName();
			lastfmFriends.add(username);
			
			if (localContacts.containsKey(username)) {
				SyncEntry entry = localContacts.get(username);
				
				if (entry.photo_timestamp == null || System.currentTimeMillis() > (entry.photo_timestamp + 604800000L)) {
					String url = null;
					if(user.getImages().length > 0)
						url = user.getImages()[0].getUrl();
					if(entry.photo_url != url)
						updateContactPhoto(operationList, entry.raw_id, url);
				}
				try {
					Track[] tracks = null;
					tracks = server.getUserRecentTracks(username, "true", 1);
					if (tracks.length > 0) {
						updateContactStatus(operationList, entry.raw_id, tracks[0]);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					if (!account.name.equals(username) && (entry.taste_timestamp == null || System.currentTimeMillis() > (entry.taste_timestamp + 2628000000L))) {
						Tasteometer taste;
						taste = server.tasteometerCompare(account.name, username, 3);
						updateTasteometer(operationList, entry.raw_id, username, taste);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				updateContactName(operationList, entry.raw_id, user.getRealName(), username);
			}

			if(operationList.size() >= 50) {
				try {
					mContentResolver.applyBatch(ContactsContract.AUTHORITY, operationList);
				} catch (Exception e) {
					e.printStackTrace();
				}
				operationList.clear();
			}
		}

		if(operationList.size() > 0) {
			try {
				mContentResolver.applyBatch(ContactsContract.AUTHORITY, operationList);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		Iterator<String> i = localContacts.keySet().iterator();
		while(i.hasNext()) {
			String user = i.next();
			if(!lastfmFriends.contains(user))
				deleteContact(context, localContacts.get(user).raw_id);
		}
	}
}