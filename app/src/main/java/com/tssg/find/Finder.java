/**
 * This class searches through event list seeking entries that match certain criteria.
 */
package com.tssg.find;

import com.tssg.eventsource.*;

import java.util.List;
import java.util.Date;

/**
 * @author Larry Medwin
 * April 7, 2011
 * @author rjd
 * May, 2011
 *
 */
public interface Finder {
	
	/**
	 * match List<BELEvent> against a specific field, with a specific value.
	 * @param input List<BELEvent> input event list
	 * @param field String that indicates which part of BELEvent to match
	 * @param value String that indicates value to match
	 * @return List<BELEvent> consisting of matched events
	 */
	
//	public List<BELEvent> matchFieldValue(List<BELEvent> input, String field, String value);
	public List<BELEvent> match(List<BELEvent> input, String field, String value);

	/**
	 * match List<BELEvent> against a specific date, specified in the Date parameter.
	 * @param input List<BELEvent> input event list
	 * @param date Date object that provides the date to match with event.
	 * @return List<BELEvent> consisting of matched events
	 */
	public List<BELEvent> matchDate(List<BELEvent> input, Date date);
	
	/**
	 * match List<BELEvent> against a specified range of dates, inclusively, specified 
	 * in the Date parameters.   Note that the Dates may occur across a change in year, so the 
	 * Date must provide the values for year as well as day-of-year values.
	 * @param input List<BELEvent> input event list
	 * @param startDate Date object that provides the start of date range to match with event.
	 * @param endDate Date object that provides the end of the date range to match with event.
	 * @return List<BELEvent> consisting of matched events
	 */
	public List<BELEvent> matchDateRange(List<BELEvent> input, Date startDate, Date endDate);


}

