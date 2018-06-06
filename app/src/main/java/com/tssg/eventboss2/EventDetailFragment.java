package com.tssg.eventboss2;


import java.util.Date;
import java.util.Locale;

import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.tssg.datastore.DatabaseHelper;
import com.tssg.eventsource.BELEvent;


/** displays and handles the details of an event */
/* Notes for development:
 *
 * */
public class EventDetailFragment extends Fragment {

	protected final String TAG = getClass().getSimpleName();

	// Get the EB2 Interface
	public static EB2Interface EB2 = new EB2MainActivity();

	// Debug flag
	private static boolean DEBUG = EB2.DEBUG();		// Global
	// private boolean DEBUG = true;	// Local

	final public static String EVENTITEM_POS = "position";
	public static final String DB_HELPER = "DBHelper";

	public static final String LIST_TYPE = null;

	int m_isListType;		// Current (0), Saved (1), Search (2)
	DatabaseHelper mDbh;	// = new DatabaseHelper(getActivity()) EBMainActivity;
	String mId;

	private BELEvent mEvent;
	private TextView mTitleText;	// title
	private TextView mStartText;	// start time
	private TextView mEndText;		// end time
	private TextView mTypeText;
	private TextView mLinkText;
	private TextView mLocationText;
	private TextView mDescriptionText;

	public void setListType(int listType ) {
		Log.i(TAG, "setListType(" +listType+ ")");

		m_isListType = listType;
	}

	public void setEventId(String id) {
		Log.i(TAG, "setEventId(" +id+ ")");

		mId = id;
	}

	public void setDBhelper(DatabaseHelper db) {
		Log.i(TAG, "setDBHelper()");

		mDbh = db;
	}

	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		Log.i(TAG, "onCreate()");

		if (DEBUG)
			Log.d(TAG, "List Type = " + m_isListType
				 				  + " w/id: " + mId);

		// Get a database handle
		// for tablet dbh is set when 'displayEventDetails' creates an EventDetailFragment
		// for phone  dbh is set here, because 'EventDetailActivity' creates the EventDetailFragment
		if( mDbh == null )  {
			mDbh = new DatabaseHelper(getActivity());
		}

		// If the mId is empty
		if (DEBUG)
			if (mId == null || mId.isEmpty())
				Log.d(TAG, "mId is empty");
			else
				Log.d(TAG, "type: " + m_isListType
									+ ",  mId: " + mId
									+ ", db: " + mDbh);

		if( m_isListType == 0 && mId != null && !mId.isEmpty()) {	// ----- Current Section

			if (DEBUG)
				Log.d(TAG, "current Event: " + mEvent + ", id = " + mId);

			// Get the Current event
			try {
				mEvent = mDbh.getEventById(mId);
			}
			catch (android.database.CursorIndexOutOfBoundsException exept ) {
				if (mEvent == null) {
					Log.e(TAG, "we have an id, but no Event: " + mEvent);
					return;
				}
				;
			}

			if (DEBUG)
				Log.d(TAG, "current Event: " +mEvent);

		}	// ListType == 0

		if( m_isListType == 1 && mId != null && !mId.isEmpty()) {	// ----- Saved Section

			if (DEBUG)
				Log.d(TAG, "saved Event: " + mEvent
							+ ", mId: " + mId
							+ ", dbh: " + mDbh);

			// Get the Saved event
			try {
				mEvent = mDbh.getSavedEventById(mId);
			}
			catch (android.database.CursorIndexOutOfBoundsException exept ) {
				if (mEvent == null) {
					Log.e(TAG, "we have an id, but no Event: " + mEvent);
					return;
				}
				;
			}

			if (DEBUG)
				Log.d(TAG, "saved Event: " +mEvent);

		}	// ListType == 1

