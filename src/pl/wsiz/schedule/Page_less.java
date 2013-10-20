package pl.wsiz.schedule;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class Page_less extends SherlockActivity {
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.less);
		int INT = 1;
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		ListView listView = (ListView) findViewById(R.id.less_list);
		((ImageView) findViewById(R.id.logo_small_less)).setAlpha(55);
		
		Intent intent = getIntent();
		String title = intent.getStringExtra("title");
		String form = intent.getStringExtra("id");
		
		ArrayList<HashMap<String, Object>> myTT;
		Cursor c;
		String[] selectionArgs = null;
		HashMap<String, Object> hm;
		DBHelper dbHelper = new DBHelper(this, "wsiz_sch");
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		db = dbHelper.getWritableDatabase();
		final String SUBJECT = "subject";
		final String DATE = "date";
		
		myTT = new ArrayList<HashMap<String, Object>>();
		selectionArgs = new String[] { title, form };
		c = db.query("timetable", null, "subject = ? AND form = ?", selectionArgs, null, null, null);
		if (c != null && c.moveToFirst()) {
				do {
					hm = new HashMap<String, Object>();
					hm.put("int", INT); INT++;
					hm.put(SUBJECT, c.getString(c.getColumnIndex(SUBJECT)));
					hm.put(DATE, c.getString(c.getColumnIndex(DATE)));
					myTT.add(hm);
				} while (c.moveToNext());
				c.close();
				SimpleAdapter adapter = new SimpleAdapter(this, myTT, R.layout.list_less,
						new String[] {
							"int",
							SUBJECT,
							DATE
						}, new int[] {
							R.id.list_less_num,
							R.id.less_text1,
							R.id.less_text2}) {
					  @Override
					  public View getView (int position, View convertView, ViewGroup parent) {
					    View view = super.getView(position, convertView, parent);
					    return view;
					  }
					};
				listView.setAdapter(adapter);
			} else 
			listView.setAdapter(null);
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
			Page_less.this.startActivity(new Intent(Page_less.this,
					Page_settings.class));
			break;
		case R.id.item3:
			Page_less.this.startActivity(new Intent(Page_less.this,
					Page_about.class));
			break;
		case R.id.item4:
			moveTaskToBack(true);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
