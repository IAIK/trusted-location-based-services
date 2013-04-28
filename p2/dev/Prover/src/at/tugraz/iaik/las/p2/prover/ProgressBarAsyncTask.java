package at.tugraz.iaik.las.p2.prover;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

/**
 * An AsyncTask that automatically displays a progress bar while executing.
 * 
 * Inspired by:
 * http://androidtipps.blogspot.co.at/2011/10/progressdialog-in-einem
 * -asynctask.html
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 * @param <T1>
 *            Params: the type of the parameters sent to the task upon
 *            execution.
 * @param <T3>
 *            Result: the type of the result of the background computation.
 */
public class ProgressBarAsyncTask<T1, T3> extends AsyncTask<T1, String, T3> {
	ProgressDialog dialog;
	private Context context;

	public ProgressBarAsyncTask(Context context) {
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		// Setup Progress Dialog
		this.dialog = new ProgressDialog(this.context);
		this.dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		this.dialog.show();
	}

	@Override
	protected void onProgressUpdate(String[] values) {
		this.dialog.setMessage(values[0]);
	}

	@Override
	protected void onPostExecute(T3 result) {
		super.onPostExecute(result);

		this.dialog.dismiss();
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();

		this.dialog.dismiss();
	}

	@Override
	protected T3 doInBackground(T1... params) {
		return null;
	}
}
