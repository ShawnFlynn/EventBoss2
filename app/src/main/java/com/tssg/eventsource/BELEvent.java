package com.tssg.eventsource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.tssg.eventboss2.utils.date.DateUtils;


/**
 * This class encapsulates the information about a single event published 
 * by the Boston Event List.
 * It is the primary data object of the EventBoss application.
 */
public class BELEvent  implements Comparable<BELEvent>
{
	// A unique identifier for this event. The Boston Events List
	//  does not send us an unique identifier so we should make one up
	//  until they do.
	// Make up an identifier to use whenever the constructor is passed a null
	Integer m_id = null;
	Integer m_feed = null;
	String m_title = null;
	String m_startTime = null;
	String m_endTime = null;
	String m_eventType = null;
	String m_linkToGroup = null;
	String m_organizer = null;
	String m_location = null;
	String m_description = null;
	String m_longDescription = null;
	Date  m_startDate = null;
	Date  m_endDate = null;

	/** no-arg constructor uses defaults */
	public BELEvent() {
	}
	
	/**
	 * Class constructor
	 * Should we change constructor to create a Date and URL
	 * from the times and linkToGroup fields?  If so, make sure the
	 * constructor throws error if the input strings are not valid.
	 * 
	 * @param id Event identifier
	 * @param feed Feed identifier
	 * @param title Event title
	 * @param startDate start {@link Date}
	 * @param endDate end {@link Date}
	 * @param startTime Event start time
	 * @param endTime Event end time
	 * @param type Event type
	 * @param linkToGroup
	 * @param org Event organizer
	 * @param loc Event location
	 * @param description Event (short) description
	 * @param longDescription Event long description
	 */
	public BELEvent(Integer id, Integer feed, String title, 
					Date startDate, Date endDate, String startTime, String endTime,
					String type,
					String linkToGroup, String org, String loc,
					String description, String longDescription )
	{
		m_id = id;
		m_feed = feed;
		m_title = title;
		m_startTime = startTime;
		m_endTime = endTime;
		m_startDate = startDate;
		m_endDate = endDate;
		m_eventType = type;
		m_linkToGroup = linkToGroup;
		m_organizer = org;
		m_location = loc;
		m_description = description;
		m_longDescription = longDescription;
	}
	
	/**
	 * Class constructor
	 * Should we change constructor to create a Date and URL
	 * from the times and linkToGroup fields?  If so, make sure the
	 * constructor throws error of the input strings are not valid.
	 * 
	 * @param id Event identifier
	 * @param feed Feed identifier
	 * @param title Event title
	 * @param startTime Event start time
	 * @param endTime Event end time
	 * @param type Event type
	 * @param linkToGroup
	 * @param org Event organizer
	 * @param loc Event location
	 * @param description Event (short) description
	 * @param longDescription Event long description
	 */
	public BELEvent( Integer id, Integer feed, String title, 
					 String startTime, String endTime,
					 String type,
					 String linkToGroup, String org, String loc,
					 String description, String longDescription )
	{
		m_id = id;
		m_feed = feed;
		m_title = title;
		m_startTime = startTime;
		m_endTime = endTime;
		setStartDate(m_startTime);
		setEndDate(m_endTime);
		m_eventType = type;
		m_linkToGroup = linkToGroup;
		m_organizer = org;
		m_location = loc;
		m_description = description;
		m_longDescription = longDescription;
	}

	/**
	 * Gets the Event Id
	 * @return event Id
	 */
	public Integer getId() { 
		return m_id; 
	}

	/**
	 * Set the event Id 
	 * @param id the event Id
	 */
	public void setId( Integer id ) { 
		m_id = id; 
	}

	BELEvent setId(String key) {
		this.m_id = keyHash(key);
		return this;  
	}

	// Hash function
	int keyHash(String key) {
		int k = (int)key.length();
		int u = 0, n = 0;

		for (int i=0; i<k; i++) {
			n = (int)key.charAt(i);
			u += i*n;
		}
		return u % Integer.MAX_VALUE;
	}

	/**
	 * Gets the feed Id
	 * @return feed Id
	 */
	public Integer getFeed() { 
		return m_feed; 
	}

	/**
	 * Set the feed Id 
	 * @param id the feed Id
	 */
	public void setFeed( Integer feed ) { 
		m_feed = feed; 
	}

	/** since id is based on hashcode, it should not be set until other fields are set */
	BELEvent setId() {
		this.m_id = hashCode();	// that makes a unique id
		return this;  
	}

	/**
	 * Gets the event title
	 * @return The event title
	 */
	public String getTitle() { 
		return m_title; 
	}

	/**
	 * Set the event title 
	 * @param title The title of the event
	 */
	public void setTitle( String title ) { 
		m_title = title; 
	}

	/**
	 * Gets the event start time
	 * @return the event start time
	 */
	public String getStartTime() { 
		return m_startTime; 
	}

	/**
	 * 
	 * @param startTime
	 */
	public void setStartTime( String startTime ) { 
		m_startTime = startTime; 
	}

	/**
	 * 
	 * @param startTime
	 */
	public void setStartTime( java.util.Calendar startTime ) { 
		if (startTime == null) {
			m_startTime = null; 
		} else {
			m_startTime = simpDateFormat.format(startTime.getTime()); 
		}
	}

	/**
	 * Gets the event end time
	 * @return event end time
	 */
	public String getEndTime() { 
		return m_endTime; 
	}

	/**
	 * Set the event end time
	 * @param event end Time
	 */
	public void setEndTime( String endTime ) { 
		m_endTime = endTime; 
	}

	/**
	 * Set the event end time
	 * @param event end Time
	 */
	public void setEndTime( java.util.Calendar  endTime ) { 
		if (endTime == null) {
			m_endTime = null; 
		} else {
			m_endTime = simpDateFormat.format(endTime.getTime()); 
		}
	}

