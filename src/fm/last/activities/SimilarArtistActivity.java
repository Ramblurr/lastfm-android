package fm.last.activities;

import fm.last.ImageLoader;
import fm.last.LastFmApplication;
import fm.last.Log;
import fm.last.R;
import fm.last.TrackInfo;
import fm.last.api.Artist;
import fm.last.api.Station;
import fm.last.radio.Radio;
import fm.last.radio.RadioEventHandler;
import fm.last.tasks.TuneRadioTask;
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
import androidx.util.ResultReceiver;

public class SimilarArtistActivity extends Activity implements ResultReceiver<Station> {
	private Button m_tuneButton = null;
	private EditText m_artistInput = null;
	private Radio m_radio = null;
	private ImageLoader m_imageLoader;
	private EditText similarArtistEditText;
	private Dialog tunerDialog;
	private ProgressDialog progressDialog;

	private enum Requests {
		Login
	}

	private enum MenuItems {
		Account, Events
	}

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.similar_artist);

		tunerDialog = createTunerDialog();
		
//		setHeaderResource(R.layout.similar_artist_radio_partial);
		m_artistInput = (EditText) findViewById(R.id.artist_input);
		m_tuneButton = (Button) findViewById(R.id.tune);
		m_tuneButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				doTune();
			}
		});

		String user = LastFmApplication.instance().userName();
		String pass = LastFmApplication.instance().password();

		m_imageLoader = new ImageLoader(this);

		if (user.length() == 0 || pass.length() == 0) {
			// show username / password activity
			startActivityForResult(new Intent("ACCOUNTSETTINGS"), Requests.Login
					.ordinal());
			return;
		} else {
				init();
		}
	}

	private void doTune() {
		tuneInSimilarArtists(m_artistInput.getText().toString());
	}
	
	RadioEventHandler m_radioEventHandler = new RadioEventHandler() {
		public void onTrackEnded(TrackInfo track) {
		}

		public void onTrackStarted(TrackInfo track) {
			setupUi(track);
		}
	};

	private void setupUi(TrackInfo t) {
		TextView tv;
		tv = (TextView) findViewById(R.id.artist);
		tv.setText(t.artist());
		tv = (TextView) findViewById(R.id.track_title);
		tv.setText(t.title());

		ImageView v = (ImageView) findViewById(R.id.album_art);
		try {
			m_imageLoader.loadImage(v, t.imageUrl());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	final private void init() {
		m_radio = new Radio();
		m_radio.addRadioHandler(m_radioEventHandler);

		setContentView(R.layout.radio_client);

		LinearLayout l = (LinearLayout) findViewById(R.id.layout);
		LinearLayout bl = (LinearLayout) findViewById(R.id.buttonLayout);

		// l.setBackgroundDrawable( new GradientDrawable( Orientation.TOP_BOTTOM,
		// new int[]{ Color.BLACK, Color.rgb( 20, 20, 20)} ) );
		GradientDrawable gradient = new GradientDrawable(Orientation.TOP_BOTTOM,
				new int[] { Color.argb(150, 150, 150, 150),
						Color.argb(0, 150, 150, 150), Color.argb(0, 0, 0, 0) });
		bl.setBackgroundDrawable(gradient);
		animate();

		ImageButton stopButton = (ImageButton) findViewById(R.id.stop);

		stopButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				stopButtonPressed();
			}
		});

		ImageButton skip = (ImageButton) findViewById(R.id.skip);
		skip.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				m_radio.skip();
			}
		});
	}

	
	private Dialog createTunerDialog() {
		// create the text field we will show in the dialog
		similarArtistEditText = new EditText(this);
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
						tuneToSimilarArtist();
					}
				})
		.setNegativeButton("Cancel", null)
		).create();		
	}
	
	private void stopButtonPressed() {
		tunerDialog.show();
	}
	
	private void tuneToSimilarArtist() {
		tuneInSimilarArtists(similarArtistEditText.getText().toString());
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
		Log.i("Tuning-in to '" + artist + "'");

		String title = getResources().getString(R.string.authProgressTitle);
		String message = getResources().getString(R.string.authProgressMessage);
		progressDialog = ProgressDialog.show(this, title, message, true);
		GUITaskQueue.getInstance().addTask(new TuneRadioTask(artist, this));
		
/*
		String stationName = m_radio.tuneToSimilarArtist( artist );

		TextView v = (TextView) findViewById(R.id.station_name);
v.setText(artist + ".similar");		
		// v.setText( stationName );

		// m_radio.play();
		 * 
		 */
	}

	public void handle_exception(Throwable t) {
		progressDialog.dismiss();
		DialogUtil.showAlertDialog(this, R.string.badAuthTitle, R.string.badAuth, R.drawable.icon, 2000);
		// call finish on this activity after the alert dialog is dismissed
		GUITaskQueue.getInstance().addTask(new FinishLaterTask(this, RESULT_CANCELED, 0));
	}

	public void resultObtained(Station result) {
		progressDialog.dismiss();
		TextView v = (TextView) findViewById(R.id.station_name);
		v.setText(result.getName());		
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


}
