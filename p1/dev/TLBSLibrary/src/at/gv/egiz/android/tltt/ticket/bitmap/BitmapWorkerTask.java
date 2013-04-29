package at.gv.egiz.android.tltt.ticket.bitmap;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import at.gv.egiz.android.R;
import at.gv.egiz.android.tltt.utils.ImageUtils;

/**
 * Loading and downsampling a bitmap takes some time, so to not block the UI
 * thread use AsyncTask here.
 * 
 * @see <a href=
 *      "http://developer.android.com/training/displaying-bitmaps/process-bitmap.html"
 *      </a >
 * 
 */
public class BitmapWorkerTask extends AsyncTask<String, Object, Bitmap> {
	private final WeakReference<ImageView> imageViewReference;
	private final WeakReference<View> parentViewReference;
	private String filepath = "";
	private ProgressDialog dialog;
	private Activity callingActivity;

	public BitmapWorkerTask(ImageView imageView, View parentView,
			Activity callingActivity) {
		// Use a WeakReference to ensure the ImageView can be garbage collected
		this.imageViewReference = new WeakReference<ImageView>(imageView);
		this.parentViewReference = new WeakReference<View>(parentView);
		this.callingActivity = callingActivity;
	}

	@Override
	protected void onPreExecute() {
		// show progress dialog
		dialog = new ProgressDialog(callingActivity);
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setMessage(callingActivity
				.getString(R.string.tltt_progress_displayfile));
		dialog.show();
	}

	// Decode image in background.
	@Override
	protected Bitmap doInBackground(String... params) {
		filepath = params[0];
		return ImageUtils.decodeSampledBitmapFromResource(filepath, 480, 800);
	}

	// Once complete, see if ImageView is still around and set bitmap.
	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if (imageViewReference != null && bitmap != null
				&& parentViewReference != null) {
			final ImageView imageView = imageViewReference.get();
			if (imageView != null) {
				imageView.setImageBitmap((Bitmap) bitmap);
				// // now resize the image to the dimensions given in the parent
				// view
				imageView.setMaxWidth(parentViewReference.get().getWidth());
				imageView.setVisibility(0);
			}
		}
		dialog.dismiss();
	}

}
