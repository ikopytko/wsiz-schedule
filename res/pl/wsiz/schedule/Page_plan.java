package pl.wsiz.schedule;

import org.taptwo.android.widget.TitleFlowIndicator;
import org.taptwo.android.widget.ViewFlow;

import pl.wsiz.schedule.adapters.PlanAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class Page_plan extends SherlockActivity {

	private ViewFlow viewFlow;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.title_layout);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
        viewFlow = (ViewFlow) findViewById(R.id.viewflow);
        TitleFlowIndicator indicator = (TitleFlowIndicator) findViewById(R.id.viewflowindic);
        
		SharedPreferences spf = getSharedPreferences("AppPreferences", MODE_PRIVATE);
		
		PlanAdapter adapter = new PlanAdapter(this, spf.getInt(Prefs.GLOBAL_DEEP, 0));
		viewFlow.setAdapter(adapter);
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
}
