package omar.expenses;

import java.util.Calendar;
import java.util.List;

import omar.expenses.exceptions.ExpenseException;
import omar.expenses.persist.DBManager;
import omar.expenses.persist.model.Expense;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;

public class NewExpenseActivity extends ActionBarActivity {
	// Intent codes
	public static final String EXPENSE_ID = "expenseId";
	public static final String REMOVED_EXPENSE = "removedExpense";
	
	private long expenseId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_expense);
		// Show the Up button in the action bar.
		setupActionBar();
		
		Intent intent = this.getIntent();
		expenseId = intent.getLongExtra(EXPENSE_ID, -1);
		
		Spinner prelist = (Spinner) findViewById(R.id.predefined_spinner);
		List<String> typeList = null;
		try {
			typeList = DBManager.getInstance().getSingleColumnValues(new Expense(), "tipo", null, null);
		} catch (ExpenseException e) {
			e.printStackTrace();
		}
		addToList(typeList, "Energia");
		addToList(typeList, "Gás");
		addToList(typeList, "Telefone");
		addToList(typeList, "Celular");
		addToList(typeList, "Vivo");
		addToList(typeList, "Tim");
		addToList(typeList, "TV");
		addToList(typeList, "Internet");
		addToList(typeList, "Net");
		addToList(typeList, "Sky");
		addToList(typeList, "Supermercado");
		addToList(typeList, "Restaurante");
		addToList(typeList, "Empregada");
		addToList(typeList, "Gasolina");
		addToList(typeList, "TransportePúblico");
		addToList(typeList, "Farmácia");
		addToList(typeList, "Salário");

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, typeList);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		prelist.setAdapter(adapter);
		
		if (this.expenseId != -1) {
			this.setTitle(R.string.title_activity_edit_expense);
			Expense exp = null;
			try {
				exp = DBManager.getInstance().getById(new Expense(), expenseId);
			} catch (ExpenseException e) {
				e.printStackTrace();
			}
			
			prelist.setSelection(typeList.indexOf(exp.getTipo()));
			
			EditText valorText = (EditText) findViewById(R.id.expense_value);
			valorText.setText(exp.getValor().toString());
			
			CheckBox ehDespesa = (CheckBox) findViewById(R.id.is_expense);
			ehDespesa.setChecked(exp.getDespesa());			
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(exp.getData());
			DatePicker dia = (DatePicker) findViewById(R.id.expense_data);
			dia.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
			TimePicker hora = (TimePicker) findViewById(R.id.expense_time);
			hora.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
			hora.setCurrentMinute(cal.get(Calendar.MINUTE));
			
			EditText descricaoText = (EditText) findViewById(R.id.expense_descricao);
			descricaoText.setText(exp.getDescricao());
		}
	}

	private void addToList(List<String> list, String s) {
		if (!list.contains(s)) {
			list.add(s);
		}
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_expense, menu);
		
		if (this.expenseId == -1) {
			MenuItem delete = (MenuItem) menu.findItem(R.id.action_delete);
			delete.setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_done:
			this.done();
			return true;
		case R.id.action_cancel:
			setResult(RESULT_CANCELED);
			finish();
			return true;
		case R.id.action_delete:
			this.discard();
			return true;
		case R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onRadioButtonClicked(View view) {
	    boolean checked = ((RadioButton) view).isChecked();

	    switch(view.getId()) {
	        case R.id.radio_fromlist:
	            if (checked) {
	            	Spinner prelist = (Spinner) findViewById(R.id.predefined_spinner);
	            	EditText newtype = (EditText) findViewById(R.id.new_typename);
	            	prelist.setEnabled(true);
	            	newtype.setEnabled(false);
	            }
	            break;
	        case R.id.radio_newtype:
	            if (checked) {
	            	Spinner prelist = (Spinner) findViewById(R.id.predefined_spinner);
	            	EditText newtype = (EditText) findViewById(R.id.new_typename);
	            	prelist.setEnabled(false);
	            	newtype.setEnabled(true);
	            }
	            break;
	    }
	}

	public void done() {
		// Tentaremos criar uma expense
		Expense exp = new Expense();
		if (expenseId != -1) {
			try {
				exp = DBManager.getInstance().getById(exp, expenseId);
			} catch (ExpenseException e) {
				e.printStackTrace();
			}
		}
		
		// Pegando o TIPO primeiro
		RadioButton rFromList = (RadioButton) findViewById(R.id.radio_fromlist);
		RadioButton rNewType = (RadioButton) findViewById(R.id.radio_newtype);
		String tipo = "";
		if (rFromList.isChecked()) {
			Spinner prelist = (Spinner) findViewById(R.id.predefined_spinner);
			tipo = prelist.getSelectedItem().toString();
		}
		else if (rNewType.isChecked()) {
			EditText newtype = (EditText) findViewById(R.id.new_typename);
			tipo = newtype.getText().toString();
		}
		if (tipo.equals("")) {
			Toast.makeText(this, "Tipo não pode ser nulo!", Toast.LENGTH_SHORT).show();
			return;
		}
		exp.setTipo(tipo);
		
		// Depois o VALOR
		EditText valorText = (EditText) findViewById(R.id.expense_value);
		if (valorText.getText().toString().equals("")) {
			Toast.makeText(this, "Valor não pode ser nulo!", Toast.LENGTH_SHORT).show();
			return;
		}
		exp.setValor( Double.parseDouble(valorText.getText().toString()) );
		
		// Depois se EH DESPESA
		CheckBox ehDespesa = (CheckBox) findViewById(R.id.is_expense);
		exp.setDespesa(ehDespesa.isChecked());
		
		// Depois a DATA/HORA
		DatePicker dia = (DatePicker) findViewById(R.id.expense_data);
		TimePicker hora = (TimePicker) findViewById(R.id.expense_time);
		Calendar cal = Calendar.getInstance();
		//cal.setTimeInMillis(dia.getCalendarView().getDate()); // soh pra apis novas =/
		cal.set(Calendar.YEAR, dia.getYear());
		cal.set(Calendar.MONTH, dia.getMonth());
		cal.set(Calendar.DAY_OF_MONTH, dia.getDayOfMonth());
		cal.set(Calendar.HOUR_OF_DAY, hora.getCurrentHour());
		cal.set(Calendar.MINUTE, hora.getCurrentMinute());
		exp.setData(cal.getTime());
		
		// E finalmente a DESCRICAO
		EditText descricaoText = (EditText) findViewById(R.id.expense_descricao);
		exp.setDescricao(descricaoText.getText().toString());
		
		// ai atualizamos o DB
		try {
			if (expenseId == -1) {
				DBManager.getInstance().create(exp);
			}
			else {
				DBManager.getInstance().update(exp);
			}
		} catch (ExpenseException e) {
			e.printStackTrace();
		}
		// E fechamos a tela
		Intent resultData = new Intent();
		setResult(RESULT_OK, resultData);
		finish();
	}
	
	public void discard() {
		if (expenseId == -1) {
			Toast.makeText(this, "Não é possível remover um lançamento novo.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle("Removendo Lançamento")
			.setMessage("Você tem certeza que deseja remover esse lançamento?")
			.setPositiveButton("Sim", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which) {
					   actualDiscard(); 
				}

			})
			.setNegativeButton("Não", null)
			.show();
	}
	
	public void actualDiscard() {
		Expense exp = new Expense();
		try {
			exp = DBManager.getInstance().getById(exp, expenseId);
		} catch (ExpenseException e) {
			e.printStackTrace();
		}

		try {
			DBManager.getInstance().delete(exp);
		} catch (ExpenseException e) {
			e.printStackTrace();
		}
		Intent resultData = new Intent();
		resultData.putExtra(REMOVED_EXPENSE, true);
		setResult(RESULT_OK, resultData);
		finish();
	}
}
