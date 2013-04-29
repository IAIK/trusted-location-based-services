package at.gv.egiz.android.tltt;

import java.io.IOException;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import at.gv.egiz.android.R;
import at.gv.egiz.android.tltt.utils.FileUtils;

/**
 * Background Task to base64 encode a given local file.
 * 
 * @author sandra.kreuzhuber@iaik.tugraz.at
 * 
 */
public class AddFileTask extends AsyncTask<String, Object, String> {

	private ProgressDialog dialog;
	private CreateTicketActivity callingActivity;

	public AddFileTask(CreateTicketActivity callingActivity) {
		this.callingActivity = callingActivity;
	}

	@Override
	protected void onPreExecute() {
		// show progress dialog
		dialog = new ProgressDialog(callingActivity);
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setMessage(callingActivity
				.getString(R.string.tltt_progress_addfile));
		dialog.show();
	}

	// convert file to base 64 string
	@Override
	protected String doInBackground(String... params) {
		try {
			return FileUtils.getBase64StringFromFile(params[0]);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	@Override
	protected void onPostExecute(String fileContent) {
		callingActivity.setAttachment(fileContent);
		dialog.dismiss();
	}
}
