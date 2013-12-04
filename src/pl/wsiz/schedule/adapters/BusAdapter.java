package pl.wsiz.schedule.adapters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.taptwo.android.widget.TitleProvider;

import pl.wsiz.schedule.DBHelper;
import pl.wsiz.schedule.R;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class BusAdapter extends BaseAdapter implements TitleProvider {

	private LayoutInflater mInflater;

	private Context context;
	
	private static String[] dates;
	private ArrayList<BusTableAdapter> tbAdapter = new ArrayList<BusTableAdapter>();
	
	boolean direction;
	
	
	private class ViewHolder {
		ListView mList;
	}
	
	
	public BusAdapter(Context context, boolean direction) {
		this.context = context;
		this.direction = direction;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		prepareDates();
	}
	
	@Override
	public BusTableAdapter getItem(int position) {
		try {
			return tbAdapter.get(position);
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
			view = mInflater.inflate(R.layout.bus, null);
			
			holder = new ViewHolder();

			holder.mList = (ListView) view.findViewById(R.id.bus_list);
			
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}		


		final BusTableAdapter o = getItem(position);
		if (o != null) {
			holder.mList.setAdapter(o);
		} else 
			prepareAdapter(position, view);
	
		return view;
	}

	private void prepareAdapter(int position, View view) {
		BusTableAdapter bta = new BusTableAdapter(context, dates[position], direction);
		// Setup adapter
		
		tbAdapter.add(bta);
		drawView(position, view);
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
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

		String currentDate = sdf.format(new Date());
		
		Cursor c;
		DBHelper dbHelper = new DBHelper(context, "bus_tt");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		
		// get all rows
		c = db.query("timetable", null, "date >= " + currentDate, null, "date", null, null);
		
		Log.wtf("TTAG", "C.size " + c.getCount() + " " + currentDate);
		
		dates = new String[c.getCount()];
		
		// if table not empty
		if (c.moveToFirst()) {
			do {
				dates[c.getPosition()] = c.getString(c.getColumnIndex("date"));
			} while (c.moveToNext());
		}
		c.close();
		dbHelper.close();
	}	
}
