package com.tssg.eventsource;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.*;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.tssg.eventboss2.*;

import android.util.Log;
import android.util.Xml;


abstract class BaseFeedParser implements BELSourceForEvents {

	private final String TAG = getClass().getSimpleName();

	static final Integer CONNECT_TIMEOUT = 15;	// seconds
	static final Integer READ_TIMEOUT    = 15;	// seconds

	// Debug flag
	private static boolean DEBUG = RSSFeedReader.EB2.DEBUG();	// Global
	// private boolean DEBUG = true;	// Local

	boolean bLOGGING = false;	// enable/disable logging

	static final String ITEM = "item";
	static final String GUID = "guid";

	// names of the XML tags of interest
	static final  String DESCRIPTION = "description";
	static final  String LINK = "link";
	static final  String TITLE = "title";

	static final  String newline = System.getProperty("line.separator");

	/** matches the content p's in the item description */
	static final  Pattern P_MASTER = Pattern.compile("<p class=\"(\\w+)\">(.*?)</p>", Pattern.DOTALL);
	static final  Pattern DIV_MASTER = Pattern.compile("<div class = '(\\w+)'>(.*?)</div>", Pattern.DOTALL);

	private final URL feedUrl;

	private String file_path = RSSFeedReader.EB2.getInternalFilePath();

	/**
	 *
	 * @param feedUrl the URL for the feed, like "http://www.bostoneventslist.com/us/bo/events/rss.rxml"
	 *  @throws RuntimeException in response to MalformedURLException
	 */
	protected BaseFeedParser(String feedUrl) {
		Log.i(TAG, "BaseFeedParser()");

		try {
			this.feedUrl = new URL(feedUrl);
		} catch (MalformedURLException e) {
			String message = "Failed to get input stream from URL: " + feedUrl;
			Log.e( "BELSourceForEventsImpl", message, e );
			throw new RuntimeException(e);
		}
	}

	/**
	 *  @throws RuntimeException in response to IOException
	 */
	protected InputStream getInputStream() {
		Log.i(TAG, "getInputStream()");

		HttpURLConnection connection = null;
		InputStream inputStream = null;

		// Get the "Stored" tab label
		String storedTabLabel = RSSFeedReader.EB2.getEB2Resources().getString(R.string.Stored);

		try {
			if (RSSFeedReader.EB2.ifReadingFromInternalFile()){
				inputStream = readEventsFromFile();
				RSSFeedReader.EB2.setTab0Label(RSSFeedReader.EB2.getEB2Resources().getString(R.string.localFile));
			}
			else{
				URL url = getFeedUrl( );
				connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(CONNECT_TIMEOUT * 1000);
				connection.setReadTimeout(READ_TIMEOUT * 1000);
				connection.setRequestMethod("GET");
				connection.setRequestProperty("Accept", "application/xml");
				connection.setRequestProperty("Connection", "close");
				connection.connect();
				int statusCode = connection.getResponseCode();
				// connection = feedUrl.openConnection();	// might hang here due to firewall issues
				if (DEBUG)
					Log.d( TAG, "status code " + statusCode + " on url " + url);
				// Extremely slow here in debug mode on Windows 8.1 with Norton and Windows Defender; not an issue on Mac OSX.
				// Works ok for connected physical Nexus7
				inputStream = new java.io.BufferedInputStream(connection.getInputStream(), 65536);
			}
			return inputStream;
		} catch (java.net.SocketTimeoutException ste) {
			RSSFeedReader.EB2.setTab0Label(storedTabLabel);
			String message = "timeout opening URL: " + feedUrl + " (firewall issue?)";
			Log.e( TAG, message, ste );
			throw new RuntimeException( message, ste);
		} catch (FileNotFoundException e) {
			RSSFeedReader.EB2.setTab0Label(storedTabLabel);
			String message = "Failed to read events from the file: " + file_path;
			Log.e( TAG, message, e );
			throw new RuntimeException( message, e);
		} catch (IOException e) {
			RSSFeedReader.EB2.setTab0Label(storedTabLabel);
			String message = "Failed to get input stream from URL: " + feedUrl;
			Log.e( TAG, message, e );
			throw new RuntimeException( message, e);
		}

	}	//  end - getInputStream()

