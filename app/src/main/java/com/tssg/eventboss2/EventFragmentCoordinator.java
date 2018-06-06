/**
 * 
 */
package com.tssg.eventboss2;

/**
 * @author jjeremie
 *
 *  Contains the messages fragments can pass to each other. 
 *  The class {@link EB2MainActivity} implements this interface
 *  and that enables it to act as a "mediator" between fragments.
 */
public interface EventFragmentCoordinator {
	/**
	 * Displays the detailed view of a particular event.
	 * 
	 * 
	 * @param eventID: identifier of the event in a database table.
	 * @param listType: indicating the type of list the event comes from.
	 */
//	 * @param isSavedEvent: flag indicating whether the event is a saved event.

	public void displayEventDetails(String eventID, int listType);
}
