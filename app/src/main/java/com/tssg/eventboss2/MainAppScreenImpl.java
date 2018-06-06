package com.tssg.eventboss2;

//import com.android.demo.eventsearchv1.FindSpinner;
//import com.tssg.eventboss2.FindHandler;
//import com.tssg.eventboss2.FindUtilsImpl;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.tssg.eventsource.BELEvent;
//import com.tssg.eventboss2.ToggleHandler;
//import com.tssg.eventboss2.ToggleHandlerImpl;

/**
 * Implementation of MainAppScreen interface.
 * 
 * @see MainAppScreen
 */
public class MainAppScreenImpl implements MainAppScreen {
	boolean m_bLOGGING = false;
	String m_TAG = "";

	public ListView m_listView = null;
	EventListAdapter m_adapter = null;
	public Context m_context = null;	// <--
	TextView m_nodataView = null;
	View m_mainAppView = null;

	public void setupLogging(boolean bLog, String logTag) {
		m_bLOGGING = bLog;
		m_TAG = logTag;
	}

	public View setUp(final Context context, boolean bLog, String logTag) {
		// TODO learn about getting information from intents
		// TODO can things found in an intent be specified in the manifest? If
		// so, this could be helpful for setting up launch configurations so
		// that, for example, you could use a mock object instead of the real
		// deal.

		m_adapter = new EventListAdapter();
		m_context = context;

		// set up the window to have no title bar, add the action bar and a
		// list;
// inflate the entire view
/*
		LayoutInflater layoutInflater = null;
		layoutInflater = LayoutInflater.from(context);
		//		m_mainAppView = layoutInflater.inflate(R.layout.main_app_screen, null);
///		m_mainAppView = layoutInflater.inflate(R.layout.listitem_fragment, null);
		m_mainAppView = layoutInflater.inflate(R.layout.activity_eventlist_display, null);

		m_nodataView = (TextView) m_mainAppView.findViewById(R.id.status_line);
		m_nodataView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

		// Set up the list view
		m_listView = (ListView) m_mainAppView.findViewById(R.id.main_window_listview);
*/
		return getView();
	}
	
	public View setUp( View view) {
		// this version is for the case we are working with a fragment

		m_adapter = new EventListAdapter();
		m_mainAppView = view;

		// set up the window to have no title bar, add the action bar and a
		// list;
		// inflate the entire view
		m_listView = (ListView) m_mainAppView.findViewById(R.id.main_window_listview);

		return getView();
	}


	public void setEventList(List<BELEvent> eventList, String title) {
		setListTitle(title);
		setNoDataMessageIfNeeded(eventList);
		m_adapter.setList(eventList);
		m_listView.setAdapter(m_adapter);
		m_listView.setItemsCanFocus(false);
	}

	
	public EventListAdapter getAdapter() {
		return m_adapter;
	}

	public View getView() {
		return m_mainAppView;
	}

	public void setNoDataMessageIfNeeded(List<BELEvent> list) {
		if (list != null && list.size() != 0) {
			m_nodataView.setVisibility(View.GONE);
		} else {
			m_nodataView.setVisibility(View.VISIBLE);
		}
	}

	public void setListTitle(String listTitle) {
		// TODO Auto-generated method stub

	}


}
