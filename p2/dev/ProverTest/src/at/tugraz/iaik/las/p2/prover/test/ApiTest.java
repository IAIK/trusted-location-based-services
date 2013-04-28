package at.tugraz.iaik.las.p2.prover.test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import android.os.AsyncTask;
import android.util.Log;
import at.tugraz.iaik.las.p2.common.Utils;
import at.tugraz.iaik.las.p2.prover.ProverApp;
import at.tugraz.iaik.las.p2.prover.server.ProxyFactory;
import at.tugraz.iaik.las.p2.ttp.server.TtpApi;
import junit.framework.TestCase;

/**
 * Some basic tests of the API.
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class ApiTest extends TestCase {

	public void setUp() {
		ClassLoader cl = TtpApi.class.getClassLoader();
		if (cl == null) {
			Log.e(ProverApp.PT, "No class loader.");
		}
		ProxyFactory.init(cl, Preferences.ttpApiUrl);
		Log.d(ProverApp.P, "Server: " + Preferences.ttpApiUrl);
	}

	public void testLevel0ServerReachable() {
		try {
			URL url = new URL("http://" + Preferences.ttpApiUrl + "/api");
			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			Log.d(ProverApp.PT,
					url.toString() + ": " + huc.getResponseMessage());
			assertTrue(huc.getResponseMessage().length() > 0);
		} catch (IOException e) {
			assertTrue(false);
		}
	}

	public void testLevel1HelloNegative() throws InterruptedException {
		final String name = "Christian";
		final CountDownLatch signal = new CountDownLatch(1);

		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				TtpApi api = ProxyFactory.getProxy(TtpApi.class, "api");
				return api.hallo(name);
			}

			protected void onPostExecute(String result) {
				assertFalse(result.contains(name + "x"));
				signal.countDown();
				return;
			};

		}.execute();

		signal.await();
	}

	public void testLevel1Hello() throws InterruptedException {
		final String name = "Christian";
		final CountDownLatch signal = new CountDownLatch(1);

		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				TtpApi api = ProxyFactory.getProxy(TtpApi.class, "api");
				return api.hallo(name);
			}

			protected void onPostExecute(String result) {
				assertTrue(result.contains(name));
				signal.countDown();
				return;
			};

		}.execute();

		signal.await();
	}

	public void testLevel2GetNonceKnownTagUid() throws InterruptedException {
		final byte[] uid = { 0x13, (byte) 0xC8, 0x37, 0x6D, (byte) 0xD5,
				(byte) 0xE5, (byte) 0xB6 };

		final CountDownLatch signal = new CountDownLatch(1);

		new AsyncTask<Void, Void, byte[]>() {

			@Override
			protected byte[] doInBackground(Void... params) {
				TtpApi api = ProxyFactory.getProxy(TtpApi.class, "api");
				return api.getNonce(uid).Nonce;
			}

			protected void onPostExecute(byte[] nonce) {
				Log.d(ProverApp.PT,
						String.format("Received Nonce: %s",
								Utils.byteArrayToHexString(nonce)));
				assertEquals(16, nonce.length);
				signal.countDown();
				return;
			};

		}.execute();

		signal.await();
	}

	public void testLevel2GetNonceUnknownTagUid() throws InterruptedException {
		final byte[] uid = { 0x00, 0x11, 0x22, 0x37, 0x13, 0x37, 0x13, 0x37 };

		final CountDownLatch signal = new CountDownLatch(1);

		new AsyncTask<Void, Void, byte[]>() {

			@Override
			protected byte[] doInBackground(Void... params) {
				TtpApi api = ProxyFactory.getProxy(TtpApi.class, "api");
				return api.getNonce(uid).Nonce;
			}

			protected void onPostExecute(byte[] nonce) {
				Log.d(ProverApp.PT,
						String.format("Received Nonce: %s",
								Utils.byteArrayToHexString(nonce)));
				assertNull(nonce);
				signal.countDown();
				return;
			};

		}.execute();

		signal.await();
	}
}
