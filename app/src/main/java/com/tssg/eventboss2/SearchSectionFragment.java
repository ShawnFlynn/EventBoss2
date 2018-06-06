package com.tssg.eventboss2;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.tssg.datastore.DatabaseHelper;
import com.tssg.eventsource.BELEvent;

/*
 *
 * Fragment which contains the search activity.
 * ======================================================================================
 */
public class SearchSectionFragment  extends EventBossListFragment
									implements TextWatcher{

	static final String TAG = "SearchSectionFragment";

	public static final String ARG_SECTION_NUMBER = "section_number";

	public static int mListType = 2;	// -> SearchList
	private EditText mSearchText;
	private String mSearch = "";

	public static long mId;
	public BELEvent mEvent;
	private DatabaseHelper dbh;
	private Cursor mCursor;
	private SimpleCursorAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		dbh = new DatabaseHelper(getActivity());
		Log.i(TAG,"onCreate()");
		Log.d(TAG, "before cursor, dbh:" +dbh+ ", search string: " +mSearch);
		mCursor = dbh.getCursorSearchEvents(mSearch);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(TAG,"onCreateView()");
		View rootView = inflater.inflate(R.layout.fragment_section_launchpad,
				container, false);
		mSearchText = (EditText) rootView.findViewById(R.id.searchText);
		mSearchText.addTextChangedListener(this);
		Log.d("SearchSectionFragment:search_section_text:layout",
			  " " + rootView);

		Log.d(TAG, "after, mCursor: " +mCursor);

		// For the cursor adapter, specify which columns go into which views
		String[] fromColumns = { DatabaseHelper.KEY_TITLE,
								 DatabaseHelper.KEY_DESCRIPTION,
								 DatabaseHelper.KEY_EVENTID};

		// The TextView a simple_list_item_1
		int[] toViews = { R.id.title, R.id.description };

		// Create an empty adapter we will use to display the loaded data.
		// We pass null for the cursor, then update it in onLoadFinished()
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
				R.layout.eventlist_row, mCursor, fromColumns, toViews, 0);
		setListAdapter(adapter);
		mAdapter = adapter;

		return rootView;
	}


	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// do something with the data
		Log.i(TAG, "onListItemClick()");
		Log.d(TAG, ":View = " + v + ":Position = " + position + ":Id=" + id);

		// here we need to get the Id of the original record
		// id is selection in the now updated list (search result)
		// it contains the id of the event in the list (loaded from current list)
		mId = id;
		Log.d(TAG, "onListItemClick:Id = " +id+ " mId = " +mId);
		String idStr = Long.toString(mId);	
		mEvent = dbh.getEventById( idStr );
		Log.d(TAG, "id = " +id+ " mId = " +mId+ " : " +mEvent);

		eventFragmentCoordinator.displayEventDetails(Long.toString(id), 2);
	}
	
	public void updateSearch() {
		mCursor.close();
		Log.i(TAG, "updateSearch()");
		Log.d(TAG, "before cursor, dbh: " +dbh);
		Log.d(TAG, "search term: " +mSearch);
		mCursor = dbh.getCursorSearchEvents(mSearch);
		Log.d(TAG, "after cursor, dbh: " +dbh);
		mAdapter.swapCursor(mCursor);
		Log.d(TAG, "after cursor swap");
		setListAdapter(mAdapter);
	}

	public void beforeTextChanged(CharSequence arg0,
			int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		Log.i(TAG, "beforeTextChanged()");
		Log.d(TAG, "cursor: " +mCursor+ ", dbh: " +dbh);
	}

	public void afterTextChanged(Editable text) {
		// TODO Auto-generated method stub
		mSearch = text.toString();
		Log.i(TAG, "afterTextChanged( " +mSearch+ " )");
		Log.d(TAG, "cursor: " +mCursor);
		updateSearch();
	}

	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onTextChanged()");
		Log.d(TAG, "cursor: " +mCursor+ ", dbh: " +dbh);
	}
	

	@Override
	public void onDestroyView() {
		Log.i(TAG, "onDestroyView()");
		super.onDestroyView();
	}

}	//	end - SearchSectionFragment class

