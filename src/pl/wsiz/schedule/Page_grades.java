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

public class Page_grades extends SherlockActivity implements View.OnClickListener {
	ListView listView;
	int current_deep;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grades);
		
		int deep;
		SharedPreferences spf;
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		listView = (ListView) findViewById(R.id.grades_list);

		spf = getSharedPreferences("AppPreferences", MODE_PRIVATE);
		deep = spf.getInt(Prefs.GLOBAL_DEEP, 0);
		current_deep = getIntent().getIntExtra("current_deep",0);
		
		if (current_deep == 0) ((Button) findViewById(R.id.grades_next)).setEnabled(false);
		if (current_deep == deep) ((Button) findViewById(R.id.grades_prev)).setEnabled(false);
		
		((TextView) findViewById(R.id.grades_title)).append(" "+(deep+1-current_deep));
		
		((Button) findViewById(R.id.grades_prev)).setOnClickListener(this);
		((Button) findViewById(R.id.grades_next)).setOnClickListener(this);
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			   @SuppressWarnings("unchecked")
			@Override
			   public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				   HashMap<String, Object> hm;   
				   hm = new HashMap<String, Object>();
				   hm =  (HashMap<String, Object>) listView.getItemAtPosition(position);
				   //Start detail page
				   Intent intent = new Intent(Page_grades.this, Grade_details.class);
				   intent.putExtra("id", Integer.parseInt(hm.get("id").toString()));
				   if(Integer.parseInt(hm.get("id").toString()) != -1)
					   Page_grades.this.startActivity(intent);
			   }
			 });
		
		ArrayList<HashMap<String, Object>> myTT;
		Cursor c;
		String[] selectionArgs = null;
		HashMap<String, Object> hm;
		DBHelper dbHelper = new DBHelper(this, "wsiz_sch");
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		db = dbHelper.getWritableDatabase();
		final String IMGKEY = "img";
		myTT = new ArrayList<HashMap<String, Object>>();
		selectionArgs = new String[] { ""+current_deep };
		c = db.query("grades", null, "deep = ?", selectionArgs, null, null, null);
		float sum = 0;
		int div = 0;

		if (c != null && c.moveToFirst()) {
				do {
					
					hm = new HashMap<String, Object>();
					hm.put("id", c.getString(c.getColumnIndex("id")));
					hm.put("name", c.getString(c.getColumnIndex("name")));
					hm.put("grade", c.getString(c.getColumnIndex("grade")));
					for (int i = 4; i<=8; i++) {
						try {
						sum += Float.parseFloat(c.getString(i));
						div++;
						} catch(NumberFormatException ex) { }
					}
					switch (c.getString(c.getColumnIndex("type")).charAt(0)) {
					case 'w':
						hm.put(IMGKEY, R.drawable.w);
						break;
					case 'c':
						hm.put(IMGKEY, R.drawable.c);
						break;
					case 'l':
						hm.put(IMGKEY, R.drawable.l);
						break;
					case 'K':
						hm.put(IMGKEY, R.drawable.k);
						break;
					case 'W':
						hm.put(IMGKEY, R.drawable.w);
						break;
					case 'J':
						hm.put(IMGKEY, R.drawable.j);
						break;
					default:
						hm.put(IMGKEY, R.drawable.i);
						break;
					}
					myTT.add(hm);
				} while (c.moveToNext());
				c.close();
				
				if (div!=0) {
					hm = new HashMap<String, Object>();
					hm.put("id", "-1");
					hm.put("name", getResources().getString(R.string.grade_averange));
					hm.put("grade", ""+(sum/div));
					hm.put(IMGKEY, R.drawable.i);
				myTT.add(0,hm);
				}
				
				SimpleAdapter adapter = new SimpleAdapter(this, myTT, R.layout.list_grades,
						new String[] {
							"id",
							IMGKEY,
							"name",
							"grade"
						}, new int[] {
							R.id.grade_id,
							R.id.grades_image1,
							R.id.grades_text1,
							R.id.grades_text2});
				listView.setAdapter(adapter);
				listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

			} else 
			listView.setAdapter(null);
		dbHelper.close();
		db.close();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(Page_grades.this, Page_grades.class);
		switch (v.getId()) {
		case R.id.grades_prev:
			finish();
			intent.putExtra("current_deep", current_deep+1);
			Page_grades.this.startActivity(intent);
			break;
		case R.id.grades_next:
			finish();
			intent.putExtra("current_deep", current_deep-1);
			Page_grades.this.startActivity(intent);
			break;
		}
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
			Page_grades.this.startActivity(new Intent(Page_grades.this,
					Page_settings.class));
			break;
		case R.id.item3:
			Page_grades.this.startActivity(new Intent(Page_grades.this,
					Page_about.class));
			break;
		case R.id.item4:
			moveTaskToBack(true);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
