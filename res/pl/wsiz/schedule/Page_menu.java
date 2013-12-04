package pl.wsiz.schedule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import pl.wsiz.schedule.https.MyHttpClient;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class Page_menu extends SherlockActivity implements View.OnClickListener {
	private SharedPreferences sp;
	private String haslo;
	Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard_menu);
		context = this;
		
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		
		Button btn_to_schedule = (Button) findViewById(R.id.btn_to_schedule);
		Button btn_to_grades = (Button) findViewById(R.id.btn_to_grades);
		Button btn_to_plan = (Button) findViewById(R.id.btn_to_plan);
		Button btn_to_groups = (Button) findViewById(R.id.btn_to_groups);
		Button btn_to_bus = (Button) findViewById(R.id.btn_to_bus);
		
		btn_to_schedule.setOnClickListener(this);
		btn_to_grades.setOnClickListener(this);
		btn_to_plan.setOnClickListener(this);
		btn_to_groups.setOnClickListener(this);
		btn_to_bus.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_to_schedule:
			Intent intent1 = new Intent(Page_menu.this, Page_schedule.class);
			intent1.putExtra("TYPE", 2);
			//Bundle translateBundle = ActivityOptions.makeCustomAnimation(Page_menu.this,
			//		R.anim.slide_in_left, R.anim.slide_out_left).toBundle();
			Page_menu.this.startActivity(intent1);
			break;
		case R.id.btn_to_grades:
			Page_menu.this.startActivity(new Intent(Page_menu.this, Page_grades.class));
			break;
		case R.id.btn_to_plan:
			Page_menu.this.startActivity(new Intent(Page_menu.this, Page_plan.class));
			break;
		case R.id.btn_to_groups:
			Page_menu.this.startActivity(new Intent(Page_menu.this, Page_groups.class));
			break;
		case R.id.btn_to_bus:
			Page_menu.this.startActivity(new Intent(Page_menu.this, Page_bus.class));
			break;
		}
	}
	 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuinf = getSupportMenuInflater();
		menuinf.inflate(R.menu.default_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_refresh:
			if (Utils.getPrefs(this).getBoolean(Prefs.APP_MODE, false)) // check availible login/password
			{
				haslo = Utils.getPrefs(this).getString(Prefs.USER_PASSWORD, "");
			    try {
					haslo = Secure.decrypt("Sch_for_WSIiZ_stud", haslo);
				} catch (Exception e) { e.printStackTrace(); }
			    
				if (!Utils.hasInternetConnection(this))// check internet
				{
					Toast.makeText(Page_menu.this, getResources().getString(R.string.settings_error_internet), Toast.LENGTH_SHORT).show();
					break;
				}
				// delete db
				DBHelper dbHelper = new DBHelper(Page_menu.this, "wsiz_sch");
				SQLiteDatabase db = dbHelper.getWritableDatabase();

				try {
					if (sp.getBoolean(Prefs.UPDATE_SCHEDULE, true)) {
						db.delete("timetable", null, null);
					}
					if (sp.getBoolean(Prefs.UPDATE_GRADES, true)) {
						db.delete("grades", null, null);
					}
					if (sp.getBoolean(Prefs.UPDATE_PLAN, true)) {
						db.delete("academic_plan", null, null);
					}
					if (sp.getBoolean(Prefs.UPDATE_GROUPS, true)) {
						db.delete("modules", null, null);
					}
				} catch (SQLException sqle) {sqle.printStackTrace();}
				dbHelper.close();
				db.close();
				
				// set connection
				SetConnect sc = new SetConnect();
    			sc.execute();
			} else {
				Toast.makeText(Page_menu.this, getResources().getString(R.string.menu_login), Toast.LENGTH_SHORT).show();
			}
			
			break;
		case R.id.item2:
			Page_menu.this.startActivity(new Intent(Page_menu.this, Page_settings.class));
			break;
		case R.id.item3:
			Page_menu.this.startActivity(new Intent(Page_menu.this, Page_about.class));
			break;
		case R.id.item4:
			moveTaskToBack(true);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	class SetConnect extends AsyncTask<Void, Void, Void> {
		
		ProgressDialog pd;
		HttpParams httpParameters;
		HttpClient client;
		CookieStore cookieStore;
		HttpContext httpContext;
		String NL = System.getProperty("line.separator");
		String line = "";
		String page;
		int result;
		boolean status_login = false;
		String db_select = "";
		
		String POST_VIEWSTATE = null;
		String POST_EVENTVALIDATION = null;
		final String POST_EMPTY_PARAM1 = "ctl00_ctl00_TopMenuPlaceHolder_TopMenuContentPlaceHolder_MenuTop3_menuTop3_ClientState";
		final String POST_LOGIN = "ctl00$ctl00$ContentPlaceHolder$MiddleContentPlaceHolder$txtIdent";
		final String POST_PASSW = "ctl00$ctl00$ContentPlaceHolder$MiddleContentPlaceHolder$txtHaslo";
		final String POST_ZALOGUJ = "ctl00$ctl00$ContentPlaceHolder$MiddleContentPlaceHolder$butLoguj";
		final String POST_POPRZEDNI = "ctl00$ctl00$ContentPlaceHolder$RightContentPlaceHolder$butPop";

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			httpParameters = new BasicHttpParams();
			int timeoutConnection = 12000; // ms
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 12000; // ms
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			client = new MyHttpClient(httpParameters, getApplicationContext());
			cookieStore = new BasicCookieStore();
			httpContext = new BasicHttpContext();
			httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

			// Show dialog
			pd = new ProgressDialog(Page_menu.this);
			pd.setTitle(getResources().getString(R.string.async_work));
			pd.setMessage(getResources().getString(R.string.settings_request));
			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pd.setMax(100);
			pd.setIndeterminate(true);
			pd.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			// connect to page with login & password
			if (firstRequest()) {
				pd.setIndeterminate(false);
				status_login = true;
				
					if (sp.getBoolean(Prefs.UPDATE_SCHEDULE, true)) {
						pd.incrementProgressBy(20);
						scheduleRequest();
					}
					if (sp.getBoolean(Prefs.UPDATE_GRADES, true)) {
						pd.incrementProgressBy(20);
						gradeRequest();// load grades
					}
					if (sp.getBoolean(Prefs.UPDATE_PLAN, true)) {
						pd.incrementProgressBy(20);
						academic_planRequest(); // load academic plan
					}
					if (sp.getBoolean(Prefs.UPDATE_GROUPS, true)) {
						pd.incrementProgressBy(20);
						modulesRequest(); // load groups
					}
					pd.incrementProgressBy(20);
				POST_VIEWSTATE = null;
				POST_EVENTVALIDATION = null;
				//if (act < 4)  result = 2;	// DB error
				result = 0;			// no error
			} else result = 1;		// Wrong pare login & password
			return null;
		}

		@Override
		protected void onPostExecute(Void r) {
			super.onPostExecute(r);
			pd.dismiss();
		      switch (result) {
			case 0:
				Editor ed = Utils.getPrefs(context).edit();
				ed.putBoolean(Prefs.DB_STATE, true);
				ed.putBoolean(Prefs.APP_MODE, true);
				SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy, HH:mm", getResources().getConfiguration().locale);
				ed.putString(Prefs.LAST_UPDATE_TIME, sdf.format(new Date()));
				ed.commit();
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {/*
						btn_to_schedule.setEnabled(true);
						btn_to_grades.setEnabled(true);
						btn_to_plan.setEnabled(true);
						btn_to_groups.setEnabled(true);*/
						
						Toast.makeText(Page_menu.this, getResources().getString(R.string.menu_updated), Toast.LENGTH_SHORT).show();
					}
						//finish();
						//Page_menu.this.startActivity(new Intent(Page_menu.this, Page_menu.class));							}
				}, 500);
				break;
			case 1:
				Toast.makeText(Page_menu.this, getResources().getString(R.string.settings_wrong), Toast.LENGTH_SHORT).show();
				break;
			case 2:
				AlertDialog.Builder builder = new AlertDialog.Builder(Page_menu.this);
				builder.setTitle("Network error").setMessage("Try again?").setCancelable(false)
				        .setPositiveButton(getResources().getString(R.string.choice_ok), new DialogInterface.OnClickListener() {
				            public void onClick(DialogInterface dialog, int id) {
				            	SetConnect sc = new SetConnect();
				    			sc.execute();
				            }
				        }).setNegativeButton(getResources().getString(R.string.choice_back), new DialogInterface.OnClickListener() {
				           		public void onClick(DialogInterface dialog, int id) {}
				            }).setIcon(R.drawable.alerts_and_states_warning);
				builder.create().show();
				break;
			default:
				Toast.makeText(Page_menu.this, getResources().getString(R.string.settings_error), Toast.LENGTH_SHORT).show();
				break;
			}
		}

		protected boolean firstRequest() // return true in case success login
		{
			BufferedReader pg_main = null;
			try {
				List<NameValuePair> postValPr = new ArrayList<NameValuePair>(2);
				postValPr.add(new BasicNameValuePair("__EVENTTARGET", ""));
				postValPr.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
				postValPr.add(new BasicNameValuePair(POST_EMPTY_PARAM1, ""));
				postValPr.add(new BasicNameValuePair(POST_LOGIN, Utils.getPrefs(context).getString(Prefs.USER_LOGIN, "")));
				postValPr.add(new BasicNameValuePair(POST_PASSW,haslo));
				postValPr.add(new BasicNameValuePair(POST_ZALOGUJ, "Zaloguj"));
				// -- Login
				HttpPost request1 = new HttpPost("https://wu.wsiz.rzeszow.pl/wunet/Logowanie2.aspx");
				request1.setEntity(new UrlEncodedFormEntity(postValPr,HTTP.UTF_8));
				HttpResponse response1 = client.execute(request1, httpContext);
				pg_main = new BufferedReader(new InputStreamReader(response1.getEntity().getContent()));
				StringBuffer sb1 = new StringBuffer("");
				while ((line = pg_main.readLine()) != null)
					sb1.append(line + NL);
				pg_main.close();
				page = sb1.toString();
				if ((page.indexOf("Wyloguj")) == -1) { // if button "Log off" not found
					return false; //Wrong password or login
				}
				StringBuilder xmlAsString = new StringBuilder(page);
			    
			    Pattern hrefRegex = Pattern.compile("<input type=\"hidden\" name=\"__VIEWSTATE\" id=\"__VIEWSTATE\" value=\"([^\"]*)\" />");
			    Matcher m = hrefRegex.matcher(xmlAsString);
			    if (m.find())
			    	POST_VIEWSTATE = m.group(1);

			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (pg_main != null) {
					try {
						pg_main.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return true;
		}

		protected void gradeRequest() {
			int deep = 0;
			int deep_prev;
			db_select = "grades";
			String td_name[] ={"name","type","grade","one","two","conditional","advance","commission","deep"};
			DBHelper dbHelper = new DBHelper(Page_menu.this, "wsiz_sch");
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			Cursor c = db.query(db_select, null, null, null, null, null, null);

			do {
				deep_prev = deep;
				BufferedReader pg_grade = null;
				HttpResponse response2 = null;
				StringBuffer sb2;
				try {
					if (deep == 0)
					{
						HttpGet request_oceny;
						request_oceny = new HttpGet("https://wu.wsiz.rzeszow.pl/wunet/OcenyP.aspx");
						request_oceny.setHeader("Host", "wu.wsiz.rzeszow.pl");
						request_oceny.setHeader("Referer", "https://wu.wsiz.rzeszow.pl/wunet/pusta2.aspx");
						request_oceny.setHeader("Connection", "keep-alive");
						response2 = client.execute(request_oceny, httpContext);
					} else { 
						List<NameValuePair> postValPr = new ArrayList<NameValuePair>(2);
						postValPr.add(new BasicNameValuePair("__EVENTTARGET", ""));
						postValPr.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
						postValPr.add(new BasicNameValuePair("__VIEWSTATE", POST_VIEWSTATE));
						postValPr.add(new BasicNameValuePair("__EVENTVALIDATION", POST_EVENTVALIDATION));
						postValPr.add(new BasicNameValuePair(POST_EMPTY_PARAM1, ""));
						postValPr.add(new BasicNameValuePair(POST_POPRZEDNI, "Poprzedni"));
						
						HttpPost request_oceny = new HttpPost("https://wu.wsiz.rzeszow.pl/wunet/OcenyP.aspx");
						request_oceny.setHeader("Host", "wu.wsiz.rzeszow.pl");
						request_oceny.setHeader("Referer", "https://wu.wsiz.rzeszow.pl/wunet/OcenyP.aspx");
						request_oceny.setHeader("Connection", "keep-alive");
						request_oceny.setEntity(new UrlEncodedFormEntity(postValPr));
						response2 = client.execute(request_oceny, httpContext);
					}
					pg_grade = new BufferedReader(new InputStreamReader(response2.getEntity().getContent(), "UTF-8"));
					sb2 = new StringBuffer("");
					
					while ((line = pg_grade.readLine()) != null) {
						sb2.append(line + NL);
					}
					pg_grade.close();
					page = sb2.toString();
					page = page.substring(1);

					if ((page.indexOf("Poprzedni")) != -1)
						deep++;
					
					StringBuilder xmlAsString = new StringBuilder(page);
					
					Pattern hrefRegex = Pattern.compile("<input type=\"hidden\" name=\"__EVENTVALIDATION\" id=\"__EVENTVALIDATION\" value=\"([^\"]*)\" />");
				    Matcher m = hrefRegex.matcher(xmlAsString);
				    if (m.find())
				    	POST_EVENTVALIDATION = m.group(1);
				    
				    hrefRegex = Pattern.compile("<input type=\"hidden\" name=\"__VIEWSTATE\" id=\"__VIEWSTATE\" value=\"([^\"]*)\" />");
				    m = hrefRegex.matcher(xmlAsString);
				    if (m.find())
				    	POST_VIEWSTATE = m.group(1);

					try {
						if (c.getCount() == 0) {
							ContentValues cv = new ContentValues();
							
							TestHtmlParse thp = new TestHtmlParse(page);
							List<TagNode> tr = thp.getDivsByClass("gridDane");
							for (Iterator<TagNode> iterator = tr.iterator(); iterator.hasNext();) {
								int i =0;
								TagNode divElement = (TagNode) iterator.next();
								@SuppressWarnings("unchecked")
								List<TagNode> td = divElement.getAllElementsList(false);
								for (Iterator<TagNode> iterator_child = td.iterator(); iterator_child.hasNext();) {
									TagNode divChElement = (TagNode) iterator_child.next();
									String part = divChElement.getText().toString();
									if (part.equalsIgnoreCase("&nbsp;")) part=" ";
									cv.put(td_name[i], part);
									i++;
								}
								cv.put(td_name[i], deep_prev);
								
						        db.insert(db_select, null, cv);
							}
						}
					} catch (Exception e) {}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (pg_grade != null) {
						try {
							pg_grade.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			} while (deep_prev != deep);
			c = db.query(db_select, null, null, null, null, null, null);
			dbHelper.close();
			db.close();
		}

		protected void academic_planRequest() {
			int deep = 0;
			int deep_prev;
			db_select = "academic_plan";
			String td_name[] ={"","course","teacher","type","cl_per_sem","","examination","deep"};
			DBHelper dbHelper = new DBHelper(Page_menu.this, "wsiz_sch");
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			Cursor c = db.query(db_select, null, null, null, null, null, null);

			do {
				deep_prev = deep;
				BufferedReader pg_grade = null;
				HttpResponse response2 = null;
				StringBuffer sb2;
				try {
					if (deep == 0)
					{
						HttpGet request_oceny;
						request_oceny = new HttpGet("https://wu.wsiz.rzeszow.pl/wunet/PlanStudiow.aspx");
						request_oceny.setHeader("Host", "wu.wsiz.rzeszow.pl");
						request_oceny.setHeader("Referer", "https://wu.wsiz.rzeszow.pl/wunet/pusta2.aspx");
						request_oceny.setHeader("Connection", "keep-alive");
						response2 = client.execute(request_oceny, httpContext);
					} else {
						List<NameValuePair> postValPr = new ArrayList<NameValuePair>(2);
						postValPr.add(new BasicNameValuePair("__EVENTTARGET", ""));
						postValPr.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
						postValPr.add(new BasicNameValuePair("__VIEWSTATE", POST_VIEWSTATE));
						postValPr.add(new BasicNameValuePair("__EVENTVALIDATION", POST_EVENTVALIDATION));
						postValPr.add(new BasicNameValuePair(POST_EMPTY_PARAM1, ""));
						postValPr.add(new BasicNameValuePair(POST_POPRZEDNI, "Poprzedni"));
						
						HttpPost request_oceny = new HttpPost("https://wu.wsiz.rzeszow.pl/wunet/PlanStudiow.aspx");
						request_oceny.setHeader("Host", "wu.wsiz.rzeszow.pl");
						request_oceny.setHeader("Referer", "https://wu.wsiz.rzeszow.pl/wunet/PlanStudiow.aspx");
						request_oceny.setHeader("Connection", "keep-alive");
						request_oceny.setEntity(new UrlEncodedFormEntity(postValPr));
						response2 = client.execute(request_oceny, httpContext);
					}
					pg_grade = new BufferedReader(new InputStreamReader(response2.getEntity().getContent()));
					sb2 = new StringBuffer("");
					while ((line = pg_grade.readLine()) != null)
						sb2.append(line + NL);
					pg_grade.close();
					page = sb2.toString();
					page = page.substring(1);

					StringBuilder xmlAsString = new StringBuilder(page);
					
					Pattern hrefRegex = Pattern.compile("<input type=\"hidden\" name=\"__EVENTVALIDATION\" id=\"__EVENTVALIDATION\" value=\"([^\"]*)\" />");
				    Matcher m = hrefRegex.matcher(xmlAsString);
				    if (m.find())
				    	POST_EVENTVALIDATION = m.group(1);
				    
				    hrefRegex = Pattern.compile("<input type=\"hidden\" name=\"__VIEWSTATE\" id=\"__VIEWSTATE\" value=\"([^\"]*)\" />");
				    m = hrefRegex.matcher(xmlAsString);
				    if (m.find())
				    	POST_VIEWSTATE = m.group(1);
						
					if ((page.indexOf("Poprzedni")) != -1)
						deep++;

					try {
						if (c.getCount() == 0) {
							ContentValues cv = new ContentValues();
							
							TestHtmlParse thp = new TestHtmlParse(page);
							List<TagNode> tr = thp.getDivsByClass("gridDane");
							for (Iterator<TagNode> iterator = tr.iterator(); iterator.hasNext();) {
								int i =0;
								TagNode divElement = (TagNode) iterator.next();
								@SuppressWarnings("unchecked")
								List<TagNode> td = divElement.getAllElementsList(false);
								for (Iterator<TagNode> iterator_child = td.iterator(); iterator_child.hasNext();) {
									TagNode divChElement = (TagNode) iterator_child.next();
									if (i==0 || i==5) { i++; continue; }
									String part = divChElement.getText().toString();
									cv.put(td_name[i], part);
									i++;
								}
								cv.put(td_name[i], deep_prev);
						        db.insert(db_select, null, cv);
							}
						}
					} catch (Exception e) {}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (pg_grade != null) {
						try {
							pg_grade.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				//postValPr.add(new BasicNameValuePair("__EVENTTARGET", ""));
			} while (deep_prev != deep);
			c = db.query(db_select, null, null, null, null, null, null);
			dbHelper.close();
			db.close();
		}
		
		protected void modulesRequest() {
			int deep = 0;
			int deep_prev;
			db_select = "modules";
			String td_name[] ={"","groups","type","","deep"};
			DBHelper dbHelper = new DBHelper(Page_menu.this, "wsiz_sch");
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			Cursor c = db.query(db_select, null, null, null, null, null, null);

			do {
				deep_prev = deep;
				BufferedReader pg_grade = null;
				HttpResponse response2 = null;
				StringBuffer sb2;
				try {
					if (deep == 0)
					{
						HttpGet request_oceny;
						request_oceny = new HttpGet("https://wu.wsiz.rzeszow.pl/wunet/ModulyGrupy.aspx");
						request_oceny.setHeader("Host", "wu.wsiz.rzeszow.pl");
						request_oceny.setHeader("Referer", "https://wu.wsiz.rzeszow.pl/wunet/pusta2.aspx");
						request_oceny.setHeader("Connection", "keep-alive");
						response2 = client.execute(request_oceny, httpContext);
					} else {
						List<NameValuePair> postValPr = new ArrayList<NameValuePair>(2);
						postValPr.add(new BasicNameValuePair("__EVENTTARGET", ""));
						postValPr.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
						postValPr.add(new BasicNameValuePair("__VIEWSTATE", POST_VIEWSTATE));
						postValPr.add(new BasicNameValuePair("__EVENTVALIDATION", POST_EVENTVALIDATION));
						postValPr.add(new BasicNameValuePair(POST_EMPTY_PARAM1, ""));
						postValPr.add(new BasicNameValuePair(POST_POPRZEDNI, "Poprzedni"));
						
						HttpPost request_oceny = new HttpPost("https://wu.wsiz.rzeszow.pl/wunet/ModulyGrupy.aspx");
						request_oceny.setHeader("Host", "wu.wsiz.rzeszow.pl");
						request_oceny.setHeader("Referer", "https://wu.wsiz.rzeszow.pl/wunet/ModulyGrupy.aspx");
						request_oceny.setHeader("Connection", "keep-alive");
						request_oceny.setEntity(new UrlEncodedFormEntity(postValPr));
						response2 = client.execute(request_oceny, httpContext);
					}
					pg_grade = new BufferedReader(new InputStreamReader(response2.getEntity().getContent()));
					sb2 = new StringBuffer("");
					while ((line = pg_grade.readLine()) != null)
						sb2.append(line + NL);
					pg_grade.close();
					page = sb2.toString();
					page = page.substring(1);

					StringBuilder xmlAsString = new StringBuilder(page);
					
					Pattern hrefRegex = Pattern.compile("<input type=\"hidden\" name=\"__EVENTVALIDATION\" id=\"__EVENTVALIDATION\" value=\"([^\"]*)\" />");
				    Matcher m = hrefRegex.matcher(xmlAsString);
				    if (m.find())
				    	POST_EVENTVALIDATION = m.group(1);
				    
				    hrefRegex = Pattern.compile("<input type=\"hidden\" name=\"__VIEWSTATE\" id=\"__VIEWSTATE\" value=\"([^\"]*)\" />");
				    m = hrefRegex.matcher(xmlAsString);
				    if (m.find())
				    	POST_VIEWSTATE = m.group(1);
						
					if ((page.indexOf("Poprzedni")) != -1)
						deep++;

					try {
						if (c.getCount() == 0) {
							ContentValues cv = new ContentValues();
							
							TestHtmlParse thp = new TestHtmlParse(page);
							List<TagNode> tr = thp.getDivsByClass("gridDane");
							for (Iterator<TagNode> iterator = tr.iterator(); iterator.hasNext();) {
								int i =0;
								TagNode divElement = (TagNode) iterator.next();
								@SuppressWarnings("unchecked")
								List<TagNode> td = divElement.getAllElementsList(false);
								for (Iterator<TagNode> iterator_child = td.iterator(); iterator_child.hasNext();) {
									TagNode divChElement = (TagNode) iterator_child.next();
									if (i==3 || i==0) { i++; continue; }
									String part = divChElement.getText().toString();
									cv.put(td_name[i], part);
									i++;
								}
								cv.put(td_name[i], deep_prev);
						        db.insert(db_select, null, cv);
							}
						}
					} catch (Exception e) {}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (pg_grade != null) {
						try {
							pg_grade.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				//postValPr.add(new BasicNameValuePair("__EVENTTARGET", ""));
			} while (deep_prev != deep);
			c = db.query(db_select, null, null, null, null, null, null);
			Editor ed = Utils.getPrefs(context).edit();
			ed.putInt(Prefs.GLOBAL_DEEP, deep);
			ed.commit();
			dbHelper.close();
			db.close();
		}

		protected void scheduleRequest() {
			db_select = "timetable";
			DBHelper dbHelper = new DBHelper(Page_menu.this, "wsiz_sch");
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			Cursor c = db.query(db_select, null, null, null, null, null, null);

				BufferedReader pg_grade = null;
				try {
					HttpGet request_zaj = new HttpGet("https://wu.wsiz.rzeszow.pl/wunet/PodzGodzDruk.aspx?typ=csv");
					request_zaj.setHeader("Host", "wu.wsiz.rzeszow.pl");
					request_zaj.setHeader("Referer","https://wu.wsiz.rzeszow.pl/wunet/PodzGodzDruk0.aspx");
					request_zaj.setHeader("Connection", "keep-alive");
					HttpResponse response3 = client.execute(request_zaj, httpContext);
					pg_grade = new BufferedReader(new InputStreamReader(response3.getEntity().getContent(), "CP1251"));

					if (c.getCount() == 0) {
						try {
							pg_grade.readLine();
							while ((line = pg_grade.readLine()) != null)
							{
								line = line.replaceAll("(\")", "");
								line = line.replaceAll("nadzw. ", "");
								String[] RowData = line.split(",");

								String temat = Utils.replaceSpec(RowData[0]);
								String[] RowData1 = temat.split(" - ");

								String subject = RowData1[0];
								String teacher = "";
								try {
									teacher = RowData1[1];
								} catch (ArrayIndexOutOfBoundsException e) {
									return;
								}
								String date = RowData[1];
								String time_s = RowData[2];
								time_s = time_s.substring(0,5);
								String time_e = RowData[4];
								time_e = time_e.substring(0,5);
								String form = ""+((RowData[13].length()==1)?RowData[13].charAt(0):RowData[13].charAt(1));
								String type = RowData[14];
								type = type.replaceFirst(" ", "");
								String room = RowData[17];
								Calendar mydate = new GregorianCalendar();
								try {
									Date thedate = new SimpleDateFormat("yyyy-MM-dd", getResources().getConfiguration().locale).parse(date);
									mydate.setTime(thedate);
								} catch (ParseException e) {e.printStackTrace();}
								String week = ""+mydate.get(Calendar.WEEK_OF_YEAR);
								String mounth = ""+mydate.get(Calendar.MONTH);
								
								ContentValues cv = new ContentValues();
								cv.put("subject", subject);
								cv.put("teacher", teacher);
								cv.put("date", date);
								cv.put("time_s", time_s);
								cv.put("time_e", time_e);
								cv.put("room", room);
								cv.put("form", form);
								cv.put("type", type);
								cv.put("week", week);
								cv.put("mounth", mounth);
								
								db.insert(db_select, null, cv);
							}
												
						} catch (java.io.IOException e) {e.printStackTrace();}
					} 
					pg_grade.close();	
				} catch (IOException e){
					e.printStackTrace();
				} finally {
					if (pg_grade != null) {
						try {
							pg_grade.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				c = db.query(db_select, null, null, null, null, null, null);
				dbHelper.close();
				db.close();
		}
	}
	
	
	public class TestHtmlParse {
		TagNode rootNode;

		public TestHtmlParse(String htmlPage) throws IOException {
			HtmlCleaner cleaner = new HtmlCleaner();
			rootNode = cleaner.clean(htmlPage);
		}

		List<TagNode> getDivsByClass(String CSSClassname) {
			List<TagNode> divList = new ArrayList<TagNode>();

			TagNode divElements[] = rootNode.getElementsByName("tr", true);
			for (int i = 0; divElements != null && i < divElements.length; i++) {
				String classType = divElements[i].getAttributeByName("class");
				if (classType != null && classType.equals(CSSClassname)) {
					divList.add(divElements[i]);
				}
			}
			return divList;
		}
	}

}
