/**
 * 
 */
package com.tssg.eventboss2;

import android.app.Activity;
import android.support.v4.app.ListFragment;
import android.view.Menu;

/**
 * @author jjeremie
 * 
 * Represents the base abstraction of a fragment to be displayed in
 * the Event Boss application.
 *
 */
public abstract class EventBossListFragment extends ListFragment {
	
	/** Indirectly (via an {@link Activity } invokes operations on other fragments **/
	protected EventFragmentCoordinator eventFragmentCoordinator;

	@SuppressWarnings("deprecation")
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			eventFragmentCoordinator = (EventFragmentCoordinator) activity;
		} catch (ClassCastException e){
			throw new ClassCastException(activity.toString()+" must implement onListSelect");
		}
	}

	public void onLoadFinished() {
		// TODO Auto-generated method stub
		
	}

	public boolean onCreateOptionsMenu(Menu main_activity_action) {
		// TODO Auto-generated method stub
		return false;
	}
	
	

}
