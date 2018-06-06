/**
 * 
 */
package com.tssg.eventboss2;

import java.util.List;

import android.content.Context;
import android.view.View;

import com.tssg.eventsource.BELEvent;


/**
 *  The main, or home, screen for the EventBoss application; its
 *  primary task is to
 *  display a list of events.  In addition it 
   <ul>
     <li> displays a control to initiate a find (or search)
          operation on the displayed list
     </li>
     <li>a control to toggle to switch to a different list.  Typically,
         this switches back and forth between a list of current events and a
         list of saved events.  (The current event list is the list of events
         most recently fetched from the web site providing the data in an RSS
         feed.)
     </li>
     <li> A label to identify the list being displayed
     </li>
   </ul>
 * @author JimCant
 *
 */
public interface MainAppScreen
{

	/**
	  Sets the display title of the  list.
	 * @param listTitle 
	 */
	public void setListTitle( String listTitle );

	/**
	  Sets the list which the screen is to display and the title
	  for the list.
	  @param eventList the list; it may be null or empty
	  @param title the title to display; it may be null or empty
	 */
	public void setEventList( List<BELEvent> eventList, String title );

	/**
	  Returns the view so the caller can display it as needed.
	  @return the view implementing the main screen for the
	  application.
	 */
	View getView();
	
	/**
	 * access to the adapter
	 * @return
	 */
	public EventListAdapter getAdapter();



	/**
	  Creates a view.  Typically, this view will be used as the main
	  screen of an EventBoss application.
	 */
	public View setUp( Context context, boolean bLog, String logTag );

	
	public View setUp( View view );			// use with fragment


	/**
	  Sets the handler which should be called when the toggle control
	  is activated.
	 */
	//	public void setToggleHandler( ToggleHandler toggleHandler );

	/**
	  Sets the handler which should be called when the Find control
	  is activated.
	 */
	//	public void setFindHandler( FindHandler findHandler );
	//TODO replace Object with FindHandler once it is defined

	public void setupLogging(boolean bLOGGING, String tag);


	/* Displays a no data message on the screen if the list is null or
	 * empty
	 */
	void setNoDataMessageIfNeeded( List<BELEvent> list );


	/*
	 * This function finishes the work of the OnClickListener for the "find button". 
	 */
	//	void completeOCL(Vector<BELEvent> findList, String name, String value);
	/*
	 * This function finishes the work of the OnClickListener for the "find button". 
	 */
	//	void completeOCL(Vector<BELEvent> findList,  long date);
}