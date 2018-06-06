package com.tssg.eventboss2;

import android.content.Intent;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.util.Log;

import com.tssg.eventsource.BELEvent;


public class CalendarAppointment {

	protected final static String TAG = "CalendarAppointment";

	public static Intent makeCalendarAppointment(BELEvent event) {

		Log.i(TAG, "makeCalendarAppointment()");

		// Create a Calendar Intent
		Intent intent = new Intent(Intent.ACTION_INSERT, Events.CONTENT_URI);

		// Insert Date & Time info into the Intent
		intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
										event.getStartDate().getTime());
		intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
										event.getEndDate().getTime());

		// Insert Title, Location and (not an) All Day event info
		intent.putExtra(Events.TITLE, event.getTitle());
		intent.putExtra(Events.EVENT_LOCATION, event.getLocation());
		intent.putExtra(Events.ALL_DAY, false);

		// Return the Intent
		return intent;

	}	//  end  - makeCalendarAppointment()

}	//  end - CalendarAppointment class
