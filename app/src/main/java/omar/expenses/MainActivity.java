package omar.expenses;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import omar.expenses.adapter.ExpensesAdapter;
import omar.expenses.adapter.MonthlySelectionAdapter;
import omar.expenses.exceptions.ExpenseException;
import omar.expenses.persist.DBManager;
import omar.expenses.persist.model.Expense;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements OnItemClickListener, OnNavigationListener {
	
	private static final int REQUEST_NEW = 0;
	private static final int REQUEST_EDIT = 1;
	
	private TextView balancoText;
	private int currentMonth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if (!DBManager.isInitialized()) {
			DBManager.registerModel(Expense.class);
			DBManager.initializeModule(this, "EXPENSES_APP_DB", 1);
		}
		
		ListView list = (ListView) findViewById(R.id.list);
		list.setOnItemClickListener(this);
		
		final ActionBar bar = getSupportActionBar();
	    //bar.setDisplayShowTitleEnabled(false);
	    bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
	    
		MonthlySelectionAdapter adapter = new MonthlySelectionAdapter();
		bar.setListNavigationCallbacks(adapter, this);
		bar.setSelectedNavigationItem(adapter.getTodayPosition()); 
		
		currentMonth = MonthlySelectionAdapter.getMonthValueFrom(new Date());
		this.refresh();
	}
	
	public void updateMonthlySelection() {
		final ActionBar bar = getSupportActionBar();
		MonthlySelectionAdapter adapter = new MonthlySelectionAdapter();
		bar.setListNavigationCallbacks(adapter, this);
		int pos = adapter.getPositionForId(currentMonth);
		if (pos == -1)
			pos = adapter.getPositionForId(MonthlySelectionAdapter.getMonthValueFrom(new Date()));
		bar.setSelectedNavigationItem( pos );
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		MenuItem balanceItem = menu.findItem(R.id.action_balance);
		MenuItemCompat.setActionView(balanceItem, R.layout.balance_widget);
		MenuItemCompat.expandActionView(balanceItem);
	    View balanceView = (View) MenuItemCompat.getActionView(balanceItem);
	    balancoText = (TextView) balanceView.findViewById(R.id.balance_value);
	    refreshBalanco();
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		switch (item.getItemId()) {
        	case R.id.action_new:
        		Intent intent = new Intent(this, NewExpenseActivity.class);
    			intent.putExtra(NewExpenseActivity.EXPENSE_ID, -1L);
    			this.startActivityForResult(intent, REQUEST_NEW);
        		return true;
        	default:
        		return super.onOptionsItemSelected(item);
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_NEW) {
				Toast.makeText(this, "Criou novo lançamento!", Toast.LENGTH_SHORT).show();
				this.refresh();
				this.updateMonthlySelection();
			}
			else if (requestCode == REQUEST_EDIT) {
				boolean removeu = data.getBooleanExtra(NewExpenseActivity.REMOVED_EXPENSE, false);
				if (!removeu)
					Toast.makeText(this, "Editou lançamento!", Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(this, "Removeu lançamento!", Toast.LENGTH_SHORT).show();
				this.refresh();
				this.updateMonthlySelection();
			}
		}
		else if (resultCode == RESULT_CANCELED) {
			if (requestCode == REQUEST_NEW) {
				Toast.makeText(this, "Novo lançamento cancelado.", Toast.LENGTH_SHORT).show();
			}
			else if (requestCode == REQUEST_EDIT) {
				Toast.makeText(this, "Edição de lançamento cancelada.", Toast.LENGTH_SHORT).show();
			}
		}
		else 
			Toast.makeText(this, "Uatahell?", Toast.LENGTH_SHORT).show();
	}

	public void refresh() {
		Calendar c = Calendar.getInstance();
		c.setTime( MonthlySelectionAdapter.getMonthFromValue(currentMonth) );
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		Date init = c.getTime();
		c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		Date end = c.getTime();

		List<Expense> expenses;
		try {
			expenses = DBManager.getInstance().getSome(
					new Expense(), "data >= ? and data <= ? ",
					new String[] { ((Long)init.getTime()).toString(), ((Long)end.getTime()).toString() });			
			
		} catch (ExpenseException e) {
			e.printStackTrace();
			expenses = new ArrayList<Expense>();
		}
		
		ExpensesAdapter expAdapter = new ExpensesAdapter(this, expenses);
		ListView list = (ListView) findViewById(R.id.list);
		list.setAdapter(expAdapter);
		
		refreshBalanco();
	}
	
	public void refreshBalanco() {
		ListView list = (ListView) findViewById(R.id.list);
		Double balanco = 0.0;
		for (int i=0; i < list.getAdapter().getCount(); i++) {
			Expense exp = (Expense)list.getAdapter().getItem(i);
			double valor = exp.getValor();
			balanco += (exp.getDespesa()) ? -valor : valor; 
		}
		if (balancoText == null)
			return;
		
		if (balanco < 0) {
			balancoText.setText(String.format("%.2f", -balanco));
			balancoText.setTextColor(Color.RED);
		}
		else {
			balancoText.setText(String.format("%.2f", balanco));
			balancoText.setTextColor(Color.GREEN);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(this, NewExpenseActivity.class);
		intent.putExtra(NewExpenseActivity.EXPENSE_ID, id);
		this.startActivityForResult(intent, REQUEST_EDIT);
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		currentMonth = (int) id;
		this.refresh();
		return true;
	}
}
