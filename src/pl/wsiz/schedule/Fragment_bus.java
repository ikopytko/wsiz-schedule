package pl.wsiz.schedule;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import de.marcreichelt.android.RealViewSwitcher;
import de.marcreichelt.android.RealViewSwitcher.OnScreenSwitchListener;

public class Fragment_bus extends Fragment {
	RealViewSwitcher realViewSwitcher = null;
	Context context = null;
	String currentDate;
	int[] dateArray;
	int[] datePositionArray;
	int dateArraySize;
	int offset = -1;
	/** direct 
	 * false - to Kielnarowa
	 * true - to Rzeszow*/
	boolean direct = false;

	public void newInstance(boolean to) {
		direct = to;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", getResources().getConfiguration().locale);
		currentDate = sdf.format(new Date());
		
		context = inflater.getContext();
		realViewSwitcher = new RealViewSwitcher(context);
		
		Cursor c;
		DBHelper dbHelper = new DBHelper(context, "bus_tt");
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		
		// get all rows
		c = db.query("timetable", null, null, null, null, null, null);
		
		// if table not empty
		if (c.moveToFirst()) {
			// initialize store variables
			datePositionArray = new int[31];
			dateArray = new int[31];
			dateArraySize = 0;
			
			int prew_date = -1;
			for (int i = 0; c != null && i < c.getCount(); i++) {
				if (prew_date == -1 || c.getInt(1) != prew_date)
				{
					dateArray[dateArraySize] = c.getInt(1);
					datePositionArray[dateArraySize] = i;
					dateArraySize++;
					prew_date = c.getInt(1);
				}
				c.moveToNext();
			}
			
			for (int i = 0; i < dateArraySize; i++) {
				if (dateArray[i] == Integer.parseInt(currentDate)) {
					offset = i;
					break;
				}
			}

			// TODO: Test this sh*t
			if (offset != -1) {
				// current date available
				// is it first, middle or last
				if (offset == dateArraySize-1) offset -= 2;
				if (offset != 0) offset -= 1;
			} else {
				// date doesn't find
				// find next date
				offset = 0;
			}
		
			realViewSwitcher.removeAllViews();
			
			c = db.query("timetable", null, null, null, null, null, null);
			
			TableLayout tableLayout = null;
			String date_tmp = null, date;
			
			c.moveToPosition(datePositionArray[offset]);
			int showedScreen = 0;
			
			int k = (direct) ? 3 : 5;
			int n = (direct) ? 5 : 0;
			
			for (int i = 0; c != null && showedScreen < 3; i++) {
				TableRow tableRow1 = new TableRow(context);
				
				date = c.getString(1);
				if (date_tmp == null || !date_tmp.equalsIgnoreCase(date)) {
					if (date_tmp != null && !date_tmp.equalsIgnoreCase(date))
					{
						realViewSwitcher.addView(tableLayout);
						showedScreen++;
					}
					tableLayout = new TableLayout(context);
					tableLayout.setLayoutParams(new TableLayout.LayoutParams(
					        ViewGroup.LayoutParams.MATCH_PARENT,
					        ViewGroup.LayoutParams.WRAP_CONTENT ));
					tableLayout.setStretchAllColumns(true);
					
					TextView textView1 = new TextView(context);
					textView1.setTextSize(18);
					textView1.setPadding(24, 8, 0, 8);
					
					int parseDate = Integer.parseInt(date);
					textView1.setText(String.format("%02d-%02d-%02d", parseDate / 10000, (parseDate % 10000) /100, parseDate % 100));
					textView1.setTypeface(Typeface.SERIF);
					
					TableRow tableRow2 = new TableRow(context);
					tableRow2.addView(textView1);
					tableLayout.addView(tableRow2);
					
					// Add title
					TableRow tableRowTitle = new TableRow(context);
					tableRowTitle.setGravity(Gravity.CENTER_HORIZONTAL);
					VerticalTextView textViewC = new VerticalTextView(context);
					VerticalTextView textViewD = new VerticalTextView(context);
					VerticalTextView textViewE = new VerticalTextView(context);
					
					
					if (direct) {
						textViewC.setText("CTiR");
						textViewD.setText("Tyczyn");
						textViewE.setText("TESCO");
					} else {
						VerticalTextView textViewA = new VerticalTextView(context);
						VerticalTextView textViewB = new VerticalTextView(context);
						textViewA.setText("ul. Ofiar\nKatynia");
						textViewB.setText("Cieplińskiego");
						textViewC.setText("TESCO");
						textViewD.setText("Tyczyn");
						textViewE.setText("CTiR");
						textViewA.setTypeface(Typeface.SERIF);
						textViewB.setTypeface(Typeface.SERIF);
						
						textViewA.setPadding(0, 20, 0, 0);
						textViewB.setPadding(10, 20, 0, 0);
						textViewC.setPadding(0, 15, 0, 0);
						textViewD.setPadding(0, 15, 0, 0);
						textViewE.setPadding(0, 15, 0, 0);
					
						tableRowTitle.addView(textViewA);
						tableRowTitle.addView(textViewB);
					}
					textViewC.setTypeface(Typeface.SERIF);
					textViewD.setTypeface(Typeface.SERIF);
					textViewE.setTypeface(Typeface.SERIF);
					
					tableRowTitle.addView(textViewC);
					tableRowTitle.addView(textViewD);
					tableRowTitle.addView(textViewE);
					tableLayout.addView(tableRowTitle);
				}
				
				for (int j = 0; j < k; j++) {
					TextView textView1 = new TextView(context);
					textView1.setText(c.getString((j+2) + n));
					textView1.setTypeface(Typeface.SERIF);
					textView1.setGravity(Gravity.CENTER);
					tableRow1.addView(textView1);
				}
				if (i%2 != 0)
					tableRow1.setBackgroundColor(0xfff2f2f2);
				else
					tableRow1.setBackgroundColor(0xffd6d6d6);
				
				tableLayout.addView(tableRow1);
				
				date_tmp = date;
				c.moveToNext();
			}
			if (offset != 0)
				realViewSwitcher.setCurrentScreen(1);
			
			c.close();
			dbHelper.close();
			db.close();
			
			realViewSwitcher.setOnScreenSwitchListener(onScreenSwitchListener);
			
		} else {
			TextView textView1 = new TextView(context);
			textView1.setText("Empty. Reload page");
			realViewSwitcher.addView(textView1);
		}
		c.close();
		dbHelper.close();
		db.close();
		return realViewSwitcher;
	}
	
	private final OnScreenSwitchListener onScreenSwitchListener = new OnScreenSwitchListener() {
		@Override
		public void onScreenSwitched(int screen) {
			// this method is executed if a screen has been activated, i.e. the screen is completely visible
			//  and the animation has stopped
			
			if (screen != 1)
			{
				if (screen == 2) // to right 
				{
					//realViewSwitcher.removeViews(0, 1);
				}
				else if (screen == 0) // to left
				{
					//realViewSwitcher.removeViews(2, 1);
				}
			} /*else {
				
				if (screen == 2) // to right
			    	realViewSwitcher.addView(tv);
				else if (screen == 0) // to left
					realViewSwitcher.addView(tv,0);
			}*/
			
			//realViewSwitcher.setCurrentScreen(1);
			}
		};
}
