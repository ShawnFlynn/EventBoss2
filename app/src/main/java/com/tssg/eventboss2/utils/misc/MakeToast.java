package com.tssg.eventboss2.utils.misc;

import android.content.Context;
// import android.view.Gravity;
import android.widget.Toast;

/**
 * MakeToast provides a "global" toast generator with central control so 
 * that toasts only used for debug can be turned off in one central place. 
 * Callers to the makeToast method may specify whether they are debug
 * messages or user-visible messages, and also specify their intended
 * duration if they want to. 
 *  
 * MakeToast uses an enum to make a singleton as suggested by Bloch
 * "Effective Java", pg 18.
 * 
 * @author Peter Nelson
 *
 */
public enum MakeToast {
	
	LEVEL_USER,
	LEVEL_DEBUG,
	DURATION_SHORT,
	DURATION_LONG;
		
    private static int displayTime = Toast.LENGTH_SHORT;

    /**
     * The makeToast method has two forms with different signatures (overloads).   This one generates a toast message on the display with a
     * default short duration 
     *    
     * @param context The caller's context
     * @param msg  The content of the toast message
     * @param level  Whether the message is for debugging (LEVEL_DEBUG) or should be seen by normal users (LEVEL_USER)
     */   
	public static void makeToast(Context context, String msg, MakeToast level) {

            makeToast(context, msg, level, DURATION_SHORT);
		}
		
     /**
     * The makeToast method has two forms with different signatures (overloads).   This one generates a toast message on the display. And allows
     *  the caller to specify the duration.
     *    
     * @param context The caller's context
     * @param msg  The content of the toast message
     * @param level  whether the message is a debug (LEVEL_DEBUG) or a normal user application message (LEVEL_USER)
     * @param duration  Whether the message has a short (DURATION_SHORT) or long (DURATION_LONG) duration on the screen
     */
	public static void makeToast(Context context, String msg, MakeToast level, MakeToast duration) {

		// Most of the MakeToast calls are flagged with "LEVEL_DEBUG"
		// The intent of the level check implemented here appears to be to make a quick
		// return, which would disable display of the toasts. The original
		// logic was to say level 'not equal' LEVEL_DEBUG, but that enabled
		// all LEVEL_DEBUG toasts. Change it to "==" to disable them!
		// An alternative would be to make a resource to control this.
			if (level == MakeToast.LEVEL_DEBUG)  {
				return;
			}
	        	
		    if (duration == DURATION_LONG) {
		    	displayTime = Toast.LENGTH_LONG;
		    }
			Toast toast = Toast.makeText(context, msg, displayTime );
			toast.show();
		} 
	
	
	
	

	
  }	

