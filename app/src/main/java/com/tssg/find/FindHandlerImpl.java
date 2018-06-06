/**
 * 
 */
package com.tssg.find;

//import tssg.eventboss.ui.*;
import com.tssg.eventboss2.MainAppScreen;  ///// may be not the right one
import com.tssg.eventsource.*;

import java.util.List;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

//import android.widget.ListAdapter;
//import android.widget.ListView;

/**
 * @author Owner
 * 
 * 
 *         ??? extend Activity to allow call to startActivityForResult(...)
 */
// public class FindHandlerImpl extends Activity implements FindHandler {
public class FindHandlerImpl implements FindHandler {

	final int result = 1;
	public static final int FIND_REQUEST = 1;
	public static final int MY_REQUEST_CODE = 2;
	public static final String CRITERIA_NAME = "name";
	public static final String CRITERIA_VALUE = "value";

	Activity m_activity = null;
	Context m_context = null;
	MainAppScreen m_mainAppScreen = null;

	List<BELEvent> m_targetList = null;

	FindHandlerImpl() {
	};

	public FindHandlerImpl(Context context, MainAppScreen mainAppScreen) {
		m_context = context;
		m_mainAppScreen = mainAppScreen;
	}

	public FindHandlerImpl(Activity activity, Context context,
			MainAppScreen mainAppScreen) {
		m_activity = activity;
		m_context = context;
		m_mainAppScreen = mainAppScreen;
	}

	public void setList(List<BELEvent> targetList) {
		// TODO Auto-generated method stub
		Log.i("INFO", "findHandler's target list set");
		m_targetList = targetList;
	}

	// TODO - Implement the following in FinderImlp
	// List<BELEvent> results = (List<BELEvent>) m_findHandler.doFind(List
	// BELEvent targetList, FindCriteria findCriteria);

	/**
	 * Debug/Prototype code
	 */

	public List<BELEvent> doFind(List<BELEvent> list) {

		int len = list.size();
		List<BELEvent> tmpList = new ArrayList<BELEvent>(list);
		if (len >= 2) {
			tmpList.remove(1);
			Log.i("INFO", "returning results of Find");
			return tmpList;
		}
		Log.i("INFO", "returning results of Find - no change made");
		return (List<BELEvent>) list;
	}

	//@Override
	public List<BELEvent> doFind(Context ctx, List<BELEvent> targetList) {
		// TODO Auto-generated method stub
		return null;
	}

	
	/*
	 * doFind  This overloaded method switches on event type to call the appropriate 
	 * finder.match.  This is because some find criteria values are strings, some are dates, etc.
	 * 
	 */
	public List<BELEvent> doFind(List<BELEvent> list, String name,
			String value) {

		FinderImpl finder = new FinderImpl();

		int len = list.size();
		List<BELEvent> tmpList = new ArrayList<BELEvent>(list);
		if (len > 0) {

			tmpList = (List<BELEvent>) finder.match(list, name, value);

			Log.i("INFO", "Find returning " + tmpList.size() + " events.");
			return tmpList;
		}

		return list;
	}

	/*
	 * doFind  This overloaded method switches on event type to call the appropriate 
	 * finder.match.
	 * 
	 */
	public List<BELEvent> doFind(List<BELEvent> list, long date) {

		FinderImpl finder = new FinderImpl();

		int len = list.size();
		List<BELEvent> tmpList = new ArrayList<BELEvent>(list);
		if (len > 0) {
			// last parameter true will return later dates in the list
			tmpList = (List<BELEvent>) finder.matchDate(list, date, true);
			Log.i("INFO", "Find returning " + tmpList.size() + " events.");
			return tmpList;
		}

		return list;
	}


	
}
