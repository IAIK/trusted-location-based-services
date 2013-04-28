package at.tugraz.iaik.las.p2.prover;

import java.util.ArrayList;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import at.tugraz.iaik.las.p2.prover.R;
import at.tugraz.iaik.las.p2.prover.cryptotag.ICryptoTag;
import at.tugraz.iaik.las.p2.prover.cryptotag.crypta.IaikCryptaTag;
import at.tugraz.iaik.las.p2.prover.cryptotag.mock.ExtStorage;
import at.tugraz.iaik.las.p2.prover.cryptotag.mock.MockTagManager;
import at.tugraz.iaik.las.p2.prover.cryptotag.mock.RandomMockCryptoTag;
import at.tugraz.iaik.las.p2.prover.server.ProxyFactory;
import at.tugraz.iaik.las.p2.ttp.server.TtpApi;

/**
 * Main UI class.
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class MainActivity extends Activity {

	private NfcAdapter nfcAdapter;
	private PendingIntent pendingIntent;
	private IntentFilter[] intentFiltersArray;

	private MockTagManager mockTagManager = new MockTagManager();

	private Spinner mockTagSpinner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// NFC
		this.nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		this.pendingIntent = PendingIntent.getActivity(this, 0, new Intent(
				this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		// listen for ANY kind of tag, so we can tell the user
		// he touched a wrong kind of tag.
		IntentFilter intentFilter = new IntentFilter(
				NfcAdapter.ACTION_TAG_DISCOVERED);
		intentFiltersArray = new IntentFilter[] { intentFilter, };

		// UI
		this.mockTagSpinner = (Spinner) this
				.findViewById(R.id.main_mockTagSpinner);
	}

	private void initMockTagSpinner(Spinner spinner) {
		final ArrayList<ICryptoTag> mockTags = new ArrayList<ICryptoTag>();
		mockTags.add(new DummySpinnerItem());
		mockTags.add(new RandomMockCryptoTag());
		mockTags.addAll(this.mockTagManager.getAvailableMockTags());
		ArrayAdapter<ICryptoTag> adapter = new ArrayAdapter<ICryptoTag>(this,
				android.R.layout.simple_spinner_item, mockTags);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (position >= 1) {
					doRunProtocol(mockTags.get(position));
				}
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	public void onPause() {
		super.onPause();
		this.nfcAdapter.disableForegroundDispatch(this);
	}

	public void onResume() {
		super.onResume();

		// check if SD card is available
		// if not, close app (simplification)
		if (!ExtStorage.isExternalStorageAvailable()) {
			Toast.makeText(getApplicationContext(),
					"No external storage available. Unmount first.",
					Toast.LENGTH_LONG).show();
			this.finish();
			return;
		}
		
		this.nfcAdapter.enableForegroundDispatch(this, this.pendingIntent,
				this.intentFiltersArray, null);
		this.initMockTagSpinner(this.mockTagSpinner);
	}

	public void onNewIntent(Intent intent) {
		Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if (tagFromIntent != null) {
			try {
				doRunProtocol(new IaikCryptaTag(tagFromIntent));
			} catch (Exception e) {
				Toast.makeText(this, "Tag does not support ISO-DEP.",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void doRunProtocol(ICryptoTag tag) {
		RunProtocolActivty.init(tag);
		Intent intent = new Intent(this, RunProtocolActivty.class);
		this.startActivityForResult(intent, 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.main_menu_test:
			this.doTestConnection();
			return true;

		case R.id.main_menu_create_mocktag:
			this.doCreateMockTag();
			return true;

		case R.id.main_menu_preferences:
			this.goToSettings();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void doTestConnection() {
		new ProgressBarAsyncTask<Void, String>(this) {

			@Override
			protected String doInBackground(Void... params) {
				super.doInBackground(params);
				String result = "Error.";
				TtpApi api = ProxyFactory.getProxy(TtpApi.class, "api");
				this.publishProgress(String.format(
						"Contacting server at %s ...",
						ProxyFactory.lastUsedApiUrl));
				try {
					result = api.hallo("IAIK");
				} catch (Exception e) {
					Log.w(ProverApp.P, "Connection test failed.", e);
				}
				return result;
			}

			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				Toast.makeText(getApplicationContext(), result,
						Toast.LENGTH_LONG).show();
			};

		}.execute();
	}

	private void doCreateMockTag() {
		ProgressBarAsyncTask<MockTagManager, String> task = new ProgressBarAsyncTask<MockTagManager, String>(
				this) {
			private Exception e = null;

			@Override
			protected void onPostExecute(String path) {
				super.onPostExecute(path);

				if (this.e != null || path == null) {
					e.printStackTrace();
					Toast.makeText(getApplicationContext(),
							"Could not create Mock Tag", Toast.LENGTH_LONG)
							.show();
					return;
				}

				// success
				Toast.makeText(getApplicationContext(),
						String.format("Mock Tag created in %s.", path),
						Toast.LENGTH_LONG).show();

				MainActivity.this
						.initMockTagSpinner(MainActivity.this.mockTagSpinner);
			}

			@Override
			protected String doInBackground(MockTagManager... params) {
				super.doInBackground(params);

				this.publishProgress("Creating Mock Tag on external USB storage...");

				String path = "";
				try {
					path = params[0].createAnotherMockTag();
				} catch (Exception e) {
					this.e = e;
				}

				return path;
			}
		};
		task.execute(this.mockTagManager);
	}

	private void goToSettings() {
		Intent intent = new Intent(this, PreferencesAcitivity.class);
		this.startActivityForResult(intent, 0);
	}
}
