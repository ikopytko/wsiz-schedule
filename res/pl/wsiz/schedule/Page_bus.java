package pl.wsiz.schedule;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.taptwo.android.widget.TitleFlowIndicator;
import org.taptwo.android.widget.ViewFlow;

import pl.wsiz.schedule.adapters.BusAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class Page_bus extends SherlockActivity implements ActionBar.TabListener {
	private String data[], urls[];
	private AlertDialog.Builder adb;
	private ProgressDialog pd;
	
	private ViewFlow viewFlow;
	private TitleFlowIndicator indicator;
	
	private boolean direction = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        setContentView(R.layout.title_layout);
        viewFlow = (ViewFlow) findViewById(R.id.viewflow);
        indicator = (TitleFlowIndicator) findViewById(R.id.viewflowindic);
        
        
        // Check DB
        final DBHelper dbHelper = new DBHelper(Page_bus.this, "bus_tt");
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor c1 = null;
		try {
			c1 = db.query("timetable", null, null, null, null, null, null);
			if (c1.getCount() != 0) {
				// DB exist and not empty
				
				// Add tabs
		        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		        getSupportActionBar().addTab(getSupportActionBar().newTab().setText("Kielnarowa").setTabListener(this));
		        getSupportActionBar().addTab(getSupportActionBar().newTab().setText("Rzeszow").setTabListener(this));
		        
		        // show
				showToday();
				
			} else {
				// DB doesn't exist or empty
				// Ask user access to update DB
				AlertDialog.Builder builder = new AlertDialog.Builder(Page_bus.this);
				builder.setTitle("Info").setMessage("Downoad file?").setCancelable(false)
				        .setPositiveButton(getResources().getString(R.string.choice_ok), new DialogInterface.OnClickListener() {
				            public void onClick(DialogInterface dialog, int id) {
				            	pd = ProgressDialog.show(Page_bus.this,null, "Please Wait ...", true);
				            	dbHelper.close();
				        		db.close();
				            	update();
				            }
				        }).setNegativeButton(getResources().getString(R.string.choice_back), new DialogInterface.OnClickListener() {
				           		public void onClick(DialogInterface dialog, int id) { 
				           		// Go to main menu
				            		finish();
				           		}
				            });
				builder.create().show();
			}
		} catch (Exception e) {e.printStackTrace();}
		finally {
			if (c1 != null)
				c1.close();
		}
		if (dbHelper != null)
			dbHelper.close();
		if (db != null)
			db.close();
    }
    
    void showToday() {
		BusAdapter adapter = new BusAdapter(this, direction);
		viewFlow.setAdapter(adapter);
		indicator.setTitleProvider(adapter);
		
		viewFlow.setFlowIndicator(indicator);
    }
    
    protected void update() {
    	// Check Internet
    	if (!Utils.hasInternetConnection(this))
    	{
    		Toast.makeText(Page_bus.this, "Check your internet connection", Toast.LENGTH_LONG).show();
    		finish();
    	} else {
    		// Load base file
    		DownloadSheet mt = new DownloadSheet();
    		mt.execute();
    	}
    }
    
    // Dialog click listener
    OnClickListener myClickListener = new OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        ListView lv = ((AlertDialog) dialog).getListView();
        // Pressed OK button
        if (which == Dialog.BUTTON_POSITIVE) {
        	// index of selected item doesn't equals -1
        	if (lv.getCheckedItemPosition() != -1)
        	{
        		// put selected item to the [0] position
        		urls[0] = urls[lv.getCheckedItemPosition()];
        		// Download selected file
        		ProgressBack PB = new ProgressBack();
                PB.execute();
        	}
        	else
        	{
        		// If not checked any item
        		// go to main menu
        		finish();
        	}
        }
      }
    };
    
    class DownloadSheet extends AsyncTask<Void, Void, String> {
    	String sheet = "";
    	
        @Override
        protected void onPreExecute() {
          super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
          try {
        	  sheet = downloadSheet(new URI("http://locator.byethost7.com/sheet.wsb")); // http://master.tak-ye.com/sheet.wsb
		}  catch (URISyntaxException e) {
			e.printStackTrace();
		}
          Log.i("BUSTAG", "Im return " + sheet + " from locator.byethost7.com/sheet.wsb");
          return sheet;
        }
        
        @Override
        protected void onPostExecute(String sheet) {
        	super.onPostExecute(sheet);
        	// hide progress dialog
        	pd.dismiss();
        	if (sheet.equalsIgnoreCase("")) {
        		// string empty, http error
        		finish();
      		} else {
      		// parse string
      		// if in page are '<' or '>' then it's error page (Captain Obvious)
				if (sheet.indexOf(">") != -1 || sheet.indexOf("<") != -1) {
					Toast.makeText(Page_bus.this, "Error, try again2", Toast.LENGTH_LONG).show();
					Log.i("BUSTAG", "Error 1");
					Log.i("BUSTAG", sheet);
					finish();
				} else {
					data = sheet.split(";");
					urls = new String[data.length];
				
					for (int i = 0; i < data.length; i++) {
						// copy file address
						urls[i] = data[i].substring(2);
						// assign name of month to sting instead number  XX201310281447
						data[i] = getResources().getStringArray(R.array.mounth)[Integer.parseInt(data[i].substring(0, 2))]; // Integer.parseInt(data[i].substring(0, 2))
					}
					// Show available files
					adb = new AlertDialog.Builder(Page_bus.this);
					adb.setTitle("Select file");
					adb.setPositiveButton("oK", myClickListener);
				}
      		}
        	if (data == null)
        	{
        		Toast.makeText(Page_bus.this, "Error, try again", Toast.LENGTH_LONG).show();
        		finish();
        	} else {
        		adb.setSingleChoiceItems(data, -1, myClickListener);
        		adb.create().show();
        	}
        }
        
        public String downloadSheet(URI url) {
        	StringBuilder builder = new StringBuilder();
			HttpClient client = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);
			
			try {
				HttpResponse response = client.execute(httpGet);
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode == 200) {
					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(content));
					
					String line;
					
					while ((line = reader.readLine()) != null)
						builder.append(line);
				} else {
					Log.e("TAG", "Failed to download file");
				}
				
				
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return builder.toString();
        }
      }
    
    class ProgressBack extends AsyncTask<Void,Void,Void> {
        ProgressDialog PD;
       @Override
       protected void onPreExecute() {
           PD = ProgressDialog.show(Page_bus.this,null, "Please Wait ...", true);
       }

       @Override
       protected Void doInBackground(Void... params) {
    	   String withoutNulls = urls[0].trim();
    	   Log.i("TAG", withoutNulls);
    	   DownloadFile("https://wu.wsiz.rzeszow.pl/wunet/pliki/Kielnarowa/" + withoutNulls + ".xls");
    	   return null;
       }

       @Override
       protected void onPostExecute(Void result) {
    	   super.onPostExecute(result);
           PD.dismiss();
           showToday();
       }
       
       public void DownloadFile(String fileURL) {
           try {
               URL url = new URL(fileURL);
               URLConnection ucon = url.openConnection();
               InputStream is = new BufferedInputStream(ucon.getInputStream());

               Log.i("TAG", "Im inside");
               Workbook w;
               
               // delete old data
               Page_bus.this.deleteDatabase("bus_tt");
               
               DBHelper dbHelper = new DBHelper(Page_bus.this, "bus_tt");
               SQLiteDatabase db = dbHelper.getWritableDatabase();
               String td_name[] ={"date","k1","k2","k3","k4","k5","r1","r2","r3"};
               
               try {
            	   w = Workbook.getWorkbook(is);
            	   // Get the first sheet
                   Sheet sheet = w.getSheet(0);
                   
                   // first column with date
                   String date = ""; 
                   
                   Log.i("BUSTAG", "Im inside 1");
                   
                   // start with 3rd row (shift rows' titles)
                   for (int i = 2; i < sheet.getRows(); i++) {
                	   // pare [column name, value]
                	   ContentValues cv = new ContentValues();
                	   
                	   Cell cell = sheet.getCell(0, i);
                	   
                	   if (!cell.getContents().equalsIgnoreCase(""))
                       { // if it's empty - don't change date else store new
                    	   date = cell.getContents();
                       }
                	   
                	   // parse & insert to store
                	   //date = date.replace('/', '.');
                	   Log.i("TTAG", "ADATE: " + date);
                	   date = date.replace("/", "");
                	   Log.i("TTAG", "BDATE: " + date);
                	   cv.put(td_name[0], date);
                	   
                	   for (int j = 1; j < sheet.getColumns()-1; j++) {
                           cell = sheet.getCell(j, i);
                           String content = cell.getContents();
                           // parse & insert to store
                           content = content.replace('.', ':'); // 22.05 -> 22:05
                           try {
                        	   content = content.split(":")[0] + ":" + content.split(":")[1];
                           } catch (ArrayIndexOutOfBoundsException e) {
                        	   e.printStackTrace();
                           }
                           cv.put(td_name[j], content);
                       }
                	   // write store to DB
                	   db.insert("timetable", null, cv);
                	   cv.clear();
                   }
               } catch (Exception e) {e.printStackTrace();}
               
               if (db != null)
            	   db.close();
               if (dbHelper != null)
            	   dbHelper.close();
               
           } catch (Exception e) {e.printStackTrace();}
       }
   }
    
    @Override
    public void onTabReselected(Tab tab, FragmentTransaction transaction) {
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction transaction1) {
        if (tab.getText().equals("Kielnarowa")) {
        	direction = true;
		} else if (tab.getText().equals("Rzeszow")) {
			direction = false;
		}
        showToday();
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction transaction) {
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuinf = getSupportMenuInflater();
		menuinf.inflate(R.menu.menu_bus, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.item_today:
			viewFlow.setSelection(0);
			break;
		case R.id.item2:
			Page_bus.this.startActivity(new Intent(Page_bus.this,
					Page_settings.class));
			break;
		case R.id.item3:
			Page_bus.this.startActivity(new Intent(Page_bus.this,
					Page_about.class));
			break;
		case R.id.item_update:
			pd = ProgressDialog.show(Page_bus.this,null, "Please Wait ...", true);
			update();
			break;
		case R.id.item4:
			moveTaskToBack(true);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
