package com.tssg.eventboss2;

import java.util.List;

//import com.tssg.eventboss2.utils.R;
import com.tssg.eventsource.BELEvent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;


/*
 * class is instantiated  	
 *             in MainAppScreenImpl.setUp()
 *                 m_adapter = new EventListAdapter();
 */
class EventListAdapter extends BaseAdapter implements ListAdapter
{
	//	private static String m_TAG = tssg.eventboss.EBMainActivity.TAG;

	// Listeners for a) select this event and decorate it with quick action bar
	//               b) display a fancy list of the associated list item 
	//	ItemListener m_itemListener = null;
	//	ItemListener2 m_itemListener2 = null;

	EventListAdapter()
	{
		super();
	}

	List<BELEvent> m_events = null;

	public void setList( List<BELEvent> list )
	{
		m_events = list;
	}

	public int getCount()
	{
		// TODO Auto-generated method stub
		if ( m_events == null )
		{
			return 0;
		}

		return m_events.size();
	}

	public Object getItem(int arg0)
	{
		return String.format( "This is event %s",
				m_events.get( arg0).getTitle() );
	}

	public long getItemId(int arg0)
	{
		return arg0;
	}

	public View getView(int position, View reusable, ViewGroup group)
	{
		// TODO if we are passed a reusable, reuse it rather than
		// creating a new view.

		TextView textView = new TextView( group.getContext() );
		textView.setText( (String)getItem( position ) );

		//Create a view for the brief description of the data
		LayoutInflater layoutInflater = null;      
		layoutInflater = LayoutInflater.from( group.getContext() );

		final RelativeLayout briefEventView =
				(RelativeLayout) layoutInflater.inflate(R.layout.event_brief, null);

		// set the data for each field in the view.
		TextView fieldView = null;
		fieldView = (TextView) briefEventView.findViewById( R.id.name );
		fieldView.setText( m_events.get( position ).getTitle() );

		fieldView = (TextView) briefEventView.findViewById( R.id.time );
		fieldView.setText( m_events.get( position ).getStartTime() );

		fieldView = (TextView) briefEventView.findViewById( R.id.endtime );
		fieldView.setText( m_events.get( position ).getEndTime() );

		fieldView = (TextView) briefEventView.findViewById( R.id.location );
		fieldView.setText( m_events.get( position ).getLocation() );

		fieldView = (TextView) briefEventView.findViewById( R.id.url );
		fieldView.setText( m_events.get( position ).getLinkToGroup() );

		fieldView = (TextView) briefEventView.findViewById( R.id.eventtype );
		fieldView.setText( m_events.get( position ).getEventType() );

		fieldView = (TextView) briefEventView.findViewById( R.id.organizer );
		// fieldView.setText( m_events.get( position ).getOrganizer() );
		fieldView.setText( "" );

		fieldView = (TextView) briefEventView.findViewById( R.id.description );
		String str = m_events.get( position ).getDescription();
		fieldView.setText(str );

		return briefEventView;
	}
}
