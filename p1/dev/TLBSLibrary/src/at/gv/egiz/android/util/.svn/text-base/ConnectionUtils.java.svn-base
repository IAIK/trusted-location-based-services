package at.gv.egiz.android.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import at.gv.egiz.android.application.Constants;

public class ConnectionUtils {

	public static boolean isInternetConnectionAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	public static void enableInternetConnection(Activity activity) {
		Intent enableIntent = new Intent(Settings.ACTION_SETTINGS);
		activity.startActivityForResult(enableIntent,
				Constants.CHANCE_INTERNET_SETTINGS);

	}

}
