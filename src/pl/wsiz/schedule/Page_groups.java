package pl.wsiz.schedule;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class Page_groups extends SherlockActivity implements View.OnClickListener {
	int current_deep;
	ListView listView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.groups);
		
		SharedPreferences spf;
		int deep;
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		listView = (ListView) findViewById(R.id.groups_list);

		spf = getSharedPreferences("AppPreferences", MODE_PRIVATE);
		deep = spf.getInt(Prefs.GLOBAL_DEEP, 0);
		Intent intent = getIntent();
		current_deep = intent.getIntExtra("current_deep",0);
		
		if (current_deep == 0) ((Button) findViewById(R.id.groups_next)).setEnabled(false);
		if (current_deep == deep) ((Button) findViewById(R.id.groups_prev)).setEnabled(false);
		
		((TextView) findViewById(R.id.groups_title)).append(" "+(deep+1-current_deep));
		
		((Button) findViewById(R.id.groups_prev)).setOnClickListener(this);
		((Button) findViewById(R.id.groups_next)).setOnClickListener(this);
		
		ArrayList<HashMap<String, Object>> myTT;
		Cursor c;
		String[] selectionArgs = null;
		HashMap<String, Object> hm;
		DBHelper dbHelper = new DBHelper(this, "wsiz_sch");
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		db = dbHelper.getWritableDatabase();
		final String GROUPS = "groups";
		final String TYPE = "type";
		
		myTT = new ArrayList<HashMap<String, Object>>();
		selectionArgs = new String[] { ""+current_deep };
		c = db.query("modules", null, "deep = ?", selectionArgs, null, null, null);
		if (c != null && c.moveToFirst()) {
				do {
					hm = new HashMap<String, Object>();
					hm.put(GROUPS, c.getString(c.getColumnIndex(GROUPS)));
					hm.put(TYPE, c.getString(c.getColumnIndex(TYPE)));
					myTT.add(hm);
				} while (c.moveToNext());
				c.close();
				SimpleAdapter adapter = new SimpleAdapter(this, myTT, R.layout.list_group,
						new String[] {
							GROUPS,
							TYPE
						}, new int[] {
							R.id.groups_text2,
							R.id.groups_text1});
				listView.setAdapter(adapter);
				//listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			} else 
			listView.setAdapter(null);
		dbHelper.close();
		db.close();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(Page_groups.this, Page_groups.class);
		switch (v.getId()) {
		case R.id.groups_prev:
			finish();
			intent.putExtra("current_deep", current_deep+1);
			Page_groups.this.startActivity(intent);
			break;
		case R.id.groups_next:
			finish();
			intent.putExtra("current_deep", current_deep-1);
			Page_groups.this.startActivity(intent);
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
	protected void onDestroy() {
		super.onDestroy();
		listView.removeAllViewsInLayout();
		listView = null;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.item2:
			Page_groups.this.startActivity(new Intent(Page_groups.this,
					Page_settings.class));
			break;
		case R.id.item3:
			Page_groups.this.startActivity(new Intent(Page_groups.this,
					Page_about.class));
			break;
		case R.id.item4:
			moveTaskToBack(true);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
