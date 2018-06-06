/**
 * 
 */
package com.tssg.find;

import com.tssg.eventsource.*;

import java.util.List;

import android.content.Context;

/**
 * @author Owner
 *
 */
public interface FindHandler {

	// public FindHandlerImpl FindHandlerImpl( Context context, MainAppScreen mainAppScreen );
	// public FindHandlerImpl FindHandlerImpl( Activity activity, Contex context, MainAppScreen mainAppScreen );
	public List<BELEvent> doFind(List<BELEvent> targetList);
	 // function as it is called via intent to kick of an activity
	public List<BELEvent> doFind(Context ctx, List <BELEvent> targetList);
	// search by key value string pairs.
	public List<BELEvent> doFind(List<BELEvent> targetList, String key, String value); 
	// search by key value string pairs.
	public List<BELEvent> doFind(List<BELEvent> targetList,  long date); 

}
