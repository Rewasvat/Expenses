package omar.expenses.persist;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import omar.expenses.persist.annotation.Column;
import omar.expenses.persist.annotation.Id;
import omar.expenses.persist.annotation.NotNull;
import omar.expenses.persist.annotation.Table;
import omar.expenses.persist.model.BaseEntity;

public class DatabaseHelper extends SQLiteOpenHelper {

	private List<Class<? extends BaseEntity>>	classes;
	private Map<Class<?>, String>				columnTypes;

	public DatabaseHelper(final Context context, final String databaseName, final int version,
			final List<Class<? extends BaseEntity>> classes) {
		super(context, databaseName, null, version);
		this.classes = classes;
		this.initColumnTypes();
	}

	public void initColumnTypes() {
		this.columnTypes = new HashMap<Class<?>, String>();
		this.columnTypes.put(String.class, "text");
		this.columnTypes.put(int.class, "integer");
		this.columnTypes.put(Integer.class, "integer");
		this.columnTypes.put(double.class, "real");
		this.columnTypes.put(Double.class, "real");
		this.columnTypes.put(boolean.class, "integer");
		this.columnTypes.put(Boolean.class, "integer");
		this.columnTypes.put(Date.class, "integer");
		this.columnTypes.put(long.class, "integer");
		this.columnTypes.put(Long.class, "integer");
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		this.auxiliar(db, 1, 1);
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		this.auxiliar(db, oldVersion + 1, newVersion);
	}

	private void auxiliar(final SQLiteDatabase db, final int minVersion, final int maxVersion) {
		for (Class<?> clazz : this.classes) {
			Table table = (Table) clazz.getAnnotation(Table.class);
			if (table == null) {
				continue;
			}
			StringBuilder sql = null;
			if (table.version() >= minVersion) {
				// CREATE
				sql = new StringBuilder("CREATE TABLE ");
				sql.append(table.name()).append("(");

				Field[] fields = clazz.getDeclaredFields();
				for (Field field : fields) {
					Column column = field.getAnnotation(Column.class);
					if (column == null) {
						continue;
					}
					Id id = field.getAnnotation(Id.class);
					NotNull notNull = field.getAnnotation(NotNull.class);

					sql.append(column.name()).append(" ");
					sql.append(this.columnTypes.get(field.getType()));

					if (id != null) {
						sql.append(" primary key autoincrement");
					}
					if (notNull != null) {
						sql.append(" not null");
					}
					sql.append(", ");
				}
				sql.replace(sql.length() - 2, sql.length(), ");");
				Log.i("Creating Table", sql.toString());
				db.execSQL(sql.toString());
			} else {
				// UPDATE
				Field[] fields = clazz.getDeclaredFields();
				for (Field field : fields) {
					Column column = field.getAnnotation(Column.class);
					if (column == null) {
						continue;
					}
					if (column.version() < minVersion) {
						continue;
					}
					Id id = field.getAnnotation(Id.class);
					NotNull notNull = field.getAnnotation(NotNull.class);

					sql = new StringBuilder("ALTER TABLE ");
					sql.append(table.name()).append(" ADD COLUMN ");
					sql.append(column.name()).append(" ");
					sql.append(this.columnTypes.get(field.getType()));

					if (id != null) {
						sql.append(" primary key autoincrement");
					}
					if (notNull != null) {
						sql.append(" not null");
					}
					sql.append("; ");
					Log.i("Updating Table", sql.toString());
					db.execSQL(sql.toString());
				}
			}
		}
	}
}