	protected URL getFeedUrl() {
		return feedUrl;
	}

	/** Read from {@link EB2MainActivity#internalFilePath} */
	private BufferedInputStream readEventsFromFile() throws FileNotFoundException {
		Log.i(TAG, "readEventsFromFile()");

		BufferedInputStream buf = null;

		if (file_path != null)
		{
			try {
				buf = new BufferedInputStream(new FileInputStream(file_path));
			} catch (FileNotFoundException e) {
				Log.e(TAG, "readEventsFromFile cannot find "
						+ file_path, e);
				throw e;
			}
		}
		return buf;
	}

	// for time like 2014-06-06 - 07:30 PM */
	static final String timepat = "(\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d)\\s+-\\s+(\\d\\d):(\\d\\d)\\s([A|P]M)";
	static final String timeFmt = "yyyy-MM-dd - hh:mm aa";
	static final int timeFmtLen = timeFmt.length();
	// for time like 2014-06-06 19:30:00 */
	static final String time24pat = "(\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d)\\s+(\\d\\d):(\\d\\d):(\\d\\d)";
	static final String time24Fmt = "yyyy-MM-dd HH:mm:ss";
	static final int time24FmtLen = time24Fmt.length();
	// want to match {@literal Time : 2014-06-06 - 07:30 PM}
	static final Pattern P_START2 = Pattern.compile(timepat);
	static final Pattern P_ENDING2 = Pattern.compile(timepat);
	// want to match {@literal Time : 2014-06-06 19:30:00}
	static final Pattern P_24START2 = Pattern.compile(time24pat);
	static final Pattern P_24ENDING2 = Pattern.compile(time24pat);

	/** parse out Time. Consider a weakhashmap for times already parsed */
	static java.util.Date parseEventTime(Pattern pat, String format, String xmlString) {
		String TAG = "BaseFeedParser";

		if (DEBUG)
			Log.i(TAG, "parseEventTime()");

		final Matcher matcher = pat.matcher(xmlString);
		java.util.Date retval = null;
		try  {
			if (matcher.find() ) {
				java.text.SimpleDateFormat fmt1 = new java.text.SimpleDateFormat(format);
				retval = fmt1.parse(matcher.group(0));
			}
		} catch (IllegalStateException  excp) {
			Log.e(TAG, xmlString, excp);
		} catch (ParseException ex) {
			Log.e(TAG, xmlString, ex);
		}
		return retval;
	}

}	//  end - BaseFeedParser class


/** A bunch of things changed in the feed XML in May 2014.
 * Methods protected in case we want to subclass for different XML formats.
 */
public class BELSourceForEventsImpl extends BaseFeedParser {

	protected final String TAG = getClass().getSimpleName();

	// Debug flag
	private static boolean DEBUG = RSSFeedReader.EB2.DEBUG();	// Global
	// private boolean DEBUG = true;	// Local

	private RSSFeedReader currentTask = null;

	public BELSourceForEventsImpl(String feedUrl, RSSFeedReader currentTask) {
		super(feedUrl);
		Log.i(TAG, "BELSourceForEventsImpl()");
		this.currentTask = currentTask;
	}

