package com.tssg.eventboss2;

//import java.text.SimpleDateFormat;
//import java.util.Locale;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.tssg.datastore.DatabaseHelper;

/*
 * A fragment for the view of the current list display.
 * ======================================================================================
 */
public class CurrentSectionFragment extends EventBossListFragment {

	protected final String TAG = getClass().getSimpleName();

	public static int mListType = 0;	// -> currentList
	public static TextView mListHeader;
	public static int mPosition = -1;
	public static long mId;

	Cursor mCursor = null;
	SimpleCursorAdapter mAdapter;
	static int mEventItemCount;
	DatabaseHelper dbh = null;

	LayoutInflater mLayoutInflater;
	ViewGroup mViewGroup;
	ListView mLV;

	// Get the EB2 Interface
	private static EB2Interface EB2 = new EB2MainActivity();

	// Debug flag
	private static boolean DEBUG = EB2.DEBUG();		// Global
	// private boolean DEBUG = true;	// Local

	// Local EB2 resources
	private Resources mResources = EB2.getEB2Resources();


	// also see eventDetailActivity
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i(TAG, "onCreate()");

		if ( dbh == null ) {
			dbh = new DatabaseHelper(getActivity());
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		Log.i(TAG, "onCreateView()");

		mViewGroup = container;
		mLayoutInflater = inflater;
		// Do we need this?
		if (DEBUG)
			Log.d(TAG, "onCreateView: container :" + container);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Log.i(TAG, "onActivityCreated()");

		if (DEBUG)
			Log.d(TAG, "before cursor: " +mCursor+", dbh: "+dbh);
		if (mCursor == null) {
			mCursor = dbh.getCursorAllEvents();
		}
		if (DEBUG)
			Log.d(TAG, "after cursor: " +mCursor);

		// For the cursor adapter, specify which columns go into which views
		String[] fromColumns = {DatabaseHelper.KEY_TITLE,
								DatabaseHelper.KEY_STARTTIME,
								DatabaseHelper.KEY_ENDTIME,
								DatabaseHelper.KEY_LOCATION};

		// The TextView in simple_list_item_1
		int[] toViews = { R.id.title, R.id.time, R.id.endtime, R.id.location};

		// Setup the list header
		mListHeader = (TextView) mLayoutInflater.inflate(R.layout.listheader, null);

		mEventItemCount = mCursor.getCount();
		mLV = getListView();
		mLV.setHeaderDividersEnabled(true);
		mLV.setDividerHeight(5);
		mLV.addHeaderView(mListHeader);

		// Create an empty adapter we will use to display the loaded data.
		// We pass null for the cursor, then update it in onLoadFinished()
		mAdapter = new SimpleCursorAdapter(getActivity(),
											R.layout.eventlist_row,
											mCursor,
											fromColumns,
											toViews,
											0);

		// Update the Current list
		updateList();

	}

	@Override
	public void onLoadFinished() {

		Log.i(TAG, "onLoadFinished()");

		setListAdapter(mAdapter);
	}


	@Override
	public void onDestroyView() {

		Log.i(TAG, "onDestroyView()");

		mCursor.close();
		setListAdapter(null);

		super.onDestroyView();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.i(TAG, "onListItemClick(" + position + ")");

		if (DEBUG) {
			Log.d(TAG, "(106)---> eventFragmentCoordinator ->displayEventDetails (" + mListType + ")");
			Log.d(TAG, "onListItemClick: Position=" + mPosition + ":mId=" + id);
		}
		if( position > 0 ) {
			mPosition = position;
			mId = id;

			if (DEBUG)
				Log.d(TAG, "onListItemClick: Position=" + mPosition + ":mId=" + mId);

			eventFragmentCoordinator.displayEventDetails(Long.toString(mId), 0);
		}
	}

	// Set the Current Tab Label in the action bar
	@SuppressWarnings("deprecation")
	public void setCurrentTabLabel() {

		// Get the selected tab label
		String currentTab = (String) EB2.getEB2ActionBar().getSelectedTab().getText();

		Log.i(TAG, "setCurrentTabLabel(" +currentTab+ ")");

		// Don't change the "Saved" tab label
		if (currentTab != mResources.getString(R.string.Saved))
			EB2.getEB2ActionBar().getSelectedTab().setText(EB2.getTab0Label());
	}

	@SuppressWarnings("deprecation")
	public void updateList() {

		Log.i(TAG, "updateList()");

		// Set the proper tab label (Current or Stored)
		if (EB2.getEB2ActionBar().getSelectedTab().getPosition() == 0)
			setCurrentTabLabel();

		mCursor = dbh.getCursorAllEvents();
		mAdapter.swapCursor(mCursor);
		mEventItemCount = mCursor.getCount();

		if (DEBUG)
			Log.d(TAG, "count: " +mEventItemCount);

		updateListHeader(EB2.getFeedName());

		setListAdapter(mAdapter);
	}

	void updateListHeader( String extraText ) {

		Log.i(TAG, "updateListHeader()");

		String tempEvents = null;

		// Create a list-header (TextView) and add it to the list like this:

		SimpleDateFormat simpFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm",
														Locale.getDefault() );
		Date m_channelDate = new Date(System.currentTimeMillis());
		String channelDate = m_channelDate == null?
				"--" : simpFormat.format(EB2.getCurrentDate());

		// Get "Event" or "Events" based on event count
		if (mEventItemCount == 1)
			tempEvents = mResources.getString(R.string.Event);
		else
			tempEvents = mResources.getString(R.string.Events);

		// This should be the current date or the date when data was saved into the database
		String tempString = extraText + "@ " +
							channelDate + ": " +
							mEventItemCount +
							" " + tempEvents;

		// Set the Current list header text
		mListHeader.setText( tempString );

		// Set the Current/Stored tab label
		setCurrentTabLabel();

		if (DEBUG)
			Log.d(TAG, tempString);
	}

}	// end - CurrentSectionFragment class
