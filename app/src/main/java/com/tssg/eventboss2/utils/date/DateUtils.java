/**
 * 
 */
package com.tssg.eventboss2.utils.date;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.util.Log;


/** <h3>Various date-parsing methods:</h3>
 * <dl>
 * <dt>{@link #parseRFC822}</dt><dd>Handle RFC822 format</dd>
 * <dt>{@link #parseW3CDateTime}</dt><dd>Handle W3C format</dd>
 * <dt>{@link #parseDate}</dt><dd>Handle other formats</dd>
 * <dt>{@link #formatRFC822}</dt><dd>format the RFC822 way</dd> 
 * <dt>{@link #formatW3CDateTime}</dt><dd>format the W3C way</dd> 
 * </dl>
 * @author Kathy
 *
 */
public class DateUtils {
	private static String[] ADDITIONAL_MASKS = {
		"yyyy-MM-dd '2000-01-01' HH:mm:ss 'UTC'",		// this is what we're seeing 5/2014
		"MM/dd/yyyy HH:mm",
		"MMM d, yyyy hh:mm aaa", 
		"yyyy-MM-dd HH:mm",
		"yyyy-MM-dd HH:mm:ss",
		"EE, dd MMM yyyy HH:mm:ss",
		"EE, dd MMM yyyy HH:mm:ss z",
		"yyyy-MM-dd'T'HH:mm:ss" 
	};

	// order is like this because the SimpleDateFormat.parse does not fail with exception
	// if it can parse a valid date out of a substring of the full string given the mask
	// so we have to check the most complete format first, then it fails with exception
	private static final String[] RFC822_MASKS = {
		"EEE, dd MMM yy HH:mm:ss z",
		"EEE, dd MMM yy HH:mm z",
		"dd MMM yy HH:mm:ss z",
		"dd MMM yy HH:mm z"
	};



	// order is like this because the SimpleDateFormat.parse does not fail with exception
	// if it can parse a valid date out of a substring of the full string given the mask
	// so we have to check the most complete format first, then it fails with exception
	private static final String[] W3CDATETIME_MASKS = {
		"yyyy-MM-dd'T'HH:mm:ss.SSSz",
		"yyyy-MM-dd't'HH:mm:ss.SSSz",
		"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
		"yyyy-MM-dd't'HH:mm:ss.SSS'z'",
		"yyyy-MM-dd'T'HH:mm:ssz",
		"yyyy-MM-dd't'HH:mm:ssz",
		"yyyy-MM-dd'T'HH:mm:ssZ",
		"yyyy-MM-dd't'HH:mm:ssZ",
		"yyyy-MM-dd'T'HH:mm:ss'Z'",
		"yyyy-MM-dd't'HH:mm:ss'z'",
		"yyyy-MM-dd'T'HH:mmz",   // together with logic in the parseW3CDateTime they
		"yyyy-MM'T'HH:mmz",      // handle W3C dates without time forcing them to be GMT
		"yyyy'T'HH:mmz",          
		"yyyy-MM-dd't'HH:mmz", 
		"yyyy-MM-dd'T'HH:mm'Z'", 
		"yyyy-MM-dd't'HH:mm'z'", 
		"yyyy-MM-dd",
		"yyyy-MM",
		"yyyy"
	};



	/**
	 * The masks used to validate and parse the input to this Atom date.
	 * These are a lot more forgiving than what the Atom spec allows.  
	 * The forms that are invalid according to the spec are indicated.
	 */
	//TODO figure out if we need this
	@SuppressWarnings("unused")
	private static final String[] masks = {
		"yyyy-MM-dd'T'HH:mm:ss.SSSz",
		"yyyy-MM-dd't'HH:mm:ss.SSSz",                         // invalid
		"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
		"yyyy-MM-dd't'HH:mm:ss.SSS'z'",                       // invalid
		"yyyy-MM-dd'T'HH:mm:ssz",
		"yyyy-MM-dd't'HH:mm:ssz",                             // invalid
		"yyyy-MM-dd'T'HH:mm:ss'Z'",
		"yyyy-MM-dd't'HH:mm:ss'z'",                           // invalid
		"yyyy-MM-dd'T'HH:mmz",                                // invalid
		"yyyy-MM-dd't'HH:mmz",                                // invalid
		"yyyy-MM-dd'T'HH:mm'Z'",                              // invalid
		"yyyy-MM-dd't'HH:mm'z'",                              // invalid
		"yyyy-MM-dd",
		"yyyy-MM",
		"yyyy"
	};

