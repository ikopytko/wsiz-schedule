package pl.wsiz.schedule.adapters;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.taptwo.android.widget.TitleProvider;

import pl.wsiz.schedule.DBHelper;
import pl.wsiz.schedule.Page_less;
import pl.wsiz.schedule.R;
import pl.wsiz.schedule.Utils;
import pl.wsiz.schedule.model.Day;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ScheduleDetailsAdapter extends BaseAdapter implements TitleProvider {

	private LayoutInflater mInflater;
	
	private Cursor c;
	private DBHelper dbHelper;
	private SQLiteDatabase db;
	
	private Context context;
	
	private static String[] dates;
	private String[] selectionArgs;
	private ArrayList<Day> days = new ArrayList<Day>();
	
	
	private class ViewHolder {
		ProgressBar mProgressBar;
		View mContent;
		ImageView mImage;
		TextView mTitle;
		TextView mTeacher;
		TextView mDate;
		TextView mTime;
		TextView mLocation;
		Button mShowAll;
	}
	
	
	public ScheduleDetailsAdapter(Context context, String firstid, String lastid) {
		this.context = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		selectionArgs = new String[] { firstid, lastid };
		prepareDates();
	}
	
	@Override
	public Day getItem(int position) {
		try {
			return days.get(position);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position; 
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return drawView(position, convertView);
	}

	private View drawView(int position, View view) {
		ViewHolder holder = null;
		
		if(view == null) {
			view = mInflater.inflate(R.layout.schedule_detail, null);
			
			holder = new ViewHolder();

			holder.mProgressBar = (ProgressBar) view.findViewById(R.id.sd_progress);
			holder.mImage = (ImageView) view.findViewById(R.id.sd_image);
			holder.mContent = (View) view.findViewById(R.id.sd_content);
			holder.mTitle = (TextView) view.findViewById(R.id.sd_title);
			holder.mTeacher = (TextView) view.findViewById(R.id.sd_teacher);
			holder.mDate = (TextView) view.findViewById(R.id.sd_date);
			holder.mTime = (TextView) view.findViewById(R.id.sd_time);
			holder.mLocation = (TextView) view.findViewById(R.id.sd_location);
			holder.mShowAll = (Button) view.findViewById(R.id.sd_button);
			
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}		


		final Day o = getItem(position);
		if (o != null) {
			holder.mProgressBar.setVisibility(View.GONE);
			
			holder.mImage.setImageResource(days.get(position).image);
			holder.mTitle.setText(days.get(position).title);
			holder.mTeacher.setText(days.get(position).teacher);
			holder.mDate.setText(days.get(position).date);
			holder.mTime.setText(days.get(position).time);
			holder.mLocation.setText(days.get(position).getLocale());
			
			final String mid = days.get(position).form;
			final String mtitle = days.get(position).title;
			
			holder.mShowAll.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent1 = new Intent(context, Page_less.class);
					intent1.putExtra("title", mtitle);
					intent1.putExtra("id", mid);
					context.startActivity(intent1);
				}
			});
			
			holder.mContent.setVisibility(View.VISIBLE);
		}
		else {

			holder.mContent.setVisibility(View.GONE);
			holder.mProgressBar.setVisibility(View.VISIBLE);
		}
	
		return view;
	}

	@Override
	public String getTitle(int position) {
		return dates[position];
	}

	@Override
	public int getCount() {
		return dates.length;
	}
	
	/**
	 * Prepare dates for navigation, to past and to future
	 */
	private void prepareDates() {
		dbHelper = new DBHelper(context, "wsiz_sch");
		db = dbHelper.getWritableDatabase();
		
		c = db.query("timetable", null, "id BETWEEN ? AND ?", selectionArgs, null, null,
				null);
		
		dates = new String[ c.getCount() ];
		if (c != null && c.moveToFirst()) {
			Calendar mydate = new GregorianCalendar();
			String[] days = context.getResources().getStringArray(
					R.array.days);
			do {
				Day day;
				try {
					Date thedate = Utils.sdf.parse(c.getString(c.getColumnIndex("date")));
					mydate.setTime(thedate);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				dates[ c.getPosition() ] = days[mydate.get(Calendar.DAY_OF_WEEK) - 2];
				
				
				day = new Day(context,
						c.getString(c.getColumnIndex(Utils.ID)),
						c.getString(c.getColumnIndex(Utils.SUBJECT)),
						c.getString(c.getColumnIndex(Utils.TEACHER)),
						c.getString(c.getColumnIndex(Utils.ROOM)),
						c.getString(c.getColumnIndex(Utils.TYPE)),
						c.getString(c.getColumnIndex(Utils.FORM)),
						c.getString(c.getColumnIndex(Utils.TIME_S)),
						c.getString(c.getColumnIndex(Utils.TIME_E)),
						c.getString(c.getColumnIndex("date")),
						Utils.getDrawableIcon(c.getString(c
								.getColumnIndex(Utils.FORM))));

				this.days.add(day);
			} while (c.moveToNext());
		 }
		c.close();
		db.close();
		
		int total = 1, n=0;
		int[] classperday = new int[5];
		for (int i = 0; i < dates.length-1; i++)
			if (dates[i] == dates[i+1])
				total++;
			else {
				classperday[n++] = total; total = 1;
			}
		if (dates[dates.length-2] == dates[dates.length-1])
			classperday[n] = total;
		else classperday[n] = 1;
			
		n = 0;
		int x = 1;
		for (int i = 0; i < dates.length; i++) {
			dates[i] += " " + x + "/" + classperday[n];
			if (x++ == classperday[n]) {n++; x=1;}
		}
	}
}
