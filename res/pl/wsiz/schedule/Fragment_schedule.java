package pl.wsiz.schedule;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import pl.wsiz.schedule.adapters.ScheduleListAdapter;
import pl.wsiz.schedule.model.Day;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class Fragment_schedule extends Fragment {
	static View v;
	static Context cont;
	ListView listView;
	ArrayList<Day> days;
	ScheduleListAdapter sAdapter;

	public void newInstance(int TYP, String COUNT) {
		listView = (ListView) v.findViewById(R.id.list_schedule);

		listView.setEmptyView(v.findViewById(R.id.emptyElement));

		listView.setClickable(true);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				
				Intent intent = new Intent(cont, Schedule_details.class);
				intent.putExtra("id", position);
				intent.putExtra("firstid", days.get(0).id);
				intent.putExtra("lastid", days.get(days.size() - 1).id);
				cont.startActivity(intent);
			}
		});

		days = new ArrayList<Day>();
		
		String db_select = "timetable";
		Cursor c = null;
		String selection = null;
		String[] selectionArgs = null;
		DBHelper dbHelper = new DBHelper(cont, "wsiz_sch");
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		db = dbHelper.getWritableDatabase();

		switch (TYP) {
		case 1:
			selection = "week = ?";
			// modulo to weeks in actual year
			COUNT = ""
					+ (Integer.parseInt(COUNT) % new GregorianCalendar()
							.getActualMaximum(Calendar.WEEK_OF_YEAR));

			selectionArgs = new String[] { COUNT };
			c = db.query(db_select, null, selection, selectionArgs, null, null,
					null);
			break;
		case 2:
			selection = "mounth = ?";
			selectionArgs = new String[] { COUNT };
			c = db.query(db_select, null, selection, selectionArgs, null, null,
					null);
			break;
		case 3:
			c = db.query(db_select, null, null, null, null, null, null);
			break;
		}

		if (c != null && c.moveToFirst()) {
			do {
				Day day = new Day(cont,
						c.getString(c.getColumnIndex(Utils.ID)),
						c.getString(c.getColumnIndex(Utils.SUBJECT)),
						c.getString(c.getColumnIndex(Utils.ROOM)),
						c.getString(c.getColumnIndex(Utils.TYPE)),
						c.getString(c.getColumnIndex(Utils.TIME_S)),
						c.getString(c.getColumnIndex(Utils.TIME_E)),
						c.getString(c.getColumnIndex("date")),
						Utils.getDrawableIcon(c.getString(c
								.getColumnIndex(Utils.FORM))));
				days.add(day);
			} while (c.moveToNext());

			sAdapter = new ScheduleListAdapter(cont, days);
			listView.setAdapter(sAdapter);

			listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		} else {
			listView.setAdapter(null);
			c.close();
		}
		dbHelper.close();
		db.close();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.fragment_schedule, null);
		cont = getActivity();
		
		newInstance(2,"" + (Integer.parseInt(new SimpleDateFormat("w",
								Locale.ENGLISH).format(new Date(System
								.currentTimeMillis()))) - 1));
		return v;
	}
}