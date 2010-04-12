/**
 * 
 */
package fm.last.android.widget;

import java.net.URL;

import fm.last.android.R;
import fm.last.android.activity.Profile;
import fm.last.android.utils.UserTask;
import fm.last.api.User;
import fm.last.util.UrlUtil;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.QuickContactBadge;
import android.widget.TextView;

/**
 * @author sam
 *
 */
public class QuickContactProfileBubble extends ProfileBubble {
	QuickContactBadge mBadge = null;
	/**
	 * @param context
	 */
	public QuickContactProfileBubble(Context context) {
		super(context);
		if(Profile.isHTCContactsInstalled(context)) {
			throw new java.lang.VerifyError(); //Fall back to the non-quickcontact version if HTC SenseUI is running
		}
		init();
	}

	private void init() {
		removeAllViews();
		
		LayoutInflater.from(getContext()).inflate(R.layout.profile_bubble_quickcontact, this);
		// we did do this but it looks wrong due to lack of
		// this.setBackgroundResource(R.drawable.profile_bubble_bg);

		mFirst = (TextView) findViewById(R.id.profile_username);
		mSecond = (TextView) findViewById(R.id.profile_meta);
		mSecond.setText(getContext().getText(R.string.profile_loading));
		mAvatar = null;
		mBadge = (QuickContactBadge)findViewById(R.id.profile_avatar);
	}

	private void setQuickContactId(long id) {
		mBadge.setExcludeMimes(new String[] {"vnd.android.cursor.item/vnd.fm.last.android.profile"});
		mBadge.assignContactUri(ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id));
		Bitmap avatar = BitmapFactory.decodeStream(ContactsContract.Contacts.openContactPhotoInputStream(getContext().getContentResolver(), ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id)));
		if(avatar != null) {
			mBadge.setImageBitmap(avatar);
		} else if(mUser.getImages().length > 0) {
			new FetchArtTask(mUser.getImages()[0].getUrl()).execute((Void) null);
		}
	}
	
	@Override
	public void setUser(User user) {
		super.setUser(user);

		Cursor c = getContext().getContentResolver().query(ContactsContract.Data.CONTENT_URI, new String[] { ContactsContract.Data.CONTACT_ID },
				ContactsContract.Data.DATA1 + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "='vnd.android.cursor.item/vnd.fm.last.android.profile'",
				new String[] { user.getName() }, null);

		if (c.moveToNext()) {
			setQuickContactId(c.getLong(0));
		}
	}
	
	private class FetchArtTask extends UserTask<Void, Void, Boolean> {
		Bitmap mBitmap = null;
		String mURL = null;

		public FetchArtTask(String url) {
			super();

			mURL = url;
			Log.i("Last.fm", "Fetching art: " + url);
		}

		@Override
		public Boolean doInBackground(Void... params) {
			boolean success = false;
			try {
				mBitmap = UrlUtil.getImage(new URL(mURL));
				success = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return success;
		}

		@Override
		public void onPostExecute(Boolean result) {
			if (result && !isCancelled()) {
				mBadge.setImageBitmap(mBitmap);
			}
		}
	}
}