	/**
	 * Parses a Date out of a string using an array of masks.
	 * <p/>
	 * It uses the masks in order until one of them succeeds or all fail.
	 * <p/>
	 *
	 * @param masks array of masks to use for parsing the string
	 * @param sDate string to parse for a date.
	 * @return the Date represented by the given string using one of the given masks.
	 * It returns <b>null</b> if it was not possible to parse the the string with any of the masks.
	 *
	 */
	private static Date parseUsingMask(String[] masks,String sDate) {
		sDate = (sDate!=null) ? sDate.trim() : null;
		ParsePosition pp = null;
		Date d = null;
		for (int i=0;d==null && i<masks.length;i++) {
			DateFormat df = new SimpleDateFormat(masks[i],Locale.getDefault());
			//df.setLenient(false);
			df.setLenient(true);
			try {
				pp = new ParsePosition(0);
				d = df.parse(sDate,pp);
				if (pp.getIndex()!=sDate.length()) {
					d = null;
				}
				//System.out.println("pp["+pp.getIndex()+"] s["+sDate+" m["+masks[i]+"] d["+d+"]");
			}
			catch (Exception ex1) {
				//System.out.println("s: "+sDate+" m: "+masks[i]+" d: "+null);
			}
		}
		return d;
	}

	/**
	 * Parses a Date out of a String with a date in RFC822 format.
	 * <p/>
	 * It parsers the following formats:
	 * <ul>
	 *   <li>"EEE, dd MMM yyyy HH:mm:ss z"</li>
	 *   <li>"EEE, dd MMM yyyy HH:mm z"</li>
	 *   <li>"EEE, dd MMM yy HH:mm:ss z"</li>
	 *   <li>"EEE, dd MMM yy HH:mm z"</li>
	 *   <li>"dd MMM yyyy HH:mm:ss z"</li>
	 *   <li>"dd MMM yyyy HH:mm z"</li>
	 *   <li>"dd MMM yy HH:mm:ss z"</li>
	 *   <li>"dd MMM yy HH:mm z"</li>
	 * </ul>
	 * <p/>
	 * Refer to the java.text.SimpleDateFormat javadocs for details on the format of each element.
	 * <p/>
	 * @param sDate string to parse for a date.
	 * @return the Date represented by the given RFC822 string.
	 *         It returns <b>null</b> if it was not possible to parse the given string into a Date.
	 *
	 */
	public static Date parseRFC822(String sDate) {
		int utIndex = sDate.indexOf(" UT");
		if (utIndex>-1) {
			String pre = sDate.substring(0,utIndex);
			String post = sDate.substring(utIndex+3);
			sDate = pre + " GMT" + post;
		}
		return parseUsingMask(RFC822_MASKS,sDate);
	}


