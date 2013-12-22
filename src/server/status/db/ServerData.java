package server.status.db;

import java.util.ArrayList;
import java.util.Observable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import server.status.Server;
import server.status.check.Checker;
import server.status.check.Status;

/**
 * Wrapper class that communicates to the DB. Will notify when the data changes.
 */
public class ServerData extends Observable {
	private static ServerData instance = null;
	private final SortedList<Server> servers;
	private boolean loaded;
	private ThreadPoolExecutor threads = new ThreadPoolExecutor(1, 1, 1,
			TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

	private ServerData() {
		servers = new SortedList<Server>();
		loaded = false;
	}

	public static ServerData getInstance() {
		// Singleton pattern
		if (instance == null) {
			synchronized (ServerData.class) {
				if (instance == null) {
					instance = new ServerData();
				}
			}
		}
		return instance;
	}

	private void loadServers(Context context) {
		ArrayList<Server> res = ServerDbHelper.getInstance(context).load();
		boolean changed = false;
		synchronized (servers) {
			changed = servers.addAll(res);
		}
		if (changed) {
			setChanged();
			notifyObservers();
		}
	}

	public void loadServersSync(Context context) {
		boolean load = false;
		synchronized (servers) {
			if (!loaded) {
				loaded = true;
				// Load in this thread
				load = true;
			}
		}
		if (load) {
			loadServers(context);
		}
	}

	public void loadServersAsync(final Context context) {
		synchronized (servers) {
			if (!loaded) {
				loaded = true;
				// Load in the background
				threads.execute(new Runnable() {
					@Override
					public void run() {
						loadServers(context);
					}
				});
			}
		}
	}

	public void insertAsync(final Context context, final Server server,
			final Runnable onFail) {
		threads.execute(new Runnable() {
			@Override
			public void run() {
				boolean inserted = ServerDbHelper.getInstance(context).insert(
						server);
				if (inserted) {
					boolean added = false;
					synchronized (servers) {
						added = servers.add(server);
					}
					if (added) {
						setChanged();
						notifyObservers();
					}
				} else if (onFail != null) {
					onFail.run();
				}
			}
		});
	}

	public boolean updateSync(Context context, Server server) {
		boolean updated = ServerDbHelper.getInstance(context).update(server);
		if (updated) {
			boolean changed = false;
			synchronized (servers) {
				changed = servers.update(server);
			}
			if (changed) {
				setChanged();
				notifyObservers();
			}
		}
		return updated;
	}

	public void updateAsync(final Context context, final Server server,
			final Runnable onFail) {
		threads.execute(new Runnable() {
			@Override
			public void run() {
				boolean updated = updateSync(context, server);
				if (!updated && onFail != null) {
					onFail.run();
				}
			}
		});
	}

	public void deleteAsync(final Context context, final Server server,
			final Runnable onFail) {
		threads.execute(new Runnable() {
			@Override
			public void run() {
				boolean deleted = ServerDbHelper.getInstance(context).delete(
						server);
				if (deleted) {
					boolean removed = false;
					synchronized (servers) {
						removed = servers.remove(server);
					}
					if (removed) {
						setChanged();
						notifyObservers();
					}
				} else if (onFail != null) {
					onFail.run();
				}
			}
		});
	}

	public boolean saveSync(Context context, Server server, Checker checker,
			Status status) {
		boolean saved = ServerDbHelper.getInstance(context).save(server,
				checker, status);
		if (saved) {
			boolean updated = false;
			synchronized (servers) {
				updated = servers.update(server);
			}
			if (updated) {
				setChanged();
				notifyObservers();
			}
		}
		return saved;
	}

	@SuppressWarnings("static-method")
	public void cleanupSync(Context context) {
		ServerDbHelper.getInstance(context).cleanup();
	}

	public SortedList<Server> getServers() {
		return servers;
	}
}
