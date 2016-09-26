package omar.expenses.adapter;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

import omar.expenses.R;
import omar.expenses.persist.model.Expense;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

@SuppressLint("SimpleDateFormat")
public class ExpensesAdapter extends BaseAdapter {
	private Context context;
	private List<Expense> listaExpenses;

	public ExpensesAdapter(final Context context, final List<Expense> listaCardapio) {
		this.context = context;
		this.listaExpenses = listaCardapio;
		Collections.sort(this.listaExpenses, new Comparator<Expense>() {
			@Override
			public int compare(final Expense lhs, final Expense rhs) {
				Date lDate = lhs.getData();
				Date rDate = rhs.getData();

				if (lDate == null) {
					return -1;
				} else if (rDate == null) {
					return 1;
				}
				return lDate.compareTo(rDate);
			}
		});
	}

	@Override
	public int getCount() {
		return this.listaExpenses.size();
	}

	@Override
	public Object getItem(final int position) {
		return this.listaExpenses.get(position);
	}

	@Override
	public long getItemId(final int position) {
		return this.listaExpenses.get(position).getId();
	}

	@Override
	public View getView(final int i, final View view, final ViewGroup viewGroup) {
		Expense exp = this.listaExpenses.get(i);

		LayoutInflater inflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View aView = inflater.inflate(R.layout.view_expense_item, null);

		setTextAttrs(aView, R.id.item_expense_tipo, exp.getTipo(), Color.BLACK);
		if (exp.getDespesa())
			setTextAttrs(aView, R.id.item_expense_valor, String.format("%.2f", exp.getValor()), Color.RED);
		else
			setTextAttrs(aView, R.id.item_expense_valor, String.format("%.2f", exp.getValor()), Color.GREEN);
		
		if (i%2 == 0) {
			setTextAttrs(aView, R.id.item_expense_descricao, exp.getDescricao(), Color.GRAY);
			aView.setBackgroundColor(Color.WHITE);
		}
		else {
			setTextAttrs(aView, R.id.item_expense_descricao, exp.getDescricao(), Color.DKGRAY);
			aView.setBackgroundColor(Color.LTGRAY);
		}
		
		SimpleDateFormat data = new SimpleDateFormat("HH:mm dd/MM/yyyy");
		setTextAttrs(aView, R.id.item_expense_data, data.format(exp.getData()), Color.BLACK);
		
		return aView;
	}

	public void setTextAttrs(View parent, int id, String text, int color) {
		TextView t = (TextView) parent.findViewById(id);
		t.setText(text);
		t.setTextColor(color);
	}
}
