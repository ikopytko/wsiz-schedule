package pl.wsiz.schedule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Page_choice extends Activity {
	private static final int PICK_FILE_RESULT_CODE = 0;
	String file_path;
	SharedPreferences spf;
	boolean isGood = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choice);

		((Button) findViewById(R.id.btn_choice)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pickFile(new File(Environment.getExternalStorageDirectory() + ""));
			}
		});
		
		((ImageView) findViewById(R.id.choice_help)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(Page_choice.this);
				builder.setTitle("Help").setMessage(getResources().getString(R.string.help)).setCancelable(false)
				        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				            public void onClick(DialogInterface dialog, int id) {
				            	
				            }
				        }).setIcon(R.drawable.help);
				builder.create().show(); 
			}
		});
		
		((Button) findViewById(R.id.btn_choice_accept)).setEnabled(false);
		((Button) findViewById(R.id.btn_choice_back)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("isGood", false);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
				
		((Button) findViewById(R.id.btn_choice_accept)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SetConnect sc = new SetConnect();
				sc.execute();
			}
		});
	}

	void pickFile(File aFile) {
		Intent theIntent = new Intent(this, pl.wsiz.schedule.FileDialog.class);
		theIntent.putExtra("CAN_SELECT_DIR", false);
		theIntent.putExtra("START_PATH", Environment
				.getExternalStorageDirectory().getPath());
		theIntent.putExtra("FORMAT_FILTER", new String[] { "csv" });
		theIntent.putExtra("SELECTION_MODE", /*.MODE_OPEN*/1);
		try {
			startActivityForResult(theIntent, PICK_FILE_RESULT_CODE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			file_path = data.getStringExtra("RESULT_PATH");
			((Button) findViewById(R.id.btn_choice_accept)).setEnabled(true);
			((TextView) findViewById(R.id.edt_choice)).setText(file_path);
		}
	}
	
	class SetConnect extends AsyncTask<Void, Void, Integer> {
		ProgressDialog pd;
		
		@Override
		protected void onPreExecute() {
			pd = ProgressDialog.show(Page_choice.this, getResources().getString(R.string.async_work), getResources().getString(R.string.async_parse), true, false);
		}
		
		@Override
		protected Integer doInBackground(Void... arg0) {
		String db_select = "timetable";
		String line;
		DBHelper dbHelper = new DBHelper(Page_choice.this, "wsiz_sch");
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
							Toast.makeText(Page_choice.this, getResources().getString(R.string.parse_error), Toast.LENGTH_LONG).show();
							return 1;
						}
						String k = line.substring(0,1);
						while ((line = pg_grade.readLine()) != null)
						{
							if (!line.substring(0,1).equalsIgnoreCase(k))
							{
								isGood = false;
								Toast.makeText(Page_choice.this, getResources().getString(R.string.parse_error), Toast.LENGTH_LONG).show();
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
			db.close();
			isGood = true;
		return 0;
	}
		
		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (isGood)
			{
				spf = getSharedPreferences("AppPreferences", MODE_PRIVATE);
				Editor ed = spf.edit();
				ed.putBoolean(Prefs.DB_STATE, true);
				ed.putBoolean(Prefs.APP_MODE, false);
				SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy, HH:mm", getResources().getConfiguration().locale);
				ed.putString(Prefs.LAST_UPDATE_TIME, sdf.format(new Date()));
				ed.commit();
				pd.dismiss();
			}
		Intent intent = new Intent();
		intent.putExtra("isGood", isGood);
		setResult(RESULT_OK, intent);
		finish();
		}
	}
}
