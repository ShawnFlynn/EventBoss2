package com.tssg.eventboss2;

import com.tssg.datastore.DatabaseHelper;
import com.tssg.eventboss2.utils.misc.MakeToast;
import com.tssg.eventsource.BELEvent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
//import android.support.v4.app.NavUtils;
//import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
//import android.view.LayoutInflater;
import android.view.MenuItem;

import java.util.Date;
import java.util.Locale;

import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.widget.ShareActionProvider;
import android.widget.Toast;


/**
 * An activity representing a single Event detail screen. This activity is only
 * used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link EventListDisplayActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link EventDetailFragment}.
 */

public class EventDetailActivity extends FragmentActivity {

	protected final String TAG = getClass().getSimpleName();

	public Context context = this;
	public static long mId;
	public static int mType;
	private DatabaseHelper mDbh;
	EventDetailFragment mDetailFragment = new EventDetailFragment();

	// Get the EB2 Interface
	EB2Interface EB2 = new EB2MainActivity();

	// Local DEBUG flag
	private final boolean DEBUG = EB2.DEBUG();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i(TAG, "onCreate()");

		setContentView(R.layout.activity_event_detail);
		Log.d(TAG, "Fragment = " + mDetailFragment);

		mDbh = new DatabaseHelper(this);
		mType = getIntent().getIntExtra(EventDetailFragment.LIST_TYPE, 0); // 0, 1, 2
		Log.d(TAG, "list type = " + mType);
		Log.d(TAG, "for current / saved list = " + mType);
		mDetailFragment.setListType(mType);		// the type (current, saved, search) List to use
		mDetailFragment.setEventId(getIntent().getStringExtra(EventDetailFragment.EVENTITEM_POS));

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// savedInstanceState is non-null when there is fragment state
		// saved from previous configurations of this activity
		// (e.g. when rotating the screen from portrait to landscape).
		// In this case, the fragment will automatically be re-added
		// to its container so we don't need to manually add it.
		// For more information, see the Fragments API guide at:
		//
		// http://developer.android.com/guide/components/fragments.html
		//

		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.
			Log.d(TAG, "(savedInstanceState)EventDetailFragment = "
						+ EventDetailFragment.EVENTITEM_POS);

