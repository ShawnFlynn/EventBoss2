package com.tssg.find;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.tssg.eventboss2.R;
import com.tssg.eventboss2.utils.misc.MakeToast;

/*
 * Displays a list of the type that exist in the list subject to the Find.
 */

public class EventTypeList extends ListActivity {
	/**
	 * Fields to contain the current position and display contents of the
	 * spinner
	 */
	protected int mPos;
	protected String mSelection;
	protected Intent mResult;
	private ListView lv = null;
	private static LayoutInflater mLayoutInflater = null;

	/**
	 * ArrayAdapter connects the spinner widget to array-based data.
	 */
	protected ArrayAdapter<CharSequence> mAdapter;

	/** Called when the activity is first created. */
	@SuppressLint("InflateParams")
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//this.setContentView(R.layout.type_list);
		Intent intent = getIntent(); // Get the calling intent
		ArrayList<String> types = intent.getStringArrayListExtra("TypeValues");
		setListAdapter(new ArrayAdapter<String>(this, R.layout.type_row, types));

		mLayoutInflater = LayoutInflater.from(this.getBaseContext());
		View layout = mLayoutInflater.inflate(R.layout.type_list, null);

		View actionbar = layout.findViewById(R.id.type_banner);
		actionbar.setBackgroundResource(R.color.darkblue);

		this.lv = getListView();

		lv.setTextFilterEnabled(true);
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lv.setCacheColorHint(R.color.white);
		lv.setBackgroundResource(R.color.white);
		lv.setSelector(getResources().getDrawable(R.drawable.highlight));
		lv.setDivider(getResources().getDrawable(R.drawable.divider));
		lv.setDividerHeight(20);		
		lv.setCacheColorHint(0x000000);
		lv.setItemsCanFocus(false);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				MakeToast.makeToast(getApplicationContext(),
									((TextView) view).getText().toString(),
									MakeToast.LEVEL_DEBUG);
				Intent retIntent = new Intent();
				String temp = (String) ((TextView) view).getText();
				retIntent.putExtra(Constants.FIND_VALUE, temp);
				setResult(RESULT_OK, retIntent);
				finish();
			}
		});
	}
}
