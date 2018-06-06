package com.tssg.find;

public class Constants {

	public static final String INTENT_ACTION_FILTER_CRITERIA = "tssg.eventboss.ui.FilterCriteria";
	public static final String FIND_KEY = "find_key";
	public static final String FIND_VALUE = "key_value";

	public static final String EVENT_TYPE 			  = "Event Type";
	public static final String EVENT_TITLE            = "Title";
	public static final String EVENT_DATE             = "Date";
	public static final String EVENT_ORG              = "Organizer";
	public static final String EVENT_LOC              = "Location";
	public static final String EVENT_DESC             = "Description";
	public static final String EVENT_LONG_DESC        = "Long Description";
	public static final String EVENT_UNKNOWN          = "Unknown";
	
	
/*	// RD: these constants are borrowed from DatbaseHelper.
	//static final String KEY_EVENTID       = "eventId";
	static final String KEY_TITLE           = "title";
	static final String KEY_STARTTIME       = "startTime";
	static final String KEY_ENDTIME         = "endTime";
	static final String KEY_TYPE            = "type";
	static final String KEY_LINK            = "link";
	static final String KEY_ORGANIZER       = "organizer";
	static final String KEY_LOCATION        = "location";
	static final String KEY_DESCRIPTION     = "description";
	static final String KEY_LONGDESCRIPTION = "longDescription";

	static public enum CRITERIA_KEY_ENUM { TITLE, TYPE, ORGANIZER, LOCATION, LINK, DESCRIPTION, STARTTIME, ENDTIME};
*/
		
//	public static final int RESULT_OK = 1;
	public static final int FIND_REQ = 1;
	public static final int FIND_TEXT_REQ = 2;
	public static final int FIND_TYPE_REQ = 3;
	public static final int FIND_DATE_REQ = 4;

	public static final String DATE_FORMAT = "MM/dd/yyyy - HH:mm"; // rob: See FindCriteria date selector

	/*
	 * This is equivalent to the enum in the FinderImpl class.
	 */

	// TODO decide on how to manage enum, or whether to use string resources.
	public enum FindKeyEnum {
		TYPE("type"), TITLE("title"), ORGANIZER("organizer"), LOCATION(
				"location"), DESCRIPTION("description"), LONGDESCRIPTION(
				"longDescription"), DATE("date");

		private final String key;

		FindKeyEnum(String key) {
			this.key = key;
		}

		// return string version of enumeration.
		private String key() {
			return key;
		}

		@Override
		public// return string version of enumeration.
		String toString() {
			return this.key();
		}
	}
}
