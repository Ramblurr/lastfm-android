package fm.last.activities;

import fm.last.LastfmRadio;
import fm.last.Log;
import fm.last.R;
import fm.last.api.RadioTrack;
import fm.last.api.Station;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.util.DialogUtil;
import androidx.util.FinishLaterTask;
import androidx.util.GUITaskQueue;
import androidx.util.ImageLoader;
import androidx.util.ProgressIndicator;

public class SimilarArtistActivity extends Activity 
implements LastfmRadio.Listener
{
	private Button tuneButton = null;
	private EditText artistInputEdit = null;
	private LastfmRadio radio;
	private ImageLoader imageLoader;
	private EditText similarArtistEditText;
	private TextView artistText;
	private TextView trackTitleText;
	private ImageView albumArtImage;
	private ImageButton stopButton;
	private Dialog tunerDialog;
	private ProgressDialog tuningProgressDialog;

	private enum Requests {
		Login
	}

	private enum MenuItems {
		Account, Events
	}

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		// get our one-and-only radio instance
		radio = LastfmRadio.getInstance();
		// we listen to radio events
		radio.addListener(this);

		setContentView(R.layout.similar_artist);
		
		// now get the controls that we will use to display information
		// about the current track
		artistText = (TextView) findViewById(R.id.artist);
		trackTitleText = (TextView) findViewById(R.id.track_title);
		albumArtImage = (ImageView) findViewById(R.id.album_art);
		
		artistInputEdit = (EditText) findViewById(R.id.artist_input);
		similarArtistEditText = new EditText(this);
		tunerDialog = createTunerDialog(similarArtistEditText);
		
		tuneButton = (Button) findViewById(R.id.tune);
		tuneButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				tuneToSimilarArtist(artistInputEdit);
			}
		});
		
		stopButton = (ImageButton) findViewById(R.id.stop);

		imageLoader = ImageLoader.getInstance();

		LinearLayout l = (LinearLayout) findViewById(R.id.layout);
		LinearLayout bl = (LinearLayout) findViewById(R.id.buttonLayout);

		// l.setBackgroundDrawable( new GradientDrawable( Orientation.TOP_BOTTOM,
		// new int[]{ Color.BLACK, Color.rgb( 20, 20, 20)} ) );
		GradientDrawable gradient = new GradientDrawable(Orientation.TOP_BOTTOM,
				new int[] { Color.argb(150, 150, 150, 150),
						Color.argb(0, 150, 150, 150), Color.argb(0, 0, 0, 0) });
		bl.setBackgroundDrawable(gradient);
		animate();

		stopButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				stopButtonPressed();
			}
		});

		ImageButton skip = (ImageButton) findViewById(R.id.skip);
		skip.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				radio.playNext();
			}
		});
		
		radio.sendEventsForListenerCreation();
	}

	private Dialog createTunerDialog(final EditText similarArtistEditText) {
		// create the text field we will show in the dialog
		similarArtistEditText.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		similarArtistEditText.setHint("eg. Nirvana");
		similarArtistEditText.setSingleLine(true);
		
		return (new AlertDialog.Builder(this)
		.setTitle("Similar Artist Radio")
		.setView(similarArtistEditText)
		.setPositiveButton("Tune-in",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						tuneInSimilarArtists(similarArtistEditText.getText().toString());
					}
				})
		.setNegativeButton("Cancel", null)
		).create();		
	}
	
	private void tuneToSimilarArtist(EditText similarArtistEditText) {
		tuneInSimilarArtists(similarArtistEditText.getText().toString());
	}
	
	
	private void stopButtonPressed() {
		//tunerDialog.show();
		if (radio.isPlaying()) {
			radio.stopPlaying();
		} else {
			radio.playNext();
		}
	}

	final private void animate() {
		AnimationSet set = new AnimationSet(true);

		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(1800);
		set.addAnimation(animation);

		animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, -1.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f,
				Animation.RELATIVE_TO_SELF, 0.0f);
		animation.setDuration(500);
		set.addAnimation(animation);

		LayoutAnimationController controller = new LayoutAnimationController(set,
				0.5f);
		LinearLayout l = (LinearLayout) findViewById(R.id.layout);
		l.setLayoutAnimation(controller);
	}

		
	private void tuneInSimilarArtists(String artist) {
		if (artist.equals("")) {
			Log.e("Can't tune to empty artist!");
			return;
		}
		Log.i("Tuning-in to '" + artist + "'");
		LastfmRadio.getInstance().tuneToSimilarArtist(tuneToSimilarArtistProgress, artist);
	}
	
	private ProgressIndicator tuneToSimilarArtistProgress = new ProgressIndicator() {
		public void hideProgressIndicator() {
			hideTuneToSimilarArtistProgress();
		}

		public void showProgressIndicator() {
			showTuneToSimilarArtistProgress();
		}
	};
	
	private void showTuneToSimilarArtistProgress() {
		String title = getResources().getString(R.string.tuneToSimilarArtistTitle);
		String message = getResources().getString(R.string.tuneToSimilarArtistMessage);
		tuningProgressDialog = ProgressDialog.show(this, title, message, true);
	}
	
	private void hideTuneToSimilarArtistProgress() {
		tuningProgressDialog.dismiss();
	}

	public void onFailure(Throwable t) {
		DialogUtil.showAlertDialog(this, R.string.badAuthTitle, R.string.badAuth, R.drawable.icon, 2000);
		// call finish on this activity after the alert dialog is dismissed
		GUITaskQueue.getInstance().addTask(new FinishLaterTask(this, RESULT_CANCELED, 0));
	}
	
	protected void setHeaderResource(int resId) {
		Log.d("Radioview.setHeaderRes(" + resId + ")");
		LayoutInflater inflater = LayoutInflater.from(this);

		View radioPartial = inflater.inflate(resId, null);
		LinearLayout radioLayout = (LinearLayout) findViewById(R.id.layout);
		radioPartial.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));

		radioLayout.addView(radioPartial, 0);
	}


	public void onRadioStarted() {
		stopButton.setImageResource(R.drawable.stop);
		Log.i("onRadioStarted()");
	}

	public void onRadioStopped() {
		Log.i("onRadioStopped()");
		stopButton.setImageResource(R.drawable.play);
	}

	/**
	 * This method gets called when the radio has tuned into a new station, or
	 * this activity is created and the radio is already tuned to a station.
	 */
	public void onStationChanged(Station station) {
		Log.i("onStationChanged - " + station.getName());
		TextView v = (TextView) findViewById(R.id.station_name);
		v.setText(station.getName());	
		if (!radio.isPlaying()) {
			radio.playNext();
		}
	}

	public void onTrackFinished(RadioTrack track) {
		Log.i("Finished track '" + track.getTitle() + "'");
	}

	public void onTrackFetched(RadioTrack track, boolean started) {
		Log.i("fetched track '" + track.getTitle() + "' started=" + started);
		trackTitleText.setText(track.getTitle());
	}

	public void onFetchingTrack(RadioTrack track) {
		Log.i("Fetching '" + track.getTitle() + "'");
		trackTitleText.setText("Fetching '" + track.getTitle() + "'");
		artistText.setText(track.getCreator());
		imageLoader.loadImage(track.getImageUrl(), albumArtImage);
	}

	/**
	 * System menu stuff
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MenuItems.Account.ordinal(), 0, "Account");
		menu.add(0, MenuItems.Events.ordinal(), 1, "Events");
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == MenuItems.Account.ordinal()) {
			startActivity(new Intent("ACCOUNTSETTINGS"));
		} else if (id == MenuItems.Events.ordinal()) {
			startActivity(new Intent("EVENTSVIEW"));
		} else {
			return false;
		}
		return true;
	}

	public void onRadioTechnicalProblem() {
		Log.i("onRadioTechnicalProblem()");
	}
	
}
