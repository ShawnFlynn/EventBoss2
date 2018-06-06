package com.tssg.find;

import com.tssg.eventsource.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public interface FindUtils {
		public ArrayList<BELEvent> getEventsByType(ArrayList<BELEvent> list, String EventType);
		public Boolean  hasEventType(ArrayList<BELEvent> list, String EventType);
		public Set<String> getTypeSet(ArrayList<BELEvent> list);
		//public void setTargetField(String s);
		//public void setTargetValue(String s);
		//public void resetTarget();
		public List <BELEvent> getMatches();

}
