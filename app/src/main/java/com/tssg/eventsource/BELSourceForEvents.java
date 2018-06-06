/**
 * 
 */
package com.tssg.eventsource;

import java.util.List;

/**
 * Provides access to the list of events published by Boston Event List.
 *
 */
public interface BELSourceForEvents
{
   /**
    * Retrieves the current list of events from the BEL website
    */
    public List<BELEvent> getCurrentEventList();
    
}
