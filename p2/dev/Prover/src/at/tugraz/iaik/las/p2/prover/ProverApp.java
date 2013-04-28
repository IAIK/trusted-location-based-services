package at.tugraz.iaik.las.p2.prover;

import java.security.Security;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import at.tugraz.iaik.las.p2.prover.server.ProxyFactory;

/**
 * Collection of application settings/constants.
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class ProverApp extends Application {

	public static final String extUsbDataDirectory = "LAS-Prover";

	// Tags for logging
	public static final String P = "P2-Prover";
	public static final String PT = "P2-Prover-Test";

	@Override
	public void onCreate() {
		super.onCreate();
		
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String serverUrl = sharedPref.getString("ttpApiUrl", "");
		ProxyFactory.init(getClassLoader(), serverUrl);
		Log.d(ProverApp.P, "Server: " + serverUrl);

		Security.addProvider(new iaik.security.provider.IAIK());
		Security.addProvider(new iaik.security.ecc.provider.ECCProvider(true));
		// Android includes BouncyCastle, hence we must remove it to not
		// interfere with the IAIK JCE
		Security.removeProvider("BC");
	}
}
