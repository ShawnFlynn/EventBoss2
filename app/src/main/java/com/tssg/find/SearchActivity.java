package com.tssg.find;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.tssg.eventboss2.EB2Interface;
import com.tssg.eventboss2.EB2MainActivity;
import com.tssg.eventboss2.MainAppScreen;
import com.tssg.eventboss2.MainAppScreenImpl;
import com.tssg.eventboss2.R;
import com.tssg.eventboss2.utils.misc.MakeToast;
import com.tssg.eventsource.BELEvent;


public class SearchActivity extends Activity {

//	public static final String TAG = "SearchActivity";	// log's tag
	private final String TAG = getClass().getSimpleName();

	public static MainAppScreen m_mainAppScreen = null;
	public View m_mainAppView = null;			//	view for find results	

	public static final String INTENT_LOG = "com.tssg.eventboss2.bLOGGING";
	public static TextView m_statusView = null;
	public static Context context = null;
	public boolean bLOGGING = true;				// do log

	/* 
	 *   Set up the View containing the source list
	 *   Bring up the search routine, collect the parameters
	 *   Execute the search  (in the end show the results
	 */

	public static List<BELEvent> eventsListFind = null;		// where to find events
	public static List<BELEvent> eventsListResult = null;	// what has been found

	// Get the EB2 Interface
	EB2Interface EB2 = new EB2MainActivity();


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i(TAG, "onCreate()");

		eventsListFind = EB2.getCurrentEventsList();

		String str;
		str = "Search in List of size: "+ eventsListFind.size();
		Log.d(TAG, " " + eventsListFind.size());

		MakeToast.makeToast(this, str, MakeToast.LEVEL_USER);

		// get optional parameters from the intent
		Intent intent = this.getIntent();
		bLOGGING = intent.getBooleanExtra(INTENT_LOG, bLOGGING);

		m_mainAppScreen = new MainAppScreenImpl();
		m_mainAppScreen.setupLogging(bLOGGING, TAG);
		m_mainAppScreen.setUp(this, bLOGGING, TAG);
		m_mainAppView = m_mainAppScreen.getView();
		this.setContentView(m_mainAppView);
		m_statusView = (TextView) (m_mainAppView.findViewById(R.id.status_line));

		// **** here is the ListView code:
		ListView m_listView = (ListView)findViewById(R.id.main_window_listview);
		m_listView = (ListView) m_mainAppView.findViewById(R.id.main_window_listview);

		m_listView.setCacheColorHint(R.color.white);
		m_listView.setBackgroundResource(R.color.white);
		context = this;

		m_mainAppScreen.setEventList(eventsListFind, "Source for Search");

		str = "To search" + eventsListFind.size();
		Log.d(TAG, "To search " +str);

		MakeToast.makeToast(this, str, MakeToast.LEVEL_USER);
		m_statusView.setText(str);

