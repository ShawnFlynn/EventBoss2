package com.tssg.eventsource;

import com.tssg.eventboss2.EB2Interface;
import com.tssg.eventboss2.EB2MainActivity;
import com.tssg.eventboss2.RSSFeedReader;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import android.util.Log;

/**   called to create the current List of BELEvents.
 *   The function getBELEventlist()  opens an
 *   IOStream and reads from the Boston Events List's RSS feed
 *   (if it can open a stream and read from it).
 *
 *   It returns a list of BELEvents (or an empty list if the read fails).
 */
public class BELEventlist {

	protected final String TAG = getClass().getSimpleName();

	// Debug flag
	private static boolean DEBUG = RSSFeedReader.EB2.DEBUG();	// Global
	// private boolean DEBUG = true;	// Local

	private RSSFeedReader currentTask = null;

	List<BELEvent> rssEventList;

	/** @param RSSFeedReader is needed to publish progress callback */
	public BELEventlist( RSSFeedReader currentTask) {
		super();
		Log.i(TAG, "BELEventList()");
		this.currentTask = currentTask;
	}

	/** Loads the RSS feed
	 * @return EMPTY_LIST on error
	 * @param feed_URL
	 */
	public List<BELEvent> getBELEventlist( URL[] feed_URL ){
		Log.i(TAG, "getBelEventList()");

		// open the RSS feed:
		// reads, formats Events and returns a list of Events or
		// (if there were exceptions) it returns an empty list
		try {
			return loadFeed(String.valueOf(feed_URL[0]));
		} catch (RuntimeException e) {
			Log.e(TAG,e.getMessage(),e);
			return Collections.<BELEvent>emptyList();   // an empty list
		}
	}	//	end - getBELEventList()

	/**
	 * Load all the events from the RSS feed
	 * @param feedUrl the URL for the RSS feed
	 * @return List<BELEventList> The list of events
	 * @throws RuntimeException on any error
	 */
	private List<BELEvent> loadFeed( String feedUrl ) {
		Log.i(TAG, "loadFeed(" +feedUrl+ ")");

		try {
			BELSourceForEvents eventSource = new BELSourceForEventsImpl(feedUrl, this.currentTask);
			long start = System.currentTimeMillis();
			rssEventList = eventSource.getCurrentEventList();   // here the parser makes BELSourcedEvent msg
			long duration = System.currentTimeMillis() - start;
			if (DEBUG)
				Log.d(TAG, "Parser duration=" + duration + ", size=" + rssEventList.size());
		} catch (Exception e){
			Log.e(TAG,e.getMessage(),e);
			throw new RuntimeException( e );
		}
		return rssEventList;

	}	//	end - loadFeed()

}	//	end - BELEventList class