	/** Parse the RSS feed stream (slow)
	 * @return list of messages from XML
	 *  @throws RuntimeException in response to any Exception
	 */
	public List<BELEvent> getCurrentEventList() {

		Log.i(TAG, "getCurrentEventList()");

		List<BELEvent> messages = java.util.Collections.emptyList();
		InputStream inputStream = null;
		// android.os.Debug.startMethodTracing();
		try {
			/*
			 * if m_mainEventText has no filename
			 * 		read from an XML file (a snapshot from the RSS feed)
			 * 	else
			 * 		read from the RSS feed (needs a valid URL)
			 */
			String xml = RSSFeedReader.EB2.getMainEventText();
			if( xml == null)  {
				String feedName = RSSFeedReader.EB2.getFeedName();
				// for URL source'd Eventlist
				// the input stream is read in the base class of this.
				if (DEBUG)
					Log.d(TAG, "EventSource,"
							+ " long pause (4 minutes?)"
							+ " while we get the network feed for "
							+ feedName);
				inputStream = this.getInputStream();
				if (DEBUG)
					Log.d(TAG, "EventSource, next, parse the network feed for "
							+ feedName);
				messages = pullparse(inputStream);
				if (DEBUG)
					Log.d(TAG, "parsed the feed stream for "
							+ feedName);
			}
			else {
				if (DEBUG)
					Log.d(TAG,  "parsing the feed string" );
				RSSFeedReader.EB2.setTab0Label(RSSFeedReader.EB2.getEB2Resources().getString(R.string.assetFile));
				messages = pullparse(xml);
				if (DEBUG)
					Log.d(TAG,  "parsed the feed string" );
			}

		} catch (java.net.ConnectException excp) {	// handle network disconnection
			String s = excp.getMessage();
			if ( s == null ) {
				s = "*";
			}
			Log.e(TAG, "ConnectException on "
						+ super.getFeedUrl()
						+ " "  + s, excp);
			throw new RuntimeException(excp);
		}  catch (XmlPullParserException excp) {
			String ss = excp.getMessage();
			if ( ss == null ) {
				ss= "*";
			}
			Log.e(TAG, "", excp);
			return messages;	// drop it, return what we have so far
		}	catch (IOException excp) {
			Log.e(TAG, "IOException on "
					+ super.getFeedUrl() + ": "
					+ excp.getMessage(), excp);
			; // drop it
		}  catch (Exception e) {
			String s = e.getMessage();
			if ( s == null ) {
				s = "*";
			}
			Log.e(TAG, ""
					+ super.getFeedUrl()
					+ ": " + s, e);
			throw new RuntimeException(e);
		} finally {
			// android.os.Debug.stopMethodTracing();
			try {
				if (inputStream != null)
					inputStream.close();
			} catch (IOException excp) {
				; /* drop it */ }
		}
		return messages;

	}	// end - getCurrentEventList()


/** Formerly:  &lt;p class="pDate"&gt;
*       Time : 2012-01-25 Sat Jan 01 08:00:00 UTC 2000
*       Ending : 2012-01-25 Sat Jan 01 08:00:00 UTC 2000 - Eastern Time (US & Canada)
*
* <p> on the web-site, the date is formatted like this:   Jan 20, 2012 02:00 pm,
*		the time appears to be 24-hour format.
* <p>there is also a timezone given, but it is not in the "pDate" section.
*/

	static final String startLabel = "Time : ", endLabel = "Ending : ";
	static final int startLabelLen = startLabel.length();
	static final int endLabelLen = endLabel.length();
	static final int maxDescr = 100;	// limit short description to 100 chars

	// Parsing routines:   pick out of the data-stream	all elements needed for our display

	/** null constant? */
	private static final String ns = null;
	private static final String RSS = "rss";
	private static final String CHANNEL = "channel";

