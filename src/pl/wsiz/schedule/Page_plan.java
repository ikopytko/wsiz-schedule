package pl.wsiz.schedule;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class Page_plan extends SherlockActivity implements View.OnClickListener {
	ListView listView;
	int current_deep;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plan);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		int deep;
		SharedPreferences spf;
		
		listView = (ListView) findViewById(R.id.plan_list);

		spf = getSharedPreferences("AppPreferences", MODE_PRIVATE);
		deep = spf.getInt(Prefs.GLOBAL_DEEP, 0);
		current_deep = getIntent().getIntExtra("current_deep",0);
		
		if (current_deep == 0) ((Button) findViewById(R.id.plan_next)).setEnabled(false);
		if (current_deep == deep) ((Button) findViewById(R.id.plan_prev)).setEnabled(false);
		
		((TextView) findViewById(R.id.plan_title)).append(" "+(deep+1-current_deep));
		
		((Button) findViewById(R.id.plan_prev)).setOnClickListener(this);
		((Button) findViewById(R.id.plan_next)).setOnClickListener(this);
		
		ArrayList<HashMap<String, Object>> myTT;
		Cursor c;
		String[] selectionArgs = null;
		HashMap<String, Object> hm;
		DBHelper dbHelper = new DBHelper(this, "wsiz_sch");
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		db = dbHelper.getWritableDatabase();
		final String IMGKEY = "img";
		final String COURSE = "course";
		final String TEACHER = "teacher";
		final String TYPE = "type";
		final String EXAM = "examination";
		final String HOURS = "cl_per_sem";
		
		myTT = new ArrayList<HashMap<String, Object>>();
		selectionArgs = new String[] { ""+current_deep };
		c = db.query("academic_plan", null, "deep = ?", selectionArgs, null, null, null);
		if (c != null && c.moveToFirst()) {
				do {
					hm = new HashMap<String, Object>();
					hm.put(COURSE, c.getString(c.getColumnIndex(COURSE)));
					hm.put(TEACHER, c.getString(c.getColumnIndex(TEACHER)));
					hm.put(HOURS, c.getString(c.getColumnIndex(HOURS))+"h, "+c.getString(c.getColumnIndex(EXAM)));
					hm.put(TYPE, c.getString(c.getColumnIndex(TYPE)));
					switch (c.getString(c.getColumnIndex(TYPE)).charAt(0)) {
					case 'W':
						hm.put(IMGKEY, R.drawable.w);
						break;
					case 'C':
						hm.put(IMGKEY, R.drawable.c);
						break;
					case 'L':
						hm.put(IMGKEY, R.drawable.l);
						break;
					case 'K':
						hm.put(IMGKEY, R.drawable.k);
						break;
					case 'J':
						hm.put(IMGKEY, R.drawable.j);
						break;
					case 'S':
						hm.put(IMGKEY, R.drawable.sp);
						break;
					case 'F':
						hm.put(IMGKEY, R.drawable.f);
						break;
					default:
						hm.put(IMGKEY, R.drawable.i);
						break;
					}
					if (c.getString(c.getColumnIndex(TYPE)).equalsIgnoreCase("JD"))
						hm.put(IMGKEY, R.drawable.jd);
					myTT.add(hm);
				} while (c.moveToNext());
				c.close();
				SimpleAdapter adapter = new SimpleAdapter(this, myTT, R.layout.list_plan,
						new String[] {
							IMGKEY,
							COURSE,
							TEACHER,
							HOURS,
							TYPE
						}, new int[] {
							R.id.plan_image1,
							R.id.plan_text1,
							R.id.plan_text2,
							R.id.plan_text3});
				listView.setAdapter(adapter);
				listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			} else 
			listView.setAdapter(null);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			   @SuppressWarnings("unchecked")
			@Override
			   public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				   Intent intent1 = new Intent(Page_plan.this, Page_less.class);
					//intent1.putExtra("title", ts2);
					HashMap<String, Object> hm;   
					hm = new HashMap<String, Object>();
					hm = (HashMap<String, Object>) listView.getItemAtPosition(position);
					//Start detail page
					intent1.putExtra("title", hm.get("course").toString());
					intent1.putExtra("id", hm.get("type").toString());
					startActivity(intent1);
			   }
			 });
		dbHelper.close();
		db.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuinf = getSupportMenuInflater();
		menuinf.inflate(R.menu.menu_ins, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.item2:
			Page_plan.this.startActivity(new Intent(Page_plan.this,
					Page_settings.class));
			break;
		case R.id.item3:
			Page_plan.this.startActivity(new Intent(Page_plan.this,
					Page_about.class));
			break;
		case R.id.item4:
			moveTaskToBack(true);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(Page_plan.this, Page_plan.class);
		switch (v.getId()) {
		case R.id.plan_prev:
			finish();
			intent.putExtra("current_deep", current_deep+1);
			Page_plan.this.startActivity(intent);
			break;
		case R.id.plan_next:
			finish();
			intent.putExtra("current_deep", current_deep-1);
			Page_plan.this.startActivity(intent);
			break;
		}
	}

}