	/**
	 * Parses a Date out of a String with a date in W3C date-time format.
	 * <p/>
	 * It parsers the following formats:
	 * <ul>
	 *   <li>"yyyy-MM-dd'T'HH:mm:ssz"</li>
	 *   <li>"yyyy-MM-dd'T'HH:mmz"</li>
	 *   <li>"yyyy-MM-dd"</li>
	 *   <li>"yyyy-MM"</li>
	 *   <li>"yyyy"</li>
	 * </ul>
	 * <p/>
	 * Refer to the java.text.SimpleDateFormat javadocs for details on the format of each element.
	 * <p/>
	 * @param sDate string to parse for a date.
	 * @return the Date represented by the given W3C date-time string.
	 *         It returns <b>null</b> if it was not possible to parse the given string into a Date.
	 *
	 */
	public static Date parseW3CDateTime(String sDate) {
		// if sDate has time on it, it injects 'GTM' before de TZ displacement to
		// allow the SimpleDateFormat parser to parse it properly
		int tIndex = sDate.indexOf("T");
		if (tIndex>-1) {
			if (sDate.endsWith("Z")) {
				sDate = sDate.substring(0,sDate.length()-1)+"+00:00";
			}
			int tzdIndex = sDate.indexOf("+",tIndex);
			if (tzdIndex==-1) {
				tzdIndex = sDate.indexOf("-",tIndex);
			}
			if (tzdIndex>-1) {
				String pre = sDate.substring(0,tzdIndex);
				int secFraction = pre.indexOf(",");
				if (secFraction>-1) {
					pre = pre.substring(0,secFraction);
				}
				String post = sDate.substring(tzdIndex);
				sDate = pre + "GMT" + post;
			}
		}
		else {
			sDate += "T00:00GMT";
		}
		return parseUsingMask(W3CDATETIME_MASKS,sDate);
	}


	/**
	 * Parses a Date out of a String with a date in W3C date-time format or
	 * in a RFC822 format.
	 * Mostly want to parse 
	 * <pre>"Time : 2014-05-16 2000-01-01 07:30:00 UTC Ending : - Eastern Time (US & Canada) "</pre>
	 * 
	 * @param sDate string to parse for a date.
	 * @return the Date represented by the given W3C date-time string.
	 *         It returns <b>null</b> if it was not possible to parse the given string into a Date.
	 *
	 * */
	public static Date parseDate(String sDate) {
		if (sDate == null) {
			return null; 
		}
		Date ddd = null;
		final SimpleDateFormat simpFormat1 = new SimpleDateFormat("yyyy-MM-dd 2000-01-01 HH:mm:ss", Locale.getDefault());
		/** need to handle 05/29/2014 09:00 */
		final SimpleDateFormat simpFormat2 = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault());

		try {
			ddd = simpFormat1.parse(sDate);
		} catch (ParseException excp) {
			if (null == ddd) {
				try {
					ddd = simpFormat2.parse(sDate);
				} catch (ParseException excp2) {
					Log.i("DateUtils", "parseDate needs to handle '" + sDate + "'", excp2);
				}
				if (null == ddd) {
					ddd = parseW3CDateTime(sDate);
					if (ddd==null) {
						ddd = parseRFC822(sDate);
						if (ddd==null && ADDITIONAL_MASKS.length>0) {
							ddd = parseUsingMask(ADDITIONAL_MASKS,sDate);
						}
					}
				}
			}
		}
		return ddd;
	}

	/**
	 * create a RFC822 representation of a date.
	 * <p/>
	 * Refer to the java.text.SimpleDateFormat javadocs for details on the format of each element.
	 * <p/>
	 * @param date Date to parse
	 * @return the RFC822 represented by the given Date
	 *         It returns <b>null</b> if it was not possible to parse the date.
	 *
	 */
	public static String formatRFC822(Date date) {
		SimpleDateFormat dateFormater = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'",Locale.getDefault());
		dateFormater.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormater.format(date);
	}

	/**
	 * create a W3C Date Time representation of a date.
	 * <p/>
	 * Refer to the java.text.SimpleDateFormat javadocs for details on the format of each element.
	 * <p/>
	 * @param date Date to parse
	 * @return the W3C Date Time represented by the given Date
	 *         It returns <b>null</b> if it was not possible to parse the date.
	 *
	 */
	public static String formatW3CDateTime(Date date) {
		SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.getDefault());
		dateFormater.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormater.format(date);
	}

}
