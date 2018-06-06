package com.tssg.eventboss2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.tssg.datastore.BELDatastore;
import com.tssg.datastore.BELDatastoreImpl;
import com.tssg.datastore.DatastoreException;
import com.tssg.eventboss2.utils.misc.MakeToast;
import com.tssg.eventsource.BELEvent;
import com.tssg.eventsource.BELEventlist;

import java.net.URL;
import java.sql.Date;
import java.util.List;

/* Data handling routines, ExecFeedReader is run asynchronously:
 * <p>
 *   {@link #EventsListReader(URL)}
 *      Data read from BostonEventsList RSS
 *      returns m_webEventsList  (the address of the events array)
 *      The code has currently a fixed URL,
 *      The Settings activity must be made to select one URL from a list of
 *      current BostonEventsList URLs; and to use the one passed in as an argument.
 * <p>
 *   {@link EB2MainActivity#ReadEventsFromText()}	// Data BostonEventsList RSS converted to string
 *      Retrieve data from a text file, stored in 'Assets', captured from RSS feed.
 *      this is for test purposes, it should be feed to the DOM parser (which
 *      currently chokes on it).
 */

public class RSSFeedReader extends AsyncTask<URL,
											 Integer,
											 List<BELEvent> > {

	static final String TAG = "RSSFeedReader";

	// Progress Dialog
	private ProgressDialog pDialog = null;

	// EB2MainActivity context
	private Context context;

	// Feed ID
	private static int feedId = 0;

	// Temporary Feed Id
	private static int oldFeedId = 0;

	// Feed name
	private static String feedName;

	// Cancelled flag
	private volatile boolean running = true;

	// Get the EB2 Interface
	public static EB2Interface EB2 = new EB2MainActivity();

	// Local DEBUG flag
	private final boolean DEBUG = EB2.DEBUG();		// Global
	// private final boolean DEBUG = true;		// Local

	// EB2 resources
	public Resources mResources = EB2.getEB2Resources();

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		Log.i(TAG,  "onPreExecute(" +feedId+ ")");

		// Save the EB2MainActivity context
		context = EB2.getContext();

		// Get the specified feed id
		feedId = EB2.getFeedId();

		// Get the old feed id
		oldFeedId = EB2.getOldFeedId();

		// Get the specified feed name
		feedName = EB2.getFeedName();

		// Create and setup the progress indicator
		pDialog = new ProgressDialog(this.context);

		// Set the dialog message
		pDialog.setMessage( mResources.getString(R.string.ReadingRSSFeed) +
							feedId + " " +
							feedName +
							mResources.getString(R.string.PleaseWait));

		// Specify an indeterminate duration
		pDialog.setIndeterminate(false);

		// Specify cancelable
		pDialog.setCancelable(true);

		// Make progress dialog visible
		if(!((Activity) context).isFinishing())
		{
			pDialog.show();
		}

	}	//  end - onPreExecute()

	@Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);

		if (null != pDialog) {
			switch(progress.length) {
				case 0:
					pDialog.setMessage(mResources.getString(R.string.gettingStarted) + " " + progress[0]);
					break;
				case 1:
					pDialog.setMessage(mResources.getString(R.string.digesting) + " " + progress[0]);
					break;
				case 2:
					pDialog.setMessage(mResources.getString(R.string.digesting) + " " +progress[0]+ " " +
									   mResources.getString(R.string.of) + " " +progress[1]);
					break;
				default:
					if (progress[2] == 3) {
						pDialog.setMessage(
								mResources.getString(R.string.Storing) + " " +progress[0]+ " " +
								mResources.getString(R.string.of) + " " +progress[1]+ " " +feedName+ " " +
								mResources.getString(R.string.intoDB));
					} else {
						pDialog.setMessage(
								mResources.getString(R.string.Reading) + " " +progress[0]+ " " +
								mResources.getString(R.string.From)    + " " +feedName+ " " +
								mResources.getString(R.string.Feed));
					}
					break;
			}
		}
	}	//  end - onProgressUpdate()

	@Override
	protected void onCancelled() {
		running = false;
	}

	@Override
	protected void onPostExecute( List<BELEvent> RSS_List ) {
		super.onPostExecute(RSS_List);

		Log.i(TAG, "onPostExecute(" + RSS_List.size() + ")");

		if (DEBUG)
			MakeToast.makeToast(context,
								mResources.getString(R.string.ReadRSS) +
								" ", MakeToast.LEVEL_DEBUG);

		// If the returned event list is not empty
		if (!RSS_List.isEmpty() ) {

			// Copy returned list to global list
			EB2.setCurrentEventsList(RSS_List);

			// Update the event list
			EB2.getCurrentData().updateList();

		} else {
			// No events at selected feed
			Log.e(TAG, "No events at selected feed - so not changing view.");
			if (DEBUG)
				Toast.makeText( context,
								mResources.getString(R.string.NoEvents),
								Toast.LENGTH_LONG).show();
		}

		// Remove progress indicator
		if (pDialog != null) {
			pDialog.hide();
			pDialog.cancel();
			pDialog = null;
		}

	}	//  end - onPostExecute()


