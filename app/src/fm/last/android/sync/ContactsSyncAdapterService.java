/**
 * 
 */
package fm.last.android.sync;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import fm.last.android.AndroidLastFmServerFactory;
import fm.last.android.R;
import fm.last.api.Friends;
import fm.last.api.LastFmServer;
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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContacts.Entity;
import android.util.Log;

/**
 * @author sam
 * 
 */
public class ContactsSyncAdapterService extends Service {
	private static final String TAG = "ContactsSyncAdapterService";
	private static SyncAdapterImpl sSyncAdapter = null;
	private static ContentResolver mContentResolver = null;

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
		builder.withValue(RawContacts.SYNC1, username);
		operationList.add(builder.build());

		if(name.length() > 0) {
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

		try {
			mContentResolver.applyBatch(ContactsContract.AUTHORITY, operationList);
			// Load the local Last.fm contacts
			Uri rawContactUri = RawContacts.CONTENT_URI.buildUpon().appendQueryParameter(RawContacts.ACCOUNT_NAME, account.name).appendQueryParameter(
					RawContacts.ACCOUNT_TYPE, account.type).build();
			Cursor c1 = mContentResolver.query(rawContactUri, new String[] { BaseColumns._ID, RawContacts.SYNC1 }, RawContacts.SYNC1 + " = '" + username + "'", null, null);
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
		Cursor c = mContentResolver.query(entityUri, new String[] { RawContacts.SOURCE_ID, Entity.DATA_ID, Entity.MIMETYPE, Entity.DATA1 }, null, null, null);
		try {
			while (c.moveToNext()) {
				if (!c.isNull(1)) {
					String mimeType = c.getString(2);
					String status = "";
					if (track.getNowPlaying() != null && track.getNowPlaying().equals("true"))
						status = "Listening to " + track.getName() + " by " + track.getArtist().getName();
					else
						status = "Listened to " + track.getName() + " by " + track.getArtist().getName();

					if (mimeType.equals("vnd.android.cursor.item/vnd.fm.last.android.profile")) {
						ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(ContactsContract.StatusUpdates.CONTENT_URI);
						builder.withValue(ContactsContract.StatusUpdates.DATA_ID, c.getLong(1));
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
						builder.withSelection(BaseColumns._ID + " = '" + c.getLong(1) + "'", null);
						builder.withValue(ContactsContract.Data.DATA3, status);
						operationList.add(builder.build());
					}
				}
			}
		} finally {
			c.close();
		}
	}

	private static void updateContactPhoto(ArrayList<ContentProviderOperation> operationList, long rawContactId, String url) {
		byte[] image;
		try {
			image = UrlUtil.doGetAndReturnBytes(new URL(url), 65535);
			if(image.length > 0) {
				ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
				builder.withValue(ContactsContract.CommonDataKinds.Photo.RAW_CONTACT_ID, rawContactId);
				builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
				builder.withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, image);
				operationList.add(builder.build());

				builder = ContentProviderOperation.newUpdate(ContactsContract.RawContacts.CONTENT_URI);
				builder.withSelection(ContactsContract.RawContacts.CONTACT_ID + " = '" + rawContactId + "'", null);
				builder.withValue(ContactsContract.RawContacts.SYNC2, url);
				operationList.add(builder.build());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void performSync(Context context, Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult)
			throws OperationCanceledException {
		HashMap<String, Long> localContacts = new HashMap<String, Long>();
		HashMap<String, String> localAvatars = new HashMap<String, String>();
		mContentResolver = context.getContentResolver();

		// Load the local Last.fm contacts
		Uri rawContactUri = RawContacts.CONTENT_URI.buildUpon().appendQueryParameter(RawContacts.ACCOUNT_NAME, account.name).appendQueryParameter(
				RawContacts.ACCOUNT_TYPE, account.type).build();
		Cursor c1 = mContentResolver.query(rawContactUri, new String[] { BaseColumns._ID, RawContacts.SYNC1, RawContacts.SYNC2 }, null, null, null);
		while (c1.moveToNext()) {
			localContacts.put(c1.getString(1), c1.getLong(0));
			if(c1.getString(2) != null)
				localAvatars.put(c1.getString(1), c1.getString(2));
		}

		LastFmServer server = AndroidLastFmServerFactory.getServer();
		try {
			Friends friends = server.getFriends(account.name, null, "1024");
			for (User user : friends.getFriends()) {
				if (!localContacts.containsKey(user.getName())) {
					long id = addContact(account, user.getRealName(), user.getName());
					if(id != -1)
						localContacts.put(user.getName(), id);
				}
			}

			ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
			for (User user : friends.getFriends()) {
				if (localContacts.containsKey(user.getName())) {
					if (!localAvatars.containsKey(user.getName()) && user.getImages().length > 0) {
						updateContactPhoto(operationList, localContacts.get(user.getName()), user.getImages()[0].getUrl());
					}
					Track[] tracks = server.getUserRecentTracks(user.getName(), "true", 1);
					if (tracks.length > 0) {
						updateContactStatus(operationList, localContacts.get(user.getName()), tracks[0]);
					}
				}
			}
			if(operationList.size() > 0)
				mContentResolver.applyBatch(ContactsContract.AUTHORITY, operationList);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}