package pl.wsiz.schedule.adapters;

import java.util.ArrayList;

import pl.wsiz.schedule.DBHelper;
import pl.wsiz.schedule.R;
import pl.wsiz.schedule.model.Bus;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BusTableAdapter  extends BaseAdapter {
	
	private LayoutInflater mInflater;
	
	private ArrayList<Bus> allbus = new ArrayList<Bus>();
	private boolean direction = true;
	
	private class ViewHolder {
		TextView k0;
		TextView k1;
		TextView k2;
		TextView k3;
		TextView k4;
	}

	public BusTableAdapter(Context context, String date, boolean direction) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		this.direction = direction;
		
		DBHelper dbHelper = new DBHelper(context, "bus_tt");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		Cursor c = db.query("timetable", null, "date = " + date, null, null, null, null);
		if (c != null && c.moveToFirst()) {
			do {
				Bus bus = new Bus();
				
				for (int i = 2; i <= 6; i++) {
					bus.k[i-2] = c.getString(i);
				}
				
				for (int i = 7; i <= 9; i++)
					bus.r[i-7] = c.getString(i);
				

				allbus.add(bus);
			} while (c.moveToNext());
		 }
		
		c.close();
		dbHelper.close();
	}

	@Override
	public int getCount() {
		return allbus.size()+1;
	}

	@Override
	public Object getItem(int position) {
		return allbus.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHolder holder = null;
		
		if(view == null) {
			view = mInflater.inflate(R.layout.list_bus, null);
			
			holder = new ViewHolder();

			holder.k0 = (TextView) view.findViewById(R.id.k1);
			holder.k1 = (TextView) view.findViewById(R.id.k2);
			holder.k2 = (TextView) view.findViewById(R.id.k3);
			holder.k3 = (TextView) view.findViewById(R.id.k4);
			holder.k4 = (TextView) view.findViewById(R.id.k5);
			
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}		

		if (position == 0) {
			// Show title
			if (direction) {
				holder.k0.setVisibility(View.VISIBLE);
				holder.k1.setVisibility(View.VISIBLE);
				holder.k0.setText("ul. Ofiar Katynia");
				holder.k1.setText("Cieplińskiego");
				holder.k2.setText("Tesco");
				holder.k3.setText("Tyczyn");
				holder.k4.setText("CTiR");
			} else {
				holder.k0.setVisibility(View.GONE);
				holder.k1.setVisibility(View.GONE);
				holder.k2.setText("CTiR");
				holder.k3.setText("Tyczyn");
				holder.k4.setText("Tesco");
			}
			return view;
		}

		final Bus o = (Bus) getItem(position-1);
		
		if (o != null) {
			
			if (direction) {
				holder.k0.setVisibility(View.VISIBLE);
				holder.k1.setVisibility(View.VISIBLE);
				holder.k0.setText(o.k[0]);
				holder.k1.setText(o.k[1]);
				holder.k2.setText(o.k[2]);
				holder.k3.setText(o.k[3]);
				holder.k4.setText(o.k[4]);
			} else {
				holder.k0.setVisibility(View.GONE);
				holder.k1.setVisibility(View.GONE);
				holder.k2.setText(o.r[0]);
				holder.k3.setText(o.r[1]);
				holder.k4.setText(o.r[2]);
			}
		}
		return view;
	}

}
