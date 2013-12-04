package pl.wsiz.schedule.adapters;

import java.util.ArrayList;

import pl.wsiz.schedule.R;
import pl.wsiz.schedule.model.Day;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ScheduleListAdapter extends BaseAdapter {
	LayoutInflater lInflater;
	ArrayList<Day> objects;

	public ScheduleListAdapter(Context context, ArrayList<Day> days) {
		objects = days;
		lInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	// elements amount
	@Override
	public int getCount() {
		return objects.size();
	}

	// element by position
	@Override
	public Object getItem(int position) {
		return objects.get(position);
	}

	// id by position
	@Override
	public long getItemId(int position) {
		return position;
	}

	// list item
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// use created, but unused view
		// (the views that are not visible on the screen)
		View view = convertView;
		if (view == null) {
			view = lInflater.inflate(R.layout.list_schedule, parent, false);
		}

		Day p = getDay(position);

		// fill item view
		TextView title = (TextView) view.findViewById(R.id.list_header_title);
		if (is_lecture_first_in_day(position)) { // Show day name?
			title.setVisibility(View.VISIBLE);
			title.setText(p.label);
		} else {
			title.setVisibility(View.GONE);
		}

		((TextView) view.findViewById(R.id.lc_title)).setText(p.title);
		((TextView) view.findViewById(R.id.lc_time)).setText(p.time);
		((TextView) view.findViewById(R.id.lc_locale)).setText(p.info);
		((ImageView) view.findViewById(R.id.lc_image))
				.setImageResource(p.image);

		// write position
		view.setTag(p.id);
		return view;
	}

	boolean is_lecture_first_in_day(int position) {
		if (position == 0)
			return true;

		if (!getDay(position).date.equalsIgnoreCase(getDay(position - 1).date))
			return true;

		return false;
	}

	// day by position
	Day getDay(int position) {
		return ((Day) getItem(position));
	}
}
