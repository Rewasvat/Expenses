package omar.expenses.persist;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import omar.expenses.exceptions.ExpenseException;
import omar.expenses.persist.model.BaseEntity;

public class DBManager {
	private DatabaseHelper								mDbHelper;
	private SQLiteDatabase								mDb;

	private static Context								mCtx	= null;

	private static List<Class<? extends BaseEntity>>	classes	= null;

	public static void initializeModule(final Context ctx, final String dbName, final int version) {
		DBManager.mCtx = ctx;
		DBManager dbManager = new DBManager();
		dbManager.mDbHelper = new DatabaseHelper(DBManager.mCtx, dbName, version, DBManager.classes);
		dbManager.mDb = dbManager.mDbHelper.getWritableDatabase();

		DBManager.instance = dbManager;
	}

	public static boolean isInitialized() {
		return DBManager.instance != null;
	}

	public static void registerModel(final Class<? extends BaseEntity> clazz) {
		if (DBManager.classes == null) {
			DBManager.classes = new ArrayList<Class<? extends BaseEntity>>();
		}
		DBManager.classes.add(clazz);
	}

	private DBManager() {

	}

	private static DBManager	instance	= null;

	public static DBManager getInstance() throws ExpenseException {
		if (DBManager.instance == null) {
			throw new ExpenseException("You must initialize DBManager first");
		}
		return DBManager.instance;
	}

	public long create(final BaseEntity entity) {
		String tableName = DBReflectionHelper.getTableName(entity);

		if (tableName == null) {
			return -1;
		}

		return this.mDb.insert(tableName, null, DBReflectionHelper.createValues(entity));
	}

	public boolean update(final BaseEntity entity) {
		String tableName = DBReflectionHelper.getTableName(entity);

		if (tableName == null) {
			return false;
		}

		int n = this.mDb.update(tableName, DBReflectionHelper.updateValues(entity, false),
				DBReflectionHelper.constraintEntity(entity), null);
		return n > 0;
	}

	public boolean delete(final BaseEntity entity) {
		String tableName = DBReflectionHelper.getTableName(entity);

		if (tableName == null) {
			return false;
		}
		return this.mDb.delete(tableName, DBReflectionHelper.constraintEntity(entity), null) > 0;
	}

	public <S extends BaseEntity> boolean deleteAll(final S representante) {
		return deleteSome(representante, null, null);
	}
	
	// gambiarra por não ter tempo de fazer um QueryBuilder decente
	public <S extends BaseEntity> boolean deleteSome(final S representante, String whereClause, String[] whereArgs) {
		String tableName = DBReflectionHelper.getTableName(representante);
		if (tableName == null) {
			return false;
		}
		return this.mDb.delete(tableName, whereClause, whereArgs) > 0;
	}
	
	public <S extends BaseEntity> S getLast(final S representante, String whereClause, String[] whereArgs) {
		String tableName = DBReflectionHelper.getTableName(representante);
		String idName = DBReflectionHelper.getIdName(representante);

		if (tableName != null) {
			Cursor cursor = this.mDb.query(tableName, null, whereClause, whereArgs, null, null, idName + " DESC", null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				try {
					S entity = DBReflectionHelper.createFromCursor(representante, cursor);
					return entity;
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	public <S extends BaseEntity> List<S> getAll(final S representante) {
		return getSome(representante, null, null);
	}
	
	
	// Outra gambiarra pela falta de QueryBuilder
	public <S extends BaseEntity> List<S> getSome(final S representante, String whereClause, String[] whereArgs) {
		List<S> list = new ArrayList<S>();

		String tableName = DBReflectionHelper.getTableName(representante);

		if (tableName != null) {
			Cursor cursor = this.mDb.query(tableName, null, whereClause, whereArgs, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				int count = cursor.getCount();
				for (int i = 0; i < count; i++) {
					try {
						S entity = DBReflectionHelper.createFromCursor(representante, cursor);
						list.add(entity);
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
					cursor.moveToNext();
				}
			}
		}

		return list;
	}

	public <S extends BaseEntity> S getById(final S representante, Integer id) {
		//go gambz
		String idName = DBReflectionHelper.getIdName(representante);
		String where = idName + " = ?";
		String[] args = new String[] { id.toString() };

		return getLast(representante, where, args);
	}
	public <S extends BaseEntity> S getById(final S representante, Long id) {
		//go gambz
		String idName = DBReflectionHelper.getIdName(representante);
		String where = idName + " = ?";
		String[] args = new String[] { id.toString() };

		return getLast(representante, where, args);
	}
	
	public <S extends BaseEntity> List<String> getSingleColumnValues(final S representante, String columnName, String whereClause, String[] whereArgs) {
		List<String> list = new ArrayList<String>();

		String tableName = DBReflectionHelper.getTableName(representante);

		if (tableName != null) {
			Cursor cursor = this.mDb.query(true, tableName, new String[] {columnName}, whereClause, whereArgs, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				int count = cursor.getCount();
				for (int i = 0; i < count; i++) {
					list.add(cursor.getString(0));
					cursor.moveToNext();
				}
			}
		}

		return list;
	}
}
