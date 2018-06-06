package com.tssg.find;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;

import android.util.Log;

import com.tssg.eventsource.BELEvent;

//TODO Consider using Enums for Event Typestypes
public class FindUtilsImpl implements FindUtils {

	// Parameters for the target of the find
	static class Target {
		static String field = null;
		static String value = null;
	}

	//@Override
	public ArrayList<BELEvent> getEventsByType(ArrayList<BELEvent> list,
			String type) {

		ArrayList<BELEvent> retList = new ArrayList<BELEvent>();
		ListIterator<BELEvent> it = (ListIterator<BELEvent>) list.iterator();
		String[] eventTypes = null;
		ArrayList<String> al = null;

		while (it.hasNext()) {
			BELEvent curEvent = it.next();
			String events = curEvent.getEventType();
			eventTypes = parseTypes(events);
			// add subset into the set to be returned
			al = (ArrayList<String>) Arrays.asList(eventTypes);
			if (al.contains(type.trim().toLowerCase(Locale.getDefault())))
				;
			retList.add(curEvent);
			Log.d("DEBUG", "Adding event: " + curEvent.getTitle());
		}

		Log.w("WARN", "No BEL Events of Type " + type + "found.");
		return retList;
	}

	//@Override
	/*
	 * Returns a set of all Types that exist in the event list. Types are
	 * treated case insensitive.
	 */
	public Set<String> getTypeSet(ArrayList<BELEvent> _eventList) {
		// TODO consider: Class EnumSet<E extends Enum<E>>

		String typeString;
		List<String> l = null;
		Set<String> eventSet = null;
		String[] eventTypes = null;
		eventSet = new HashSet<String>();
		Iterator<BELEvent> it = _eventList.listIterator();
//		Boolean addRes = false;

		BELEvent be = null;
		while (it.hasNext()) {
			be = it.next();
			typeString = be.getEventType();
			Log.w("DEBUG", typeString);
		}

		it = _eventList.listIterator();
		while (it.hasNext()) {
			be = it.next();
			typeString = be.getEventType();
			eventTypes = parseTypes(typeString);
			// add subset into the set to be returned
			l = new ArrayList<String>(Arrays.asList(eventTypes));
			if (l.isEmpty()) {
				Log.w("WARN", "BELEvent " + be.getTitle() + " has no Type");
			}
//			addRes = eventSet.addAll(l);
		}
		return eventSet;
	}

	// Some events have multiple Types provided as a comma separated list. This
	// function returns
	// that list as an array of strings, with each entry converted to lower case
	// form

	private String[] parseTypes(String types) {
		String[] ta = types.split(",");
		String hdr = null;

		// remove "Event type:" header from beginning of the string
		hdr = new String(ta[0]);
		int index = hdr.indexOf(':');
		ta[0] = (hdr.substring(index + 1));

		// remove whitespace from each element
		for (int i = 0; i < ta.length; i++) {
			ta[i] = ta[i].trim();
//			ta[i] = ta[i].toLowerCase();
		}
		return ta;
	}

	//@Override
	public Boolean hasEventType(ArrayList<BELEvent> list, String EventType) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setTargetField(String _field) {
		Target.field = _field;
		Log.d("DEBUG", "Target field set to " + _field);

	}

	public void setTargetValue(String _value) {
		Target.value = _value;
		Log.d("DEBUG", "Target value set to " + _value);
	}

	private String getTargetField() {
		return Target.field;
	}

	private String getTargetValue() {
		return Target.value;
	}

	public void resetTarget() {
		Target.field = null;
		Target.value = null;
	}

	// Hard coded for event Type only
	public List<BELEvent> getMatches(List<BELEvent> activeList) {

		String field = getTargetField();
		String value = getTargetValue();
		List<BELEvent> matches = new ArrayList<BELEvent>();

		BELEvent be = null;
		Iterator<BELEvent> it = activeList.listIterator();
		List<String> typeList = null;
		while (it.hasNext()) {
			be = (BELEvent) it.next();

			// final CharSequence[] fields = {"Event Type", "Location", "Time"};

			if (field.equals("Event Type")) {

				String types = be.getEventType(); // comma separated list of
													// types
				String[] typeArray = null;
				typeArray = parseTypes(types);

				typeList = Arrays.asList(typeArray);
				if (typeList.contains(value)) {
					matches.add(be);

				}

			}

			return matches;
		}
		return null;
	}

	//@Override
	public List<BELEvent> getMatches() {
		// TODO Auto-generated method stub
		return null;
	}
}
