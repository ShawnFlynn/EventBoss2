/**
 * 
 */
package com.tssg.datastore;

/**
 * Used to wrap exceptions coming
 *  from the underlying datastore and rethrow 
 *  them up to the calling application.
 * @author Jim Cant
 *
 */

/*
 * CB: need to make this class public so we can catch the exception
 * in the saveEventHandler and deleteEventHandler methods within EBMainActivity.
 */
@SuppressWarnings("serial")
public class DatastoreException extends Exception
{
	/**
	 * Creates a new DatastoreException consisting of the
	 * specified 'cause' wrapped with the specified message.
	 * Any exception that 'escapes' the datastore code and reaches 
	 * the client application should be a DatastoreException; in other 
	 * words, wrap 'em all.
	 * @param cause the original error
	 * @param wrapperMessage a message meaningful at a higher
	 * level (typically the application) that hides the underlying
	 * cause which may not be meaningful to the application.
	 */
	DatastoreException( String wrapperMessage, Throwable cause )
	{
		super( wrapperMessage, cause );
	}

	DatastoreException( String wrapperMessage )
	{
		super( wrapperMessage );
	}

}
