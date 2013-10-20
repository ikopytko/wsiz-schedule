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
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class Page_login extends Activity implements OnClickListener {
	
	private String login;
	private String haslo;
	SharedPreferences spf;
	ProgressDialog pd;

	SetConnect sc;
	final int REQUEST_CODE_CHOICE = 1;
	int act = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		spf = getSharedPreferences("AppPreferences", MODE_PRIVATE);
		
		String tmp_login=spf.getString(Prefs.USER_LOGIN, "Login");
		if (tmp_login.equalsIgnoreCase("")) tmp_login="w54321";
		((EditText) findViewById(R.id.edt_login)).setHint(tmp_login);

		Button btn_login = (Button) findViewById(R.id.btn_login);
		Button btn_dont_login = (Button) findViewById(R.id.btn_dont_login);
		btn_login.setOnClickListener(this);
		btn_dont_login.setOnClickListener(this);
		
		((ImageView) findViewById(R.id.login_info)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(Page_login.this);
				builder.setTitle("Info").setMessage(getResources().getString(R.string.info)).setCancelable(false)
				        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				            public void onClick(DialogInterface dialog, int id) {
				            }
				        }).setIcon(R.drawable.info);
				builder.create().show(); 
			}
		});
	}

	@Override
	public void onClick(View v) {
		login = ((EditText) findViewById(R.id.edt_login)).getText().toString();
		haslo = ((EditText) findViewById(R.id.edt_password)).getText().toString();
		switch (v.getId()) {
		case R.id.btn_login:
			if (!hasInternetConnection())// check internet
			{
				Toast.makeText(Page_login.this, getResources().getString(R.string.settings_error_internet), Toast.LENGTH_SHORT).show();
				break;
			}
			if (!ParseLogin(login)) {// check login/password
				Toast.makeText(Page_login.this, getResources().getString(R.string.settings_wrong_login), Toast.LENGTH_SHORT).show();
				break; 
			} 
			if (!ParsePassword(haslo)) {
				Toast.makeText(Page_login.this, getResources().getString(R.string.settings_wrong_passw), Toast.LENGTH_SHORT).show(); 
				break;
			}
			
			pd = new ProgressDialog(Page_login.this);
		      pd.setTitle(getResources().getString(R.string.async_work));
		      pd.setMessage(getResources().getString(R.string.settings_request));
		      // меняем стиль на индикатор
		      pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		      // устанавливаем максимум
		      pd.setMax(100);
		      // включаем анимацию ожидания
		      pd.setIndeterminate(true);
		      pd.show();
			// start AsyncTask
			sc = new SetConnect();
			sc.execute();
			break;
		case R.id.btn_dont_login:
			boolean state = spf.getBoolean(Prefs.DB_STATE, false);
			if (state)
			{
				finish();
				Page_login.this.startActivity(new Intent(Page_login.this, Page_menu.class));
			}
			else
				Page_login.this.startActivityForResult(new Intent(Page_login.this, Page_choice.class), REQUEST_CODE_CHOICE);
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			boolean isGood = data.getBooleanExtra("isGood", false);
			if (isGood)//start main menu
			{
				finish();
				Page_login.this.startActivity(new Intent(Page_login.this, Page_menu.class));
			}
		}
	}

	class SetConnect extends AsyncTask<Void, Void, Void> {
		
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
			httpParameters = new BasicHttpParams();
			int timeoutConnection = 12000; // ms
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 12000; // ms
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			client = new MyHttpClient(httpParameters, getApplicationContext());
			cookieStore = new BasicCookieStore();
			httpContext = new BasicHttpContext();
			httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
			super.onPreExecute();

			// Show dialog
			/*pd = new ProgressDialog(Page_login.this);
		      pd.setTitle(getResources().getString(R.string.async_work));
		      pd.setMessage(getResources().getString(R.string.settings_request));
		      // меняем стиль на индикатор
		      pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		      // устанавливаем максимум
		      pd.setMax(100);
		      // включаем анимацию ожидания
		      pd.setIndeterminate(true);
		      pd.show();*/
		      
			//pd = ProgressDialog.show(Page_login.this, "Working...", "request to server", true, false);
		}

		@Override
		protected Void doInBackground(Void... params) {
			// connect to page with login & password
			if (firstRequest()) {
				pd.setIndeterminate(false);
				status_login = true;
				// save it is checked
				if (((CheckBox) findViewById(R.id.ch_is_remember)).isChecked()) {
					spf = getSharedPreferences("AppPreferences", MODE_PRIVATE);
					Editor ed = spf.edit();
				    String crypto = "Error";
					try {
						crypto = Secure.encrypt("Sch_for_WSIiZ_stud", haslo);
					} catch (Exception e) { e.printStackTrace(); }

					ed.putString(Prefs.USER_LOGIN, login);
					ed.putString(Prefs.USER_PASSWORD, crypto.toString());
					ed.commit();
				}
				Page_login.this.deleteDatabase("wsiz_sch");
				pd.incrementProgressBy(20);
				gradeRequest();// load grades
				pd.incrementProgressBy(20);
				academic_planRequest(); // load academic plan
				pd.incrementProgressBy(20);
				modulesRequest(); // load groups
				pd.incrementProgressBy(20);
				scheduleRequest();
				pd.incrementProgressBy(20);
				if (act < 4)  result = 2;	// DB error
				result = 0;			// no error
			} else result = 1;		// Wrong pare login & password
			return null;
		}

		@Override
		protected void onPostExecute(Void r) {
			super.onPostExecute(r);
		      switch (result) {
			case 0:
				spf = getSharedPreferences("AppPreferences", MODE_PRIVATE);
				Editor ed = spf.edit();
				ed.putBoolean(Prefs.DB_STATE, true);
				ed.putBoolean(Prefs.APP_MODE, true);
				SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy, HH:mm", getResources().getConfiguration().locale);
				ed.putString(Prefs.LAST_UPDATE_TIME, sdf.format(new Date()));
				ed.commit();
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						finish();
						Page_login.this.startActivity(new Intent(Page_login.this, Page_menu.class));							}
				}, 500);
				break;
			case 1:
				Toast.makeText(Page_login.this, getResources().getString(R.string.settings_wrong), Toast.LENGTH_SHORT).show();
				break;
			case 2:
				Toast.makeText(Page_login.this, "Network error, try again", Toast.LENGTH_SHORT).show();
				break;
			default:
				Toast.makeText(Page_login.this, getResources().getString(R.string.settings_error), Toast.LENGTH_SHORT).show();
				break;
			}
		    pd.dismiss();
		    
		}

		protected boolean firstRequest() // return true in case success login
		{
			BufferedReader pg_main = null;
			try {
				List<NameValuePair> postValPr = new ArrayList<NameValuePair>(2);
				postValPr.add(new BasicNameValuePair("__EVENTTARGET", ""));
				postValPr.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
				postValPr.add(new BasicNameValuePair(POST_EMPTY_PARAM1, ""));
				postValPr.add(new BasicNameValuePair(POST_LOGIN, login));
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
				if ((page.indexOf("Wyloguj")) == -1) {
					return false; //Wrong password or login
				}
				StringBuilder xmlAsString = new StringBuilder(page);
			    
			    Pattern hrefRegex = Pattern.compile("<input type=\"hidden\" name=\"__VIEWSTATE\" id=\"__VIEWSTATE\" value=\"([^\"]*)\" />");
			    Matcher m = hrefRegex.matcher(xmlAsString);
			    if (m.find())
			    	POST_VIEWSTATE = m.group(1);
				
				if (pg_main != null) {
					try {
						pg_main.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return true;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return  false;
		}

		protected void gradeRequest() {
			int deep = 0;
			int deep_prev;
			db_select = "grades";
			String td_name[] ={"name","type","grade","one","two","conditional","advance","commission","deep"};
			DBHelper dbHelper = new DBHelper(Page_login.this, "wsiz_sch");
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
			spf = getSharedPreferences("AppPreferences", MODE_PRIVATE);
			// Add deep to preferences
			Editor ed = spf.edit();
			ed.putInt(Prefs.GLOBAL_DEEP, deep);
			ed.commit();
			c = db.query(db_select, null, null, null, null, null, null);
			if (c.getCount() != 0) act++;
			dbHelper.close();
			db.close();
			c.close();
		}

		protected void academic_planRequest() {
			int deep = 0;
			int deep_prev;
			db_select = "academic_plan";
			String td_name[] ={"","course","teacher","type","cl_per_sem","","examination","deep"};
			DBHelper dbHelper = new DBHelper(Page_login.this, "wsiz_sch");
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
			} while (deep_prev != deep);
			c = db.query(db_select, null, null, null, null, null, null);
			if (c.getCount() != 0) act++;
			dbHelper.close();
			db.close();
			c.close();
		}
		
		protected void modulesRequest() {
			int deep = 0;
			int deep_prev;
			db_select = "modules";
			String td_name[] ={"","groups","type","","deep"};
			DBHelper dbHelper = new DBHelper(Page_login.this, "wsiz_sch");
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
			if (c.getCount() != 0) act++;
			dbHelper.close();
			db.close();
			c.close();
		}

		protected void scheduleRequest() {
			db_select = "timetable";
			DBHelper dbHelper = new DBHelper(Page_login.this, "wsiz_sch");
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
								//line = line.replaceAll("nadzw. ", "");
								String[] RowData = line.split(",");

								String temat = RowData[0];
								temat = temat.replaceAll("№", "ą");
								temat = temat.replaceAll("к", "ę");
								temat = temat.replaceAll("Ј", "Ł");
								temat = temat.replaceAll("і", "ł");
								temat = temat.replaceAll("с", "ń");
								temat = temat.replaceAll("у", "ó");
								temat = temat.replaceAll("њ", "ś");
								temat = temat.replaceAll("џ", "ź");
								temat = temat.replaceAll("ї", "ż");
								String[] RowData1 = temat.split(" - ");

								String subject = RowData1[0];
								String teacher = RowData1[1];
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
				if (c.getCount() != 0) act++;
				dbHelper.close();
				db.close();
				c.close();
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
	
	public boolean hasInternetConnection() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
			return false;
		}
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		if (netInfo == null) {
			return false;
		}
		for (NetworkInfo ni : netInfo) {
			if (ni.getTypeName().equalsIgnoreCase("WIFI"))
				if (ni.isConnected()) {
					//Log.d(this.toString(), "wifi connection");
					return true;
				}
			if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
				if (ni.isConnected()) {
					//Log.d(this.toString(), "mobile connection");
					return true;
				}
		}
		return false;
	}
	
	boolean ParseLogin(String login) {
			if (login.equalsIgnoreCase(""))
				return false;
			Pattern p = Pattern.compile("w([0-9]{5,6})");
			Matcher m = p.matcher(login);
			return m.matches();
		}

	boolean ParsePassword(String password) {
			if (password.equalsIgnoreCase("") || password.length() < 5)
				return false;
			return true;
		}
}