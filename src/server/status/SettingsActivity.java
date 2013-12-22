package server.status;

import server.status.service.Starter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	public static final String PREF_ENABLED = "pref_enabled";
	public static final String PREF_SOUND = "pref_sound";
	public static final String PREF_INTERVAL = "pref_interval";
	public static final String PREF_TIMEOUT = "pref_timeout";
	public static final String PREF_RETRIES = "pref_retries";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences sharedPreferences = getPreferenceScreen()
				.getSharedPreferences();
		updateIntervalSummary(sharedPreferences);
		updateTimeoutSummary(sharedPreferences);
		updateRetriesSummary(sharedPreferences);
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		boolean enabled = sharedPreferences.getBoolean(PREF_ENABLED, true);
		if (key.equals(PREF_ENABLED)) {
			if (enabled) {
				Starter.update(getApplicationContext());
			} else {
				Starter.stop(getApplicationContext());
			}
		} else if (key.equals(PREF_INTERVAL)) {
			updateIntervalSummary(sharedPreferences);
			Starter.update(getApplicationContext());
		} else if (key.equals(PREF_TIMEOUT)) {
			updateTimeoutSummary(sharedPreferences);
			Starter.update(getApplicationContext());
		} else if (key.equals(PREF_RETRIES)) {
			updateRetriesSummary(sharedPreferences);
		}
	}

	private void updateIntervalSummary(SharedPreferences sharedPreferences) {
		findPreference(PREF_INTERVAL).setSummary(
				getString(R.string.pref_interval_summ,
						sharedPreferences.getString(PREF_INTERVAL, "10")));
	}

	private void updateTimeoutSummary(SharedPreferences sharedPreferences) {
		findPreference(PREF_TIMEOUT).setSummary(
				getString(R.string.pref_timeout_summ,
						sharedPreferences.getString(PREF_TIMEOUT, "5000")));
	}

	private void updateRetriesSummary(SharedPreferences sharedPreferences) {
		findPreference(PREF_RETRIES).setSummary(
				getString(R.string.pref_retries_summ,
						sharedPreferences.getString(PREF_RETRIES, "2")));
	}
}
