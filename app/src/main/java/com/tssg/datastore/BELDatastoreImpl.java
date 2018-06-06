package com.tssg.datastore;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.util.Log;

import com.tssg.eventsource.BELEvent;

/**
 * Implements the BELDatastore interface.
 * this is only a PLACEHOLDER
 * @author Kieu Hua
 */

public class BELDatastoreImpl implements BELDatastore
{
	DatabaseHelper mDbHelper = null;

	static final String LOG = "BELDataStoreImpl";

	/**
	 * Constructor - takes the context to allow the database to be opened/created
	 * 
	 * @param ctx the Context within which to work
	 */
	public BELDatastoreImpl(Context ctx){

		mDbHelper = new DatabaseHelper( ctx );
	}
	
	/**
	 * Create a new event using the title, location and time provided. If the event is 
	 * successfully create return the new rowId for that note, otherwise return
	 * a -1 to indicate failure.
	 * 
	 * @param title 
	 * @param location
	 * @param time
	 * @return rowId or -1 if failed
	 * @throws DatastoreException 
	 */
	public void saveEvent(BELEvent belEvent) throws DatastoreException
	{

		ContentValues initialValues = new ContentValues();

		initialValues.put(DatabaseHelper.KEY_EVENTID,
							belEvent.getId() );

		initialValues.put(DatabaseHelper.KEY_FEEDID,
							belEvent.getFeed() );

		initialValues.put(DatabaseHelper.KEY_TITLE,
							belEvent.getTitle() );

		initialValues.put(DatabaseHelper.KEY_TYPE,
							belEvent.getEventType() );

		initialValues.put(DatabaseHelper.KEY_STARTTIME,
							belEvent.getStartTime() );

		initialValues.put(DatabaseHelper.KEY_ENDTIME,
							belEvent.getEndTime() );

		initialValues.put(DatabaseHelper.KEY_LINK,
							belEvent.getLinkToGroup() );

		initialValues.put(DatabaseHelper.KEY_ORGANIZER,
							belEvent.getOrganizer() );

		initialValues.put(DatabaseHelper.KEY_LOCATION,
							belEvent.getLocation() );

		initialValues.put(DatabaseHelper.KEY_DESCRIPTION,
							belEvent.getDescription() );

		initialValues.put(DatabaseHelper.KEY_LONGDESCRIPTION,
							belEvent.getLongDescription() );

		mDbHelper.insert(DatabaseHelper.DATABASE_SAVED, initialValues);

	}


	/**
	  * Return a Cursor over the list of all events in the database
	  * 
	  * @return Cursor over all events
	  * @throws SQLException if Event could not be found/retrieve
	  */
	public List<BELEvent> getAllStoredEvents() throws SQLException {

		//TODO put this in a try block to catch SQLException and wrap
		// it in our exception
		return mDbHelper.getAllStoredEvents();
	}


	public void deleteEvent(BELEvent belEvent) throws DatastoreException
	{
		Log.d(LOG, "BelDatasoreImpl:deleteEvent ******** event id = "
					+ belEvent.getId() );

		List<BELEvent> eventList = null;
		eventList = getAllStoredEvents();
		
		Log.d(LOG, "BelDatasoreImpl:deleteEvent ******** eventList.size() = "
					+ eventList.size());
		
		String eventId = belEvent.getId().toString();
		mDbHelper.delete(DatabaseHelper.DATABASE_SAVED, eventId);
	}
	
	public void deleteAll() throws DatastoreException
	{
		 mDbHelper.deleteAllSavedEvents();
	}

	/*
	 *---------- for web Event table
	*/
	public void saveWebEvent(BELEvent belEvent) throws DatastoreException
	{
		ContentValues initialValues = new ContentValues();

		initialValues.put(DatabaseHelper.KEY_EVENTID,
							belEvent.getId() );

		initialValues.put(DatabaseHelper.KEY_FEEDID,
							belEvent.getFeed() );

		initialValues.put(DatabaseHelper.KEY_TYPE,
							belEvent.getEventType() );

		initialValues.put(DatabaseHelper.KEY_TITLE,
							belEvent.getTitle() );

		initialValues.put(DatabaseHelper.KEY_TYPE,
							belEvent.getEventType() );

		initialValues.put(DatabaseHelper.KEY_STARTTIME,
							belEvent.getStartTime() );

		initialValues.put(DatabaseHelper.KEY_ENDTIME,
							belEvent.getEndTime() );

		initialValues.put(DatabaseHelper.KEY_LINK,
							belEvent.getLinkToGroup() );

		initialValues.put(DatabaseHelper.KEY_ORGANIZER,
							belEvent.getOrganizer() );

		initialValues.put(DatabaseHelper.KEY_LOCATION,
							belEvent.getLocation() );

		initialValues.put(DatabaseHelper.KEY_DESCRIPTION,
							belEvent.getDescription() );

		initialValues.put(DatabaseHelper.KEY_LONGDESCRIPTION,
							belEvent.getLongDescription() );

		mDbHelper.insert(DatabaseHelper.DATABASE_WEB, initialValues);

	}


	/**
	  * Return a Cursor over the list of all events in the database
	  * 
	  * @return Cursor over all events
	  * @throws SQLException if Event could not be found/retrieve
	  */
	public List<BELEvent> getAllWebEvents() throws SQLException {

		//TODO put this in a try block to catch SQLException and wrap
		//it in our exception
		return mDbHelper.getAllWebEvents();
	}


	public void deleteWebEvent(BELEvent belEvent) throws DatastoreException
	{
		Log.d(LOG, "BelDatasoreImpl:deleteWebEvent ******** event id = "
					+ belEvent.getId() );
		List<BELEvent> eventList = null;
		eventList = getAllWebEvents();

		Log.d(LOG, "BelDatasoreImpl:deleteWebEvent ******** eventList.size() = "
					+ eventList.size());

		mDbHelper.delete(DatabaseHelper.DATABASE_WEB,
							belEvent.getId().toString());
	}
	
	public void deleteAllWebEvents() throws DatastoreException
	{
		mDbHelper.deleteAllWebEvents();
	}

}	//	end - BELDatastoreImpl class
