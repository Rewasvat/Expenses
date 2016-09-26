package omar.expenses.persist;

import java.lang.reflect.Field;
import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import omar.expenses.persist.annotation.Column;
import omar.expenses.persist.annotation.Id;
import omar.expenses.persist.annotation.Table;
import omar.expenses.persist.model.BaseEntity;

public class DBReflectionHelper {

	protected static String getTableName(final BaseEntity entity) {
		Class<? extends BaseEntity> clazz = entity.getClass();

		Table table = clazz.getAnnotation(Table.class);
		if (table == null) {
			return null;
		}
		return table.name();
	}

	protected static ContentValues createValues(final BaseEntity entity) {
		ContentValues values = DBReflectionHelper.updateValues(entity, true);

		values.remove(DBReflectionHelper.getIdName(entity));

		return values;
	}

	protected static ContentValues updateValues(final BaseEntity entity, boolean includeId) {
		ContentValues values = new ContentValues();
		Class<? extends BaseEntity> clazz = entity.getClass();

		Field[] fields = clazz.getDeclaredFields();

		for (Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			if (column == null) {
				continue;
			}
			Id id = field.getAnnotation(Id.class);
			if (id != null && !includeId) {
				continue;
			}

			Object value = null;
			try {
				field.setAccessible(true);
				value = field.get(entity);
				field.setAccessible(false);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				continue;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				continue;
			}

			Class<?> tipofield = field.getType();

			if (tipofield.equals(String.class)) {
				values.put(column.name(), (String) value);
			} else if (tipofield.equals(Integer.class) || tipofield.equals(int.class)) {
				values.put(column.name(), (Integer) value);
			} else if (tipofield.equals(Double.class) || tipofield.equals(double.class)) {
				values.put(column.name(), (Double) value);
			} else if (tipofield.equals(Boolean.class) || tipofield.equals(boolean.class)) {
				values.put(column.name(), (Boolean) value == true ? 1 : 0);
			} else if (tipofield.equals(Date.class)) {
				Long time = value == null ? null : ((Date) value).getTime();
				values.put(column.name(), time);
			} else if (tipofield.equals(Long.class) || tipofield.equals(long.class)) {
				values.put(column.name(), (Long) value);
			}
		}
		return values;
	}

	protected static String getIdName(final BaseEntity entity) {
		Class<?> clazz = entity.getClass();
		Field[] fields = clazz.getDeclaredFields();

		for (Field field : fields) {
			Id id = field.getAnnotation(Id.class);
			if (id != null) {
				Column column = field.getAnnotation(Column.class);
				return column == null ? null : column.name();
			}
		}

		return null;
	}

	protected static String constraintEntity(final BaseEntity entity) {
		Class<?> clazz = entity.getClass();
		Field[] fields = clazz.getDeclaredFields();

		for (Field field : fields) {
			Id id = field.getAnnotation(Id.class);
			if (id != null) {
				Column column = field.getAnnotation(Column.class);
				try {
					return column == null ? null : column.name() + " = " + DBReflectionHelper.getValue(field, entity);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					break;
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					break;
				}
			}
		}

		return null;
	}

	protected static <S extends BaseEntity> S createFromCursor(final S representante, final Cursor cursor)
			throws InstantiationException, IllegalAccessException {
		Class<?> clazz = representante.getClass();
		@SuppressWarnings("unchecked")
		S instance = (S) clazz.newInstance();

		Field[] fields = clazz.getDeclaredFields();

		for (Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			if (column == null) {
				continue;
			}

			int index = cursor.getColumnIndex(column.name());

			Object value = DBReflectionHelper.getValueFromCursor(cursor, index, field.getType());

			field.setAccessible(true);
			field.set(instance, value);
			field.setAccessible(false);
		}

		return instance;
	}

	private static Object getValueFromCursor(final Cursor cursor, final int index, final Class<?> clazz) {
		if (clazz.equals(String.class)) {
			return cursor.getString(index);
		} else if (clazz.equals(Integer.class) || clazz.equals(int.class)) {
			return cursor.getInt(index);
		} else if (clazz.equals(Double.class) || clazz.equals(double.class)) {
			return cursor.getDouble(index);
		} else if (clazz.equals(Boolean.class) || clazz.equals(boolean.class)) {
			Integer value = cursor.getInt(index);
			return value != 0;
		} else if (clazz.equals(Date.class)) {
			Long time = cursor.getLong(index);
			return time == null ? null : new Date(time);
		} else if (clazz.equals(Long.class) || clazz.equals(long.class)) {
			return cursor.getLong(index);
		}
		return null;
	}

	private static <S extends BaseEntity> Object getValue(final Field field, final S entity)
			throws IllegalArgumentException, IllegalAccessException {

		Class<?> clazz = field.getType();
		
		Object val = null;
		field.setAccessible(true);

		if (clazz.equals(String.class) || clazz.equals(Integer.class) || clazz.equals(int.class)
				|| clazz.equals(Double.class) || clazz.equals(double.class)
				|| clazz.equals(Long.class) || clazz.equals(long.class)) {
			val = field.get(entity);
		} else if (clazz.equals(Boolean.class) || clazz.equals(boolean.class)) {
			Boolean value = field.getBoolean(entity);
			val = value ? 1 : 0;
		} else if (clazz.equals(Date.class)) {
			Date date = (Date) field.get(entity);
			val = date == null ? null : date.getTime();
		}
		field.setAccessible(false);
		return val;
	}
}
