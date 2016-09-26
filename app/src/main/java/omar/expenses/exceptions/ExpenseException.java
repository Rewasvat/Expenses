package omar.expenses.exceptions;

public class ExpenseException extends Exception {
	private static final long serialVersionUID = 1L;

	public ExpenseException(final String message) {
		super(message);
	}
}
