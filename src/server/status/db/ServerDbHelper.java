package server.status.db;

import java.util.ArrayList;

import server.status.Server;
import server.status.check.Checker;
import server.status.check.Checker.Type;
import server.status.check.Status;
import server.status.check.Status.Result;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class ServerDbHelper extends SQLiteOpenHelper {
	private interface ServerEntry extends BaseColumns {
		public static final String TABLE_NAME = "servers";
		public static final String COLUMN_NAME_HOST = "host";

		public static final String SQL_CREATE_ENTRIES = "CREATE TABLE "
				+ TABLE_NAME + " (" + _ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_NAME_HOST
				+ " TEXT NOT NULL)";
		public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
				+ TABLE_NAME;
	}

	private interface CheckerStatusEntry extends BaseColumns {
		public static final String TABLE_NAME = "checkerstatus";
		public static final String COLUMN_NAME_SERVER = "server";
		public static final String COLUMN_NAME_TYPE = "type";
		public static final String COLUMN_NAME_ARGS = "args";
		public static final String COLUMN_NAME_RESULT = "result";
		public static final String COLUMN_NAME_REASON = "reason";
		public static final String COLUMN_NAME_TIME = "time";

		public static final String SQL_CREATE_ENTRIES = "CREATE TABLE "
				+ TABLE_NAME + " (" + _ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_NAME_SERVER
				+ " INTEGER NOT NULL, " + COLUMN_NAME_TYPE
				+ " INTEGER NOT NULL, " + COLUMN_NAME_ARGS + " TEXT NOT NULL, "
				+ COLUMN_NAME_RESULT + " INTEGER NOT NULL, "
				+ COLUMN_NAME_REASON + " TEXT NOT NULL, " + COLUMN_NAME_TIME
				+ " INTEGER NOT NULL)";
		public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
				+ TABLE_NAME;
	}

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "server.db";

	private static ServerDbHelper instance = null;

	private ServerDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static ServerDbHelper getInstance(Context context) {
		// Singleton pattern
		if (instance == null) {
			synchronized (ServerDbHelper.class) {
				if (instance == null) {
					instance = new ServerDbHelper(context);
				}
			}
		}
		return instance;
	}

	/**
	 * Updates or inserts depending on the value of server.getId
	 * 
	 * @param server
	 * @return True on success
	 */
	public boolean save(Server server) {
		if (server.getId() == -1) {
			return insert(server);
		}
		return update(server);
	}

	private synchronized boolean insert(Server server) {
		SQLiteDatabase db = instance.getWritableDatabase();
		db.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			values.put(ServerEntry.COLUMN_NAME_HOST, server.getHost());

			long id = db.insert(ServerEntry.TABLE_NAME, null, values);
			server.setId(id);
			if (-1 != id) {
				boolean saved = saveCheckerStatus(server, db);
				if (saved) {
					db.setTransactionSuccessful();
					return true;
				}
			}
			return false;
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	private synchronized boolean update(Server server) {
		SQLiteDatabase db = instance.getWritableDatabase();
		db.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			values.put(ServerEntry.COLUMN_NAME_HOST, server.getHost());

			int count = db.update(ServerEntry.TABLE_NAME, values,
					ServerEntry._ID + "=?",
					new String[] { String.valueOf(server.getId()) });

			if (1 == count) {
				boolean saved = saveCheckerStatus(server, db);
				if (saved) {
					db.setTransactionSuccessful();
					return true;
				}
			}
			return false;
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	/**
	 * Deletes the server.
	 * 
	 * @param server
	 * @return True on success.
	 */
	public synchronized boolean delete(Server server) {
		SQLiteDatabase db = instance.getWritableDatabase();
		db.beginTransaction();
		try {
			int count = db.delete(ServerEntry.TABLE_NAME, ServerEntry._ID
					+ "=?", new String[] { String.valueOf(server.getId()) });
			if (1 == count) {
				deleteCheckerStatus(server, db);
				db.setTransactionSuccessful();
				server.setId(-1);
				return true;
			}
			return false;
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	private boolean saveCheckerStatus(Server server, SQLiteDatabase db) {
		db.beginTransaction();
		try {
			// First remove old ones
			deleteCheckerStatus(server, db);
			boolean saved = insertCheckerStatus(server, db);
			if (saved) {
				db.setTransactionSuccessful();
				return true;
			}
			return false;
		} finally {
			db.endTransaction();
		}
	}

	@SuppressWarnings("static-method")
	private boolean insertCheckerStatus(Server server, SQLiteDatabase db) {
		db.beginTransaction();
		try {
			ArrayList<Checker> checkers = server.getCheckers();
			ArrayList<Status> results = server.getResults();
			int size = checkers.size();
			for (int i = 0; i < size; i++) {
				Checker checker = checkers.get(i);
				Status status = results.get(i);

				ContentValues values = new ContentValues();
				values.put(CheckerStatusEntry.COLUMN_NAME_SERVER,
						server.getId());
				values.put(CheckerStatusEntry.COLUMN_NAME_TYPE, checker
						.getType().ordinal());
				values.put(CheckerStatusEntry.COLUMN_NAME_ARGS,
						checker.getArgs());
				values.put(CheckerStatusEntry.COLUMN_NAME_RESULT,
						status.result.ordinal());
				values.put(CheckerStatusEntry.COLUMN_NAME_REASON, status.reason);
				values.put(CheckerStatusEntry.COLUMN_NAME_TIME, status.time);

				long id = db
						.insert(CheckerStatusEntry.TABLE_NAME, null, values);
				checker.setId(id);
				if (-1 == id) {
					return false;
				}
			}
			db.setTransactionSuccessful();
			return true;
		} finally {
			db.endTransaction();
		}
	}

	@SuppressWarnings("static-method")
	private void deleteCheckerStatus(Server server, SQLiteDatabase db) {
		db.delete(CheckerStatusEntry.TABLE_NAME,
				CheckerStatusEntry.COLUMN_NAME_SERVER + "=?",
				new String[] { String.valueOf(server.getId()) });
		// Impossible to know how many to delete if checkers were added/removed
	}

	@SuppressWarnings("static-method")
	public synchronized boolean save(Server server, Checker checker,
			Status status) {
		SQLiteDatabase db = instance.getWritableDatabase();
		db.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			values.put(CheckerStatusEntry.COLUMN_NAME_SERVER, server.getId());
			values.put(CheckerStatusEntry.COLUMN_NAME_TYPE, checker.getType()
					.ordinal());
			values.put(CheckerStatusEntry.COLUMN_NAME_ARGS, checker.getArgs());
			values.put(CheckerStatusEntry.COLUMN_NAME_RESULT,
					status.result.ordinal());
			values.put(CheckerStatusEntry.COLUMN_NAME_REASON, status.reason);
			values.put(CheckerStatusEntry.COLUMN_NAME_TIME, status.time);

			int count = db.update(CheckerStatusEntry.TABLE_NAME, values,
					CheckerStatusEntry._ID + "=?",
					new String[] { String.valueOf(checker.getId()) });

			if (1 == count) {
				db.setTransactionSuccessful();
				return true;
			}
			return false;
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	/**
	 * Load server by id.
	 * 
	 * @param serverId
	 *            Id of the server.
	 * @return The server or null if not found.
	 */
	public synchronized Server load(long serverId) {
		Server res = null;
		SQLiteDatabase db = instance.getReadableDatabase();
		Cursor cursor = db.query(ServerEntry.TABLE_NAME, new String[] {
				ServerEntry._ID, ServerEntry.COLUMN_NAME_HOST },
				ServerEntry._ID + "=?",
				new String[] { String.valueOf(serverId) }, null, null, null);
		if (cursor.moveToFirst()) {
			long id = cursor.getLong(0);
			String host = cursor.getString(1);

			res = new Server(id, host);
			loadCheckerStatus(res, db);
		}
		cursor.close();
		db.close();
		return res;
	}

	/**
	 * Loads all servers.
	 * 
	 * @return All servers.
	 */
	public synchronized ArrayList<Server> load() {
		ArrayList<Server> res = new ArrayList<Server>();
		SQLiteDatabase db = instance.getReadableDatabase();
		Cursor cursor = db.query(ServerEntry.TABLE_NAME, new String[] {
				ServerEntry._ID, ServerEntry.COLUMN_NAME_HOST }, null, null,
				null, null, ServerEntry._ID + " ASC");

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			long id = cursor.getLong(0);
			String host = cursor.getString(1);

			Server server = new Server(id, host);
			loadCheckerStatus(server, db);
			res.add(server);
			cursor.moveToNext();
		}
		cursor.close();
		db.close();
		return res;
	}

	@SuppressWarnings("static-method")
	private void loadCheckerStatus(Server server, SQLiteDatabase db) {
		server.clear(); // Make sure it is empty
		Cursor cursor = db.query(CheckerStatusEntry.TABLE_NAME, new String[] {
				CheckerStatusEntry._ID, CheckerStatusEntry.COLUMN_NAME_TYPE,
				CheckerStatusEntry.COLUMN_NAME_ARGS,
				CheckerStatusEntry.COLUMN_NAME_RESULT,
				CheckerStatusEntry.COLUMN_NAME_REASON,
				CheckerStatusEntry.COLUMN_NAME_TIME },
				CheckerStatusEntry.COLUMN_NAME_SERVER + "=?",
				new String[] { String.valueOf(server.getId()) }, null, null,
				CheckerStatusEntry._ID + " ASC");

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			long id = cursor.getLong(0);
			Type type = Type.parse(cursor.getInt(1));
			String args = cursor.getString(2);
			Result result = Result.parse(cursor.getInt(3));
			String reason = cursor.getString(4);
			long time = cursor.getLong(5);

			Checker checker = Checker.parse(id, type, args);
			Status status = Status.parse(result, reason, time);
			server.add(checker, status);
			cursor.moveToNext();
		}
		cursor.close();
	}
	
	// TODO Clean up results if server gets removed while update is running

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(ServerEntry.SQL_CREATE_ENTRIES);
		db.execSQL(CheckerStatusEntry.SQL_CREATE_ENTRIES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(ServerEntry.SQL_DELETE_ENTRIES);
		db.execSQL(CheckerStatusEntry.SQL_DELETE_ENTRIES);
		onCreate(db);
	}
}
