package pl.wsiz.schedule;

import org.taptwo.android.widget.TitleFlowIndicator;
import org.taptwo.android.widget.ViewFlow;

import pl.wsiz.schedule.adapters.ScheduleDetailsAdapter;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class Schedule_details extends SherlockActivity {
	
	private ViewFlow viewFlow;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.title_layout);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		Intent intent = getIntent();
		int position = intent.getIntExtra("id", 0);
		String firstid = intent.getStringExtra("firstid");
		String lastid = intent.getStringExtra("lastid");
		
		viewFlow = (ViewFlow) findViewById(R.id.viewflow);
		ScheduleDetailsAdapter adapter = new ScheduleDetailsAdapter(this, firstid, lastid);
		viewFlow.setAdapter(adapter);
		viewFlow.setSelection(position);
		
		TitleFlowIndicator indicator = (TitleFlowIndicator) findViewById(R.id.viewflowindic);
		indicator.setTitleProvider(adapter);
		
		viewFlow.setFlowIndicator(indicator);
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
			Schedule_details.this.startActivity(new Intent(
					Schedule_details.this, Page_settings.class));
			break;
		case R.id.item3:
			Schedule_details.this.startActivity(new Intent(
					Schedule_details.this, Page_about.class));
			break;
		case R.id.item4:
			moveTaskToBack(true);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
