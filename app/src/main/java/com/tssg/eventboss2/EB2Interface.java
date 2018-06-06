package com.tssg.eventboss2;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Resources;

import com.tssg.eventboss2.EB2MainActivity.event_list;
import com.tssg.eventsource.BELEvent;

import java.sql.Date;
import java.util.List;


// Interface for EB2MainActivity
public interface EB2Interface {

	boolean DEBUG();

	Resources getEB2Resources();

	Context getContext();

	int getFeedId();
	void setFeedId(int feedId);

	int getOldFeedId();
	public void setOldFeedId(int feedId);

	String getFeedName();

	Date getCurrentDate();
	void setCurrentDate(Date curDate);

	CurrentSectionFragment getCurrentData();

	List<BELEvent> getEventsListCacheEntry(int feedId);
	void setEventsListCacheEntry(int feedId, List<BELEvent> eventsList);
	void clearEventsListCacheEntry(int feedId);

	int getEventsListCacheSize();
	boolean EventsListCacheIsEmpty(int feedId);

	List<BELEvent> getCurrentEventsList();
	void setCurrentEventsList(List<BELEvent> webEventsList);

	Date getEventsListTime(int feedId);
	long getExpireTime();

	event_list getLastList();

	ActionBar getEB2ActionBar();

	String getTab0Label();
	void setTab0Label(String tabString);

	String getMainEventText();

	boolean ifReadingFromInternalFile();
	String getInternalFilePath();

	String getDBName();

}