		// start search in FilterCriteria class
		startFilterCriteria();

	}	//	end - OnCreate( ... )


	private void startFilterCriteria() {

		Log.i(TAG, "startFilterCriteria()");

		FindUtilsImpl utils = new FindUtilsImpl();
		Set<String> tSet = utils.getTypeSet((ArrayList<BELEvent>) eventsListFind);
		ArrayList<String> tList = new ArrayList<String>(cleanSet(tSet));

		String str = "start FilterCriteria.class";
		Log.i(TAG, "Search Activity: " +str);

			Intent intent = new Intent(context, FilterCriteria.class);
			intent.putStringArrayListExtra("TypesKey", tList);
			//TODO capture activity not found exception
			//	((Activity) context).startActivityForResult(intent,Constants.FIND_REQ);
			startActivityForResult(intent,Constants.FIND_REQ);

			str = "startActivity(intent,Constants.FIND_REQ)";
			//	str = "startActivityForResult(intent,Constants.FIND_REQ)";
			Log.e("Search Dlg: ", str);
		}


		// Remove empty strings from the list
		private Set<String> cleanSet(Set<String> s){
			Log.i(TAG, "cleanSet(msg)");

			if (s.contains("")) {
				s.remove("");	
			}if (s.contains("etc")) {  // Developers prerogative
				s.remove("etc");
			}
			return s;
		}

			/*
			 * Find process: 
			 * 	Get find criteria
			 *  find in 'eventsListFind' matches
			 *      and copy them 'eventsListResults' (as result(i) )
			 *      
			 *      
			 *      Find stack:      'Source'              'Result'  
			 *          0          =eventsListFind         =result0 
			 *          1          =result0                =result1 
			 *          2              :                       :
			 *          
			 *   to back up the stack just load source from stack index current-1
			 *   so after a find  operation the 
			 *     user can search again (going one deeper)
			 *       or back up to previous search
			 *       or to exit
			 *   (at this point eventslistFind can not be deleted 
			 *      because it is the Eventslist) 
			 * 
			 * 
			 *   when the whole stack is popped we probably have to 
			 *   execute just one line:     
			 *   	mainAppScreen.setEventList(eventsListFind, "List to search");       
			 *   unless other settings have been changed.
			 */
		
		/*
		 * Results from SearchActivity.
		 */
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data)
		{
			Log.i(TAG, "onActivityResult()");

			String str = "Req: "+requestCode+","+" Res: "+resultCode+","+" data: "+data;
			Log.e("Result from SearchActivity: ", str);

			String findKey = null;
			String findValue = null;
			long lDate = 0;
			
			if (resultCode == RESULT_OK) {
				if (requestCode == Constants.FIND_REQ) {
					findKey = data.getStringExtra(Constants.FIND_KEY);
					findValue = data.getStringExtra(Constants.FIND_VALUE);
					Log.v(TAG, "Search result: key: "+ findKey +" value: "+ findValue );
				}

				if (findKey.compareTo(Constants.EVENT_DATE) == 0) {
					lDate = data.getLongExtra("long_date", 0);
					Log.v(TAG, "Search result: key: "+ findKey + " value: " + lDate );
					}
				else   //---not a date ---//
					{
						// yet another ?? code 
						Log.v(TAG, "Criteria returned, key:" + findKey + " value: " + findValue );
					}
					//--- end Find Date or Keyword ---//	

				List<BELEvent> findEventsList1 = null;	//from getfindlist in EBMainActivity (current or saved)
				findEventsList1 = new ArrayList<BELEvent>(eventsListFind);// = m_webEventsList);
				Collections.copy(findEventsList1, eventsListFind); 	// no need: after construct w/collection copy is already made

				FindHandler fH = new FindHandlerImpl(this, m_mainAppScreen);
				Vector<BELEvent> findEventsList2 = new Vector<BELEvent> ( findEventsList1 );
				// Pass the result back down the line.

				// JGB - Ticket # 86 : Date search fails to return valid events
				if (lDate == 0)
					eventsListResult = fH.doFind( findEventsList2, findKey, findValue);
				else
					eventsListResult = fH.doFind( findEventsList2, lDate );
				//eventsListResult = fH.doFind( findEventsList2, findKey, findValue);

				m_mainAppScreen.setEventList(eventsListResult, "Search Result");
				str = "Found " + eventsListResult.size();
				Log.e("Search result", str);
				MakeToast.makeToast(this, str, MakeToast.LEVEL_USER);

				m_statusView = (TextView) (m_mainAppView.findViewById(R.id.status_line));
				m_statusView.setText(str);

				setResult( RESULT_OK ); 
				}	// RESULT_OK
				else { //--- not RESULT_OK --- 
				// failed to return Find criteria
				Log.i(TAG, "Unknown Find Criteria");
				setResult( RESULT_CANCELED  );  
				}

		}	// end onActivityResult(...)


		// Called when an options item is clicked
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			Log.i(TAG, "onItemSelected()");

			switch (item.getItemId()) { 
			case R.id.idSearchNew:
				//  start a new search 
				MakeToast.makeToast(this, "start a new search",
									MakeToast.LEVEL_DEBUG);
				break;

			case R.id.idSearchAgain:
				//  search again in current results 
				MakeToast.makeToast(this, "start search in current result",
									MakeToast.LEVEL_DEBUG);
				break;

			case R.id.idEndSearch:
				// done searching
				finish();		// end this task
				break;
			}
			return true;
		}


		@Override
		protected void onResume() {
			// TODO implement onResume
			super.onResume();

			Log.i(TAG, "onResume()");

			MakeToast.makeToast(this, "S onResume", MakeToast.LEVEL_DEBUG);
		}

		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			Log.i(TAG, "onCreateOptionsMenu()");

			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.searchactivity, menu);
			return true;
		}

}	//	end SearchActivity class