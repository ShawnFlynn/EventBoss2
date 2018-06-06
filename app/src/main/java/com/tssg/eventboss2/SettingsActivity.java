package com.tssg.eventboss2;

import com.tssg.eventboss2.utils.misc.MakeToast;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;


public class SettingsActivity extends Activity {

	private final String TAG = getClass().getSimpleName();

//	/** How many choices in location button group */
//	public static final int RBG_CHOICES = 5;

	View mSettingsView = null;

	// Get the EB2 Interface
	EB2Interface EB2 = new EB2MainActivity();

	// Local DEBUG flag
	private final boolean DEBUG = EB2.DEBUG();
	//private final boolean DEBUG = true;

	private boolean bNewFeed  = true;
	private int selectedFeedId = EB2.getFeedId();
	private int currentFeedId  = EB2.getFeedId();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i(TAG, "onCreate()");

		/** sets outer class members according to which button clicked */
		class prefclick implements OnClickListener {
			private int clickedFeedId = currentFeedId;

			public prefclick(int feedId) {
				clickedFeedId = feedId;
			}

			public void onClick(View v) {

				// Check if it is not the current feed
				if (clickedFeedId != currentFeedId) {
					Log.i(TAG, "New feed selected: " + clickedFeedId);
					bNewFeed = true;
				} else {
					// Check if the current feed is from the DB
					if (EB2.EventsListCacheIsEmpty(currentFeedId)) {
						Log.i(TAG, "New feed selected: " + clickedFeedId);
						bNewFeed = true;
					} else {
						// Same as the current feed
						bNewFeed = false;
					}
				}

				// Set the selected feed ID
				if (bNewFeed)
					selectedFeedId = clickedFeedId;
				else
					selectedFeedId = currentFeedId;
			}
		}

		// Set the ContentView
		setContentView(R.layout.settings_screen);

		// Set up the RSS feed RadioGroup
		final RadioGroup rgFeed = (RadioGroup) findViewById(R.id.feedSelection);

		final RadioButton choice1 = (RadioButton) findViewById(R.id.choice1);
		choice1.setOnClickListener(new prefclick(0) );

		final RadioButton choice2 = (RadioButton) findViewById(R.id.choice2);
		choice2.setOnClickListener(new prefclick(1) );

		final RadioButton choice3 = (RadioButton) findViewById(R.id.choice3);
		choice3.setOnClickListener(new prefclick(2) );

		// Preselect the current feed Id
		switch(currentFeedId)
		{
		case (0):
			rgFeed.check(R.id.choice1);
			break;
		case (1):
			rgFeed.check(R.id.choice2);
			break;
		case (2):
			rgFeed.check(R.id.choice3);
			break;
		default:
			rgFeed.check(R.id.choice1);
			break;
		}

		// Log the preselected item
		if (DEBUG)
			Log.d(TAG, "button " + currentFeedId + " preselected");

		rgFeed.setOnCheckedChangeListener ( new OnCheckedChangeListener() {
			public void onCheckedChanged( RadioGroup rg, int checkedId )
			{
				if (DEBUG) {
					switch( checkedId )
					{
					case R.id.choice1:
						MakeToast.makeToast(getApplicationContext(),
								"set 1 id = " + Integer.toHexString(checkedId),
								MakeToast.LEVEL_USER);
						break;
					case R.id.choice2:
						MakeToast.makeToast(getApplicationContext(),
								"set 2 id = " + Integer.toHexString(checkedId),
								MakeToast.LEVEL_USER);
						break;
					case R.id.choice3:
						MakeToast.makeToast(getApplicationContext(),
								"set 3 id = " + Integer.toHexString(checkedId),
								MakeToast.LEVEL_USER);
						break;
					default:
						MakeToast.makeToast(getApplicationContext(),
								"RG "+rg+"; id " + checkedId,
								MakeToast.LEVEL_USER);
						break;
					}
				}
			}
		});

		final Button doneBtn = (Button) findViewById(R.id.idDone);
		doneBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Log.i(TAG, "on Click() - done");
				if (DEBUG) {
					MakeToast.makeToast(getApplicationContext(),
										"pressed 'Done' -- bye bye",
										MakeToast.LEVEL_USER);
				}

				// Don't reread the current Feed Id
				if (bNewFeed) {
					Log.i(TAG, "Reading feed: " + selectedFeedId);
					if (DEBUG) {
						// EventsListReader(url) will do the reading using the AsyncTask
						MakeToast.makeToast(getApplicationContext(),
											"Read feed # " + selectedFeedId,
											MakeToast.LEVEL_USER);
					}
					// Set the global feed IDs
					EB2.setFeedId(selectedFeedId);

					// Load the selected feed
					loadfeed(selectedFeedId);
				}

				// Return
				finish();
			}
		});

	}	// end OnCreate ()


	public void loadfeed(int whichfeed) {

		Log.i(TAG, "loadFeed( " +EB2.getFeedName()+ " )");

		// Generate and start a new EB2MainActivity
		Intent intent = new Intent(this, EB2MainActivity.class);
		intent.putExtra("feedId", whichfeed);
		// TODO
		// Use these flags to "NOT" Backstack the EB2 activities
		//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);

	}


	/**
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 *
	 * Called first time user clicks on the menu button
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		Log.i(TAG, "onCreateOptionsMenu()");

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main_activity, menu);

		// Remove the search button
		MenuItem search_item = menu.findItem(R.id.action_search);
		if (search_item != null) {
			search_item.setVisible(false);
		}

		return true;
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Log.i(TAG, "onOptionsItemSelected()");

		final int itemId = item.getItemId();

		switch (itemId) {
		case R.id.idDone:
			Log.i(TAG, "done button");
			if (DEBUG) {
				MakeToast.makeToast(this,
									"pressed 'Done'",
									MakeToast.LEVEL_USER);
			}
			finish();
			break;
		default:
			Log.e(TAG, "unknown button " + Integer.toHexString(itemId));
			if (DEBUG) {
				MakeToast.makeToast(this,
									"pressed " + Integer.toHexString(itemId),
									MakeToast.LEVEL_USER);
			}
			break;
		}

		return true;
	}

}	// end - SettingsActivity class