	public static final String simpDatePatternStored = "MM/dd/yyyy hh:mm aa";
	public static final String simpDatePatternSource = "yyyy-MM-dd - hh:mm aa";
	// note: not static because not thread-safe
	private final SimpleDateFormat simpDateFormat =
			new SimpleDateFormat(simpDatePatternStored, Locale.getDefault() );

	private BELEvent setStartDate(String date) {
		try {
			if (null != date && !date.isEmpty() ) {
				this.m_startDate = simpDateFormat.parse(date);
			} else {
				this.m_startDate = null;
			}
		} catch (ParseException excp) {
			this.m_startDate = DateUtils.parseDate(date);
		}
		return this;
	}

	private BELEvent setEndDate(String date) {
		try {
			if (null != date && !date.isEmpty() ) {
				this.m_endDate = simpDateFormat.parse(date);
			} else {
				this.m_endDate = null;
			}
		} catch (ParseException excp) {
			this.m_endDate = DateUtils.parseDate(date);
		}
		return this;
	}

	/**
	 * @param date converted elsewhere
	 * @return self
	 */
	BELEvent setStartDate(Date date) {
		if (date != null) {
			this.m_startDate = new Date(date.getTime());
		}
		return this;
	}

	/**
	 * @param datemsec milliseconds since 1970, from a Date.getTime()
	 * @return self
	 */
	BELEvent setStartDate(long datemsec) {
		if (datemsec != 0L) {
			this.m_startDate = new Date(datemsec);
		}
		return this;
	}

	/**
	 * @param date converted elsewhere
	 * @return self
	 */
	BELEvent setEndDate(Date date) {
		if (date != null) {
			this.m_endDate = new Date(date.getTime());		
		}
		return this;
	}

	/**
	 * @param datemsec milliseconds since 1970, from a Date.getTime()
	 * @return self
	 */
	BELEvent setEndDate(long datemsec) {
		if (datemsec != 0L) {
			this.m_endDate = new Date(datemsec);
		}
		return this;
	}

	/** @return Date value of start time, or null if missing or misparsed */
	public Date getStartDate() {
		Date retval = null;
		if (null != this.m_startDate) {
			retval = new Date(this.m_startDate.getTime()) ;	// defensive copy
		}
		return retval;
	}

	/** @return Date value of end time, or null if missing or misparsed */
	public Date getEndDate() {
		Date retval = null;
		if (null != this.m_endDate) {
			retval = new Date(this.m_endDate.getTime()) ;	// defensive copy
		}
		return retval;
	}

	/**
	 * Gets the event type 
	 * @return the event type
	 */
	public String getEventType() { 
		return m_eventType; 
	}

	/**
	 * 
	 * @param eventType
	 */
	public void setEventType( String eventType ) { 
		m_eventType = eventType; 
	}

	/**
	 *  Gets the Link to Group
	 * @return linkToGroup
	 */
	public String getLinkToGroup() { 
		return m_linkToGroup; 
	}

	/**
	 * Set the Link to Group
	 * @param linkToGroup
	 */
	public void setLinkToGroup( String linkToGroup ) { 
		m_linkToGroup = linkToGroup; 
	}

	/**
	 * Gets the event organizer
	 * @return event organizer
	 */
	public String getOrganizer() { 
		return m_organizer; 
	}

	/**
	 * Set the event organizer
	 * @param organizer
	 */
	public void setOrganizer( String organizer ){ 
		m_organizer = organizer; 
	}

	/**
	 * Get the event location
	 * @return event location
	 */
	public String getLocation() { 
		return m_location; 
	}

	/**
	 * Set the event location
	 * @param location
	 */
	public void setLocation( String location ) { 
		m_location = location; 
	}

	/**
	 * Get the event short description 
	 * @return event short description
	 */
	public String getDescription() { 
		return m_description; 
	}

	/**
	 * Set the event short description
	 * @param description
	 */
	public void setDescription( String description ){ 
		m_description = description; 
	}

	/**
	 * Get the event's long description
	 * @return long description
	 */
	public String getLongDescription() { 
		return m_longDescription; 
	}

	/**
	 * Set the event's long description
	 * @param long Description
	 */
	public void setLongDescription( String longDescription ){ 
		m_longDescription = longDescription; 
	}
	
	/** comparison of objects is comparison of start date/time */
	public int compareTo(BELEvent another) {
		if (another == null) {
			return 1;
		}
		return another.m_startDate.compareTo(m_startDate);
	}

	@Override
	public String toString() {
		// id is not included
		StringBuilder sb = new StringBuilder();
		sb.append("Title: ").append(getTitle()).append('\n');
		sb.append("Link: ").append(getLinkToGroup()).append('\n');
		if ( null != getStartDate() ) {
			sb.append("Event Start Time: ").append(simpDateFormat.format(getStartDate()))
			.append(", '").append(getStartTime()).append("'")
			.append('\n');
		}
		if ( null != getEndDate() ) {
			sb.append("Event  End  Time: ").append(simpDateFormat.format(getEndDate()))
			.append(", '").append(getEndTime()).append("'")
			.append('\n');
		}
		sb.append("Event Type: ").append(getEventType()).append('\n');
		sb.append("Event Link: ").append(getLinkToGroup()).append('\n');
		sb.append("Event Location: ").append(getLocation()).append('\n');
		sb.append("Organizer: ").append(getOrganizer()).append('\n');
		sb.append("Description: ").append(getDescription()).append('\n');
		sb.append("Long Description: ").append(getLongDescription()).append('\n');
		return sb.toString();
	}

}	//  end - BELEvent
