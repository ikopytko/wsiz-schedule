package pl.wsiz.schedule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
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
import android.os.Environment;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class Page_settings extends SherlockPreferenceActivity {
	private SharedPreferences spf, sp;
	private static final int PICK_FILE_RESULT_CODE = 0;
	private String file_path;
	
	CheckBoxPreference auto_refresh;

	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		spf = getSharedPreferences("AppPreferences", MODE_PRIVATE);
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		
		findPreference("info_update").setSummary(spf.getString(Prefs.LAST_UPDATE_TIME, "newer :("));
		
		if (spf.getString(Prefs.USER_LOGIN, "").equalsIgnoreCase("")) 
			findPreference("button_delete_login_password").setEnabled(false);
		if (!spf.getBoolean(Prefs.DB_STATE, false)) 
			findPreference("button_delete_db").setEnabled(false);
		
		findPreference("user_update").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) { 
            	pickFile(new File(Environment.getExternalStorageDirectory() + ""));
                return true;
            }
        });
		
		findPreference("user_update_next").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) { 
            	ParseFile sc = new ParseFile();
				sc.execute();
                return true;
            }
        });
		
		findPreference("button_delete_login_password").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) { 
            	AlertDialog.Builder builder = new AlertDialog.Builder(Page_settings.this);
				builder.setTitle(getResources().getString(R.string.settings_warning)).setMessage(getResources().getString(R.string.settings_warning_message_1)).setCancelable(false)
				        .setPositiveButton(getResources().getString(R.string.choice_ok), new DialogInterface.OnClickListener() {
				            public void onClick(DialogInterface dialog, int id) {
				            	Editor ed = spf.edit();
								ed.putString(Prefs.USER_LOGIN, "");
								ed.putString(Prefs.USER_PASSWORD, "");
								ed.putBoolean(Prefs.APP_MODE, false);
								ed.commit();
								
								auto_refresh.setEnabled(false);
								ed = sp.edit();
								ed.putBoolean("auto_refresh", false);
								ed.commit();
								Toast.makeText(Page_settings.this, getResources().getString(R.string.settings_deleted), Toast.LENGTH_SHORT).show(); 
				            }
				        }).setNegativeButton(getResources().getString(R.string.choice_back), new DialogInterface.OnClickListener() {
				           		public void onClick(DialogInterface dialog, int id) {}
				            });
				builder.create().show();
                return true;
            }
        });
		
		findPreference("button_delete_db").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) { 
            	AlertDialog.Builder builder = new AlertDialog.Builder(Page_settings.this);
				builder.setTitle(getResources().getString(R.string.settings_warning)).setMessage(getResources().getString(R.string.settings_warning_message_1)).setCancelable(false)
				        .setPositiveButton(getResources().getString(R.string.choice_ok), new DialogInterface.OnClickListener() {
				            public void onClick(DialogInterface dialog, int id) {
				            	Context context = getApplicationContext();
								context.deleteDatabase("wsiz_sch");
								findPreference("info_update").setSummary("newer :(");
								Editor ed = spf.edit();
								ed.putBoolean(Prefs.DB_STATE, false);
								ed.putInt(Prefs.GLOBAL_DEEP, 0);
								ed.putString(Prefs.LAST_UPDATE_TIME, "newer :(");
								ed.commit();
								Toast.makeText(Page_settings.this, getResources().getString(R.string.settings_deleted), Toast.LENGTH_SHORT).show(); 
				            }
				        }).setNegativeButton(getResources().getString(R.string.choice_back), new DialogInterface.OnClickListener() {
				           		public void onClick(DialogInterface dialog, int id) {}
				            });
				builder.create().show(); 
                return true;
            }
        });
		
		findPreference("edit_login").setSummary(spf.getString(Prefs.USER_LOGIN, ""));
		
		findPreference("button_check").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) { 
            	if (!hasInternetConnection()) {
            		Toast.makeText(Page_settings.this, getResources().getString(R.string.settings_error_internet), Toast.LENGTH_SHORT).show();
            		return true;
            	}
            	if (!ParseLogin(findPreference("edit_login").getSummary().toString())) {
            		Toast.makeText(Page_settings.this, getResources().getString(R.string.settings_wrong_login), Toast.LENGTH_SHORT).show();
            		return true;
            	}
            	if (!ParsePassword(findPreference("edit_password").getSummary().toString())) {
            		Toast.makeText(Page_settings.this, getResources().getString(R.string.settings_wrong_passw), Toast.LENGTH_SHORT).show();
            		return true;
            	}
            	if (findPreference("edit_password").getSummary().toString().equalsIgnoreCase("Not changed")) {
            		Toast.makeText(Page_settings.this, getResources().getString(R.string.settings_passw_new), Toast.LENGTH_SHORT).show();
            		return true;
            	}
            	SetConnect sc = new SetConnect();
    			sc.execute();
            	
            	Editor ed = sp.edit();
				ed.putString("edit_login", "");
				ed.putString("edit_password", "");
				ed.commit();
				return true;
            }
        });
		
		findPreference("edit_login").setOnPreferenceChangeListener(
				new Preference.OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference p,
				Object newValue) {
				// Set the summary based on the new label.
					findPreference("edit_login").setSummary((String) newValue);
				return true;
				}
				});
		findPreference("edit_password").setOnPreferenceChangeListener(
				new Preference.OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference p,
				Object newValue) {
				// Set the summary based on the new label.
					findPreference("edit_password").setSummary((String) newValue);
				return true;
				}
				});
	}
	
	void pickFile(File aFile) {
		Intent theIntent = new Intent(this, pl.wsiz.schedule.FileDialog.class);
		theIntent.putExtra("CAN_SELECT_DIR", false);
		theIntent.putExtra("START_PATH", Environment
				.getExternalStorageDirectory().getPath());
		theIntent.putExtra("FORMAT_FILTER", new String[] { "csv" });
		theIntent.putExtra("SELECTION_MODE", /*SelectionMode.MODE_OPEN*/1);
		try {
			startActivityForResult(theIntent, PICK_FILE_RESULT_CODE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			file_path = data.getStringExtra("RESULT_PATH");
			findPreference("user_update_next").setEnabled(true);
			findPreference("user_update").setSummary(file_path);
		}
	}

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
		
		String POST_VIEWSTATE = null;
		String POST_EVENTVALIDATION = null;
		final String POST_EMPTY_PARAM1 = "ctl00_ctl00_TopMenuPlaceHolder_TopMenuContentPlaceHolder_MenuTop3_menuTop3_ClientState";
		final String POST_LOGIN = "ctl00$ctl00$ContentPlaceHolder$MiddleContentPlaceHolder$txtIdent";
		final String POST_PASSW = "ctl00$ctl00$ContentPlaceHolder$MiddleContentPlaceHolder$txtHaslo";
		final String POST_ZALOGUJ = "ctl00$ctl00$ContentPlaceHolder$MiddleContentPlaceHolder$butLoguj";
		
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
			pd = new ProgressDialog(Page_settings.this);
		      pd.setTitle(getResources().getString(R.string.async_work));
		      pd.setMessage(getResources().getString(R.string.settings_request));
		      // меняем стиль на индикатор
		      pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		      // устанавливаем максимум
		      pd.setMax(100);
		      // включаем анимацию ожидания
		      pd.setIndeterminate(true);
		      pd.show();
		 }

		@SuppressWarnings("deprecation")
		@Override
		protected Void doInBackground(Void... params) {
			// connect to page with login & password
			if (firstRequest()) {
				status_login = true;
				// save it is checked
				spf = getSharedPreferences("AppPreferences", MODE_PRIVATE);
				Editor ed = spf.edit();
				String crypto = "Error";
				try {
					crypto = Secure.encrypt("Sch_for_WSIiZ_stud", findPreference("edit_password").getSummary().toString());
				} catch (Exception e) { e.printStackTrace(); }

				ed.putString(Prefs.USER_LOGIN, findPreference("edit_login").getSummary().toString());
				ed.putString(Prefs.USER_PASSWORD, crypto.toString());
				ed.putBoolean(Prefs.APP_MODE, true);
				ed.commit();
				result = 0;			// no error
			} else result = 1;		// Wrong pare login & password
			return null;
		}

		@Override
		protected void onPostExecute(Void r) {
			super.onPostExecute(r);
		      switch (result) {
			case 0:
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(Page_settings.this, getResources().getString(R.string.settings_saved), Toast.LENGTH_SHORT).show();							}
				}, 500);
				break;
			case 1:
				Toast.makeText(Page_settings.this, getResources().getString(R.string.settings_wrong), Toast.LENGTH_SHORT).show();
				break;
			default:
				Toast.makeText(Page_settings.this, getResources().getString(R.string.settings_error), Toast.LENGTH_SHORT).show();
				break;
			}
		    pd.dismiss();
		    
		}

		@SuppressWarnings("deprecation")
		protected boolean firstRequest() // return true in case success login
		{
			BufferedReader pg_main = null;
			try {
				List<NameValuePair> postValPr = new ArrayList<NameValuePair>(2);
				postValPr.add(new BasicNameValuePair("__EVENTTARGET", ""));
				postValPr.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
				postValPr.add(new BasicNameValuePair(POST_EMPTY_PARAM1, ""));
				postValPr.add(new BasicNameValuePair(POST_LOGIN, findPreference("edit_login").getSummary().toString()));
				postValPr.add(new BasicNameValuePair(POST_PASSW,findPreference("edit_password").getSummary().toString()));
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
				try {
					pg_main.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} catch (ClientProtocolException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
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

	class ParseFile extends AsyncTask<Void, Void, Integer> {
		ProgressDialog pd;
		private boolean isGood = false;
		
		@Override
		protected void onPreExecute() {
			pd = ProgressDialog.show(Page_settings.this, getResources().getString(R.string.async_work), getResources().getString(R.string.async_parse), true, false);
		}
		
		@Override
		protected Integer doInBackground(Void... arg0) {
		String db_select = "timetable";
		String line;
		DBHelper dbHelper = new DBHelper(Page_settings.this, "wsiz_sch");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor c = db.query(db_select, null, null, null, null, null, null);

			BufferedReader pg_grade = null;
			
			try {
				pg_grade = new BufferedReader(new InputStreamReader(new FileInputStream(new File(file_path)), "CP1251"));
				if (c.getCount() == 0) {
					try {
						line = pg_grade.readLine();
						if (line.length() != 390 
								|| !line.substring(0,27).equalsIgnoreCase("\"Temat\",\"Data rozpoczкcia\","))
						{
							isGood = false;
							Toast.makeText(Page_settings.this, getResources().getString(R.string.parse_error), Toast.LENGTH_LONG).show();
							return 1;
						}
						String k = line.substring(0,1);
						while ((line = pg_grade.readLine()) != null)
						{
							if (!line.substring(0,1).equalsIgnoreCase(k))
							{
								isGood = false;
								Toast.makeText(Page_settings.this, getResources().getString(R.string.parse_error), Toast.LENGTH_LONG).show();
								return 1;
							}
							line = line.replaceAll("(\")", "");
							line = line.replaceAll("nadzw. ", "");
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
					} catch (IOException e) {e.printStackTrace();}
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
			dbHelper.close();
			isGood = true;
		return 0;
	}
		
		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (isGood)
			{
				spf = getSharedPreferences("AppPreferences", MODE_PRIVATE);
				Editor ed = spf.edit();
				ed.putBoolean(Prefs.DB_STATE, true); 
				SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy, HH:mm", getResources().getConfiguration().locale);
				ed.putString(Prefs.LAST_UPDATE_TIME, sdf.format(new Date()));
				ed.commit();
				findPreference("info_update").setSummary(sdf.format(new Date()));
				pd.dismiss();
				Toast.makeText(Page_settings.this, "Successfully saved!", Toast.LENGTH_SHORT).show();
			} else Toast.makeText(Page_settings.this, "Failed! Try again", Toast.LENGTH_SHORT).show();
		}
	}
}