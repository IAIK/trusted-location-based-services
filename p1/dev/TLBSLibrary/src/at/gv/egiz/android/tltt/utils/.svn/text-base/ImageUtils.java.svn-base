package at.gv.egiz.android.tltt.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageUtils {

	// /**
	// * Returns an image from SD Card, so that it can be displayed in an
	// * ImageView.
	// *
	// * @param path
	// * to the image
	// * @param activity
	// * the Activity that calls the method
	// * @return <code>null</code> if image could not be found, Bitmap object of
	// * the image located on the given path
	// */
	// public static Bitmap getImageFromSD(String path, Activity activity) {
	// Uri uri = Uri.parse("file://" + path);
	// try {
	// return MediaStore.Images.Media.getBitmap(
	// activity.getContentResolver(), uri);
	// } catch (FileNotFoundException e) {
	// return null;
	// } catch (IOException e) {
	// return null;
	// }
	// }

	/**
	 * @see <a href=
	 *      "http://developer.android.com/training/displaying-bitmaps/load
	 *      -bitmap.html"</a >
	 * @param filepath
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static Bitmap decodeSampledBitmapFromResource(String filepath,
			int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(filepath, options);
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filepath, options);
	}

	/**
	 * @see <a href=
	 *      "http://developer.android.com/training/displaying-bitmaps/load-bitmap.html"
	 *      </a >
	 * 
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		return inSampleSize;
	}

}
