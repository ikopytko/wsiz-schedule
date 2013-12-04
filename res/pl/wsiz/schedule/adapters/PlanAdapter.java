package pl.wsiz.schedule.adapters;

import java.util.ArrayList;
import java.util.HashMap;

import org.taptwo.android.widget.TitleProvider;

import pl.wsiz.schedule.DBHelper;
import pl.wsiz.schedule.Page_less;
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

public class PlanAdapter extends BaseAdapter implements TitleProvider {

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
	
	public PlanAdapter(Context context, int deep) {
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
		c = db.query("academic_plan", null, "deep = " + current_deep, null, null, null, null);

		if (c != null && c.moveToFirst()) {
			do {
				hm = new HashMap<String, Object>();
				hm.put(Utils.COURSE, c.getString(c.getColumnIndex(Utils.COURSE)));
				hm.put(Utils.TEACHER, c.getString(c.getColumnIndex(Utils.TEACHER)));
				hm.put(Utils.HOURS, c.getString(c.getColumnIndex(Utils.HOURS))+"h, "
						+c.getString(c.getColumnIndex(Utils.EXAM)));
				hm.put(Utils.TYPE, c.getString(c.getColumnIndex(Utils.TYPE)));
				hm.put(Utils.IMGKEY, Utils.getDrawableIcon(c.getString(c.getColumnIndex(Utils.TYPE))));
				
				myTT.add(hm);
			} while (c.moveToNext());
			c.close();
			pages.add(new SimpleAdapter(context, myTT, R.layout.list_plan,
					new String[] {
					Utils.IMGKEY,
					Utils.COURSE,
					Utils.TEACHER,
					Utils.HOURS,
					Utils.TYPE
					}, new int[] {
						R.id.plan_image1,
						R.id.plan_text1,
						R.id.plan_text2,
						R.id.plan_text3}));
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
					   Intent intent = new Intent(context, Page_less.class);
					   intent.putExtra("title", hm.get("course").toString());
					   intent.putExtra("id", hm.get("type").toString());
					   
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