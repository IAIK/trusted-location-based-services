package at.tugraz.iaik.las.p2.prover;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import at.tugraz.iaik.las.p2.prover.R;
import at.tugraz.iaik.las.p2.prover.server.ProxyFactory;

/**
 * Consolidates global app settings.
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class PreferencesAcitivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.compareTo("ttpApiUrl") == 0) {
			String serverUrl = sharedPreferences.getString("ttpApiUrl", "");
			ProxyFactory.init(getClassLoader(), serverUrl);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

}