		if( m_isListType == 2 && mId != null && !mId.isEmpty()) {	// ----- Search section

			if (DEBUG)
				Log.d(TAG, "EventDetailFragment(read id: " + mId);

			// - mId - should be the Id from CurrentEventList
			//     also see --> SearchSectionFragment line 97
			try {
				mEvent = mDbh.getEventById(mId);
			}
			catch ( SQLException exp ) {
				Log.e( TAG, "caught SQLException: reading this id: "
						+ mId ,exp ); }
			catch (android.database.CursorIndexOutOfBoundsException exept ) {
				Log.e( TAG, "caught CursorIndexOutOfBoundsException "
						+ mId ,exept ); }

			if (DEBUG)
				Log.d(TAG, "search Event: "+mEvent);

		}	// ListType == 2

	}	//  end - onCreate()

	@Override
	public View onCreateView(LayoutInflater inflater,
							 ViewGroup container,
							 Bundle SavedInstanceState) {

		Log.i(TAG, "onCreateView()");

		if (DEBUG)
			Log.d(TAG, " inflate: -> fragment_event_detail, container:" + container);
		View view = inflater.inflate(R.layout.fragment_event_detail, container, false);
		if (DEBUG)
			Log.d(TAG, " view: " + view);

		mTitleText = (TextView) view.findViewById(R.id.titleText);
		mStartText = (TextView) view.findViewById(R.id.startText);
		mEndText = (TextView) view.findViewById(R.id.endText);
		mTypeText = (TextView) view.findViewById(R.id.typeText);
		mLinkText = (TextView) view.findViewById(R.id.linkText);
		mLocationText = (TextView) view.findViewById(R.id.locationText);
		mDescriptionText = (TextView) view.findViewById(R.id.descriptionText);

		if (DEBUG) {
			Log.d(TAG, " text: " + mTitleText);
			Log.d(TAG, "................");
			Log.d(TAG, " star: " + mStartText);
			Log.d(TAG, " end: " + mEndText);
		}

		return view;

	}	//  end - onCreateView()

	@Override
	public void onStart() {
		super.onStart();
		Log.i(TAG, "onStart()");

		refreshView();	// show all of the list entry
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.i(TAG, "onDestroyView()");
	}

	public void refreshView() {
		Log.i(TAG, "refreshView()");

		if (DEBUG)
			Log.d(TAG,"mEvent= " +mEvent);

		if(mEvent == null ) {
			mTitleText.setText(" **** no Event ****");
		} else {
			mTitleText.setText(mEvent.getTitle());
			mStartText.setText(mEvent.getStartTime());
			if (m_isListType < 2) {
				mEndText.setText(mEvent.getEndTime());
				mTypeText.setText(mEvent.getEventType());
				mLinkText.setText(mEvent.getLinkToGroup());
				mLocationText.setText(mEvent.getLocation());
				mDescriptionText.setText(mEvent.getLongDescription());
			}
		}
		if (DEBUG)
			Log.d(TAG, "text: " + mTitleText.getText());

	}	//  end - refreshView()

	void makeAppointment(String title, String location, Date start, Date end ) {
		Log.i(TAG, "makeAppointment()");

		Intent intent =  new Intent(Intent.ACTION_INSERT, Events.CONTENT_URI);
		long startL, endL;
		if (null != start) {
			startL = start.getTime();
			intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startL);
		}
		if (null != end) {
			endL = end.getTime();
			intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endL);
		}
		String strEvent = String.format(Locale.getDefault(),
										"%d",
										CurrentSectionFragment.mId);
		BELEvent event = mDbh.getEventById(strEvent);
		title = event.getTitle();
		location = event.getLocation();
		start = (Date) event.getStartDate();
		end = (Date) event.getEndDate();

		intent.putExtra(Events.TITLE, title);
		intent.putExtra(Events.ALL_DAY, false);
		intent.putExtra(Events.EVENT_LOCATION, location);

		startActivity(intent);

	}	//  end - makeAppointment()

	public int get_isEventType() {
		return m_isListType;
	}

	public void set_isEventType(int m_isEventType) {
		this.m_isListType = m_isEventType;
	}

}	//  end - EventDetailFragment
