package at.tugraz.iaik.las.p2.prover.cryptotag.mock;

import android.os.Environment;

/**
 * Manages the external storage (usb storage) of the app.
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class ExtStorage {
	public static boolean isExternalStorageAvailable() {
		// http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
		
		boolean externalStorageAvailable = false;
		boolean externalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			externalStorageAvailable = externalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			externalStorageAvailable = true;
			externalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			externalStorageAvailable = externalStorageWriteable = false;
		}

		return (externalStorageAvailable && externalStorageWriteable);
	}
}