			getSupportFragmentManager().beginTransaction()
										.add(R.id.event_detail_container,
											 mDetailFragment).commit();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		Log.i(TAG, "onStart()");

	}	//  end - onStart()

	@Override
	protected void onRestart() {
		super.onRestart();

		Log.i(TAG, "onRestart()");

	}	//  end - onRestart()

	@Override
	protected void onPause() {
		super.onPause();

		Log.i(TAG, "onPause()");

	}	//  end - onPause()

	@Override
	protected void onResume() {
		super.onResume();

		Log.i(TAG, "onResume()");

	}	//  end - onResume()

	@Override
	protected void onStop() {
		super.onStop();

		Log.i(TAG, "onStop()");

	}	//  end - onStop()

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		Log.i(TAG, "onCreateOptionsMenu()");

		// inflate menu items for the action bar (will be called every time the activity starts)
		MenuInflater inflater= getMenuInflater();
		if (mType == 0) {
			inflater.inflate(R.menu.menu_detail_activity_cur, menu);
			Log.d(TAG, "EventDetailActivity - save only");
		} else {
			inflater.inflate(R.menu.menu_detail_activity_sav, menu);
			Log.d(TAG, "EventDetailActivity - delete only");
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Log.i(TAG, "onOptionsItemSelected()");

		// Handle 
		String strEvent;

		switch (item.getItemId()) {
			case android.R.id.home:
				// This ID represents the Home or Up button. In the case of this
				// activity, the Up button is shown. Use NavUtils to allow users
				// to navigate up one level in the application structure. For
				// more details, see the Navigation pattern on Android Design:
				//
				// http://developer.android.com/design/patterns/navigation.html#up-vs-back
				//
				NavUtils.navigateUpTo(this,
								new Intent(this, EventDetailActivity.class));
				if (DEBUG)
					MakeToast.makeToast(this,
								"Up Nav - implemented", MakeToast.LEVEL_DEBUG);
				break;

			case R.id.idSaveSelected:
				Log.d(TAG, "Save Selected " + mDetailFragment.mId);
				/* can do this only if in CurrentSectionFragment */
				if(CurrentSectionFragment.mId == 0)  {
					if (DEBUG)
						Toast.makeText(context,
									" - Save only from Current Tab",
									Toast.LENGTH_LONG).show();
				} else {
					strEvent = String.format(Locale.getDefault(),
												"%d", CurrentSectionFragment.mId);
					Log.d(TAG, "strEvent: " + strEvent
								+ " from mId :" + CurrentSectionFragment.mId);
					mDbh.saveEvent(strEvent);
				}
				break;

			case R.id.idDeleteSelected:
				Log.d(TAG, "Delete Selected " + mDetailFragment.mId);
				/* can do this only if in SavedSectionFragment */
				if(SavedSectionFragment.mId == 0)  {
					if (DEBUG)
						Toast.makeText(context,
										" - Delete only from Saved Tab",
										Toast.LENGTH_LONG).show();
				} else {
					strEvent = String.format(Locale.getDefault(),
												"%d", SavedSectionFragment.mId); 
					Log.d(TAG, "strEvent: " + strEvent
								+" from mId :"+ SavedSectionFragment.mId);
					mDbh.deleteSavedEvent(strEvent);

					// Go back to the Saved list view
					NavUtils.navigateUpTo(this,
									new Intent(this, EventDetailActivity.class));
					if (DEBUG)
						MakeToast.makeToast(this,
											"Up Nav - go back to listview",
											MakeToast.LEVEL_DEBUG);
				}
				break;

			case R.id.action_calendar:
				Log.d(TAG, " Calendar " + mDetailFragment.mId);
				if (DEBUG)
					Toast.makeText(context,
									" Calendar",
									Toast.LENGTH_SHORT).show();
 
				BELEvent event = mDbh.getEventById(mDetailFragment.mId);
				Log.d(TAG, " Calendar: event " + event
							+ " -> CalendarAppointment.makeCalendarAppointment");
				Intent intent = CalendarAppointment.makeCalendarAppointment(event);
				startActivity(intent);
				break;

			case R.id.action_share:
				Log.d(TAG, " - idShare pressed");
				if (DEBUG)
					Toast.makeText(context,
									"Share",
									Toast.LENGTH_SHORT).show();
				Log.d(TAG, " item: " +  SavedSectionFragment.mId);
				ProcessShare(item);
				break;
		}
		return super.onOptionsItemSelected(item);

	}	//  end   onOptionsItemSelected()	


	// is a duplicate of the version in EB2MainActivity
	public void makeAppointment(String title, String location, Date start, Date end ) {

		Log.i(TAG, "makeAppointment()");

		Intent intent =  new Intent(Intent.ACTION_INSERT, Events.CONTENT_URI);

		intent.putExtra(Events.TITLE, title);
		intent.putExtra(Events.ALL_DAY, false);
		intent.putExtra(Events.EVENT_LOCATION, location);

		long startL, endL;
		if (null != start) {
			startL = start.getTime();
			intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startL);
		}
		if (null != end) {
			endL = end.getTime();
			intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endL);
		}

		Log.d(TAG, "makeAppointment " + intent);
		startActivity(intent);

	}	//  end  - makeAppointment()


	void ProcessShare(MenuItem item) {

		Log.i(TAG, "ProcessShare()");

		ShareActionProvider mShareActionProvider
							= (ShareActionProvider) item.getActionProvider();
/*
 * This class is a mediator for accomplishing a given task,
 *  for example sharing a file. It is responsible for creating a view that
 *  performs an action that accomplishes the task.
 * This class also implements other functions such a performing a default action. 

 * An ActionProvider can be optionally specified for a MenuItem and in such
 *  a case it will be responsible for creating the action view that appears
 *  in the android.app.ActionBar as a substitute for the menu item when the
 *  item is displayed as an action item.
 * Also the provider is responsible for performing a default action if a menu
 *  item placed on the overflow menu of the ActionBar is selected and none of
 *  the menu item callbacks has handled the selection.
 * For this case the provider can also optionally provide a sub-menu for
 *  accomplishing the task at hand. 

 * There are two ways for using an action provider for creating and
 *  handling of action views: 

 * Setting the action provider on a MenuItem directly by
 *  calling MenuItem.setActionProvider(ActionProvider). 
 * Declaring the action provider in the menu XML resource. For example:

	<item android:id="@+id/my_menu_item"
		android:title="Title"
		android:icon="@drawable/my_menu_item_icon"
		android:showAsAction="ifRoom"
		android:actionProviderClass="foo.bar.SomeActionProvider" />

	See Also:
	MenuItem.setActionProvider(ActionProvider)
	MenuItem.getActionProvider()

 */

		if (DEBUG)
			Toast.makeText(this, "doTheShare" + " ",
								Toast.LENGTH_SHORT).show();

		// collect data for sharing - this sends an MMS  ?????
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.putExtra(Intent.EXTRA_TEXT, "enters text to sent!");
		shareIntent.setType("text/plain");

		shareIntent.putExtra(Intent.EXTRA_EMAIL, "this is an extra string");
		String str[] = {"qwerty", "asdfgh"};
		shareIntent.putExtra(Intent.EXTRA_EMAIL, str);

		mShareActionProvider.setShareIntent(shareIntent);

		Log.d(TAG,"ProcessShare - shareIntent " + shareIntent);

		// send off shared data
		startActivity(Intent.createChooser(shareIntent, "Events List"));
		Log.d(TAG,"ProcessShare: after chooser " + shareIntent);

	}	//  end - ProcessShare()

}	//	end - EventDetailActivity class
