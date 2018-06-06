
package com.tssg.eventboss2;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.tssg.datastore.DatabaseHelper;
import com.tssg.eventboss2.utils.misc.MakeToast;
import com.tssg.eventsource.BELEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;



/*
 *    MainActivity  implements
 *      ActionBar, with 3 Tabs: CurrentList, SavedList & Search
 *      manages the fragments for the above mentioned entities and
 *      the other ActionBar items (save, delete, calendar, share).
 *      On start up it reads the RSS feed and stores events into a list.
 *      Handles a Settings activity (for any user selections we implement)
 */

// TODO: replace ActionBar.Tab functionality with Material Design
// Suppression is for the deprecated ActionBar.Tab functionality
@SuppressWarnings("deprecation")
public class EB2MainActivity extends FragmentActivity
							 implements ActionBar.TabListener,
										EventFragmentCoordinator,
										EB2Interface {

	static final String TAG = "EB2MainActivity";

	// Activity context
	private static Context context;

	// Get current context
	public Context getContext() {
		return context;
	}

	// Debug mode - controls "toast" messages
	private static final boolean DEBUG = false;
//	private static final boolean DEBUG = true;

	// Get the DEBUG flag
	public boolean DEBUG() {
		return DEBUG;
	}

	// en/disable tracing to device SD
	private static final boolean bTRACE = false;

	// Controls strictMode - set to false for release
	private static final boolean bDEVELOPER_MODE = false;

	// Action bar
	private static ActionBar mActionBar;

	// Get Action Bar
	public ActionBar getEB2ActionBar() {
		return mActionBar;
	}

	// Database helper
	private static DatabaseHelper mDbh;

	// Database name
	private final String DATABASE_NAME = "EventStore";

	// Database file path
	private static String DbFilePath = null;

	// Get the DB name
	// internal or on the SD card - if mounted
	public String getDBName() {
		return DbFilePath;
	}

	// Resources
	private static Resources mResources = null;

	// Get Resources
	public Resources getEB2Resources() {
		return mResources;
	}

	// Selected feed strings
	private static volatile String mFeedName = null;	// Current feed name string
	private static volatile String mFeedURL  = null;	// Current feed URL string

	// Feed string arrays
	private static volatile String[] mFeedNameArray = null;	// Feed name string array
	private static volatile String[] mFeedURLArray  = null;	// Feed URL string array

	private void SetupFeedDataArrays() {

		Log.i(TAG, "SetupFeedDataArrays()");

		// Setup resources
		if (mResources == null) {
			mResources = getResources();
			Log.d(TAG, "\tmResources = " + mResources);
		}

		// Get the feed name and URL arrays
		if (mFeedNameArray == null) {
			mFeedNameArray = mResources.getStringArray(R.array.eventslists);
			Log.d(TAG, "\tmFeedNameArray = " + mFeedNameArray);
		}
		if (mFeedURLArray == null) {
			mFeedURLArray = mResources.getStringArray(R.array.rss_src_names);
			Log.d(TAG, "\tmFeedURLArray = " + mFeedURLArray);
		}
	}

	// Feed array size
	// TODO use R.array.rss_src_names.length so hardcoding is not necessary
	private static final int mFeedListSize = 3;

	// Get Feed Name
	public String getFeedName() {
		return mFeedName;
	}

	// String to hold Current/Stored string for tab 0 label
	private static volatile String tab0Label;

	// Get tab 0 label
	public String getTab0Label() {
		return tab0Label;
	}

	// Set tab 0 label
	public void setTab0Label(String tabString) {
		tab0Label = tabString;
	}

	// Current feed ID
	private static volatile int mFeedId = 0; // start with Boston

	// Get the current feed ID
	public int getFeedId() {
		return mFeedId;
	}

	// Set the current feed ID
	public void setFeedId(int feedId) {

		Log.i(TAG, "setFeedId(" +feedId+ ")");

		// Check validity
		if (feedId >= mFeedListSize)
			return;

		// Set the specified feed ID
		mFeedId = feedId;

		// Check for valid arrays
		if (mFeedNameArray == null ||
			mFeedURLArray == null)
			SetupFeedDataArrays();

		// Update the selected feed Name and URL
		mFeedName = mFeedNameArray[feedId];
		mFeedURL  = mFeedURLArray[feedId];
	}

	// Temporary feed ID
	private static int oldFeedId = mFeedId; // backup copy

	// Get the old feed ID
	public int getOldFeedId() {
		return oldFeedId;
	}

	// Set the old feed ID
	public void setOldFeedId(int feedId) {

		// Check validity
		if (feedId >= mFeedListSize)
			return;

		oldFeedId = feedId;
	}

	// Current date and time
	private static Date currentDate = new Date(System.currentTimeMillis());

	// Get Current Date
	public Date getCurrentDate() {
		return currentDate;
	}

	public void setCurrentDate(Date curDate) {
		currentDate = curDate;
	}

	// RSS data as saved text; use for debugging
	private static String m_mainEventText;

	// Get Main Event Text
	public String getMainEventText() {
	return m_mainEventText;
}

	// RSS data from feed
	private static volatile List<BELEvent> currentEventsList = new ArrayList<BELEvent>();

	// Get Web Events List
	public List<BELEvent> getCurrentEventsList() {
		return currentEventsList;
	}

	// Set Web Events List
	public void setCurrentEventsList(List<BELEvent> eventsList) {
		currentEventsList = eventsList;
	}

	// The use of this is to save the lists in an array indexed by feedId for rapid reload.
	private static CopyOnWriteArrayList<List<BELEvent>> eventsListCache =
													new CopyOnWriteArrayList<List<BELEvent>>();

	// Cache entry expiration time = 4 hours
	public static long timeLimit =  240 * 60000;

	// Size the eventsListCache and eventsListTime arrays
	static {
		// Set to number of feeds
		eventsListCache.addAll(Collections.nCopies(mFeedListSize, Collections.<BELEvent>emptyList()));

		// Set the appropriate cache expiration time
		if (DEBUG)
				timeLimit = 5 * 60000;	// Set time to 5 minutes for debug mode
	}

	// Getter for the cache expiriatation time
	public long getExpireTime() {
		return timeLimit;
	}

	// This is the date/time stamp for the eventsListCache entries
	private static CopyOnWriteArrayList<Date> eventsListTime =
										new CopyOnWriteArrayList<Date>(new Date[mFeedListSize]);

	// Events List Cache size
	private static int eventsListCacheSize = eventsListCache.size();

	// Get Events List Cache size
	public int getEventsListCacheSize() {
		return eventsListCacheSize;
	}

	public Date getEventsListTime(int feedId) {
		if (feedId >= eventsListCache.size())
			return (new Date(0));
		return eventsListTime.get(feedId);
	}

	// Events List cache is empty
	public boolean EventsListCacheIsEmpty(int feedId) {
		if (feedId >= eventsListCache.size())
			return true;
		return eventsListCache.get(feedId).isEmpty();
	}

	// Get Events List cache entry
	public List<BELEvent> getEventsListCacheEntry(int feedId) {
		if (feedId >= eventsListCache.size())
			return null;
		return eventsListCache.get(feedId);
	}

	// Set Events List cache entry
	public void setEventsListCacheEntry(int feedId, List<BELEvent> eventsList) {
		if (feedId >= eventsListCache.size())
			return;
		eventsListCache.set(feedId, eventsList);
		// Get and save the current date & time
		setCurrentDate(new Date(System.currentTimeMillis()));
		eventsListTime.set(feedId, getCurrentDate());
	}

	// Clear the Events List cache entry
	public void clearEventsListCacheEntry(int feedId) {
		if (feedId >= eventsListCache.size())
			return;
		eventsListCache.set(feedId, Collections.<BELEvent>emptyList());
	}

	// Current Section fragment data
	private static CurrentSectionFragment currentData = null;

	// Get Current Section fragment data
	public CurrentSectionFragment getCurrentData() {
		return currentData;
	}

	private static SavedSectionFragment savedData = null;
	private static SearchSectionFragment searchData = null;

	public enum event_list {
		Current, Saved
	};

	// Temporary event list
	private static event_list last_list = event_list.Current;

	// Get last events list
	public event_list getLastList() {
		return last_list;
	}

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the three primary sections of the app. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	AppSectionsPagerAdapter mAppSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will display the three primary sections of the
	 * app, one at a time.
	 */
	ViewPager mViewPager;

	// Dual Pane flag
	private static boolean mDualPane;

	// Current tab selected index
	private volatile int mTabSelected = -1;

	// Reading from internal file flag
	private static final boolean readingFromInternalFile = false;

	// Get reading from file flag
	public boolean ifReadingFromInternalFile() {
		return readingFromInternalFile;
	}

	// Internal file path
	private static String internalFilePath = null;

	// Get internal file path
	public String getInternalFilePath() {
		return internalFilePath;
	}

	// Note: The file specified below must exist on the device's internal storage.
	private String eventListFileName = null;

	//================
	//	Public Methods
	//================

	// Get Connectivity Manager
	public ConnectivityManager getConnectivityManager() {
		return (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		Log.i(TAG, "onConfigurationChanged()");

		if (DEBUG) {
			Log.d(TAG, mResources.getString(R.string.DetectedConfig));
			Toast.makeText(context, mResources.getString(R.string.DetectedConfig) + " ",
							Toast.LENGTH_SHORT).show();
		}
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Activity launched or recreated
		Log.i(TAG, "onCreate()");

		// Set the context
		context = this;

		if (bTRACE) {
			// trace file is created in SD device
			Debug.startMethodTracing("trace.file");
			if (DEBUG)
				MakeToast.makeToast(this, mResources.getString(R.string.startTrace) + " ",
									MakeToast.LEVEL_DEBUG);
		}

		// setContentView
		setContentView(R.layout.activity_main);

		// Reading data from file?
		if (readingFromInternalFile) {
			// Local or /asset based?
			if (internalFilePath == null) {
				if (eventListFileName != null) {
					// Read data from Device file
					File file = getBaseContext().getFileStreamPath(eventListFileName);
					if (file.exists()) {
						internalFilePath = file.getAbsolutePath();
					}
				} else {
					// Read data from /asset file
					ReadEventsFromText();
				}
			}
		}

		// Setup mFeedID using Intent Extras
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			// problem: getInt returns 0 if key is absent, but 0 key is a valid feed #
			int arg = extras.getInt("feedId", mFeedId);
			if (DEBUG)
				Log.d(TAG, "start up, change feedId from " + oldFeedId + " to " + arg);
			oldFeedId = mFeedId;
			setFeedId(arg);
		}

		// Generate the database file path
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			DbFilePath = context.getExternalFilesDir(null).getAbsolutePath() + "/" + DATABASE_NAME;
		} else {
			DbFilePath = context.getFilesDir() + "/" + DATABASE_NAME;
		}

		// Get a database helper
		mDbh = new DatabaseHelper(context);

		// Setup the RSS feed data arrays
		SetupFeedDataArrays();

		// Set the current Feed ID, name and URL
		setFeedId(mFeedId);

		// Initialize tab 0 label
		tab0Label = mResources.getString(R.string.Current);

		// StrictMode is a developer tool which detects things you might be
		// doing but not intentionally
		if (bDEVELOPER_MODE) {
			StrictMode.setThreadPolicy(
					new StrictMode.ThreadPolicy.Builder().
									detectDiskReads().
									detectDiskWrites().
									detectNetwork().	// or
									// detectAll()
														// for all detectable problems
									penaltyLog().
									build());

			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().
										detectLeakedSqlLiteObjects().
										detectLeakedClosableObjects().
										penaltyLog().
										penaltyDeath().
										build());
		}

		// Get the specified feed data
		if (savedInstanceState == null) {

			if (DEBUG)
				Log.d(TAG, "URL: " + mFeedURL);

			// Instantiate the asyncTask passing the context
			RSSFeedReader feedReader = new RSSFeedReader();

			// Execute the asyncTask passing the desired feed URL
			try {
				feedReader.execute(new URL(mFeedURL));
			} catch (MalformedURLException e) {
				Log.e(TAG, "feedReader.execute() error");
				e.printStackTrace();
			}
		}

		// ********* This test is probably not right:
		// Both versions (tablet and phone) will use 'event data'
		// one in a single fragment, the other in separate fragments !!!!!!!
		// it must be like that because the APK file does not know onto what
		// kind of a device it will be loaded!

		// Single or Dual pane?
		mDualPane = (findViewById(R.id.eventData) != null);

		// Create the adapter that will return a fragment for each of
		// the three primary sections (Tab) of the app.
		mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

		// Set up the action bar.
		mActionBar = getActionBar();

		// Check for valid actionBar
		if (mActionBar != null) {

			// Specify that the Home/Up button should not be enabled, since
			// there is no hierarchical parent.
			mActionBar.setHomeButtonEnabled(false);

			// Specify that we will be displaying tabs in the action bar.
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

			// Set up the ViewPager, attaching the adapter and setting up a
			// listener
			// for when the user swipes between sections.
			mViewPager = (ViewPager) findViewById(R.id.pager);
			mViewPager.setAdapter(mAppSectionsPagerAdapter);

			// onPageSelected
			mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

				@Override
				public void onPageSelected(int position) {
					// When swiping between different app sections, select the
					// corresponding tab. We can also use ActionBar.Tab#select()
					// to do this if we have a reference to the Tab.
					mActionBar.setSelectedNavigationItem(position);
				}
			});

			// For each of the sections in the app, add a tab to the action bar.
			for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
				// Create a tab with text corresponding to the page title
				// defined by the adapter. Also specify this Activity object,
				// which implements the TabListener interface, as the listener
				// for when this tab is selected.

				switch (i) {
				case 0: // "Current Tab"
					mActionBar.addTab(mActionBar.newTab()
							  .setText(tab0Label)
							  .setTabListener(this));
					break;

				case 1: // "Saved Tab"
					mActionBar.addTab(mActionBar.newTab()
							  .setText(mResources.getString(R.string.Saved))
							  .setTabListener(this));
					break;

				case 2: // "Search Tab"
					mActionBar.addTab(mActionBar.newTab()
							  .setText(mResources.getString(R.string.Search))
							  .setTabListener(this));
					break;

				}

			}	//	getCount()
		}	//	m_actionBar != null

	} // end --- OnCreate()

	//=========================
	//	Other Lifecycle Methods
	//=========================

	@Override
	protected void onStart() {
		super.onStart();

		Log.i(TAG, "onStart()"); // Activity starts (after created)

	} // end --- onStart()

	@Override
	protected void onRestart() {
		super.onRestart();

		Log.i(TAG, "onRestart()"); // Activity re-starts (after it was stopped)

	} // end --- onRestart()

	@Override
	protected void onPause() {
		super.onPause();

		Log.i(TAG, "onPause()"); // Activity is paused
									// (because a higher priority activity needs
									// memory)

	} // end --- onPause()

	@Override
	protected void onResume() {
		super.onResume();

		Log.i(TAG, "onResume()"); // Activity resumes after being paused

	} // end --- onResume()

	@Override
	protected void onStop() {
		super.onStop();

		Log.i(TAG, "onStop()"); // Activity is stopped ( it can resume or
								// restart or
								// it is destroyed
	} // end --- onStop()

	@Override
	protected void onDestroy() {

		Log.i(TAG, "onDestroy()");

		super.onDestroy();

	} // end - onDestroy()

	public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

		Log.i(TAG, "onTabSelected(" + tab.getPosition() + ")");

		// When the given tab is selected,
		// for specializing menu for current & saved
		mTabSelected = tab.getPosition();

		// Set selected tab
		if (mTabSelected == 0)
			last_list = event_list.Current;
		if (mTabSelected == 1)
			last_list = event_list.Saved;

		// Handle the "Search" icon visibility
		View search_item = findViewById(R.id.action_search);
		if (search_item != null) {
			if (mTabSelected == 2)
				// Hide for "Search"
				search_item.setVisibility(View.GONE);
			else
				// Show for "Current" and "Saved"
				search_item.setVisibility(View.VISIBLE);
		}

		// switch to the corresponding page in the ViewPager.
		if (mDualPane) {

			/*------------------------------------------------------------------
			 * Implements interface EventFragmentCoordinator,
			 * displays the event details (fragment)
			 * Specified by: displayEventDetails() in EventFragmentCoordinator
			 *    Parameters:	eventID, EventType
			 *----------------------------------------------------------------*/

			// Display the appropriate actionBar
			displayEventDetails("", mTabSelected);
			// Force an actionBar change
			invalidateOptionsMenu();
		}

		mViewPager.setCurrentItem(tab.getPosition());

	} // end - onTabSelected()

	public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
		Log.i(TAG, "onTabUnselected(" + tab.getPosition() + ")");
	} // end - onTabUnselected()

	public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

		// for specializing menu for current & saved
		mTabSelected = tab.getPosition();

		// Set selected tab
		if (mTabSelected == 0)
			last_list = event_list.Current;
		if (mTabSelected == 1)
			last_list = event_list.Saved;

		Log.i(TAG, "onTabReselected(" + mTabSelected + ")");
	} // end - onTabReselected()

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the primary sections of the app.
	 **/

	final String ARG_SECTION_NUMBER1 = "Current";
	final String ARG_SECTION_NUMBER2 = "Saved";
	final String ARG_SECTION_NUMBER3 = "Search";
	final String ARG_TAB_ID = "";

	private static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

		protected final String TAG = getClass().getSimpleName();

		// Constructor
		public AppSectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {

			Log.i(TAG, "getItem(" + i + ")");

			final String ARG_SECTION_CURRENT = "Current";
			final String ARG_SECTION_SAVED   = "Saved";
			final String ARG_SECTION_SEARCH  = "Search";
			final String ARG_TAB_ID = "";
			Fragment fragment;
			Bundle args;

			switch (i) {
			case 0:
				if (DEBUG)
					Log.d(TAG, "---" + ARG_SECTION_CURRENT + ": tab = 0 *");
				currentData = new CurrentSectionFragment();
				fragment = currentData;
				args = new Bundle();
				args.putInt(ARG_SECTION_CURRENT, 1);
				args.putBoolean(ARG_TAB_ID, false);
				fragment.setArguments(args);
				return fragment;

			case 1:
				if (DEBUG)
					Log.d(TAG, "---" + ARG_SECTION_SAVED + ": tab = 1 *");
				savedData = new SavedSectionFragment();
				fragment = savedData;
				args = new Bundle();
				args.putInt(ARG_SECTION_SAVED, 2);
				args.putBoolean(ARG_TAB_ID, true);
				fragment.setArguments(args);
				return fragment;

			case 2:
				if (DEBUG)
					Log.d(TAG, "---" + ARG_SECTION_SEARCH + ": tab = 2 *");
				searchData = new SearchSectionFragment();
				fragment = searchData;
				args = new Bundle();
				args.putInt(ARG_SECTION_SEARCH, 3);
				args.putBoolean(ARG_TAB_ID, false);
				fragment.setArguments(args);
				return fragment;

			}	// end switch()

			// Default
			return currentData;

		}	// end - getItem()

		@Override
		public int getCount() {
			// Return the number of tabs
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
				case 0:
					return ("Current List");
				case 1:
					return ("Saved List");
				case 2:
					return ("Search");
			}
			// Default
			return "Section: " + (position + 1);
		}

	} // end - AppSectionsPagerAdapter

	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu main_activity_action) {
		Log.i(TAG, "onCreateOptionsMenu()");

		MenuInflater inflater = getMenuInflater();
		if (mDualPane) {
			Log.i(TAG, "Dual pane");

			// use different menu depending on Tab
			// current can only save -> menu_current_fragment
			// saved can only delete -> menu_saved_current
			// search ? iterate over different searches
			switch (mTabSelected) {
			case 0:
				inflater.inflate(R.menu.menu_dual_activity_cur, main_activity_action);
				if (DEBUG)
					Log.d(TAG, "tab selected is: " + mTabSelected);
				break;
			case 1:
				inflater.inflate(R.menu.menu_dual_activity_sav, main_activity_action);
				if (DEBUG)
					Log.d(TAG, "tab selected is: " + mTabSelected);
				break;
			case 2:
				inflater.inflate(R.menu.menu_dual_activity_cur, main_activity_action);
				if (DEBUG)
					Log.d(TAG, "tab selected is: " + mTabSelected);
				break;
			default:
				// just has a preferences and search buttons
				inflater.inflate(R.menu.menu_main_activity, main_activity_action);
				if (DEBUG)
					Log.d(TAG, "tab selected is: invalid! " + mTabSelected);
				return false;
			}
			return true;
		}

		// Single pane
		if (DEBUG) {
			Log.d(TAG, "Single pane");
			Log.d(TAG, "tab selected is: " + mTabSelected);
		}
		inflater.inflate(R.menu.menu_main_activity, main_activity_action);

		return true;

	} // end - onCreateOptionsMenu()

	/*
	 * Called when an options item is clicked. Handles itemPrefs, punts on
	 * idDeleteSelected, idSaveSelected, or anything else.
	 *
	 * 'Save' only if the CurrentSectionFragment is active (Tab0) 'Delete' only
	 * if the SavedSectionFragment is active (Tab1) currentTab =
	 * m_actionBar.getSelectedTab(); <--- could use this
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int optionSelected = item.getItemId();

		Log.i(TAG, "onOptionItemSelected()");

		switch (optionSelected) {
		case R.id.itemPrefs:
			if (DEBUG)
				Log.d(TAG, " Settings ");
			startActivity(new Intent(this, SettingsActivity.class));
			break;

		case R.id.action_save: // idSaveSelected:
			// Do this only if in CurrentSectionFragment (& dual)
			if (DEBUG)
				Log.d(TAG, " - Save Selected");
			String strEvent = String.format(Locale.US, "%d", CurrentSectionFragment.mId);
			if (DEBUG)
				Log.d(TAG, " Save event Id: " +strEvent);

			// Attempt to store the specified event
			if (mDbh.saveEvent( strEvent ) == false) {
				// Didn't save the event
				Toast.makeText(context, R.string.didNotSave, Toast.LENGTH_LONG).show();
			} else {
				// Saved the event
				Toast.makeText(context, R.string.didSave, Toast.LENGTH_LONG).show();
			}

			// Update the list
			savedData.updateList();
			break;

		case R.id.action_delete: // idDeleteSelected:
			// Do this only if in SavedSectionFragment (& dual)
			if (DEBUG)
				Log.d(TAG, " - Delete Selected");
			strEvent = String.format(Locale.US, "%d", SavedSectionFragment.mId);
			if (DEBUG)
				Log.d(TAG, " Delete event: " +strEvent);

			mDbh.deleteSavedEvent(strEvent);

			// Update the list
			savedData.updateList();
			break;

		case R.id.action_calendar:
			if (DEBUG)
				Log.d(TAG, " - Calendar Selected");
			Toast.makeText(context, "EBMain - Save to Calendar selected", Toast.LENGTH_LONG).show();
			strEvent = String.format(Locale.US, "%d", CurrentSectionFragment.mId);

			BELEvent event = mDbh.getEventById(strEvent);
			if (DEBUG)
				Log.d(TAG, " Calendar: event " + event);

			// call 'CalendarAppointment' as implemented
			Intent intent = CalendarAppointment.makeCalendarAppointment(event);
			startActivity(intent);
			break;

		case R.id.action_share:
			if (DEBUG)
				Log.d(TAG, " - idShare pressed");
			Toast.makeText(context, TAG + " Share", Toast.LENGTH_SHORT).show();
			if (DEBUG)
				Log.d(TAG, " item: " + CurrentSectionFragment.mId);
			ProcessShare(item);
			break;

		case R.id.action_search:
			if (DEBUG) {
				Log.d(TAG, " - idSearch pressed");
				Toast.makeText(context, TAG + " Search", Toast.LENGTH_SHORT).show();
				Log.d(TAG, "item: " + SearchSectionFragment.mId);
			}

			// Remove the search button
			View search_item = (View) findViewById(R.id.action_search);
			if (search_item != null) {
				// Hide the "search" icon
				search_item.setVisibility(View.GONE);
			}

			// TODO - Launch the search activity without using the search tab
			boolean tab_2 = true;
			// boolean tab_2 = false;

			if (tab_2) {
				// Force a Search tab (2) selection
				mActionBar.setSelectedNavigationItem(2);
			} else {

				// Get a SearchSectionFragment
				Fragment fragment = new SearchSectionFragment();
				// Get a fragment manager and start a transaction
				FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

				// Replace whatever is in the fragment_container view with this
				// fragment,
				// and add the transaction to the back stack
				transaction.replace(R.id.fragment_container, fragment);
				transaction.addToBackStack(null);

				// Commit the transaction
				transaction.commit();
			}

			break;

		default:
			Log.d(TAG, " " + mResources.getString(R.string.unimplemented) +
					   " " + Integer.toHexString(optionSelected) +
					   " " + mResources.getString(R.string.pressed));
			if (DEBUG) {
				Toast.makeText(context,
						" " + Integer.toHexString(optionSelected) +
						" " + mResources.getString(R.string.pressed),
						Toast.LENGTH_SHORT).show();
			}
			break;
		}

		return true;

	} // end - onOptionsItemSelected()

	private void ProcessShare(MenuItem item) {
		Log.i(TAG, "ProcessShare()");

		ShareActionProvider mShareActionProvider = (ShareActionProvider) item.getActionProvider();
		/*
		 * This class is a mediator for accomplishing a given task, for example
		 * sharing a file. It is responsible for creating a view that performs
		 * an action that accomplishes the task. This class also implements
		 * other functions such a performing a default action. An ActionProvider
		 * can be optionally specified for a MenuItem and in such a case it will
		 * be responsible for creating the action view that appears in the
		 * android.app.ActionBar as a substitute for the menu item when the item
		 * is displayed as an action item. Also the provider is responsible for
		 * performing a default action if a menu item placed on the overflow
		 * menu of the ActionBar is selected and none of the menu item callbacks
		 * has handled the selection. For this case the provider can also
		 * optionally provide a sub-menu for accomplishing the task at hand.
		 *
		 * There are two ways for using an action provider for creating and
		 * handling of action views:
		 *
		 * Setting the action provider on a MenuItem directly by calling
		 * MenuItem.setActionProvider(ActionProvider). Declaring the action
		 * provider in the menu XML resource. For example:
		 *
		 * <item android:id="@+id/my_menu_item" android:title="Title"
		 * android:icon="@drawable/my_menu_item_icon"
		 * android:showAsAction="ifRoom"
		 * android:actionProviderClass="foo.bar.SomeActionProvider" />
		 *
		 * See Also: MenuItem.setActionProvider(ActionProvider)
		 * MenuItem.getActionProvider()
		 */

		if (DEBUG)
			Toast.makeText(this, "doTheShare" + " ", Toast.LENGTH_SHORT).show();

		// collect data for sharing - this sends an MMS ?????
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.putExtra(Intent.EXTRA_TEXT, " text to sent!");
		shareIntent.setType("text/plain");

		shareIntent.putExtra(Intent.EXTRA_EMAIL, " extra string");
		String str[] = { "qwerty", "asdfgh" };
		shareIntent.putExtra(Intent.EXTRA_EMAIL, str);

		mShareActionProvider.setShareIntent(shareIntent);

		if (DEBUG) {
			// set share data
			Log.d(TAG, " shareIntent " + shareIntent);
			// send off shared data
			Log.d(TAG, "after chooser " + shareIntent);
		}
		startActivity(Intent.createChooser(shareIntent, "Events List"));

	} // end - ProcessShare()

	/**
	 * ------------------------------------------------------------------------
	 ** Implements interface {@link EventFragmentCoordinator}, displays the event
	 * details (fragment)
	 **/

	public void displayEventDetails(String eventID, int EventType) {

		Log.i(TAG, "displayEventDetail(" + eventID + ")");

		String humanReadableType[] = { "Current", "Saved", "Search" };
		if (DEBUG)
			Log.d(TAG,  "displayEventDetails(String eventID = " + eventID +
					", EventType = " + humanReadableType[EventType]);

		if (mDualPane) {
			// for a dual-pane view (tablet) - the fragment implements the
			// detail-view

			EventDetailFragment mEventItemFrag = new EventDetailFragment();
			mEventItemFrag.setEventId(eventID);
			mEventItemFrag.setListType(EventType);
			mEventItemFrag.setDBhelper(new DatabaseHelper(context));
			if (DEBUG)
				Log.d(TAG, "displayEventDetails: DualPane:" +
					   " eventID: " + eventID +
					   " +eventType: " + EventType +
					   humanReadableType);
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.replace(R.id.eventData, mEventItemFrag);
			Integer i = R.id.eventData;
			if (DEBUG)
				Log.d(TAG, "id.eventData: " + Integer.toHexString(i) +
					   "," + " eventItem " + mEventItemFrag);
			transaction.commit();

		} else {
			// for a single-pane view (phone) - Start the detail-view activity
			// which
			// controls the detail-view (fragment)
			if (DEBUG) {
				Log.d(TAG, "displayEventDetails: SinglePane," + eventID);
				Log.d(TAG, "displayEventDetails: start EventDetailActivity " + eventID);
			}

			// In single-pane mode, start the detail event activity for the
			// selected item ID:
			Intent detailIntent = new Intent(this, EventDetailActivity.class);
			if (DEBUG)
				Log.d(TAG, "displayEventDetails: EventType: " + EventType); // the
																		// boolean
																		// as
																		// string
			detailIntent.putExtra(EventDetailFragment.LIST_TYPE, EventType); // !boolean
																				// anymore
			detailIntent.putExtra(EventDetailFragment.EVENTITEM_POS, eventID);
			detailIntent.putExtra(EventDetailFragment.DB_HELPER, String.valueOf(mDbh)); // ?works?

			if (DEBUG)
				Log.d(TAG, "displayEventDetails: w/intent: " + detailIntent.toString());
			startActivity(detailIntent);
		}

	} // end - displayEventDetails()


	/**
	 * Reading a Text-file which is stored in (android) Assets (directory) This
	 * file is RSS data, read from the RSS source Is intended to be fed to the
	 * RSS processing procedure at a suitable point (for decoding the feed data
	 * into BELEvents)
	 *
	 * @return m_mainEventText
	 *
	 *         There is somewhere another read (text) routine (from Jeremy) to
	 *         be manually inserted (I don't remember where).
	 *
	 *         Note: these routines are intended to provide input to EventBoss2
	 *         as if it was coming from the RSS feed. This is suitable to save
	 *         data from a problem feed for continuous testing.
	 *
	 */

	String ReadEventsFromText() {
		// Read a test-file name from Assets
		String[] files;
		try {
			files = getAssets().list("");
		} catch (IOException e) {
			String message = "Failed to get Assets list";
			Log.e(TAG, message, e);
			throw new RuntimeException(message, e);
		}
		InputStream stream = null;
		String feedFile = null;
		for (String file : files) {
			if (DEBUG)
				Log.d(TAG, "Found Assets item: " + file);
			if (file.contains(".xml")) {
				feedFile = file;
				if (DEBUG)
					Log.d(TAG, "Found file: " + feedFile);
				try {
					stream = getAssets().open(feedFile);
				} catch (IOException e) {
					String message = "Failed to open file: " + feedFile;
					Log.e(TAG, message, e);
					throw new RuntimeException(message, e);
				}
				break;
			}
		}
		// this is just a check that we actually got a stream. Really won't work
		// for anything because we didn't open the rxmlFile the first time
		// around.
		if (stream == null) {
			stream = getInputStream(feedFile);
		}

		try {
			// Convert the stream to a string
			m_mainEventText = convertStreamToString(stream);
		} finally {
			try {
				stream.close();
			} catch (IOException excp) {
				// drop it
			}
		}

		return m_mainEventText;

	} // end - ReadEventsFromText()

	/*
	 * Opens a file somewhere on the device and returns the InputStream pointer
	 *
	 * @param fileName - the path to the file to be opened
	 *
	 * @return InputStream - the stream that is opened
	 *
	 * @throws RuntimeException in response to IOException
	 */
	protected InputStream getInputStream(String fileName) {
		try {
			return new FileInputStream(fileName);
		} catch (IOException e) {
			String message = "Failed to open file: " + fileName;
			Log.e(TAG, message);
			throw new RuntimeException(message, e);
		}
	} // end - getInputStream()

	/*
	 * function: convertStreamToString This function was found in
	 * StackOverflow.com question 309424.
	 *
	 * @param is - an opened InputStream
	 *
	 * @return String - returns a text string
	 */
	@SuppressWarnings("resource")
	String convertStreamToString(java.io.InputStream is) {
		try {
			String sTemp = new java.util.Scanner(is).useDelimiter("\\A").next();
			// Remove non-ASCII control characters
			sTemp = sTemp.replaceAll("[^\\p{ASCII}]", "");
			return sTemp;
		} catch (java.util.NoSuchElementException e) {
			return "";
		}
	} // end - convertStreamToString()

} // end - MainActivity
