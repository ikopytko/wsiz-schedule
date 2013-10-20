package pl.wsiz.schedule;

import java.text.SimpleDateFormat;				
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class Page_schedule extends SherlockFragmentActivity implements
		ActionBar.OnNavigationListener, View.OnClickListener {
	int TYP = 2;
	int COUNT;
	TextView title_schedule;
	String week;
	String[] mounth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		Context context = getSupportActionBar().getThemedContext();
		ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(
				context, R.array.typ_select, R.layout.sherlock_spinner_item);
		list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(list, this);

		COUNT = Integer.parseInt(new SimpleDateFormat("w", Locale.ENGLISH)
				.format(new Date(System.currentTimeMillis()))) - 1;

		mounth = getResources().getStringArray(R.array.mounth);
		title_schedule = (TextView) findViewById(R.id.title_schedule);
		title_schedule.setText(getStartEndOFWeek(Integer
				.parseInt(new SimpleDateFormat("w", Locale.ENGLISH)
						.format(new Date(System.currentTimeMillis()))), Integer
				.parseInt(new SimpleDateFormat("yyyy", Locale.ENGLISH)
						.format(new Date(System.currentTimeMillis())))));

		((Button) findViewById(R.id.btn_sch_prev)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_sch_next)).setOnClickListener(this);
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
			Page_schedule.this.startActivity(new Intent(Page_schedule.this,
					Page_settings.class));
			break;
		case R.id.item3:
			Page_schedule.this.startActivity(new Intent(Page_schedule.this,
					Page_about.class));
			break;
		case R.id.item4:
			moveTaskToBack(true);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		switch (itemPosition) {
		case 0:
			((Button) findViewById(R.id.btn_sch_prev)).setEnabled(true);
			((Button) findViewById(R.id.btn_sch_next)).setEnabled(true);
			TYP = 1;
			COUNT = (Integer.parseInt(new SimpleDateFormat("w", Locale.ENGLISH)
					.format(new Date(System.currentTimeMillis()))));
			new Fragment_schedule().newInstance(TYP, "" + COUNT);
			title_schedule.setText(getStartEndOFWeek(COUNT, Integer
					.parseInt(new SimpleDateFormat("yyyy", Locale.ENGLISH)
							.format(new Date(System.currentTimeMillis())))));

			break;
		case 1:
			((Button) findViewById(R.id.btn_sch_prev)).setEnabled(true);
			((Button) findViewById(R.id.btn_sch_next)).setEnabled(true);
			TYP = 2;
			COUNT = (Integer.parseInt(new SimpleDateFormat("M", Locale.ENGLISH)
					.format(new Date(System.currentTimeMillis()))) - 1);
			title_schedule.setText(mounth[Integer.parseInt("" + COUNT)]);
			new Fragment_schedule().newInstance(TYP, "" + COUNT);
			break;
		case 2:
			((Button) findViewById(R.id.btn_sch_prev)).setEnabled(false);
			((Button) findViewById(R.id.btn_sch_next)).setEnabled(false);
			title_schedule.setText("Semestr");
			TYP = 3;
			new Fragment_schedule().newInstance(TYP, "" + COUNT);
			break;
		}
		return false;
	}

	String getStartEndOFWeek(int enterWeek, int enterYear) {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(Calendar.WEEK_OF_YEAR, enterWeek);
		calendar.set(Calendar.YEAR, enterYear);

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd",
				getResources().getConfiguration().locale); // PST`
		Date startDate = calendar.getTime();
		String startDateInStr = formatter.format(startDate);

		calendar.add(Calendar.DATE, 6);
		Date enddate = calendar.getTime();
		String endDaString = formatter.format(enddate);

		return startDateInStr + " - " + endDaString;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_sch_prev:
			switch (TYP) {
			case 1:
				COUNT--;
				title_schedule.setText(getStartEndOFWeek(COUNT,
						Integer.parseInt(new SimpleDateFormat(
								"yyyy", Locale.ENGLISH)
								.format(new Date(System
										.currentTimeMillis())))));
				break;
			case 2:
				int count_temp = COUNT - 1;
				if (count_temp == -1)
					count_temp = 11;
				COUNT = count_temp;
				title_schedule.setText(mounth[Integer.parseInt(""
						+ COUNT)]);
				break;
			}
			new Fragment_schedule().newInstance(TYP, "" + COUNT);
			break;
		case R.id.btn_sch_next:
			switch (TYP) {
			case 1:
				COUNT++;	
				title_schedule.setText(getStartEndOFWeek(COUNT,
						Integer.parseInt(new SimpleDateFormat(
								"yyyy", Locale.ENGLISH)
								.format(new Date(System
										.currentTimeMillis())))));
				break;
			case 2:
				int count_temp = COUNT + 1;
				if (count_temp == 12)
					count_temp = 0;
				COUNT = count_temp;
				title_schedule.setText(mounth[Integer.parseInt(""
						+ COUNT)]);
				break;
			}
			new Fragment_schedule().newInstance(TYP, "" + COUNT);
			break;	
		}
	}
}
