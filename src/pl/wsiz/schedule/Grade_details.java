package pl.wsiz.schedule;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class Grade_details extends SherlockActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grade_detail);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		Intent intent = getIntent();
		int id = intent.getIntExtra("id",0);
		
		Cursor c;
		String[] selectionArgs = null;
		DBHelper dbHelper = new DBHelper(this, "wsiz_sch");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		db = dbHelper.getWritableDatabase();
		String NAME;
		String TYPE; 
		String GRADE;
		String ONE;
		String TWO;
		String CONDITIONAL;
		String ADVANCE;
		String COMMISSION;
		
		selectionArgs = new String[] { ""+id };
		c = db.query("grades", null, "id = ?", selectionArgs, null, null, null);
		
		if (c != null && c.moveToFirst()) {
			NAME = c.getString(c.getColumnIndex("name"));
			TYPE = c.getString(c.getColumnIndex("type"));
			GRADE = c.getString(c.getColumnIndex("grade"));
			ONE = c.getString(c.getColumnIndex("one"));
			TWO = c.getString(c.getColumnIndex("two"));
			CONDITIONAL = c.getString(c.getColumnIndex("conditional"));
			ADVANCE = c.getString(c.getColumnIndex("advance"));
			COMMISSION = c.getString(c.getColumnIndex("commission"));
			
			((TextView) findViewById(R.id.gr_det_title)).setText(NAME);
			((TextView) findViewById(R.id.gr_det_form)).setText(TYPE);
			((TextView) findViewById(R.id.gr_det_grade)).setText(GRADE);
			((TextView) findViewById(R.id.gr_det_one)).setText(ONE);
			((TextView) findViewById(R.id.gr_det_two)).setText(TWO);
			((TextView) findViewById(R.id.gr_det_conditional)).setText(CONDITIONAL);
			((TextView) findViewById(R.id.gr_det_advance)).setText(ADVANCE);
			((TextView) findViewById(R.id.gr_det_commission)).setText(COMMISSION);
		}
		c.close();
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
			Grade_details.this.startActivity(new Intent(Grade_details.this,
					Page_settings.class));
			break;
		case R.id.item3:
			Grade_details.this.startActivity(new Intent(Grade_details.this,
					Page_about.class));
			break;
		case R.id.item4:
			moveTaskToBack(true);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
