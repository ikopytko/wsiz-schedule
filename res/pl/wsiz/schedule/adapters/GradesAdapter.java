package pl.wsiz.schedule.adapters;

import java.util.ArrayList;
import java.util.HashMap;

import org.taptwo.android.widget.TitleProvider;

import pl.wsiz.schedule.DBHelper;
import pl.wsiz.schedule.Grade_details;
import pl.wsiz.schedule.R;
import pl.wsiz.schedule.Utils;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class GradesAdapter extends BaseAdapter implements TitleProvider {

	private LayoutInflater mInflater;
	
	private Cursor c;
	private DBHelper dbHelper;
	private SQLiteDatabase db;
	
	private Context context;
	
	private int deep;
	
	private ArrayList<SimpleAdapter> pages = new ArrayList<SimpleAdapter>();
	
	private class ViewHolder {
		ListView listView;
	}
	
	public GradesAdapter(Context context, int deep) {
		this.context = context;
		this.deep = deep;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		for (int i = 0; i <= deep; i++)
			prepareAdapters(i);
	}
	
	private void prepareAdapters(int current_deep) {
		ArrayList<HashMap<String, Object>> myTT;
		HashMap<String, Object> hm;
		dbHelper = new DBHelper(context, "wsiz_sch");
		db = dbHelper.getWritableDatabase();

		myTT = new ArrayList<HashMap<String, Object>>();
		c = db.query("grades", null, "deep = " + current_deep, null, null, null, null);
		float sum = 0;
		int div = 0;

		if (c != null && c.moveToFirst()) {
				do {
					
					hm = new HashMap<String, Object>();
					hm.put(Utils.ID, c.getString(c.getColumnIndex(Utils.ID)));
					hm.put(Utils.NAME, c.getString(c.getColumnIndex(Utils.NAME)));
					hm.put(Utils.GRADE, c.getString(c.getColumnIndex(Utils.GRADE)));
					for (int i = 4; i<=8; i++) {
						try {
						sum += Float.parseFloat(c.getString(i));
						div++;
						} catch(NumberFormatException ex) { }
					}
					hm.put(Utils.IMGKEY, Utils.getDrawableIcon(c.getString(c.getColumnIndex(Utils.TYPE))));
					myTT.add(hm);
				} while (c.moveToNext());
				c.close();
				
				if (div!=0) {
					hm = new HashMap<String, Object>();
					hm.put(Utils.ID, "-1");
					hm.put(Utils.NAME, context.getResources().getString(R.string.grade_averange));
					hm.put(Utils.GRADE, ""+(sum/div));
					hm.put(Utils.IMGKEY, R.drawable.i);
				myTT.add(0,hm);
				}
				
				pages.add( new SimpleAdapter(context, myTT, R.layout.list_grades,
						new String[] {
							Utils.ID,
							Utils.IMGKEY,
							Utils.NAME,
							Utils.GRADE
						}, new int[] {
							R.id.grade_id,
							R.id.grades_image1,
							R.id.grades_text1,
							R.id.grades_text2}));

			}
		dbHelper.close();
		db.close();
	}

	@Override
	public int getCount() {
		return pages.size();
	}

	@Override
	public Object getItem(int position) {
		return pages.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup arg2) {
		ViewHolder holder = null;
		
		if(view == null) {
			view = mInflater.inflate(R.layout.bus, null);
			
			holder = new ViewHolder();

			holder.listView = (ListView) view.findViewById(R.id.bus_list);
			
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}		

		final SimpleAdapter o = (SimpleAdapter) getItem(position);
		if (o != null) {
			holder.listView.setAdapter(o);
			holder.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@SuppressWarnings("unchecked")
				@Override
				   public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
					   HashMap<String, Object> hm =  (HashMap<String, Object>) arg0.getItemAtPosition(position);
					   //Start detail page
					   Intent intent = new Intent(context, Grade_details.class);
					   intent.putExtra(Utils.ID, Integer.parseInt(hm.get(Utils.ID).toString()));
					   
					   if(Integer.parseInt(hm.get(Utils.ID).toString()) != -1)
						   context.startActivity(intent);
				   }
				 });
		}
	
		return view;
	}

	@Override
	public String getTitle(int position) {
		return context.getResources().getString(R.string.semestr) + " " + (deep-position+1);
	}

}
