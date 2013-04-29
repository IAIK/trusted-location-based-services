package at.gv.egiz.android.util;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

public class NotificationUtils {

	public static void displayToast(Handler handler, final Context context,
			final String message) {
		handler.post(new Runnable() {
			public void run() {
				Toast.makeText(context, message, Toast.LENGTH_LONG).show();

			}
		});

	}
}