//  fragment should have a status line and a list (for the events)
//  if the fragment is set-up right this should work:
//  pass the list of events to the adapter:
//  files   MainAppScreen and MainAppScreenImpl, EventlistAdapter
//  set the list in the adapter to the current values


	@Override
	protected List<BELEvent> doInBackground(URL... params) {

		Log.i(TAG, "doInBackground(" +feedId+ ")");

		// Local Event List
		List<BELEvent> dIBEventList = null;

		// Initialize tab 0 label = "Current"
		EB2.setTab0Label(mResources.getString(R.string.Current));

		// Check for valid feed ID
		if (feedId >= EB2.getEventsListCacheSize())
			return null;

		// Read from cached feed array
		dIBEventList = getCacheEntry(feedId);
		if (dIBEventList != null && !dIBEventList.isEmpty())
			Log.d(TAG, "retrieved "
					+ dIBEventList.size()
					+ " events from cache feedId "
					+ feedId);

		// If cache entry was empty
		if (dIBEventList==null || dIBEventList.isEmpty()) {

			// Get Connection manager and network info
			ConnectivityManager connMgr = (ConnectivityManager)
					this.context.getSystemService(Context.CONNECTIVITY_SERVICE);

			// Get the network information
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

			// If we have network info and a connection
			if (networkInfo != null && networkInfo.isConnected()) {

				// Get a new empty event list
				BELEventlist eventSource = new BELEventlist(this);
				try {

					// Get a new or empty list from RSS feedId
					// TODO - put a cancel check in getBelEventList()
					dIBEventList = eventSource.getBELEventlist(params);

					// If this list is not empty
					if (!dIBEventList.isEmpty()) {
						Log.d(TAG, "retrieved "
								+ dIBEventList.size()
								+ " new events from RSS feedId "
								+ feedId);
						// Copy to cache
						EB2.setEventsListCacheEntry(feedId, dIBEventList);
						if (DEBUG)
							Log.d(TAG, "saved " + dIBEventList.size()
											+ " events in cache feedId "
											+ feedId);
					}
				} catch (Exception excp) {
					Log.e(TAG, "doInBackground - exception "
							+ excp.toString()
							+ " in read RSS phase, feedId changed from "
							+ feedId
							+ " to "
							+ oldFeedId, excp);

					// Update the feed ID
					// Locally
					feedId = oldFeedId;
					// Globally
					EB2.setFeedId(feedId);

					// Get the current feed data
					dIBEventList = EB2.getEventsListCacheEntry(feedId);
				}
			}
		}

		// Get a data store
		BELDatastore dataStore = new BELDatastoreImpl(context);

		// If we received something from the RSS feed or cache
		if ((dIBEventList != null) && (!dIBEventList.isEmpty()) ) {

			// Save in DB for future use in case next time there is
			// nothing read from the feed.
			try {
				// First throw out previous BD entries
				dataStore.deleteAllWebEvents();
			} catch (DatastoreException e) {
				Log.e(TAG, "doInBackground: dataStore.deleteAllWebEvents: ", e);
			}

			// Second make a copy in the DB of the current list of events
			int kk = 0, size = dIBEventList.size();
			for (BELEvent next : dIBEventList) {
				try {
					// Check for a cancel
					if (running) {

						// Insert the feed ID
						next.setFeed(feedId);
						// Save the event
						dataStore.saveWebEvent( next );
						// Update the progress dialog
						publicProgressCallback(++kk, size, 3);

					} else {
						// Cancelled - return
						return null;
					}

				} catch (DatastoreException dataExp) {
					Log.e(TAG, "doInBackground: Caught exception trying to save to the database: ", dataExp);
				}
			}

			if (DEBUG)
				Log.d(TAG, "stored " + dIBEventList.size()
								 + " events into the database feedId "
								 + feedId);
		} else {
			BELEvent next;

			// Get current DB entries
			dIBEventList = dataStore.getAllWebEvents();

			// Check if we received anything
			if (!dIBEventList.isEmpty()) {

				// Get the first event record
				next = dIBEventList.get(0);
				if (null != next) {

					// Get and update the feedId
					feedId = next.getFeed();

					// Locally
					oldFeedId = feedId;

					// Globally
					// Set feedId, mFeedName and mFeedURL
					EB2.setFeedId(feedId);
					EB2.setOldFeedId(feedId);

					// If this is not a recent database store from cache
					if (EB2.EventsListCacheIsEmpty(feedId)) {
						// Set tab 0 label = Stored
						EB2.setTab0Label(mResources.getString(R.string.Stored));
					}

					Log.d(TAG,  "used " +dIBEventList.size()+
							" stored events from database feedId "
							+ feedId);
				}
			} else {
				if (DEBUG)
					Log.d(TAG, "nothing found in the database");
			}
		}

		// Return the list
		return dIBEventList;

	}	//  end - doInBackground()

	List<BELEvent> getCacheEntry(int feedId) {

		// Check for valid feedId
		if (feedId >= EB2.getEventsListCacheSize())
			return null;

		// Check for empty Cache entry
		if (EB2.EventsListCacheIsEmpty(feedId)) {
			return null;
		}

		// Get the current (expiration adjusted) time
		Date expireTime = new Date(System.currentTimeMillis() - EB2.getExpireTime());

		// Get the cache list entry date & time
		Date eventTime = EB2.getEventsListTime(feedId);

		// Check for an expired entry
		if (expireTime.after(eventTime)) {

			// Delete the expired entry
			EB2.clearEventsListCacheEntry(feedId);
			return null;
		}

		// Set the cache list date as current
		EB2.setCurrentDate(eventTime);

		// Return the specified entry
		return EB2.getEventsListCacheEntry(feedId);

	}

	/** Because {@link AsyncTask#publishProgress} is protected,
	 *  need a callback to update status.
	 */
	public void publicProgressCallback(Integer... values) {
		this.publishProgress(values);
	}

}	//  end - RSSFeedReader class
