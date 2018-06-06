/**
 * @author Kieu Hua
 */
package com.tssg.datastore;

import java.util.List;

import com.tssg.eventsource.BELEvent;


/**
 * Provides support to save BELEvents in persistent storage:
 * 		1)  save events in SQLite table 'saved events'
 * 		2)  for aggregation of events from an RSS-feed  keep 
 * 			a list of previous elements from the feed, in SQL table  'Web Events'. 
 * 			needs to implement cleaning up the list
 *
 */
public interface BELDatastore
{
	/**
	 * saved Event: to implement the saving, by the user, of selected messages
	 */

    /**
     * @throws DatastoreException 
     */
    public void saveEvent( BELEvent event ) throws DatastoreException;
	
    /**
     * Deletes the specified event from the Datastore.
     * @throws DatastoreException 
     */
    public void deleteEvent( BELEvent event ) throws DatastoreException;
    
    /**
     * Retrieves all the stored events.
     */
    public List<BELEvent> getAllStoredEvents();


	/*
	 *  Empties the stored events  data_table.
	 */
	public void deleteAll() throws DatastoreException;
	
	
	/**
	 * WebEvent: to implement aggregation of the feed messages
	 */
	
    /**
     * Adds the specified WebEvent to the datastore.
     * @throws DatastoreException 
     */
    public void saveWebEvent( BELEvent event ) throws DatastoreException;
	
    /**
     * Deletes the specified event from the datastore.
     * @throws DatastoreException 
     */
    public void deleteWebEvent( BELEvent event ) throws DatastoreException;
    
    /**
     * Retrieves all the stored events.
     */
    public List<BELEvent> getAllWebEvents();

	/*
	 *  Empties the WebEvents datatable
	 */
	public void deleteAllWebEvents() throws DatastoreException;

}