	/** try using pull parser, which is recommended at developer.android.com.
	 * @see <a href="http://developer.android.com/training/basics/network-ops/xml.html">developer.android.com</a>
	 */
	protected List<BELEvent>  pullparse(InputStream in)
			throws XmlPullParserException, IOException {
		Log.i(TAG, "pullparse(InputStream)");

		List<BELEvent>  retval= java.util.Collections.<BELEvent>emptyList();
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			retval= readFeed(parser);
		} catch (XmlPullParserException excp) {
			String s = excp.getMessage();
			if ( s == null ) {
				s = "*";
			}
			Log.e(TAG, "XmlPullParserException on "
						+ super.getFeedUrl()
						+ " " + s);
			return retval;
		} catch (java.net.ConnectException excp) {
			Log.e(TAG, "ConnectException on " + super.getFeedUrl(), excp);
			throw new RuntimeException(excp);
		} catch (IOException excp) {	// handle network disconnection
			Log.e(TAG, "IOException on " + super.getFeedUrl(), excp);
			throw new RuntimeException(excp);
		} finally {
			in.close();
		}
		return retval;	// if we get this far

	}	//  end - pullParse(InputStream)

	/** try using pull parser on string, which is recommended at developer.android.com.
	 * @see <a href="http://developer.android.com/training/basics/network-ops/xml.html">developer.android.com</a>
	 */
	protected List<BELEvent> pullparse(String strin)
			throws XmlPullParserException, IOException {
		Log.i(TAG, "pullparse(String)");

		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(new StringReader(strin));
			parser.nextTag();
			return readFeed(parser);
		} catch (java.net.ConnectException excp) { // handle network disconnection
			String s = excp.getMessage();
			if ( s == null ) {
				s = "*";
			}
			Log.e(TAG, "ConnectException on "
						+ super.getFeedUrl()
						+ " " + s, excp);
			throw new RuntimeException(excp);
		} catch (XmlPullParserException excp) { // handle network disconnection
			String s = excp.getMessage();
			if ( s == null ) {
				s = "*";
			}
			if (DEBUG)
				Log.d(TAG, "XmlPullParserException on " + super.getFeedUrl() + " " + s, excp);
			throw new RuntimeException(excp);
		} catch (IOException excp) {	// handle network disconnection
			String s = excp.getMessage();
			if ( s == null ) {
				s = "*";
			}
			Log.e(TAG, "IOException on "
						+ super.getFeedUrl()
						+ " " + s, excp);
			throw new RuntimeException(excp);
		}
	}	//  end - pullParse(String)

	/** High-level parse of the rxml feed.
	 *
	 * @param parser the {@link XmlPullParser} we're processing
	 * @return List of parsed events
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	protected List<BELEvent> readFeed(XmlPullParser parser)
										throws XmlPullParserException,
										IOException {

		if (DEBUG)
			Log.i(TAG, "readFeed()");

		ArrayList<BELEvent> messages = new ArrayList<BELEvent>(30);

		try {
			parser.require(XmlPullParser.START_TAG, ns, RSS);
			while (parser.next() != XmlPullParser.END_TAG) {
				if (parser.getEventType() != XmlPullParser.START_TAG) {
					continue;
				}
				String name = parser.getName();
				if (name.equals(CHANNEL)) {
					List<BELEvent> temp = readChannel(parser);
					messages.addAll(temp);
				} else {
					skip(parser);
				}
			}
			parser.require(XmlPullParser.END_TAG, ns, RSS);
		} catch (XmlPullParserException excp) {
			; // drop it and return what we have
		} catch (IOException excp) {
			Log.e(TAG, "IOException on " + super.getFeedUrl(), excp);
			// return what we have so far
		}
		messages.trimToSize();
		return messages;

	}	//  end - readFeed()

	/** read the {@literal <channel>} from the rxml feed.
	 * The channel contains top-level title/description, and a list of events.
	 *
	 * @param parser the {@link XmlPullParser} we're processing
	 * @return List of parsed events
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	protected List<BELEvent> readChannel(XmlPullParser parser)
			throws IOException, XmlPullParserException {

		if (DEBUG)
			Log.i(TAG, "readChannel()");

		ArrayList<BELEvent> messages = new ArrayList<BELEvent>(30);
		int ii = 0;
		try {
			parser.require(XmlPullParser.START_TAG, ns, CHANNEL);
			while (parser.next() != XmlPullParser.END_TAG) {
				if (parser.getEventType() != XmlPullParser.START_TAG) {
					continue;
				}
				String name = parser.getName();

				if (name.equals(ITEM)) {	// should be multiple of these
					messages.add(readItem(parser));
					this.currentTask.publicProgressCallback(++ii, 0, 1 );
				} else if (name.equals(TITLE)) {
					readChannelTitle(parser);	// read and discard channel title
				} else if (name.equals(DESCRIPTION)) {
					readDescription(parser);	// read and discard channel description
				} else if (name.equals(LINK)) {
					readLink(parser);		// read and discard channel link
				} else {
					skip(parser);
				}
			}
			parser.require(XmlPullParser.END_TAG, ns, CHANNEL);
		} catch (XmlPullParserException excp) {
			Log.e(TAG, "XmlPullParserException on item "
							+ ii + " "
							+ excp.getMessage() );
			// drop it and return what we have
		} catch (IOException excp) {
			Log.e(TAG, "IOException on item " + ii + " " + excp.getMessage() );
			// drop it and return what we have
		}
		messages.trimToSize();
		return messages;

	}	//  end - readChannel()

	/** Since May 2014
	 * <p>read contents of a {@literal <description><div class='...'>} from the rss.xml feed.
	 *  div has many instance, and the class attribute and some parsing tell what's contained.
	 * This is the details of the event.
	 *
	 * @param parser the {@link XmlPullParser} we're processing
	 * @param retval is IN/OUT, constructed in chain-of-responsibility or builder fashion
	 * @return its retval argument
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	protected BELEvent readDiv(String text, BELEvent retval) {

		if (DEBUG)
			Log.i(TAG, "readDiv()");

		String divClass;
		String divContent;
		final SimpleDateFormat simpFormat =
				new SimpleDateFormat(BELEvent.simpDatePatternStored,
				Locale.getDefault());
		final Matcher matcher = DIV_MASTER.matcher(text);
		while (matcher.find()) {
			divClass =matcher.group(1);
			divContent = matcher.group(2).trim();
			final int contentLen = divContent.length();
			String startSnippet = null, endSnippet = null;
			if ("timings".equals(divClass)) {
				java.util.Date eventStartDate = null, eventEndDate = null;
				int startAt = divContent.indexOf(startLabel);
				int endAt   = divContent.indexOf(endLabel);
				if (-1 < startAt && contentLen >= startAt+startLabelLen) {
					startSnippet = divContent.substring(startAt+startLabelLen,
														startAt+startLabelLen +
														timeFmt.length());
					eventStartDate = BaseFeedParser.parseEventTime(P_START2, timeFmt, startSnippet);
					// 12 hour format?
					if (eventStartDate == null) {
						// Check for 24 hour format
						startSnippet = divContent.substring(startAt+startLabelLen,
															startAt+startLabelLen +
															time24Fmt.length());
						eventStartDate = BaseFeedParser.parseEventTime(P_24START2, time24Fmt, startSnippet);
					}
					if (eventStartDate != null) {
						retval.setStartDate(eventStartDate);
						retval.setStartTime(simpFormat.format(eventStartDate));
					}
				}
				if (-1 < endAt && contentLen >= endAt+endLabelLen) {
					endSnippet = divContent.substring(endAt + endLabelLen,
													  endAt + endLabelLen +
													  timeFmt.length());
					eventEndDate = BaseFeedParser.parseEventTime(P_ENDING2, timeFmt, endSnippet);
					// 12 hour format?
					if (eventEndDate == null) {
						// Check for 24 hour format
						endSnippet = divContent.substring(endAt + endLabelLen,
														  endAt + endLabelLen +
														  time24Fmt.length());
						eventEndDate = BaseFeedParser.parseEventTime(P_24ENDING2, time24Fmt, endSnippet);
					}
					if (eventEndDate != null) {
						retval.setEndDate(eventEndDate);
						retval.setEndTime(simpFormat.format(eventEndDate));
					}
				}
			} else if ("location".equals(divClass)  && contentLen >= 16) {
				String location = divContent.substring(16).trim();
				if (location.endsWith("-")) {
					location = location.substring(0, location.length()-1).trim();
				}
				retval.setLocation(location);
			} else if ("venue".equals(divClass) && contentLen >= 13) {
				String location = divContent.substring(13).trim();
				if (location.endsWith("-")) {
					location = location.substring(0, location.length()-1).trim();
				}
				if (!location.isEmpty()) {
					// prepend with delimiter
					retval.setLocation(location + ": " + retval.getLocation());
				}
			} else if ("type".equals(divClass) && contentLen > 19) {
				String eventType = divContent.substring(19).trim();
				retval.setEventType(eventType);
			} else if ("description2".equals(divClass)) {
				// KLUGE, doing both
				retval.setDescription(divContent);
				retval.setLongDescription(divContent);
				String str = divContent;
				if (null != divContent && !divContent.isEmpty()) {
					int len = str.length();
					if (len > maxDescr)
						str = str.substring(0, maxDescr) + " ...";
					str = str.replaceAll("\n+", " - ");
					retval.setDescription(str);
				} else {
					retval.setDescription("?");
				}
			} else if ("organizer".equals(divClass) && contentLen > 17) {
				// this usually has hex junk */
				retval.setOrganizer(divContent.substring(17));
			} else {
				;
			}
		}

		return retval;

	}	//  end - readDiv()

	/** read the {@literal <channel><title>} from the rxml feed; a string-valued entity
	 *
	 * @param parser the {@link XmlPullParser} we're processing
	 * @return string within channel title tags
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	protected String readChannelTitle(XmlPullParser parser) throws IOException,
														XmlPullParserException {

		if (DEBUG)
			Log.i(TAG, "readChannelTitle()");

		parser.require(XmlPullParser.START_TAG, ns, TITLE);
		String title = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, TITLE);
		return title;
	}

	/** read the {@literal <item><title>} from the rxml feed; the event title
	 *
	 * @param parser the {@link XmlPullParser} we're processing
	 * @return string within item title tags
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	protected String readItemTitle(XmlPullParser parser) throws IOException,
														XmlPullParserException {

		if (DEBUG)
			Log.i(TAG, "readItemTitle()");

		parser.require(XmlPullParser.START_TAG, ns, TITLE);
		String title = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, TITLE);
		return title;
	}

	/** read the {@literal <channel><description>} from the rxml feed;
	 *
	 * @param parser the {@link XmlPullParser} we're processing
	 * @return string within item title tags
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	protected String readDescription(XmlPullParser parser) throws IOException,
														XmlPullParserException {

		if (DEBUG)
			Log.i(TAG, "readDescription()");

		parser.require(XmlPullParser.START_TAG, ns, DESCRIPTION);
		String description = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, DESCRIPTION);
		return description;
	}

	/** read the {@literal <channel><link>} from the rxml feed.
	 *
	 * @param parser the {@link XmlPullParser} we're processing
	 * @return string within link tags, which should be a URL
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	protected String readLink(XmlPullParser parser) throws IOException,
														XmlPullParserException {

		if (DEBUG)
			Log.i(TAG, "readLink()");

		parser.require(XmlPullParser.START_TAG, ns, LINK);
		String link = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, LINK);
		return link;
	}

	/** read the {@literal <item><link>} from the rxml feed.
	 *
	 * @param parser the {@link XmlPullParser} we're processing
	 * @return string within link tags, which should be a URL
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	protected String readItemLink(XmlPullParser parser) throws IOException,
														XmlPullParserException {

		if (DEBUG)
			Log.i(TAG, "readItemLink()");

		parser.require(XmlPullParser.START_TAG, ns, LINK);
		String link = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, LINK);
		return link;
	}

	/** read the {@literal <item><guid>} from the rxml feed.
	 *
	 * @param parser the {@link XmlPullParser} we're processing
	 * @return string within guid tags, which should be a URL
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	protected String readGuidLink(XmlPullParser parser) throws IOException,
														XmlPullParserException {

		if (DEBUG)
			Log.i(TAG, "readGuidLink(msg)");

		parser.require(XmlPullParser.START_TAG, ns, GUID);
		String link = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, GUID);
		return link;
	}

	/** extract text content */
	protected String readText(XmlPullParser parser) throws IOException,
														XmlPullParserException {

		if (DEBUG)
			Log.i(TAG, "readText()");

		String result = "";
		try {
			if (parser.next() == XmlPullParser.TEXT) {
				result = parser.getText();
				parser.nextTag();
			}
		} catch (IOException excp) {
			Log.e(TAG, "Got IOException " + excp.getMessage());
		} catch (XmlPullParserException excp) {
			Log.e(TAG, "Got XmlPullParserException " + excp.getMessage());
		}
		return result;
	}

	/** Read an event item from the rxml feed.
	 *
	 * @param parser the {@link XmlPullParser} we're processing
	 * @return parsed event
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	protected BELEvent readItem(XmlPullParser parser)
												throws XmlPullParserException,
														IOException {

		if (DEBUG)
			Log.i(TAG, "readItem()");

		String title = null;
		String linkStr = null;
		String sGUID = null;

		BELEvent retval = new BELEvent();

		try {
			parser.require(XmlPullParser.START_TAG, ns, ITEM);
			while (parser.next() != XmlPullParser.END_TAG) {
				if (parser.getEventType() != XmlPullParser.START_TAG) {
					continue;
				}
				String name = parser.getName();

				if (TITLE.equals(name)) {
					title = readItemTitle(parser);
					retval.setTitle(title);
				} else if (DESCRIPTION.equals(name)) {
					readItemDescription(parser, retval);	// retval is IN/OUT
				} else if (LINK.equals(name)) {
					linkStr = readItemLink(parser);
					// not sure which of these to use, so use both
					retval.setLinkToGroup(linkStr);
				} else if (GUID.equals(name)) {
					sGUID = readGuidLink(parser);
				} else {
					skip(parser);
				}
			}
			parser.require(XmlPullParser.END_TAG, ns, ITEM);
			retval.setId(sGUID);	// depends on the <guid> data
		} catch (XmlPullParserException excp) {
			Log.e(TAG, "Got XmlPullParserException " + excp.getMessage());
		}

		return retval;

	}	//  end - readItem()

	/**
	 * before May 2014
	 * <p>read contents of a {@literal <description><p class='...'>} from the rxml feed.
	 *  p has many instance, and the class attribute and some parsing tell what's contained.
	 * This is the details of the event.
	 *
	 * @param parser the {@link XmlPullParser} we're processing
	 * @param retval is IN/OUT, constructed in chain-of-responsibility or builder fashion
	 * @return its retval argument
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	protected BELEvent readP(String text, BELEvent retval){

		if (DEBUG)
			Log.i(TAG, "readP()");

		String pType;
		String content;
		final Matcher matcher = P_MASTER.matcher(text);
		while (matcher.find()) {
			pType =matcher.group(1);
			content = matcher.group(2).trim();
			if ("pDate".equals(pType)) {
				/*
				String eventStartTime = parseEventTime( content);
				String eventEndTime = parseEventTime( content);
				retval.setStartTime(eventStartTime);
				if (null != eventEndTime && !eventEndTime.isEmpty()) {
				retval.setEndTime(eventEndTime);
			}
				 */
			} else if ("pContent".equals(pType)) {
				/* parse out "Event Type :	Business,
											Entrepreneurship",
											"<b>Location:</b>"
				*/
				if (content.trim().startsWith("Keywords :")) {
					// Much of the time, this will be an empty array
					// String[] keywords = text.substring(10).trim().split(",");
					;	// ignore
				} else if (content.startsWith("<b>Location:</b>")) {
					String location = content.substring(16).trim();
					if (location.endsWith("-")) {
						location = location.substring(0,
													location.length()-1).trim();
					}
					retval.setLocation(location);
				} else if (content.startsWith("Event Type :")) {
					String eventType = content.substring(12).trim();
					retval.setEventType("Event type: " + eventType);
				}
			} else if ("pContentDesc".equals(pType)) {
				;	// ignore
			} else if ("pContentDescText".equals(pType)) {
				retval.setLongDescription(content);
			} else if (content.startsWith("<b>Organizer:</b>")) {
				retval.setOrganizer(content.substring(17));
			} else {
				;
			}
		}

		return retval;

	}	//  end - readP()

	/** read the {@literal <item><description>} from the rxml feed.
	 *  This has many p subelements with the meat of the event.
	 *
	 * @param parser the {@link XmlPullParser} we're processing
	 * @param retval is IN/OUT, constructed in chain-of-responsibility or builder fashion
	 * @return its retval argument
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	protected BELEvent readItemDescription( XmlPullParser parser,
											BELEvent retval)
											throws XmlPullParserException,
											IOException {

		if (DEBUG)
			Log.i(TAG, "readItemDescription()");

		parser.require(XmlPullParser.START_TAG, ns, DESCRIPTION);
		String text = readText(parser);
		text = text.replaceAll("<br />", " ");
		readDiv(text, retval);
		parser.require(XmlPullParser.END_TAG, ns, DESCRIPTION);
		return retval;
	}

	/** Skip around tags.
	 * From http://developer.android.com/training/basics/network-ops/xml.html
	 */
	protected void skip(XmlPullParser parser) throws XmlPullParserException,
																IOException {

		if (DEBUG)
			Log.i(TAG, "skip()");

		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
				case XmlPullParser.END_TAG:
					depth--;
					break;
				case XmlPullParser.START_TAG:
					depth++;
					break;
			}
		}
	}

}	//	end - BELSourceForEvents class
