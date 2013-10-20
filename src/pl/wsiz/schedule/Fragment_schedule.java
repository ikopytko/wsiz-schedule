package pl.wsiz.schedule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

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
import android.widget.SimpleAdapter;

public class Fragment_schedule extends Fragment {
	static View v;
	static Context cont;
	ListView listView;
	
	 public void newInstance(int TYP, String COUNT) {
		 listView = (ListView) v.findViewById(R.id.list_schedule);
		 listView.setEmptyView(v.findViewById(R.id.emptyElement));
		 
		 listView.setClickable(true);
		 listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

		   @SuppressWarnings("unchecked")
		@Override
		   public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			   HashMap<String, Object> hm;   
			   hm = new HashMap<String, Object>();
			   hm = (HashMap<String, Object>) listView.getItemAtPosition(position);
			   //Start detail page
			   Intent intent = new Intent(cont, Schedule_details.class);
			   intent.putExtra("id", hm.get("id").toString());
			   cont.startActivity(intent);
		   }
		 });
		 
		 ArrayList<HashMap<String, Object>> myTT;
		 String db_select = "timetable";                                                   
		 Cursor c;                               
		 String selection = null;                
		 String[] selectionArgs = null;                                                   
		 HashMap<String, Object> hm;             
		 String data_prew="", data="";
		 DBHelper dbHelper = new DBHelper(cont, "wsiz_sch"); 
		 SQLiteDatabase db = dbHelper.getWritableDatabase();
		 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", cont.getResources().getConfiguration().locale);
		 String tuday = sdf.format(new Date(System.currentTimeMillis()));
		 String tomorrw = sdf.format(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000));
		 SeparatedListAdapter sep_adapter = new SeparatedListAdapter(cont);

		db = dbHelper.getWritableDatabase();
		c = db.query(db_select, null, null, null, null, null, null);
		final String ID = "id";
		final String SUBJECT = "subject";
		// final String TEACHER = "teacher";
		// final String DATE = "date";
		final String TIME_S = "time_s";
		final String TIME_E = "time_e";
		final String ROOM = "room";
		final String FORM = "form";
		final String TYPE = "type";
		// final String WEEK = "week";
		// final String MOUNTH = "mounth";
		final String IMGKEY = "iconfromraw";

		myTT = new ArrayList<HashMap<String, Object>>();
		Calendar mydate = new GregorianCalendar();

		switch (TYP) {
		case 1:
			selection = "week = ?";
			// modulo to weeks in actual year
		    COUNT = "" + (Integer.parseInt(COUNT) % mydate.getActualMaximum(Calendar.WEEK_OF_YEAR));
		    
			selectionArgs = new String[] { COUNT };
			c = db.query(db_select, null, selection, selectionArgs, null, null, null);
			break;
		case 2:
			selection = "mounth = ?";
			selectionArgs = new String[] { COUNT };
			c = db.query(db_select, null, selection, selectionArgs, null, null, null);
			break;
		case 3:
			c = db.query(db_select, null, null, null, null, null, null);
			break;
		}

		data_prew = "";
		data = "";

		if (c != null) {
			if (c.moveToFirst()) {
				do {
					hm = new HashMap<String, Object>();
					data = c.getString(c.getColumnIndex("date"));
					if (data_prew.equalsIgnoreCase(""))
						data_prew = c.getString(c.getColumnIndex("date"));
					else {
						if (!data_prew.equalsIgnoreCase(data)) {
							SimpleAdapter adapter = new SimpleAdapter(cont,
									myTT, R.layout.list_schedule, new String[] {
											SUBJECT, ROOM, IMGKEY, TIME_S, ID },
									new int[] { R.id.text1, R.id.text2,
											R.id.image1, R.id.text4, R.id.sch_id });

							try {
								Date thedate = sdf.parse(data_prew);
								mydate.setTime(thedate);
							} catch (ParseException e) {
								e.printStackTrace();
							}

							String[] days = cont.getResources().getStringArray(
									R.array.days);
							String label = days[mydate
									.get(Calendar.DAY_OF_WEEK) - 2];
							if (data_prew.equalsIgnoreCase(tuday))
								label = days[7]; // today
							if (data_prew.equalsIgnoreCase(tomorrw))
								label = days[8]; // tomorrow

							sep_adapter.addSection(label + ", " + data_prew,
									adapter);
							myTT = null;
							myTT = new ArrayList<HashMap<String, Object>>();
						}
					}
					hm.put(ID, c.getString(c.getColumnIndex(ID)));
					hm.put(SUBJECT, c.getString(c.getColumnIndex(SUBJECT)));
					hm.put(ROOM, c.getString(c.getColumnIndex(ROOM)) + " - "
							+ c.getString(c.getColumnIndex(TYPE)));
					hm.put(TIME_S, c.getString(c.getColumnIndex(TIME_S)) + "-"
							+ c.getString(c.getColumnIndex(TIME_E)));
					switch (c.getString(c.getColumnIndex(FORM)).charAt(0)) {
					case 'C':
						hm.put(IMGKEY, R.drawable.c);
						break;
					case 'L':
						hm.put(IMGKEY, R.drawable.l);
						break;
					case 'W':
						hm.put(IMGKEY, R.drawable.w);
						break;
					case 'J':
						hm.put(IMGKEY, R.drawable.j);
						break;
					case 'D':
						hm.put(IMGKEY, R.drawable.jd);
						break;
					case 'K':
						hm.put(IMGKEY, R.drawable.k);
						break;
					case 'F':
						hm.put(IMGKEY, R.drawable.f);
						break;
					case 'P':
						hm.put(IMGKEY, R.drawable.sp);
						break;
					default:
						hm.put(IMGKEY, R.drawable.i);
						break;
					}
					myTT.add(hm);

					data_prew = data;
				} while (c.moveToNext());

				// Last item
				SimpleAdapter adapter = new SimpleAdapter(cont,
						myTT, R.layout.list_schedule, new String[] {
								SUBJECT, ROOM, IMGKEY, TIME_S, ID },
						new int[] { R.id.text1, R.id.text2,
								R.id.image1, R.id.text4, R.id.sch_id });

				//Calendar mydate = new GregorianCalendar();
				try {
					Date thedate = sdf.parse(data_prew);
					mydate.setTime(thedate);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				String[] days = cont.getResources()
						.getStringArray(R.array.days);
				String label = days[mydate.get(Calendar.DAY_OF_WEEK) - 2];
				if (data_prew.equalsIgnoreCase(tuday))
					label = days[7]; // today
				if (data_prew.equalsIgnoreCase(tomorrw))
					label = days[8]; // tomorrow

				sep_adapter.addSection(label + ", " + data_prew, adapter);
				listView.setAdapter(sep_adapter);

				listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			} else listView.setAdapter(null);
			c.close();
		} else
		{
			listView.setAdapter(null);
		}
		dbHelper.close();
		db.close();
	}

	public int getShownIndex() {
		return getArguments().getInt("index", 0);
	}
	    

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.fragment_schedule, null);
		cont = getActivity();

		newInstance(2, ""+(Integer.parseInt(new SimpleDateFormat("w",Locale.ENGLISH).format(new Date(System.currentTimeMillis())))-1));
		return v;
	}
}