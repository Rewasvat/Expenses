package omar.expenses.adapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import omar.expenses.R;
import omar.expenses.exceptions.ExpenseException;
import omar.expenses.persist.DBManager;
import omar.expenses.persist.model.Expense;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

@SuppressLint({ "SimpleDateFormat", "UseSparseArrays" })
public class MonthlySelectionAdapter implements SpinnerAdapter {
	
	private class MonthlyItem {
		private Integer month;
		private Integer expenseCount;
		private Double balance;
		
		public MonthlyItem(int month) {
			this.month = month;
			this.expenseCount = 0;
			this.balance = 0.0;
		}
	}
	
	private ArrayList<MonthlyItem> items;

	public MonthlySelectionAdapter() {
		List<Expense> expenses = null;
		try {
			expenses = DBManager.getInstance().getAll(new Expense());
		} catch (ExpenseException e) {
			e.printStackTrace();
		}
		
		HashMap<Integer, MonthlyItem> map = new HashMap<Integer, MonthlyItem>();
		for (Expense exp : expenses) {
			int month = getMonthValueFrom(exp.getData());
			if (!map.containsKey(month)) {
				map.put(month, new MonthlyItem(month));
			}
			map.get(month).expenseCount += 1;
			double valor = exp.getValor();
			map.get(month).balance += (exp.getDespesa()) ? -valor : valor;
		}
		
		int today = getMonthValueFrom(new Date());
		if (!map.containsKey(today)) {
			map.put(today, new MonthlyItem(today));
		}
		
		items = new ArrayList<MonthlyItem>();
		items.addAll(map.values());
		Collections.sort(items, new Comparator<MonthlyItem>() {
			@Override
			public int compare(final MonthlyItem lhs, final MonthlyItem rhs) {
				return lhs.month.compareTo(rhs.month);
			}
		});
	}
	
	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return items.get(position).month;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		MonthlyItem m = items.get(position);
		
		View view;
		if (convertView != null)
			view = convertView;
		else {
			LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.monthly_selection_item, parent, false);
		}
		
		SimpleDateFormat df = new SimpleDateFormat("MMM/yyyy");
		
		setTextAttrs(view, R.id.monthly_month, df.format(getMonthFromValue(m.month)), Color.LTGRAY );
		setTextAttrs(view, R.id.monthly_count, m.expenseCount+" items", Color.LTGRAY);
		if (m.balance < 0) {
			setTextAttrs(view, R.id.monthly_balance, String.format("%.2f", -m.balance), Color.RED );
		}
		else {
			setTextAttrs(view, R.id.monthly_balance, String.format("%.2f", m.balance), Color.GREEN );
		}
		return view;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return getCount() == 0;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MonthlyItem m = items.get(position);
		
		TextView view;
		if (convertView != null)
			view = (TextView) convertView;
		else {
			LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = (TextView) inflater.inflate(R.layout.monthly_selection_dropdown, parent, false);
		}
		
		SimpleDateFormat df = new SimpleDateFormat("MMM/yyyy");
		view.setText( df.format(getMonthFromValue(m.month)) );
		view.setTextColor( Color.LTGRAY );
		return view;
	}
	
	
	public int getPositionForId(int id) {
		for (int i=0; i<items.size(); i++) {
			if (items.get(i).month == id) {
				return i;
			}
		}
		return -1;
	}
	public int getTodayPosition() {
		return getPositionForId(getMonthValueFrom(new Date()));
	}

	////////////
	public static Date getMonthFromValue(Integer dataValue) {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMM");
		try {
			return df.parse(dataValue.toString());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static Integer getMonthValueFrom(Date date) {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMM");
		return Integer.valueOf(df.format(date));
	}
	
	public void setTextAttrs(View parent, int id, String text) {
		TextView t = (TextView) parent.findViewById(id);
		t.setText(text);
	}
	public void setTextAttrs(View parent, int id, String text, int color) {
		TextView t = (TextView) parent.findViewById(id);
		t.setText(text);
		t.setTextColor(color);
	}
}
