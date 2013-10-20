package pl.wsiz.schedule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import de.marcreichelt.android.RealViewSwitcher;

public class Schedule_details extends SherlockActivity {
	private int id; // INTENT
	
	private RealViewSwitcher realViewSwitcher;
	private Cursor c;
	private String[] selectionArgs = null;
	private DBHelper dbHelper;
	private SQLiteDatabase db;

	private String tuday;
	private String tomorrw;
	private SimpleDateFormat sdf;
	private int ID;
	private String SUBJECT;
	private String ts1, ts2, ts3;
	private String ti1, ti2, ti3;
	private int s = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		realViewSwitcher = new RealViewSwitcher(getApplicationContext());
		sdf = new SimpleDateFormat("yyyy-MM-dd", this.getResources().getConfiguration().locale);
		 tuday = sdf.format(new Date(System.currentTimeMillis()));
		 tomorrw = sdf.format(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000));

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		Intent intent = getIntent();
		id = Integer.parseInt(intent.getStringExtra("id"));
		
		dbHelper = new DBHelper(this, "wsiz_sch");
		db = dbHelper.getWritableDatabase();

		selectionArgs = new String[] { ""+(id-1), ""+(id+1) };
		c = db.query("timetable", null, "id BETWEEN ? AND ?", selectionArgs, null, null, null);
		
		SUBJECT = "subject";
		String TEACHER = "teacher";
		String DATE = "date";
		String TIME_S = "time_s";
		String TIME_E = "time_e";
		String ROOM = "room";
		String FORM = "form";
		String TYPE = "type";
		
		if (c != null && c.moveToFirst()) {
				do { s++;
				
				
				if (c.getCount() == 2 && s==3){
					TextView tv = new TextView(Schedule_details.this);
					tv.setText("Empty");
					realViewSwitcher.addView(tv);
					continue;
				}
					ID = c.getInt(c.getColumnIndex("id"));
					SUBJECT = c.getString(c.getColumnIndex("subject"));
					TEACHER = c.getString(c.getColumnIndex("teacher"));
					DATE = c.getString(c.getColumnIndex("date"));
					TIME_S = c.getString(c.getColumnIndex("time_s"));
					TIME_E = c.getString(c.getColumnIndex("time_e"));
					ROOM = c.getString(c.getColumnIndex("room"));
					FORM = c.getString(c.getColumnIndex("form"));
					TYPE = c.getString(c.getColumnIndex("type"));
					if (s==1) {ts1 = SUBJECT; ti1 = FORM;}
					if (s==2) {ts2 = SUBJECT; ti2 = FORM;}
					if (s==3) {ts3 = SUBJECT; ti3 = FORM;}
					if (ID == id && s==1){
						TextView tv = new TextView(Schedule_details.this);
						tv.setText("Empty");
						realViewSwitcher.addView(tv);
						continue;
					}
					
					if (ID == (id+1) && s==1){
						TextView tv = new TextView(Schedule_details.this);
						tv.setText("Empty");
						realViewSwitcher.addView(tv);
						continue;
					}
					
					if (c.getString(c.getColumnIndex("id")).equalsIgnoreCase(""+(id+1)) && s==2){
						TextView tv = new TextView(Schedule_details.this);
						tv.setText("Empty");
						realViewSwitcher.addView(tv,0);
					} else {
					
					String locale;
					switch (ROOM.charAt(0)) {
					case 'R':
						locale = "Rzeszów, ";
						break;
					case 'K':
						locale = "Kielnarowa, ";
						break;
					case 'T':
						locale = "Tyczyn, ";
						break;
					default:
						locale = "";
						break;
					}
					
					Calendar mydate = new GregorianCalendar();
					try {
						Date thedate = sdf.parse(DATE);
						mydate.setTime(thedate);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					String[] days = this.getResources().getStringArray(
							R.array.days);
					String day = days[mydate
							.get(Calendar.DAY_OF_WEEK) - 2];
					if (DATE.equalsIgnoreCase(tuday))
						day = days[7]; // today
					if (DATE.equalsIgnoreCase(tomorrw))
						day = days[8]; // tomorrow
					
					ImageView pic = new ImageView(this); 
					switch (FORM.charAt(0)) {
					case 'C':
						pic.setImageResource(R.drawable.c);
						break;
					case 'L':
						pic.setImageResource(R.drawable.l);
						break;
					case 'W':
						pic.setImageResource(R.drawable.w);
						break;
					case 'J':
						pic.setImageResource(R.drawable.j);
						break;
					case 'D':
						pic.setImageResource(R.drawable.jd);
						break;
					case 'K':
						pic.setImageResource(R.drawable.k);
						break;
					case 'F':
						pic.setImageResource(R.drawable.f);
						break;
					case 'P':
						pic.setImageResource(R.drawable.sp);
						break;
					default:
						pic.setImageResource(R.drawable.i);
						break;
					}
				    pic.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)); 
				    
				    TextView label1 = new TextView(this); 
				    label1.setText(SUBJECT); 
				    //label.setGravity(Gravity.CENTER_HORIZONTAL); 
				    label1.setPadding(10, 0, 0, 0);
				    label1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				    
				    LinearLayout ll_i = new LinearLayout(this); 
				    ll_i.setOrientation(LinearLayout.HORIZONTAL); 
				    ll_i.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)); 
				    ll_i.setPadding(0, 30, 0, 10);//(left, top, right, bottom);
				    ll_i.addView(pic);
				    ll_i.addView(label1); 

				    TextView label2 = new TextView(this); 
				    label2.setText(TEACHER); 
				    label2.setPadding(5, 8, 5, 8);
				    label2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

				    TextView label3 = new TextView(this); 
				    label3.setText(DATE+", "+day); 
				    label3.setPadding(5, 8, 5, 8);
				    label3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
				    
				    TextView label4 = new TextView(this); 
				    label4.setText(TIME_S+" - "+TIME_E); 
				    label4.setPadding(5, 8, 5, 8);
				    label4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
				    
				    TextView label5 = new TextView(this); 
				    label5.setText(locale+ROOM+" - "+TYPE); 
				    label5.setPadding(5, 8, 5, 8);
				    label5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
				    
				    Button but = new Button(this);
				    but.setText(getResources().getString(R.string.show_less));
				    but.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							Intent intent1 = new Intent(Schedule_details.this, Page_less.class);
								intent1.putExtra("title", ts2);
								intent1.putExtra("id", ti2);
							startActivity(intent1);
						}
					});
				    
				    LinearLayout ll = new LinearLayout(this); 
				    ll.setOrientation(LinearLayout.VERTICAL); 
				    ll.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)); 
				    ll.addView(ll_i); 
				    ll.addView(label2);
				    ll.addView(label3);
				    ll.addView(label4);
				    ll.addView(label5);
				    ll.addView(but);
					realViewSwitcher.addView(ll);
					
					c.moveToNext();
				}
				} while (s < 3);
		}
		c.close();
		realViewSwitcher.setCurrentScreen(1);
		// set as content view
		setContentView(realViewSwitcher);
		
		dbHelper.close();
		realViewSwitcher.setOnScreenSwitchListener(onScreenSwitchListener);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		realViewSwitcher = null;
		c = null;
		selectionArgs = null;
		dbHelper.close();
		db.close();
		sdf = null;
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

	private final RealViewSwitcher.OnScreenSwitchListener onScreenSwitchListener = new RealViewSwitcher.OnScreenSwitchListener() {
		
		@Override
		public void onScreenSwitched(int screen) {
			// this method is executed if a screen has been activated, i.e. the screen is completely visible
			//  and the animation has stopped
			if (screen != 1)
			{
				if (screen == 2) // to right 
				{
					ts1 = ts2;
					ts2 = ts3;
					ti1 = ti2;
					ti2 = ti3;
					realViewSwitcher.removeViews(0, 1);
					realViewSwitcher.setCurrentScreen(1);
					id++;
					selectionArgs = new String[] { ""+(id+1) };
				}
				else if (screen == 0) // to left
				{
					
					ts3 = ts2;
					ts2 = ts1;
					ti3 = ti2;
					ti2 = ti1;
					realViewSwitcher.removeViews(2, 1);
					realViewSwitcher.setCurrentScreen(0);
					id--;
					selectionArgs = new String[] { ""+(id-1) };
				}
			db = dbHelper.getWritableDatabase();
			c = db.query("timetable", null, "id = ?", selectionArgs, null, null, null);
			
			String SUBJECT = "subject";
			String TEACHER = "teacher";
			String DATE = "date";
			String TIME_S = "time_s";
			String TIME_E = "time_e";
			String ROOM = "room";
			String FORM = "form";
			String TYPE = "type";
			
			if (c != null && c.moveToFirst() && !c.getString(c.getColumnIndex("id")).equalsIgnoreCase("1"))
					{
						SUBJECT = c.getString(c.getColumnIndex("subject"));
						TEACHER = c.getString(c.getColumnIndex("teacher"));
						DATE = c.getString(c.getColumnIndex("date"));
						TIME_S = c.getString(c.getColumnIndex("time_s"));
						TIME_E = c.getString(c.getColumnIndex("time_e"));
						ROOM = c.getString(c.getColumnIndex("room"));
						FORM = c.getString(c.getColumnIndex("form"));
						TYPE = c.getString(c.getColumnIndex("type"));

						if (screen == 0) {ts1 = SUBJECT; ti1 = c.getString(c.getColumnIndex("form"));}
						if (screen == 2) {ts3 = SUBJECT; ti3 = c.getString(c.getColumnIndex("form"));}
						String locale;
						switch (ROOM.charAt(0)) {
						case 'R':
							locale = "Rzeszów, ";
							break;
						case 'K':
							locale = "Kielnarowa, ";
							break;
						default:
							locale = "";
							break;
						}
						
						Calendar mydate = new GregorianCalendar();
						try {
							Date thedate = sdf.parse(DATE);
							mydate.setTime(thedate);
						} catch (ParseException e) {
							e.printStackTrace();
						}
						String[] days = Schedule_details.this.getResources().getStringArray(
								R.array.days);
						String day = days[mydate
								.get(Calendar.DAY_OF_WEEK) - 2];
						if (DATE.equalsIgnoreCase(tuday))
							day = days[7]; // today
						if (DATE.equalsIgnoreCase(tomorrw))
							day = days[8]; // tomorrow
						
						ImageView pic = new ImageView(Schedule_details.this); 
						switch (FORM.charAt(0)) {
						case 'C':
							pic.setImageResource(R.drawable.c);
							break;
						case 'L':
							pic.setImageResource(R.drawable.l);
							break;
						case 'W':
							pic.setImageResource(R.drawable.w);
							break;
						case 'J':
							pic.setImageResource(R.drawable.j);
							break;
						case 'D':
							pic.setImageResource(R.drawable.jd);
							break;
						case 'K':
							pic.setImageResource(R.drawable.k);
							break;
						case 'F':
							pic.setImageResource(R.drawable.f);
							break;
						case 'P':
							pic.setImageResource(R.drawable.sp);
							break;
						default:
							pic.setImageResource(R.drawable.i);
							break;
						}
					    pic.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)); 
					    
					    TextView label1 = new TextView(Schedule_details.this); 
					    label1.setText(SUBJECT); 
					    label1.setPadding(10, 0, 0, 0);
					    label1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
					    
					    LinearLayout ll_i = new LinearLayout(Schedule_details.this); 
					    ll_i.setOrientation(LinearLayout.HORIZONTAL); 
					    ll_i.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)); 
					    ll_i.setPadding(0, 30, 0, 10);
					    ll_i.addView(pic);
					    ll_i.addView(label1); 

					    TextView label2 = new TextView(Schedule_details.this); 
					    label2.setText(TEACHER); 
					    label2.setPadding(5, 8, 5, 8);
					    label2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

					    TextView label3 = new TextView(Schedule_details.this); 
					    label3.setText(DATE+", "+day); 
					    label3.setPadding(5, 8, 5, 8);
					    label3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
					    
					    TextView label4 = new TextView(Schedule_details.this); 
					    label4.setText(TIME_S+" - "+TIME_E); 
					    label4.setPadding(5, 8, 5, 8);
					    label4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
					    
					    TextView label5 = new TextView(Schedule_details.this); 
					    label5.setText(locale+ROOM+" - "+TYPE); 
					    label5.setPadding(5, 8, 5, 8);
					    label5.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
					    
					    Button but = new Button(Schedule_details.this);
					    but.setText(getResources().getString(R.string.show_less));
					    but.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								Intent intent1 = new Intent(Schedule_details.this, Page_less.class);
									intent1.putExtra("title", ts2);
									intent1.putExtra("id", ti2);
								startActivity(intent1);
							}
						});
					    
					    LinearLayout ll = new LinearLayout(Schedule_details.this); 
					    ll.setOrientation(LinearLayout.VERTICAL); 
					    ll.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)); 
					    ll.addView(ll_i); 
					    ll.addView(label2);
					    ll.addView(label3);
					    ll.addView(label4);
					    ll.addView(label5);
					    ll.addView(but);
					    
					    if (screen == 2) // to right 
						{
					    	realViewSwitcher.addView(ll);
						}
						else if (screen == 0) // to left
						{
							realViewSwitcher.addView(ll,0);
						}
			} else {
				TextView tv = new TextView(Schedule_details.this);
				tv.setText("Empty");
				if (screen == 2) // to right
			    	realViewSwitcher.addView(tv);
				else if (screen == 0) // to left
					realViewSwitcher.addView(tv,0);
			}
			c.close();
			dbHelper.close();
			db.close();
			
			//setContentView(realViewSwitcher);
			realViewSwitcher.setCurrentScreen(1);
			}
		}
		
	};
}
