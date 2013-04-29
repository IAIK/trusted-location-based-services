package at.gv.egiz.android.util;

import java.io.InputStream;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.util.Log;
import at.gv.egiz.android.debug.DebugTags;

public class StorageUtils {

	public static InputStream loadFileFromRawResources(String filename,
			Context context, String packageName) {

		Resources resources = context.getResources();
		InputStream is = null;

		// get the resource id from the file name
		int id = resources.getIdentifier(filename, "raw", packageName);

		// get the file as a stream"
		try {
			is = resources.openRawResource(id);
		} catch (NotFoundException e) {
			Log.d(DebugTags.EGIZ + " StorageUtils", "resource not found");
			return null;
		}

		return is;
	}
}
